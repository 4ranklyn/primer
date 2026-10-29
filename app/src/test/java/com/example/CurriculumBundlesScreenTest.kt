package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.OfflineBundleEntity
import com.example.ui.screens.CurriculumBundlesScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CurriculumBundlesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun curriculumBundlesScreen_rendersBundlesCorrectly() {
        val bundle = OfflineBundleEntity(
            bundleId = "BUNDLE-LIT-01",
            title = "Pengenalan Huruf dan Suku Kata",
            category = "Literacy",
            description = "Paket audio cerita interaktif",
            sizeMb = 18.5f,
            promptCount = 30,
            isDownloaded = false
        )

        composeTestRule.setContent {
            CurriculumBundlesScreen(
                offlineBundles = listOf(bundle),
                onToggleBundleDownload = {},
                onLaunchTaskInCompanion = {}
            )
        }

        composeTestRule.onNodeWithText("Pengenalan Huruf dan Suku Kata").assertIsDisplayed()
    }
}
