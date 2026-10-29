package com.example.viewmodel

import java.util.UUID

data class DialogueMessage(
    val id: String = UUID.randomUUID().toString(),
    val speaker: String, // "Kiko" or "Child"
    val text: String,
    val subtitle: String? = null,
    val isHintStep: Boolean = false
)

data class CompanionUiState(
    val selectedProfileId: Int = 1,
    val hasParentalConsent: Boolean = true,
    val showConsentModal: Boolean = false,
    val isCoPlayDemoActive: Boolean = false,
    val currentTaskIndex: Int = 0,
    val currentStepDownLevel: Int = 0,
    val consecutiveLowSnrTurns: Int = 0,
    val currentSnrDb: Float = 24.5f,
    val isVisualFallbackActive: Boolean = false,
    val isSessionLockedWindDown: Boolean = false,
    val isSessionPaused: Boolean = false,
    val sessionElapsedSeconds: Int = 420, // 7 minutes into the 15-minute daily session
    val isOfflinePreCachedMode: Boolean = false,
    val isLoadingTurn: Boolean = false,
    val companionMoodText: String = "Kiko siap mendengarkan dan bermain!",
    val dialogueHistory: List<DialogueMessage> = emptyList(),
    val micPermissionGranted: Boolean = false
)
