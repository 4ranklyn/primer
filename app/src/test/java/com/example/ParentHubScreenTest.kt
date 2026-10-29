package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.ChildProfileEntity
import com.example.ui.screens.ParentHubScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParentHubScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun parentHubScreen_rendersParentPortalHeaderAndChildProfiles() {
        val profile = ChildProfileEntity(
            id = 1,
            name = "Bima",
            age = 5,
            avatarEmoji = "🦉",
            urrScorePercent = 78,
            medianTcrSeconds = 14,
            rbsRetentionPercent = 85
        )

        composeTestRule.setContent {
            ParentHubScreen(
                profiles = listOf(profile),
                selectedProfileId = 1,
                telemetryEvents = emptyList(),
                surveys = emptyList(),
                onSubmitSurvey = { _, _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Bima").assertIsDisplayed()
    }
}
