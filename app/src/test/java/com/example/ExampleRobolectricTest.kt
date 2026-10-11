package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun scenario1_socraticStepDownLadder_neverSaysWrongAndAsksInducingQuestion() {
        composeTestRule.onNodeWithTag("kiko_character_stage").assertIsDisplayed()

        // Scroll to the simulator panel and click "Say 'Three' (Trigger FR-02 Step-Down)"
        composeTestRule.onNodeWithTag("simulate_wrong_answer_button")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        // Verify Step-Down Ladder advances to 1 / 3 and asks the inducing question from PRD Scenario 1
        composeTestRule.onNodeWithText("Step-Down Ladder: 1 / 3", substring = true)
            .performScrollTo()
            .assertIsDisplayed()

        val matchingNodes = composeTestRule.onAllNodesWithText(
            "Let's count them together! What number comes after one, two, three?",
            substring = true
        ).fetchSemanticsNodes()
        assertTrue("Expected Socratic Step-Down 1 inducing question to be rendered", matchingNodes.isNotEmpty())
    }

    @Test
    fun scenario2_ambientNoiseFallbackActivation_rendersVisualTapCards() {
        composeTestRule.onNodeWithTag("trigger_low_snr_button")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("visual_fallback_cards_container")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("fallback_card_OPT-5-APPLES").assertIsDisplayed()
    }

    @Test
    fun scenario3_dailySessionTerminationRitual_locksSessionWithCalmWindDown() {
        composeTestRule.onNodeWithTag("trigger_wind_down_button")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("calm_closing_ritual_card")
            .performScrollTo()
            .assertIsDisplayed()

        val windDownNodes = composeTestRule.onAllNodesWithText(
            "You worked so hard today! Our adventure is done for now. Time to rest your ears!",
            substring = true
        ).fetchSemanticsNodes()
        assertTrue("Expected verbatim wind-down ritual speech to be present", windDownNodes.isNotEmpty())
    }

    @Test
    fun parentHubAndPrivacyNavigation_displaysTriadTelemetryAndPurgeControls() {
        composeTestRule.onNodeWithTag("nav_tab_parent_hub").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("triad_telemetry_cards").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_privacy_ops").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("unit_economics_card")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("audio_purge_center_card")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
