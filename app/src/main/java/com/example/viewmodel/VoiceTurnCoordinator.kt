package com.example.viewmodel

import com.example.audio.AudioSpeaker
import com.example.data.CalistungDao
import com.example.data.ChildProfileEntity
import com.example.data.EphemeralAudioBufferEntity
import com.example.data.SocraticTask
import com.example.data.TelemetryEventEntity
import com.example.data.VisualCardOption
import com.example.network.GeminiSocraticService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class VoiceTurnCoordinator(
    private val scope: CoroutineScope,
    private val dao: CalistungDao,
    private val audioController: AudioSpeaker,
    private val sessionCoordinator: CalistungSessionCoordinator,
    private val uiState: MutableStateFlow<CompanionUiState>,
    private val getProfiles: () -> List<ChildProfileEntity>,
    private val getCurrentTask: () -> SocraticTask,
    private val getTaskStartTimestamp: () -> Long,
    private val onAdvanceToNextTask: () -> Unit
) {

    private fun currentProfileName(): String {
        val id = uiState.value.selectedProfileId
        return getProfiles().firstOrNull { it.id == id }?.name ?: "Bima"
    }

    fun submitChildVoiceUtterance(utterance: String) {
        val state = uiState.value
        if (state.isSessionLockedWindDown || utterance.isBlank()) return

        sessionCoordinator.cancelInactivityTimer()
        uiState.update { it.copy(isSessionPaused = false) }

        val isLowSnr = state.currentSnrDb < 12f
        val newConsecutiveLowSnr = if (isLowSnr) state.consecutiveLowSnrTurns + 1 else 0

        val task = getCurrentTask()
        val idempotencyKey = UUID.randomUUID().toString()
        val childMsg = DialogueMessage(
            speaker = "Child",
            text = "\"$utterance\"",
            subtitle = currentProfileName()
        )

        val normalizedUtterance = utterance.trim().lowercase()
        val isCorrectEarly = task.expectedKeywords.any { normalizedUtterance.contains(it.lowercase()) }
        val optimisticStep = if (isCorrectEarly) 0 else (state.currentStepDownLevel + 1).coerceAtMost(3)

        uiState.update {
            it.copy(
                isLoadingTurn = true,
                currentStepDownLevel = optimisticStep,
                consecutiveLowSnrTurns = newConsecutiveLowSnr,
                dialogueHistory = it.dialogueHistory + childMsg
            )
        }

        scope.launch {
            val evaluation = try {
                GeminiSocraticService.evaluateChildResponse(
                    task = task,
                    childUtterance = utterance,
                    currentStepDownLevel = state.currentStepDownLevel,
                    isOfflineMode = state.isOfflinePreCachedMode,
                    idempotencyKey = idempotencyKey
                )
            } catch (_: Exception) {
                uiState.update { it.copy(isOfflinePreCachedMode = true) }
                val fallbackOfflineReply = "My ears are taking a quick nap! Let's play with our offline puzzle cards."
                audioController.speak(fallbackOfflineReply)
                GeminiSocraticService.evaluateChildResponse(
                    task = task,
                    childUtterance = utterance,
                    currentStepDownLevel = state.currentStepDownLevel,
                    isOfflineMode = true,
                    idempotencyKey = idempotencyKey
                )
            }

            if (evaluation.latencyMs > 1200 && !uiState.value.isOfflinePreCachedMode) {
                uiState.update { it.copy(isOfflinePreCachedMode = true) }
                audioController.speak("My ears are taking a quick nap! Let's play with our offline puzzle cards.")
            }

            dao.deleteExpiredAudioBuffers(System.currentTimeMillis())

            val now = System.currentTimeMillis()
            dao.insertAudioBufferLog(
                EphemeralAudioBufferEntity(
                    chunkId = "Voice Turn • ${SimpleDateFormat("HH:mm", Locale.US).format(Date(now))}",
                    childName = currentProfileName(),
                    turnSummary = "Audio suara sementara di memori • Terhapus otomatis dalam 24 jam",
                    createdAtMillis = now,
                    expiresAtMillis = now + 24 * 3600_000L
                )
            )

            val childWords = utterance.trim().split("\\s+".toRegex()).size
            val companionWords = evaluation.companionSpokenReply.trim().split("\\s+".toRegex()).size
            val tcrSec = ((now - getTaskStartTimestamp()) / 1000L).toInt().coerceIn(4, 45)
            val rbsMastery = if (evaluation.isCorrect) {
                if (state.currentStepDownLevel == 0) 92 else 82
            } else {
                75
            }

            val isFrustrationExhausted = !evaluation.isCorrect && state.currentStepDownLevel >= 3

            val finalReplyText: String
            val finalSubtitle: String
            val finalStepLevel: Int

            if (isFrustrationExhausted) {
                finalReplyText = "Let's do this one together! The answer is ${task.expectedKeywords.first()}. You did great trying! Let's move on to our next adventure!"
                finalSubtitle = "Jawaban Dibimbing Bersama • Lanjut Petualangan"
                finalStepLevel = 0
            } else {
                finalReplyText = evaluation.companionSpokenReply
                finalSubtitle = if (evaluation.isCorrect) {
                    "🌟 Story Clue Unlocked: ${task.unlockedStoryClue}"
                } else {
                    "Gentle Hint ${evaluation.nextStepDownLevel} of 3"
                }
                finalStepLevel = evaluation.nextStepDownLevel
            }

            val kikoReplyMsg = DialogueMessage(
                speaker = "Kiko",
                text = finalReplyText,
                subtitle = finalSubtitle,
                isHintStep = !evaluation.isCorrect
            )

            if (newConsecutiveLowSnr >= 3) {
                val loudMsg = "It is very loud in here right now! Let's find a quiet corner or try again after dinner."
                audioController.speak(loudMsg)
                uiState.update { current ->
                    current.copy(
                        isLoadingTurn = false,
                        currentStepDownLevel = finalStepLevel,
                        isVisualFallbackActive = true,
                        companionMoodText = "Ruangan sangat ramai! Mari cari sudut yang tenang.",
                        dialogueHistory = current.dialogueHistory + kikoReplyMsg
                    )
                }
            } else if (newConsecutiveLowSnr == 2) {
                val hearingMsg = "I'm having a little trouble hearing you. Can you tap your answer on the screen?"
                audioController.speak(hearingMsg)
                uiState.update { current ->
                    current.copy(
                        isLoadingTurn = false,
                        currentStepDownLevel = finalStepLevel,
                        isVisualFallbackActive = true,
                        companionMoodText = "Kiko agak sulit mendengar suaramu. Boleh sentuh jawabanmu di layar?",
                        dialogueHistory = current.dialogueHistory + kikoReplyMsg
                    )
                }
            } else {
                val currentElapsed = uiState.value.sessionElapsedSeconds
                val reachedWindDown = currentElapsed >= 840

                if (reachedWindDown) {
                    val windDownText = "You worked so hard today! Our adventure is done for now. Time to rest your ears!"
                    audioController.speak(windDownText)
                    uiState.update { current ->
                        current.copy(
                            isLoadingTurn = false,
                            currentStepDownLevel = 0,
                            sessionElapsedSeconds = 900,
                            isSessionLockedWindDown = true,
                            companionMoodText = "Petualangan 15 menit hari ini selesai! Waktunya istirahat.",
                            dialogueHistory = current.dialogueHistory + kikoReplyMsg + DialogueMessage(
                                speaker = "Kiko",
                                text = windDownText,
                                subtitle = "Ritual Menjelang Istirahat"
                            )
                        )
                    }
                } else {
                    audioController.speak(finalReplyText)
                    uiState.update { current ->
                        current.copy(
                            isLoadingTurn = false,
                            currentStepDownLevel = finalStepLevel,
                            companionMoodText = if (evaluation.isCorrect) {
                                "Hore hebat! Kamu berhasil menjawabnya!"
                            } else {
                                "Mari kita pikirkan bersama dengan petunjuk lembut..."
                            },
                            dialogueHistory = current.dialogueHistory + kikoReplyMsg
                        )
                    }
                    sessionCoordinator.resetInactivityTimer()
                }
            }

            val solvedUnassisted = if (evaluation.isCorrect && state.currentStepDownLevel == 0) 1 else 0
            dao.insertTelemetryEvent(
                TelemetryEventEntity(
                    idempotencyKey = idempotencyKey,
                    childId = state.selectedProfileId,
                    taskId = task.id,
                    taskTitle = task.title,
                    category = task.domain,
                    urrUnassisted = solvedUnassisted,
                    stepDownCount = state.currentStepDownLevel,
                    tcrSeconds = tcrSec,
                    rbsMasteryScore = rbsMastery,
                    snrDb = state.currentSnrDb,
                    fallbackCardUsed = false,
                    turnLatencyMs = evaluation.latencyMs,
                    childWordsSpoken = childWords,
                    companionWordsSpoken = companionWords
                )
            )

            val currentProfile = getProfiles().firstOrNull { it.id == uiState.value.selectedProfileId }
            ChildProgressTracker.updateChildAggregateStats(
                dao = dao,
                currentProfile = currentProfile,
                sessionElapsedSeconds = uiState.value.sessionElapsedSeconds,
                solvedUnassisted = solvedUnassisted == 1,
                tcrSeconds = tcrSec,
                costIdr = evaluation.estimatedCostIdr,
                childWords = childWords,
                companionWords = companionWords,
                rbsMastery = rbsMastery
            )

            if (isFrustrationExhausted) {
                delay(3000L)
                onAdvanceToNextTask()
            }
        }
    }

    fun selectVisualFallbackCard(option: VisualCardOption) {
        val state = uiState.value
        if (state.isSessionLockedWindDown) return

        sessionCoordinator.cancelInactivityTimer()
        uiState.update { it.copy(isSessionPaused = false) }

        val task = getCurrentTask()
        val idempotencyKey = UUID.randomUUID().toString()
        val tcrSec = ((System.currentTimeMillis() - getTaskStartTimestamp()) / 1000L).toInt().coerceIn(5, 35)

        val tapMessage = DialogueMessage(
            speaker = "Child",
            text = "${option.emojiVisual} ${option.spokenLabel}",
            subtitle = "Sentuh Kartu Bergambar"
        )

        if (option.isCorrect) {
            val kikoAffirmation = DialogueMessage(
                speaker = "Kiko",
                text = task.celebrationAffirmation,
                subtitle = "🌟 Story Clue Unlocked: ${task.unlockedStoryClue}"
            )
            audioController.speak(task.celebrationAffirmation)

            scope.launch {
                dao.insertTelemetryEvent(
                    TelemetryEventEntity(
                        idempotencyKey = idempotencyKey,
                        childId = state.selectedProfileId,
                        taskId = task.id,
                        taskTitle = task.title,
                        category = task.domain,
                        urrUnassisted = if (state.currentStepDownLevel == 0) 1 else 0,
                        stepDownCount = state.currentStepDownLevel,
                        tcrSeconds = tcrSec,
                        rbsMasteryScore = 88,
                        snrDb = state.currentSnrDb,
                        fallbackCardUsed = true,
                        turnLatencyMs = 180,
                        childWordsSpoken = 2,
                        companionWordsSpoken = 12
                    )
                )

                val currentProfile = getProfiles().firstOrNull { it.id == uiState.value.selectedProfileId }
                ChildProgressTracker.updateChildAggregateStats(
                    dao = dao,
                    currentProfile = currentProfile,
                    sessionElapsedSeconds = uiState.value.sessionElapsedSeconds,
                    solvedUnassisted = state.currentStepDownLevel == 0,
                    tcrSeconds = tcrSec,
                    costIdr = 18,
                    childWords = 2,
                    companionWords = 12,
                    rbsMastery = 88
                )
                dao.deleteExpiredAudioBuffers(System.currentTimeMillis())
            }

            uiState.update {
                it.copy(
                    currentStepDownLevel = 0,
                    isVisualFallbackActive = false,
                    consecutiveLowSnrTurns = 0,
                    currentSnrDb = 24.0f,
                    companionMoodText = "Hebat sekali! Kembali ke obrolan suara.",
                    dialogueHistory = it.dialogueHistory + tapMessage + kikoAffirmation
                )
            }
            sessionCoordinator.resetInactivityTimer()
        } else {
            val nextStep = (state.currentStepDownLevel + 1).coerceAtMost(3)
            val hint = when (nextStep) {
                1 -> task.stepDown1Question
                2 -> task.stepDown2Question
                else -> task.stepDown3Scaffold
            }
            audioController.speak(hint)
            uiState.update {
                it.copy(
                    currentStepDownLevel = nextStep,
                    companionMoodText = "Ayo kita coba berhitung bersama!",
                    dialogueHistory = it.dialogueHistory + tapMessage + DialogueMessage(
                        speaker = "Kiko",
                        text = hint,
                        subtitle = "Petunjuk Lembut $nextStep dari 3",
                        isHintStep = true
                    )
                )
            }
            sessionCoordinator.resetInactivityTimer()
        }
    }
}
