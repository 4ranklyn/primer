package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.VoiceCompanionAudioController
import com.example.data.CalistungDatabase
import com.example.data.CalistungDatabaseSeeder
import com.example.data.ChildProfileEntity
import com.example.data.ConsentAuditEntity
import com.example.data.EphemeralAudioBufferEntity
import com.example.data.OfflineBundleEntity
import com.example.data.ParentSurveyEntity
import com.example.data.SocraticCurriculumBank
import com.example.data.SocraticTask
import com.example.data.TelemetryEventEntity
import com.example.data.VisualCardOption
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

    private val audioController: VoiceCompanionAudioController = VoiceCompanionAudioController(
        context = application,
        onSpeechResult = { spokenText ->
            submitChildVoiceUtterance(spokenText)
        },
        onSpeechErrorOrSilence = { _ ->
            sessionCoordinator.handleNoiseOrSilenceTurn()
        },
        onRmsVolumeChanged = { rms ->
            val mappedSnr = (rms * 2.2f).coerceIn(0f, 35f)
            _uiState.update { it.copy(currentSnrDb = mappedSnr) }
        }
    )

    private val sessionCoordinator: CalistungSessionCoordinator = CalistungSessionCoordinator(
        scope = viewModelScope,
        audioController = audioController,
        uiState = _uiState,
        getCurrentTask = { currentTask() }
    )

    private var taskStartTimestampMillis: Long = System.currentTimeMillis()

    private val turnCoordinator: VoiceTurnCoordinator = VoiceTurnCoordinator(
        scope = viewModelScope,
        dao = dao,
        audioController = audioController,
        sessionCoordinator = sessionCoordinator,
        uiState = _uiState,
        getProfiles = { profiles.value },
        getCurrentTask = { currentTask() },
        getTaskStartTimestamp = { taskStartTimestampMillis },
        onAdvanceToNextTask = { advanceToNextStoryTask() }
    )

    val isSpeaking: StateFlow<Boolean> = audioController.isSpeaking
    val isListening: StateFlow<Boolean> = audioController.isListening

    init {
        viewModelScope.launch {
            CalistungDatabaseSeeder.seedInitialDataIfNeeded(dao)
        }
        checkParentalConsentGate()
        purgeExpiredAudioBuffers()
        initializeOpeningDialogue()
        sessionCoordinator.startSessionTicker()
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
        sessionCoordinator.resetInactivityTimer()
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
        sessionCoordinator.resetInactivityTimer()
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

    fun toggleNoisyRoomTapCards() {
        sessionCoordinator.toggleNoisyRoomTapCards()
    }

    fun triggerLowSnrAmbientNoiseScenario() {
        if (!_uiState.value.isVisualFallbackActive) {
            toggleNoisyRoomTapCards()
        }
    }

    fun submitChildVoiceUtterance(utterance: String) {
        turnCoordinator.submitChildVoiceUtterance(utterance)
    }

    fun selectVisualFallbackCard(option: VisualCardOption) {
        turnCoordinator.selectVisualFallbackCard(option)
    }

    fun trigger15MinuteClosingRitual() {
        sessionCoordinator.trigger15MinuteClosingRitual()
    }

    fun buildParentWhatsAppDigestText(): String {
        val profile = profiles.value.firstOrNull { it.id == _uiState.value.selectedProfileId }
        return ChildProgressTracker.buildParentWhatsAppDigestText(profile)
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
        sessionCoordinator.resetInactivityTimer()
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
                    childName = profiles.value.firstOrNull { it.id == _uiState.value.selectedProfileId }?.name ?: "Bima",
                    speaksFullSentencesScore = speaksFullSentences,
                    returnsPhoneCalmlyScore = returnsPhoneCalmly,
                    readsSyllablesIndependentlyScore = readsSyllables,
                    notes = notes.ifBlank { "Anak senang berhitung lantang bersama Kiko dan meletakkan ponsel dengan tenang." },
                    submittedAtDate = dateStr
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionCoordinator.release()
        audioController.release()
    }
}
