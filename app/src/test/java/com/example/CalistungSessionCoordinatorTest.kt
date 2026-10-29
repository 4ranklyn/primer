package com.example

import com.example.audio.AudioSpeaker
import com.example.data.SocraticCurriculumBank
import com.example.viewmodel.CalistungSessionCoordinator
import com.example.viewmodel.CompanionUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAudioSpeaker : AudioSpeaker {
    val spokenPhrases = mutableListOf<String>()
    override fun speak(text: String) {
        spokenPhrases.add(text)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CalistungSessionCoordinatorTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun handleNoiseOrSilenceTurn_escalatesThroughLowSnrStates() {
        val fakeSpeaker = FakeAudioSpeaker()
        val uiState = MutableStateFlow(CompanionUiState(consecutiveLowSnrTurns = 0, isVisualFallbackActive = false))
        val coordinator = CalistungSessionCoordinator(
            scope = testScope,
            audioController = fakeSpeaker,
            uiState = uiState,
            getCurrentTask = { SocraticCurriculumBank.tasks[0] }
        )

        // Turn 1
        coordinator.handleNoiseOrSilenceTurn()
        assertEquals(1, uiState.value.consecutiveLowSnrTurns)
        assertFalse(uiState.value.isVisualFallbackActive)

        // Turn 2: triggers visual fallback and asks to tap screen
        coordinator.handleNoiseOrSilenceTurn()
        assertEquals(2, uiState.value.consecutiveLowSnrTurns)
        assertTrue(uiState.value.isVisualFallbackActive)
        assertTrue(fakeSpeaker.spokenPhrases.any { it.contains("tap your answer") })

        // Turn 3: loud environment mitigation
        coordinator.handleNoiseOrSilenceTurn()
        assertEquals(3, uiState.value.consecutiveLowSnrTurns)
        assertTrue(fakeSpeaker.spokenPhrases.any { it.contains("very loud in here") })
    }

    @Test
    fun toggleNoisyRoomTapCards_switchesBetweenVoiceAndCardModes() {
        val fakeSpeaker = FakeAudioSpeaker()
        val uiState = MutableStateFlow(CompanionUiState(isVisualFallbackActive = false))
        val coordinator = CalistungSessionCoordinator(
            scope = testScope,
            audioController = fakeSpeaker,
            uiState = uiState,
            getCurrentTask = { SocraticCurriculumBank.tasks[0] }
        )

        // Enable
        coordinator.toggleNoisyRoomTapCards()
        assertTrue(uiState.value.isVisualFallbackActive)
        assertEquals(8.5f, uiState.value.currentSnrDb, 0.01f)

        // Disable
        coordinator.toggleNoisyRoomTapCards()
        assertFalse(uiState.value.isVisualFallbackActive)
        assertEquals(24.5f, uiState.value.currentSnrDb, 0.01f)
    }

    @Test
    fun trigger15MinuteClosingRitual_locksSessionAndSpokeWindDown() {
        val fakeSpeaker = FakeAudioSpeaker()
        val uiState = MutableStateFlow(CompanionUiState(sessionElapsedSeconds = 420, isSessionLockedWindDown = false))
        val coordinator = CalistungSessionCoordinator(
            scope = testScope,
            audioController = fakeSpeaker,
            uiState = uiState,
            getCurrentTask = { SocraticCurriculumBank.tasks[0] }
        )

        coordinator.trigger15MinuteClosingRitual()
        assertTrue(uiState.value.isSessionLockedWindDown)
        assertEquals(900, uiState.value.sessionElapsedSeconds)
        assertTrue(fakeSpeaker.spokenPhrases.any { it.contains("Time to rest your ears") })
    }
}
