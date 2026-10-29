package com.example

import com.example.data.ChildProfileEntity
import com.example.data.SocraticCurriculumBank
import com.example.viewmodel.CalistungSessionCoordinator
import com.example.viewmodel.CompanionUiState
import com.example.viewmodel.VoiceTurnCoordinator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceTurnCoordinatorTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun selectVisualFallbackCard_correctOption_unlocksStoryClueAndAffirms() {
        val fakeDao = FakeCalistungDao()
        val fakeSpeaker = FakeAudioSpeaker()
        val uiState = MutableStateFlow(CompanionUiState(isVisualFallbackActive = true, currentStepDownLevel = 1))
        val profile = ChildProfileEntity(id = 1, name = "Bima", age = 5, avatarEmoji = "🦉")
        fakeDao.profiles.add(profile)

        val sessionCoordinator = CalistungSessionCoordinator(
            scope = testScope,
            audioController = fakeSpeaker,
            uiState = uiState,
            getCurrentTask = { SocraticCurriculumBank.tasks[0] }
        )

        var advanced = false
        val coordinator = VoiceTurnCoordinator(
            scope = testScope,
            dao = fakeDao,
            audioController = fakeSpeaker,
            sessionCoordinator = sessionCoordinator,
            uiState = uiState,
            getProfiles = { fakeDao.profiles },
            getCurrentTask = { SocraticCurriculumBank.tasks[0] },
            getTaskStartTimestamp = { System.currentTimeMillis() - 5000L },
            onAdvanceToNextTask = { advanced = true }
        )

        val task = SocraticCurriculumBank.tasks[0]
        val correctCard = task.fallbackOptions.first { it.isCorrect }

        coordinator.selectVisualFallbackCard(correctCard)

        assertFalse(uiState.value.isVisualFallbackActive)
        assertEquals(0, uiState.value.currentStepDownLevel)
        assertEquals(24.0f, uiState.value.currentSnrDb, 0.01f)
        assertTrue(uiState.value.dialogueHistory.any { it.text == task.celebrationAffirmation })
        assertTrue(fakeDao.telemetry.isNotEmpty())
    }

    @Test
    fun selectVisualFallbackCard_incorrectOption_providesGentleStepDownHint() {
        val fakeDao = FakeCalistungDao()
        val fakeSpeaker = FakeAudioSpeaker()
        val uiState = MutableStateFlow(CompanionUiState(isVisualFallbackActive = true, currentStepDownLevel = 0))

        val sessionCoordinator = CalistungSessionCoordinator(
            scope = testScope,
            audioController = fakeSpeaker,
            uiState = uiState,
            getCurrentTask = { SocraticCurriculumBank.tasks[0] }
        )

        val coordinator = VoiceTurnCoordinator(
            scope = testScope,
            dao = fakeDao,
            audioController = fakeSpeaker,
            sessionCoordinator = sessionCoordinator,
            uiState = uiState,
            getProfiles = { emptyList() },
            getCurrentTask = { SocraticCurriculumBank.tasks[0] },
            getTaskStartTimestamp = { System.currentTimeMillis() },
            onAdvanceToNextTask = {}
        )

        val task = SocraticCurriculumBank.tasks[0]
        val wrongCard = task.fallbackOptions.first { !it.isCorrect }

        coordinator.selectVisualFallbackCard(wrongCard)

        assertEquals(1, uiState.value.currentStepDownLevel)
        assertTrue(fakeSpeaker.spokenPhrases.contains(task.stepDown1Question))
    }
}
