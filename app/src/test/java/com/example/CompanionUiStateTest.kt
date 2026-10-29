package com.example

import com.example.viewmodel.CompanionUiState
import com.example.viewmodel.DialogueMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionUiStateTest {

    @Test
    fun dialogueMessage_defaultValues_areProperlyInitialized() {
        val msg = DialogueMessage(speaker = "Kiko", text = "Halo teman!")
        assertNotNull(msg.id)
        assertEquals("Kiko", msg.speaker)
        assertEquals("Halo teman!", msg.text)
        assertNull(msg.subtitle)
        assertFalse(msg.isHintStep)
    }

    @Test
    fun companionUiState_defaultValues_matchSocraticDefaults() {
        val state = CompanionUiState()
        assertEquals(1, state.selectedProfileId)
        assertTrue(state.hasParentalConsent)
        assertFalse(state.showConsentModal)
        assertFalse(state.isCoPlayDemoActive)
        assertEquals(0, state.currentTaskIndex)
        assertEquals(0, state.currentStepDownLevel)
        assertEquals(0, state.consecutiveLowSnrTurns)
        assertEquals(24.5f, state.currentSnrDb, 0.01f)
        assertFalse(state.isVisualFallbackActive)
        assertFalse(state.isSessionLockedWindDown)
        assertFalse(state.isSessionPaused)
        assertEquals(420, state.sessionElapsedSeconds)
        assertFalse(state.isOfflinePreCachedMode)
        assertFalse(state.isLoadingTurn)
        assertTrue(state.dialogueHistory.isEmpty())
        assertFalse(state.micPermissionGranted)
    }

    @Test
    fun companionUiState_copyModifier_updatesTargetFieldsCleanly() {
        val original = CompanionUiState()
        val updated = original.copy(
            isVisualFallbackActive = true,
            currentStepDownLevel = 2,
            consecutiveLowSnrTurns = 3
        )
        assertTrue(updated.isVisualFallbackActive)
        assertEquals(2, updated.currentStepDownLevel)
        assertEquals(3, updated.consecutiveLowSnrTurns)
        // Ensure immutability of other properties
        assertEquals(original.selectedProfileId, updated.selectedProfileId)
    }
}
