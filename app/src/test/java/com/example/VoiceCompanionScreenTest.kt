package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.ChildProfileEntity
import com.example.data.SocraticCurriculumBank
import com.example.ui.screens.VoiceCompanionScreen
import com.example.viewmodel.CompanionUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceCompanionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun voiceCompanionScreen_rendersTaskAndCompanion() {
        val task = SocraticCurriculumBank.tasks.first()
        val profile = ChildProfileEntity(
            id = 1,
            name = "Bima",
            age = 5,
            avatarEmoji = "🦉"
        )
        val state = CompanionUiState(
            selectedProfileId = 1,
            sessionElapsedSeconds = 120
        )

        composeTestRule.setContent {
            VoiceCompanionScreen(
                uiState = state,
                profiles = listOf(profile),
                currentTask = task,
                isSpeaking = false,
                isListening = false,
                onSelectProfile = {},
                onNextStoryTask = {},
                onChildVoiceUtterance = {},
                onSelectVisualFallbackCard = {},
                onToggleNoisyRoomCards = {},
                onTriggerWindDownRitual = {},
                onParentResetSession = {},
                onRequestMicAndListen = {},
                onSpeakCurrentPrompt = {}
            )
        }

        composeTestRule.onNodeWithText(task.title).assertIsDisplayed()
    }
}
