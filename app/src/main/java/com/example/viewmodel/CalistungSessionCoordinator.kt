package com.example.viewmodel

import com.example.audio.AudioSpeaker
import com.example.data.SocraticTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalistungSessionCoordinator(
    private val scope: CoroutineScope,
    private val audioController: AudioSpeaker,
    private val uiState: MutableStateFlow<CompanionUiState>,
    private val getCurrentTask: () -> SocraticTask
) {
    private var inactivityTimerJob: Job? = null
    private var sessionTickerJob: Job? = null

    fun startSessionTicker() {
        sessionTickerJob?.cancel()
        sessionTickerJob = scope.launch {
            while (true) {
                delay(1000L)
                val state = uiState.value
                if (!state.isSessionLockedWindDown && !state.isSessionPaused) {
                    val nextSec = state.sessionElapsedSeconds + 1
                    if (nextSec >= 930) {
                        trigger15MinuteClosingRitual()
                    } else {
                        uiState.update { it.copy(sessionElapsedSeconds = nextSec) }
                    }
                }
            }
        }
    }

    fun resetInactivityTimer() {
        inactivityTimerJob?.cancel()
        val state = uiState.value
        if (state.isSessionLockedWindDown || state.isSessionPaused) return

        inactivityTimerJob = scope.launch {
            delay(10000L) // 10 seconds of silence
            if (!uiState.value.isSessionLockedWindDown && !uiState.value.isSessionPaused) {
                val lastKikoPrompt = uiState.value.dialogueHistory.lastOrNull { it.speaker == "Kiko" }?.text
                    ?: getCurrentTask().openingQuestion
                audioController.speak(lastKikoPrompt)
                uiState.update {
                    it.copy(companionMoodText = "Kiko mengulang pertanyaannya pelan-pelan ya!")
                }

                delay(15000L) // Additional 15 seconds (25s total silence)
                if (!uiState.value.isSessionLockedWindDown) {
                    uiState.update {
                        it.copy(
                            isSessionPaused = true,
                            companionMoodText = "Sesi dijeda. Sentuh layar saat siap lanjut ya!"
                        )
                    }
                }
            }
        }
    }

    fun cancelInactivityTimer() {
        inactivityTimerJob?.cancel()
    }

    fun handleNoiseOrSilenceTurn() {
        val state = uiState.value
        if (state.isSessionLockedWindDown) return

        val newConsecutive = state.consecutiveLowSnrTurns + 1
        when {
            newConsecutive >= 3 -> {
                val loudSpeech = "It is very loud in here right now! Let's find a quiet corner or try again after dinner."
                audioController.speak(loudSpeech)
                uiState.update {
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
                uiState.update {
                    it.copy(
                        consecutiveLowSnrTurns = newConsecutive,
                        isVisualFallbackActive = true,
                        companionMoodText = "Sentuh pilihan jawabanmu di layar ya!"
                    )
                }
            }
            else -> {
                uiState.update {
                    it.copy(
                        consecutiveLowSnrTurns = newConsecutive,
                        companionMoodText = "Sentuh mikrofon atau pilih jawaban untuk mengobrol dengan Kiko!"
                    )
                }
            }
        }
    }

    fun trigger15MinuteClosingRitual() {
        inactivityTimerJob?.cancel()
        val windDownSpeech = "You worked so hard today! Our adventure is done for now. Time to rest your ears!"
        audioController.speak(windDownSpeech)
        uiState.update { state ->
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

    fun toggleNoisyRoomTapCards() {
        if (uiState.value.isSessionLockedWindDown) return
        val enabling = !uiState.value.isVisualFallbackActive
        if (enabling) {
            val fallbackSpeech = "I'm having a little trouble hearing you. Can you tap your answer on the screen?"
            audioController.speak(fallbackSpeech)
            uiState.update { state ->
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
            uiState.update { state ->
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

    fun release() {
        inactivityTimerJob?.cancel()
        sessionTickerJob?.cancel()
    }
}
