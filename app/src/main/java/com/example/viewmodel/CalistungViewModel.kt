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
    val companionMoodText: String = "Kiko is ready to listen and play!",
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
                    it.copy(companionMoodText = "Kiko is gently repeating the question for you!")
                }

                delay(15000L) // Additional 15 seconds (25s total silence)
                if (!_uiState.value.isSessionLockedWindDown) {
                    _uiState.update {
                        it.copy(
                            isSessionPaused = true,
                            companionMoodText = "Session paused. Tap to resume when ready!"
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
                        companionMoodText = "Room is very noisy! Let's find a quiet corner."
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
                        companionMoodText = "Tap your answer on the screen below!"
                    )
                }
            }
            else -> {
                _uiState.update {
                    it.copy(
                        consecutiveLowSnrTurns = newConsecutive,
                        companionMoodText = "Tap the microphone or pick an answer below to talk with Kiko!"
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
                    subscriptionTier = "Family Standard Plan",
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
                    subscriptionTier = "Early Explorer Plan",
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
                    subscriptionTier = "Multi-Child Plus Plan",
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
                    title = "Forest Numbers & Counting Stories",
                    category = "Numbers & Counting • Ages 3–6",
                    description = "Warm interactive counting stories, comparing quantities, and playful addition adventures with Kiko.",
                    sizeMb = 14.2f,
                    promptCount = 48,
                    isDownloaded = true,
                    cachedPromptSavingsPercent = 44
                ),
                OfflineBundleEntity(
                    bundleId = "BUNDLE-LIT-SYLLABLES",
                    title = "River Stone Sounds & Syllables",
                    category = "Early Reading & Phonics • Ages 3–6",
                    description = "Musical syllable clapping (BO-LA, KU-DA), beginning letter sounds, and spoken vocabulary games.",
                    sizeMb = 16.8f,
                    promptCount = 56,
                    isDownloaded = true,
                    cachedPromptSavingsPercent = 41
                ),
                OfflineBundleEntity(
                    bundleId = "BUNDLE-RBS-SPIRAL",
                    title = "Treehouse Memory Treasures",
                    category = "2-Week Review & Discovery • Ages 4–6",
                    description = "Gentle review stories that help children remember and apply words and numbers they learned two weeks ago.",
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
                    parentUserId = "Parent Caregiver",
                    timestampUtc = "2026-10-11 14:00",
                    policyVersion = "Child Privacy Shield v1.0",
                    explicitAffirmationFlag = true,
                    noticeText = "We record temporary voice audio solely to converse with your child during active sessions. Raw audio recordings are deleted within 24 hours and are never used for advertising or model training."
                )
            )

            val sampleTelemetry = listOf(
                TelemetryEventEntity(
                    idempotencyKey = "session-log-001",
                    childId = 1,
                    taskId = "TASK-NUM-01",
                    taskTitle = "Five Forest Apples",
                    category = "Numbers & Counting",
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
                    taskTitle = "Syllable Clapping: BO-LA",
                    category = "Sounds & Syllables",
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
                    chunkId = "Active Voice Session",
                    childName = "Bima",
                    turnSummary = "Temporary voice turn in memory • Auto-deletes within 24 hours",
                    createdAtMillis = now - 120_000L,
                    expiresAtMillis = now + (23 * 3600_000L),
                    volatileRamOnly = true,
                    biometricStripped = true,
                    status = "Scheduled for Auto-Deletion"
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
                companionMoodText = "Switched learner profile! Kiko is ready."
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
                companionMoodText = "Playing: ${task.title}"
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
                companionMoodText = "Welcome to the 3-minute Co-Play Demo! Explore together."
            )
        }
        audioController.speak("Welcome to our 3-minute co-play demo! Let's explore together with Kiko.")
    }

    fun completeCoPlayOnboardingDemo() {
        _uiState.update {
            it.copy(
                isCoPlayDemoActive = false,
                companionMoodText = "Co-play demo complete! Kiko is ready for your adventure."
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
                    companionMoodText = "Noisy room mode: Tap a picture card below!",
                    dialogueHistory = state.dialogueHistory + DialogueMessage(
                        speaker = "Kiko",
                        text = fallbackSpeech,
                        subtitle = "Picture Cards Ready"
                    )
                )
            }
        } else {
            _uiState.update { state ->
                state.copy(
                    currentSnrDb = 24.5f,
                    consecutiveLowSnrTurns = 0,
                    isVisualFallbackActive = false,
                    companionMoodText = "Voice mode active! Speak aloud to Kiko."
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
                    turnSummary = "Temporary voice turn in memory • Auto-deletes within 24 hours",
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
                finalSubtitle = "Answer Guided Together • Advancing"
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
                        companionMoodText = "It is very loud in here right now! Let's find a quiet corner.",
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
                        companionMoodText = "I'm having a little trouble hearing you. Can you tap your answer on the screen?",
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
                            companionMoodText = "Daily 15-minute adventure complete! Time to rest.",
                            dialogueHistory = current.dialogueHistory + kikoReplyMsg + DialogueMessage(
                                speaker = "Kiko",
                                text = windDownText,
                                subtitle = "Bedtime & Wind-Down Ritual"
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
                                "Hoot-hurray! You figured it out!"
                            } else {
                                "Let's think together with a gentle clue..."
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
            subtitle = "Tapped Picture Card"
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
                    companionMoodText = "Awesome job! Back to voice play.",
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
                    companionMoodText = "Let's try counting together!",
                    dialogueHistory = it.dialogueHistory + tapMessage + DialogueMessage(
                        speaker = "Kiko",
                        text = hint,
                        subtitle = "Gentle Hint $nextStep of 3",
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
                companionMoodText = "Today's 15-minute adventure is complete!",
                dialogueHistory = state.dialogueHistory + DialogueMessage(
                    speaker = "Kiko",
                    text = windDownSpeech,
                    subtitle = "Daily Wind-Down Ritual"
                )
            )
        }
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
                companionMoodText = "Fresh 15-minute adventure started!",
                dialogueHistory = listOf(
                    DialogueMessage(
                        speaker = "Kiko",
                        text = "Welcome back for a fresh adventure! ${task.openingQuestion}",
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
                    "Offline Lesson Pack Active (No Wi-Fi needed)"
                } else {
                    "Kiko is ready to listen and play!"
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
                    parentUserId = parentId.ifBlank { "Parent Caregiver" },
                    timestampUtc = timestamp,
                    policyVersion = "Child Privacy Shield v1.0",
                    explicitAffirmationFlag = true,
                    noticeText = "We record temporary voice audio solely to converse with your child during active sessions. Raw audio recordings are deleted within 24 hours and are never used for advertising or model training."
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
                    notes = notes.ifBlank { "Enjoys counting aloud with Kiko and puts the phone down calmly." },
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
