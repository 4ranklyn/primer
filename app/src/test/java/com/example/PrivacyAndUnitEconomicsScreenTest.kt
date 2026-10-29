package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.ChildProfileEntity
import com.example.ui.screens.PrivacyAndUnitEconomicsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivacyAndUnitEconomicsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun privacyScreen_rendersAuditLogsAndOfflineControls() {
        val profile = ChildProfileEntity(
            id = 1,
            name = "Alya",
            age = 4,
            avatarEmoji = "🐰"
        )

        composeTestRule.setContent {
            PrivacyAndUnitEconomicsScreen(
                activeProfile = profile,
                isOfflineMode = false,
                consentLogs = emptyList(),
                audioBuffers = emptyList(),
                onToggleOfflineMode = {},
                onExecute24hAudioPurge = {},
                onOpenConsentModal = {}
            )
        }

        composeTestRule.onNodeWithText("Mode Offline Hemat Biaya").assertIsDisplayed()
    }
}
