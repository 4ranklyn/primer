package com.example

import com.example.data.ChildProfileEntity
import com.example.viewmodel.ChildProgressTracker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChildProgressTrackerTest {

    @Test
    fun buildParentWhatsAppDigestText_withNullProfile_usesFallbackDefaults() {
        val digest = ChildProgressTracker.buildParentWhatsAppDigestText(null)
        assertTrue(digest.contains("Si Kecil"))
        assertTrue(digest.contains("76%"))
        assertTrue(digest.contains("13 detik"))
    }

    @Test
    fun buildParentWhatsAppDigestText_withCustomProfile_rendersAccurateMetrics() {
        val profile = ChildProfileEntity(
            id = 5,
            name = "Kenzi",
            age = 5,
            avatarEmoji = "⭐",
            urrScorePercent = 88,
            medianTcrSeconds = 9,
            unlockedStoryClues = 7
        )
        val digest = ChildProgressTracker.buildParentWhatsAppDigestText(profile)
        assertTrue(digest.contains("Kenzi"))
        assertTrue(digest.contains("88%"))
        assertTrue(digest.contains("9 detik"))
        assertTrue(digest.contains("7 Petunjuk"))
    }

    @Test
    fun updateChildAggregateStats_unassistedSolve_incrementsUrrAndClues() = runBlocking {
        val fakeDao = FakeCalistungDao()
        val profile = ChildProfileEntity(
            id = 1,
            name = "Bima",
            age = 5,
            avatarEmoji = "🦉",
            urrScorePercent = 70,
            medianTcrSeconds = 14,
            unlockedStoryClues = 4,
            monthlyInferenceIdr = 5000,
            childToCompanionWordRatio = 1.0f,
            rbsRetentionPercent = 80
        )
        fakeDao.insertProfile(profile)

        ChildProgressTracker.updateChildAggregateStats(
            dao = fakeDao,
            currentProfile = profile,
            sessionElapsedSeconds = 600, // 10 minutes
            solvedUnassisted = true,
            tcrSeconds = 10,
            costIdr = 25,
            childWords = 10,
            companionWords = 5,
            rbsMastery = 90
        )

        val updated = fakeDao.profiles.first { it.id == 1 }
        assertEquals(72, updated.urrScorePercent)
        assertEquals(5, updated.unlockedStoryClues)
        assertEquals(10, updated.dailyMinutesUsedToday)
        assertEquals(5025, updated.monthlyInferenceIdr)
    }
}
