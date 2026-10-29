package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceCompanionAudioController
import com.example.data.CalistungDatabase
import com.example.data.ChildProfileEntity
import com.example.data.ConsentAuditEntity
import com.example.data.EphemeralAudioBufferEntity
import com.example.data.OfflineBundleEntity
import com.example.data.ParentSurveyEntity
import com.example.data.SocraticCurriculumBank
import com.example.data.SocraticTask
import com.example.data.TelemetryEventEntity
import com.example.data.VisualCardOption
import com.example.network.GeminiSocraticService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class DialogueMessage(
    val id: String = UUID.randomUUID().toString(),
    val speaker: String, // "Kiko" or "Child"
    val text: String,
    val subtitle: String? = null,
    val isHintStep: Boolean = false
)

data class CompanionUiState(
    val selectedProfileId: Int = 1,
    val hasParentalConsent: Boolean = true,
    val showConsentModal: Boolean = false,
    val isCoPlayDemoActive: Boolean = false,
    val currentTaskIndex: Int = 0,
    val currentStepDownLevel: Int = 0,
    val consecutiveLowSnrTurns: Int = 0,
    val currentSnrDb: Float = 24.5f,
    val isVisualFallbackActive: Boolean = false,
    val isSessionLockedWindDown: Boolean = false,
    val isSessionPaused: Boolean = false,
    val sessionElapsedSeconds: Int = 420, // 7 minutes into the 15-minute daily session
    val isOfflinePreCachedMode: Boolean = false,
    val isLoadingTurn: Boolean = false,
    val companionMoodText: String = "Kiko siap mendengarkan dan bermain!",
    val dialogueHistory: List<DialogueMessage> = emptyList(),
    val micPermissionGranted: Boolean = false
)

class CalistungViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CalistungDatabase.getDatabase(application)
    private val dao = db.dao()

    val profiles: StateFlow<List<ChildProfileEntity>> = dao.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTelemetry: StateFlow<List<TelemetryEventEntity>> = dao.getAllTelemetry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineBundles: StateFlow<List<OfflineBundleEntity>> = dao.getAllOfflineBundles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val consentLogs: StateFlow<List<ConsentAuditEntity>> = dao.getConsentAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val audioBuffers: StateFlow<List<EphemeralAudioBufferEntity>> = dao.getAudioBufferLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val parentSurveys: StateFlow<List<ParentSurveyEntity>> = dao.getParentSurveys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(CompanionUiState())
    val uiState: StateFlow<CompanionUiState> = _uiState.asStateFlow()

    private var inactivityTimerJob: Job? = null
    private var sessionTickerJob: Job? = null

    private val audioController = VoiceCompanionAudioController(
        context = application,
        onSpeechResult = { spokenText ->
            submitChildVoiceUtterance(spokenText)
        },
        onSpeechErrorOrSilence = { _ ->
            handleNoiseOrSilenceTurn()
        },
        onRmsVolumeChanged = { rms ->
            val mappedSnr = (rms * 2.2f).coerceIn(0f, 35f)
            _uiState.update { it.copy(currentSnrDb = mappedSnr) }
        }
    )

    val isSpeaking: StateFlow<Boolean> = audioController.isSpeaking
    val isListening: StateFlow<Boolean> = audioController.isListening

    private var taskStartTimestampMillis: Long = System.currentTimeMillis()

    init {
        seedInitialDataIfNeeded()
        checkParentalConsentGate()
        purgeExpiredAudioBuffers()
        initializeOpeningDialogue()
        startSessionTicker()
    }

    private fun checkParentalConsentGate() {
        viewModelScope.launch {
            dao.getConsentAuditLogs().collect { logs ->
                if (logs.isNotEmpty()) {
                    val hasConsent = logs.any { it.explicitAffirmationFlag }
                    _uiState.update { current ->
                        current.copy(
                            hasParentalConsent = hasConsent,
                            showConsentModal = !hasConsent
                        )
                    }
                }
            }
        }
    }

    private fun purgeExpiredAudioBuffers() {
        viewModelScope.launch {
            dao.deleteExpiredAudioBuffers(System.currentTimeMillis())
        }
    }

    private fun startSessionTicker() {
        sessionTickerJob?.cancel()
        sessionTickerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _uiState.value
                if (!state.isSessionLockedWindDown && !state.isSessionPaused) {
                    val nextSec = state.sessionElapsedSeconds + 1
                    if (nextSec >= 930) {
                        // Hard cutoff at 15m 30s
                        trigger15MinuteClosingRitual()
                    } else {
                        _uiState.update { it.copy(sessionElapsedSeconds = nextSec) }
                    }
                }
            }
        }
    }

    private fun resetInactivityTimer() {
        inactivityTimerJob?.cancel()
        val state = _uiState.value
        if (state.isSessionLockedWindDown || state.isSessionPaused) return

        inactivityTimerJob = viewModelScope.launch {
            delay(10000L) // 10 seconds of silence
            if (!_uiState.value.isSessionLockedWindDown && !_uiState.value.isSessionPaused) {
                val lastKikoPrompt = _uiState.value.dialogueHistory.lastOrNull { it.speaker == "Kiko" }?.text
                    ?: currentTask().openingQuestion
                audioController.speak(lastKikoPrompt)
                _uiState.update {
                    it.copy(companionMoodText = "Kiko mengulang pertanyaannya pelan-pelan ya!")
                }

                delay(15000L) // Additional 15 seconds (25s total silence)
                if (!_uiState.value.isSessionLockedWindDown) {
                    _uiState.update {
                        it.copy(
                            isSessionPaused = true,
                            companionMoodText = "Sesi dijeda. Sentuh layar saat siap lanjut ya!"
                        )
                    }
                }
            }
        }
    }

    private fun handleNoiseOrSilenceTurn() {
        val state = _uiState.value
        if (state.isSessionLockedWindDown) return

        val newConsecutive = state.consecutiveLowSnrTurns + 1
        when {
            newConsecutive >= 3 -> {
                val loudSpeech = "It is very loud in here right now! Let's find a quiet corner or try again after dinner."
                audioController.speak(loudSpeech)
                _uiState.update {
                    it.copy(
                        consecutiveLowSnrTurns = newConsecutive,
                        isVisualFallbackActive = true,
                        companionMoodText = "Ruangan sangat bising! Cari tempat tenang yuk."
                    )
                }
            }
            newConsecutive == 2 -> {
                val fallbackSpeech = "I'm having a little trouble hearing you. Can you tap your answer on the screen?"
                audioController.speak(fallbackSpeech)
                _uiState.update {
                    it.copy(
                        consecutiveLowSnrTurns = newConsecutive,
                        isVisualFallbackActive = true,
                        companionMoodText = "Sentuh pilihan jawabanmu di layar ya!"
                    )
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        consecutiveLowSnrTurns = newConsecutive,
                        companionMoodText = "Sentuh mikrofon atau pilih jawaban untuk mengobrol dengan Kiko!"
                    )
                }
            }
        }
    }

    private fun seedInitialDataIfNeeded() {
        viewModelScope.launch {
            val initialProfiles = listOf(
                ChildProfileEntity(
                    id = 1,
                    name = "Bima",
                    age = 5,
                    avatarEmoji = "🦉",
                    subscriptionTier = "Paket Standar Keluarga",
                    dailyMinutesUsedToday = 7,
                    urrScorePercent = 76,
                    medianTcrSeconds = 13,
                    rbsRetentionPercent = 86,
                    monthlyInferenceIdr = 14850,
                    childToCompanionWordRatio = 1.62f,
                    unlockedStoryClues = 4,
                    scheduledDailyTime = "4:30 PM"
                ),
                ChildProfileEntity(
                    id = 2,
                    name = "Alya",
                    age = 4,
                    avatarEmoji = "🦊",
                    subscriptionTier = "Paket Penjelajah Cilik",
                    dailyMinutesUsedToday = 5,
                    urrScorePercent = 70,
                    medianTcrSeconds = 15,
                    rbsRetentionPercent = 82,
                    monthlyInferenceIdr = 11200,
                    childToCompanionWordRatio = 1.38f,
                    unlockedStoryClues = 3,
                    scheduledDailyTime = "4:00 PM"
                ),
                ChildProfileEntity(
                    id = 3,
                    name = "Raka",
                    age = 6,
                    avatarEmoji = "🐻",
                    subscriptionTier = "Paket Multi-Anak Plus",
                    dailyMinutesUsedToday = 11,
                    urrScorePercent = 84,
                    medianTcrSeconds = 11,
                    rbsRetentionPercent = 91,
                    monthlyInferenceIdr = 18400,
                    childToCompanionWordRatio = 1.85f,
                    unlockedStoryClues = 5,
                    scheduledDailyTime = "5:00 PM"
                )
            )
            initialProfiles.forEach { dao.insertProfile(it) }

            val bundles = listOf(
                OfflineBundleEntity(
                    bundleId = "BUNDLE-NUM-CORE",
                    title = "Cerita Berhitung & Angka Hutan",
                    category = "Berhitung & Angka • Usia 3–6",
                    description = "Petualangan cerita berhitung interaktif yang hangat, membandingkan jumlah, dan penjumlahan ceria bersama Kiko.",
                    sizeMb = 14.2f,
                    promptCount = 48,
                    isDownloaded = true,
                    cachedPromptSavingsPercent = 44
                ),
                OfflineBundleEntity(
                    bundleId = "BUNDLE-LIT-SYLLABLES",
                    title = "Batu Sungai Bunyi & Suku Kata",
                    category = "Membaca & Fonik Awal • Usia 3–6",
                    description = "Tepuk tangan berirama untuk suku kata (BO-LA, KU-DA), pengenalan bunyi huruf awal, dan permainan kosakata lisan.",
                    sizeMb = 16.8f,
                    promptCount = 56,
                    isDownloaded = true,
                    cachedPromptSavingsPercent = 41
                ),
                OfflineBundleEntity(
                    bundleId = "BUNDLE-RBS-SPIRAL",
                    title = "Peti Ingatan Rumah Pohon",
                    category = "Ulasan & Penemuan 2 Minggu • Usia 4–6",
                    description = "Cerita ulasan bertahap yang membantu anak mengingat dan menerapkan kata serta angka yang dipelajari dua minggu lalu.",
                    sizeMb = 11.4f,
                    promptCount = 32,
                    isDownloaded = false,
                    cachedPromptSavingsPercent = 39
                )
            )
            bundles.forEach { dao.insertOfflineBundle(it) }

            dao.insertConsentLog(
                ConsentAuditEntity(
                    id = 1,
                    parentUserId = "Orang Tua / Wali",
                    timestampUtc = "2026-10-11 14:00",
                    policyVersion = "Perisai Privasi Anak v1.0",
                    explicitAffirmationFlag = true,
                    noticeText = "Kami merekam audio suara sementara hanya untuk bercakap-cakap dengan anak Anda selama sesi aktif. Rekaman audio mentah dihapus otomatis dalam 24 jam dan tidak pernah digunakan untuk iklan atau pelatihan model kecerdasan buatan."
                )
            )

            val sampleTelemetry = listOf(
                TelemetryEventEntity(
                    idempotencyKey = "session-log-001",
                    childId = 1,
                    taskId = "TASK-NUM-01",
                    taskTitle = "Lima Apel di Hutan",
                    category = "Berhitung & Angka",
                    urrUnassisted = 1,
                    stepDownCount = 0,
                    tcrSeconds = 11,
                    rbsMasteryScore = 88,
                    snrDb = 26.4f,
                    fallbackCardUsed = false,
                    turnLatencyMs = 420,
                    childWordsSpoken = 14,
                    companionWordsSpoken = 9,
                    timestampMillis = System.currentTimeMillis() - 86400_000L
                ),
                TelemetryEventEntity(
                    idempotencyKey = "session-log-002",
                    childId = 1,
                    taskId = "TASK-LIT-01",
                    taskTitle = "Tepuk Suku Kata: BO-LA",
                    category = "Bunyi & Suku Kata",
                    urrUnassisted = 1,
                    stepDownCount = 1,
                    tcrSeconds = 15,
                    rbsMasteryScore = 84,
                    snrDb = 22.0f,
                    fallbackCardUsed = false,
                    turnLatencyMs = 510,
                    childWordsSpoken = 12,
                    companionWordsSpoken = 10,
                    timestampMillis = System.currentTimeMillis() - 43200_000L
                )
            )
            sampleTelemetry.forEach { dao.insertTelemetryEvent(it) }

            val now = System.currentTimeMillis()
            dao.insertAudioBufferLog(
                EphemeralAudioBufferEntity(
                    chunkId = "Sesi Suara Aktif",
                    childName = "Bima",
                    turnSummary = "Audio suara sementara di memori • Terhapus otomatis dalam 24 jam",
                    createdAtMillis = now - 120_000L,
                    expiresAtMillis = now + (23 * 3600_000L),
                    volatileRamOnly = true,
                    biometricStripped = true,
                    status = "Dijadwalkan untuk Terhapus Otomatis"
                )
            )
        }
    }

    private fun initializeOpeningDialogue() {
        val firstTask = currentTask()
        taskStartTimestampMillis = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                dialogueHistory = listOf(
                    DialogueMessage(
                        speaker = "Kiko",
                        text = firstTask.openingQuestion,
                        subtitle = "${firstTask.domain} • ${firstTask.ageTarget}"
                    )
                )
            )
        }
    }

    fun currentTask(): SocraticTask {
        val index = _uiState.value.currentTaskIndex.coerceIn(0, SocraticCurriculumBank.tasks.lastIndex)
        return SocraticCurriculumBank.tasks[index]
    }

    fun selectChildProfile(profileId: Int) {
        _uiState.update {
            it.copy(
                selectedProfileId = profileId,
                isSessionLockedWindDown = false,
                isSessionPaused = false,
                sessionElapsedSeconds = 420,
                companionMoodText = "Profil anak berhasil diganti! Kiko siap belajar."
            )
        }
    }

    fun selectCurriculumTask(index: Int) {
        val safeIdx = index.coerceIn(0, SocraticCurriculumBank.tasks.lastIndex)
        val task = SocraticCurriculumBank.tasks[safeIdx]
        taskStartTimestampMillis = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                currentTaskIndex = safeIdx,
                currentStepDownLevel = 0,
                isVisualFallbackActive = false,
                consecutiveLowSnrTurns = 0,
                isSessionPaused = false,
                dialogueHistory = listOf(
                    DialogueMessage(
                        speaker = "Kiko",
                        text = task.openingQuestion,
                        subtitle = "${task.domain} • ${task.ageTarget}"
                    )
                ),
                companionMoodText = "Bermain: ${task.title}"
            )
        }
        audioController.speak(task.openingQuestion)
        resetInactivityTimer()
    }

    fun advanceToNextStoryTask() {
        val nextIndex = (_uiState.value.currentTaskIndex + 1) % SocraticCurriculumBank.tasks.size
        selectCurriculumTask(nextIndex)
    }

    fun setMicPermissionGranted(granted: Boolean) {
        _uiState.update { it.copy(micPermissionGranted = granted) }
    }

    fun startRealMicrophoneListening() {
        if (!_uiState.value.hasParentalConsent) {
            _uiState.update { it.copy(showConsentModal = true) }
            return
        }
        if (_uiState.value.isSessionLockedWindDown) return
        audioController.startRealVoiceCapture()
    }

    fun speakCurrentPromptAloud() {
        val lastKiko = _uiState.value.dialogueHistory.lastOrNull { it.speaker == "Kiko" }?.text
            ?: currentTask().openingQuestion
        audioController.speak(lastKiko)
        resetInactivityTimer()
    }

    fun startCoPlayOnboardingDemo() {
        _uiState.update {
            it.copy(
                isCoPlayDemoActive = true,
                companionMoodText = "Selamat datang di Demo Co-Play 3 Menit! Mari bermain bersama."
            )
        }
        audioController.speak("Selamat datang di demo bersama orang tua tiga menit! Mari bereksplorasi bersama Kiko!")
    }

    fun completeCoPlayOnboardingDemo() {
        _uiState.update {
            it.copy(
                isCoPlayDemoActive = false,
                companionMoodText = "Demo selesai! Kiko siap melanjutkan petualangan."
            )
        }
    }

    /**
     * Toggles Picture Tap Cards mode for noisy rooms (when household TV or chatter makes it hard to hear).
     */
    fun toggleNoisyRoomTapCards() {
        if (_uiState.value.isSessionLockedWindDown) return
        val enabling = !_uiState.value.isVisualFallbackActive
        if (enabling) {
            val fallbackSpeech = "I'm having a little trouble hearing you. Can you tap your answer on the screen?"
            audioController.speak(fallbackSpeech)
            _uiState.update { state ->
                state.copy(
                    currentSnrDb = 8.5f,
                    consecutiveLowSnrTurns = 2,
                    isVisualFallbackActive = true,
                    companionMoodText = "Mode ruangan bising: Sentuh kartu bergambar di bawah!",
                    dialogueHistory = state.dialogueHistory + DialogueMessage(
                        speaker = "Kiko",
                        text = fallbackSpeech,
                        subtitle = "Kartu Pilihan Siap"
                    )
                )
            }
        } else {
            _uiState.update { state ->
                state.copy(
                    currentSnrDb = 24.5f,
                    consecutiveLowSnrTurns = 0,
                    isVisualFallbackActive = false,
                    companionMoodText = "Mode suara aktif! Bicaralah dengan suara lantang kepada Kiko."
                )
            }
        }
        resetInactivityTimer()
    }

    fun triggerLowSnrAmbientNoiseScenario() {
        if (!_uiState.value.isVisualFallbackActive) {
            toggleNoisyRoomTapCards()
        }
    }

    fun submitChildVoiceUtterance(utterance: String) {
        val state = _uiState.value
        if (state.isSessionLockedWindDown || utterance.isBlank()) return

        // Cancel inactivity timer whenever child interacts
        inactivityTimerJob?.cancel()
        _uiState.update { it.copy(isSessionPaused = false) }

        // Noise tracking: check if utterance occurred under low SNR
        val isLowSnr = state.currentSnrDb < 12f
        val newConsecutiveLowSnr = if (isLowSnr) state.consecutiveLowSnrTurns + 1 else 0

        val task = currentTask()
        val idempotencyKey = UUID.randomUUID().toString()
        val childMsg = DialogueMessage(
            speaker = "Child",
            text = "\"$utterance\"",
            subtitle = currentProfileName()
        )

        // 1. Immediately append child message, update optimistic step-down, and set loading
        val normalizedUtterance = utterance.trim().lowercase()
        val isCorrectEarly = task.expectedKeywords.any { normalizedUtterance.contains(it.lowercase()) }
        val optimisticStep = if (isCorrectEarly) 0 else (state.currentStepDownLevel + 1).coerceAtMost(3)

        _uiState.update {
            it.copy(
                isLoadingTurn = true,
                currentStepDownLevel = optimisticStep,
                consecutiveLowSnrTurns = newConsecutiveLowSnr,
                dialogueHistory = it.dialogueHistory + childMsg
            )
        }

        viewModelScope.launch {
            val evaluation = try {
                GeminiSocraticService.evaluateChildResponse(
                    task = task,
                    childUtterance = utterance,
                    currentStepDownLevel = state.currentStepDownLevel,
                    isOfflineMode = state.isOfflinePreCachedMode,
                    idempotencyKey = idempotencyKey
                )
            } catch (_: Exception) {
                // Circuit Breaker trip on cloud exception
                _uiState.update { it.copy(isOfflinePreCachedMode = true) }
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

            // Circuit Breaker check: if turn latency > 1200ms
            if (evaluation.latencyMs > 1200 && !_uiState.value.isOfflinePreCachedMode) {
                _uiState.update { it.copy(isOfflinePreCachedMode = true) }
                audioController.speak("My ears are taking a quick nap! Let's play with our offline puzzle cards.")
            }

            // Purge expired audio buffers after each turn [FR-06 & RK-03]
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
            val tcrSec = ((now - taskStartTimestampMillis) / 1000L).toInt().coerceIn(4, 45)
            val rbsMastery = if (evaluation.isCorrect) {
                if (state.currentStepDownLevel == 0) 92 else 82
            } else {
                75
            }

            // Frustration Mitigation: if incorrect and current step down is 3
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

            // Check low SNR escalation
            if (newConsecutiveLowSnr >= 3) {
                val loudMsg = "It is very loud in here right now! Let's find a quiet corner or try again after dinner."
                audioController.speak(loudMsg)
                _uiState.update { current ->
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
                _uiState.update { current ->
                    current.copy(
                        isLoadingTurn = false,
                        currentStepDownLevel = finalStepLevel,
                        isVisualFallbackActive = true,
                        companionMoodText = "Kiko agak sulit mendengar suaramu. Boleh sentuh jawabanmu di layar?",
                        dialogueHistory = current.dialogueHistory + kikoReplyMsg
                    )
                }
            } else {
                // Check 14-minute session wind-down ritual condition
                val currentElapsed = _uiState.value.sessionElapsedSeconds
                val reachedWindDown = currentElapsed >= 840

                if (reachedWindDown) {
                    val windDownText = "You worked so hard today! Our adventure is done for now. Time to rest your ears!"
                    audioController.speak(windDownText)
                    _uiState.update { current ->
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
                    _uiState.update { current ->
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
                    resetInactivityTimer()
                }
            }

            // Telemetry logging & aggregate profile stats
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

            updateChildAggregateStats(
                solvedUnassisted = solvedUnassisted == 1,
                tcrSeconds = tcrSec,
                costIdr = evaluation.estimatedCostIdr,
                childWords = childWords,
                companionWords = companionWords,
                rbsMastery = rbsMastery
            )

            if (isFrustrationExhausted) {
                delay(3000L)
                advanceToNextStoryTask()
            }
        }
    }

    fun selectVisualFallbackCard(option: VisualCardOption) {
        val state = _uiState.value
        if (state.isSessionLockedWindDown) return

        inactivityTimerJob?.cancel()
        _uiState.update { it.copy(isSessionPaused = false) }

        val task = currentTask()
        val idempotencyKey = UUID.randomUUID().toString()
        val tcrSec = ((System.currentTimeMillis() - taskStartTimestampMillis) / 1000L).toInt().coerceIn(5, 35)

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

            viewModelScope.launch {
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

                updateChildAggregateStats(
                    solvedUnassisted = state.currentStepDownLevel == 0,
                    tcrSeconds = tcrSec,
                    costIdr = 18,
                    childWords = 2,
                    companionWords = 12,
                    rbsMastery = 88
                )
                dao.deleteExpiredAudioBuffers(System.currentTimeMillis())
            }

            _uiState.update {
                it.copy(
                    currentStepDownLevel = 0,
                    isVisualFallbackActive = false,
                    consecutiveLowSnrTurns = 0,
                    currentSnrDb = 24.0f,
                    companionMoodText = "Hebat sekali! Kembali ke obrolan suara.",
                    dialogueHistory = it.dialogueHistory + tapMessage + kikoAffirmation
                )
            }
            resetInactivityTimer()
        } else {
            val nextStep = (state.currentStepDownLevel + 1).coerceAtMost(3)
            val hint = when (nextStep) {
                1 -> task.stepDown1Question
                2 -> task.stepDown2Question
                else -> task.stepDown3Scaffold
            }
            audioController.speak(hint)
            _uiState.update {
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
            resetInactivityTimer()
        }
    }

    fun trigger15MinuteClosingRitual() {
        inactivityTimerJob?.cancel()
        val windDownSpeech = "You worked so hard today! Our adventure is done for now. Time to rest your ears!"
        audioController.speak(windDownSpeech)
        _uiState.update { state ->
            state.copy(
                sessionElapsedSeconds = 900,
                isSessionLockedWindDown = true,
                isVisualFallbackActive = false,
                isSessionPaused = false,
                companionMoodText = "Petualangan 15 menit hari ini selesai!",
                dialogueHistory = state.dialogueHistory + DialogueMessage(
                    speaker = "Kiko",
                    text = windDownSpeech,
                    subtitle = "Ritual Istirahat Harian"
                )
            )
        }
    }

    /**
     * Generates the WhatsApp Post-Session Digest message text for parents per PRD Section 3 & 4.
     */
    fun buildParentWhatsAppDigestText(): String {
        val profile = profiles.value.firstOrNull { it.id == _uiState.value.selectedProfileId }
        val childName = profile?.name ?: "Si Kecil"
        val urr = profile?.urrScorePercent ?: 76
        val tcr = profile?.medianTcrSeconds ?: 13
        return """
            🦉 *Laporan Belajar Kiko (Sesi 15 Menit)*
            Halo Bunda / Ayah! $childName baru saja menyelesaikan petualangan Calistung hari ini:
            
            ✅ *Skor Pemahaman Mandiri (URR):* $urr%
            ⚡ *Kecepatan Respon (Median TCR):* $tcr detik
            ✨ *Petunjuk Cerita Terbuka:* ${profile?.unlockedStoryClues ?: 4} Petunjuk
            
            🍽️ *Pertanyaan Meja Makan Hari Ini:*
            "Tadi bersama Kiko, kata apa yang berima dengan 'MATA' dan 'BATA' ya?"
        """.trimIndent()
    }

    fun parentResetDailySessionLock() {
        val task = currentTask()
        taskStartTimestampMillis = System.currentTimeMillis()
        _uiState.update { state ->
            state.copy(
                sessionElapsedSeconds = 0,
                isSessionLockedWindDown = false,
                isSessionPaused = false,
                currentStepDownLevel = 0,
                companionMoodText = "Petualangan 15 menit baru dimulai!",
                dialogueHistory = listOf(
                    DialogueMessage(
                        speaker = "Kiko",
                        text = "Selamat datang kembali di petualangan baru! ${task.openingQuestion}",
                        subtitle = "${task.domain} • ${task.ageTarget}"
                    )
                )
            )
        }
        resetInactivityTimer()
    }

    fun toggleOfflineMode(enabled: Boolean) {
        val message = if (enabled) {
            "My ears are taking a quick nap! Let's play with our offline puzzle cards."
        } else {
            "Kiko is back online and ready to chat!"
        }
        if (enabled) {
            audioController.speak(message)
        }
        _uiState.update {
            it.copy(
                isOfflinePreCachedMode = enabled,
                companionMoodText = if (enabled) {
                    "Paket Belajar Luar Jaringan Aktif (Tanpa Internet)"
                } else {
                    "Kiko siap mendengarkan dan bermain!"
                }
            )
        }
    }

    fun toggleOfflineBundleDownload(bundle: OfflineBundleEntity) {
        viewModelScope.launch {
            dao.updateOfflineBundle(bundle.copy(isDownloaded = !bundle.isDownloaded))
        }
    }

    fun openParentalConsentModal(show: Boolean) {
        _uiState.update { it.copy(showConsentModal = show) }
    }

    fun recordParentalConsentAffirmation(parentId: String) {
        viewModelScope.launch {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
            dao.insertConsentLog(
                ConsentAuditEntity(
                    parentUserId = parentId.ifBlank { "Orang Tua / Wali" },
                    timestampUtc = timestamp,
                    policyVersion = "Perisai Privasi Anak v1.0",
                    explicitAffirmationFlag = true,
                    noticeText = "Kami merekam audio suara sementara hanya untuk bercakap-cakap dengan anak Anda selama sesi aktif. Rekaman audio mentah dihapus otomatis dalam 24 jam dan tidak pernah digunakan untuk iklan atau pelatihan model kecerdasan buatan."
                )
            )
            _uiState.update {
                it.copy(
                    hasParentalConsent = true,
                    showConsentModal = false
                )
            }
        }
    }

    fun executeImmediate24hAudioPurge() {
        viewModelScope.launch {
            dao.purgeAllAudioBuffers()
        }
    }

    fun submitMonthlyParentSurvey(
        speaksFullSentences: Int,
        returnsPhoneCalmly: Int,
        readsSyllables: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())
            dao.insertParentSurvey(
                ParentSurveyEntity(
                    childId = _uiState.value.selectedProfileId,
                    childName = currentProfileName(),
                    speaksFullSentencesScore = speaksFullSentences,
                    returnsPhoneCalmlyScore = returnsPhoneCalmly,
                    readsSyllablesIndependentlyScore = readsSyllables,
                    notes = notes.ifBlank { "Anak senang berhitung lantang bersama Kiko dan meletakkan ponsel dengan tenang." },
                    submittedAtDate = dateStr
                )
            )
        }
    }

    private fun currentProfileName(): String {
        val id = _uiState.value.selectedProfileId
        return profiles.value.firstOrNull { it.id == id }?.name ?: "Bima"
    }

    private suspend fun updateChildAggregateStats(
        solvedUnassisted: Boolean,
        tcrSeconds: Int,
        costIdr: Int,
        childWords: Int,
        companionWords: Int,
        rbsMastery: Int
    ) {
        val currentProfile = profiles.value.firstOrNull { it.id == _uiState.value.selectedProfileId } ?: return
        val newUrr = if (solvedUnassisted) {
            (currentProfile.urrScorePercent + 2).coerceAtMost(98)
        } else {
            currentProfile.urrScorePercent
        }
        val newTcr = ((currentProfile.medianTcrSeconds + tcrSeconds) / 2).coerceIn(8, 30)
        val newSpend = (currentProfile.monthlyInferenceIdr + costIdr).coerceAtMost(25000)
        val newClues = if (solvedUnassisted) currentProfile.unlockedStoryClues + 1 else currentProfile.unlockedStoryClues

        val currentRatio = currentProfile.childToCompanionWordRatio
        val turnRatio = if (companionWords > 0) childWords.toFloat() / companionWords.toFloat() else currentRatio
        val newRatio = ((currentRatio * 0.8f) + (turnRatio * 0.2f)).coerceIn(0.5f, 3.0f)

        val currentRbs = currentProfile.rbsRetentionPercent
        val newRbs = ((currentRbs * 0.85f) + (rbsMastery * 0.15f)).toInt().coerceIn(50, 99)

        dao.updateProfile(
            currentProfile.copy(
                urrScorePercent = newUrr,
                medianTcrSeconds = newTcr,
                monthlyInferenceIdr = newSpend,
                unlockedStoryClues = newClues,
                dailyMinutesUsedToday = (_uiState.value.sessionElapsedSeconds / 60).coerceAtMost(15),
                childToCompanionWordRatio = newRatio,
                rbsRetentionPercent = newRbs
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        inactivityTimerJob?.cancel()
        sessionTickerJob?.cancel()
        audioController.release()
    }
}
