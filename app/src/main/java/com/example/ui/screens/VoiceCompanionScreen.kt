package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChildProfileEntity
import com.example.data.SocraticTask
import com.example.data.VisualCardOption
import com.example.viewmodel.CompanionUiState
import com.example.viewmodel.DialogueMessage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceCompanionScreen(
    uiState: CompanionUiState,
    profiles: List<ChildProfileEntity>,
    currentTask: SocraticTask,
    isSpeaking: Boolean,
    isListening: Boolean,
    onSelectProfile: (Int) -> Unit,
    onNextStoryTask: () -> Unit,
    onChildVoiceUtterance: (String) -> Unit,
    onSelectVisualFallbackCard: (VisualCardOption) -> Unit,
    onToggleNoisyRoomCards: () -> Unit,
    onTriggerWindDownRitual: () -> Unit,
    onParentResetSession: () -> Unit,
    onRequestMicAndListen: () -> Unit,
    onSpeakCurrentPrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val infiniteTransition = rememberInfiniteTransition(label = "kiko_breathing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isSpeaking || isListening) 1.08f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking || isListening) 650 else 1600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val remainingSeconds = (900 - uiState.sessionElapsedSeconds).coerceAtLeast(0)
    val remMin = remainingSeconds / 60

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Child Learner Switcher & Gentle 15-Minute Daily Timer
        ChildSessionHeader(
            profiles = profiles,
            selectedProfileId = uiState.selectedProfileId,
            remainingMinutes = remMin,
            sessionProgress = (uiState.sessionElapsedSeconds / 900f).coerceIn(0f, 1f),
            isLocked = uiState.isSessionLockedWindDown,
            onSelectProfile = onSelectProfile,
            onTriggerWindDownRitual = onTriggerWindDownRitual
        )

        // Calm Closing Ritual Screen when the 15-minute daily session finishes
        if (uiState.isSessionLockedWindDown) {
            CalmClosingRitualCard(
                onParentResetSession = onParentResetSession
            )
        } else {
            // Main Kiko Storybook Companion Stage
            KikoCharacterStageCard(
                currentTask = currentTask,
                stepDownLevel = uiState.currentStepDownLevel,
                companionMoodText = uiState.companionMoodText,
                pulseScale = pulseScale,
                isSpeaking = isSpeaking,
                isListening = isListening,
                isVisualFallbackActive = uiState.isVisualFallbackActive,
                onSpeakPrompt = onSpeakCurrentPrompt,
                onStartListening = onRequestMicAndListen,
                onToggleNoisyRoomCards = onToggleNoisyRoomCards,
                onNextStoryTask = onNextStoryTask
            )

            // Large Non-Text Picture Tap Cards when the room is noisy ("Hard to Hear" mode)
            if (uiState.isVisualFallbackActive) {
                MultimodalVisualFallbackSection(
                    task = currentTask,
                    onSelectCard = onSelectVisualFallbackCard
                )
            }

            // Friendly Voice Reply Bubbles (Child can tap to say an answer aloud or test without a mic)
            QuickChildVoiceBubblesCard(
                currentTask = currentTask,
                onChildUtterance = onChildVoiceUtterance
            )
        }

        // Story Conversation History
        StoryConversationCard(
            dialogueHistory = uiState.dialogueHistory
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChildSessionHeader(
    profiles: List<ChildProfileEntity>,
    selectedProfileId: Int,
    remainingMinutes: Int,
    sessionProgress: Float,
    isLocked: Boolean,
    onSelectProfile: (Int) -> Unit,
    onTriggerWindDownRitual: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    profiles.forEach { profile ->
                        val isSelected = profile.id == selectedProfileId
                        AssistChip(
                            onClick = { onSelectProfile(profile.id) },
                            label = {
                                Text(
                                    text = "${profile.avatarEmoji} ${profile.name}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            modifier = Modifier.testTag("profile_chip_${profile.id}")
                        )
                    }
                }

                if (!isLocked) {
                    AssistChip(
                        onClick = onTriggerWindDownRitual,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Wind down session",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Wind Down") },
                        modifier = Modifier.testTag("trigger_wind_down_button")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLocked) {
                        "🌙 Today's 15-Minute Story Time is Complete"
                    } else {
                        "⏳ Daily 15-Minute Voice Play • $remainingMinutes min left today"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isLocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                )
            }

            LinearProgressIndicator(
                progress = { sessionProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("daily_session_progress"),
                color = if (isLocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun KikoCharacterStageCard(
    currentTask: SocraticTask,
    stepDownLevel: Int,
    companionMoodText: String,
    pulseScale: Float,
    isSpeaking: Boolean,
    isListening: Boolean,
    isVisualFallbackActive: Boolean,
    onSpeakPrompt: () -> Unit,
    onStartListening: () -> Unit,
    onToggleNoisyRoomCards: () -> Unit,
    onNextStoryTask: () -> Unit
) {
    val currentKikoPrompt = currentTask.let { task ->
        when (stepDownLevel) {
            0 -> task.openingQuestion
            1 -> task.stepDown1Question
            2 -> task.stepDown2Question
            else -> task.stepDown3Scaffold
        }
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kiko_character_stage")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${currentTask.domain} • ${currentTask.ageTarget}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = if (stepDownLevel > 0) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("step_down_badge")
                ) {
                    Text(
                        text = if (stepDownLevel == 0) {
                            "Exploring Together"
                        } else {
                            "Step-Down Ladder: $stepDownLevel / 3"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (stepDownLevel > 0) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Breathing Kiko Owl Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.img_companion_kiko_1791699428835),
                    contentDescription = "Kiko the Owl Companion",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }

            Text(
                text = currentTask.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            // Large Story Illustration Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentTask.visualSceneEmojis,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "\"$currentKikoPrompt\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = companionMoodText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Primary Voice Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartListening,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("speak_to_kiko_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Talk to Kiko"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) "Listening..." else "Talk to Kiko",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                FilledTonalButton(
                    onClick = onSpeakPrompt,
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("replay_kiko_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Hear Kiko again"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSpeaking) "Speaking..." else "Listen")
                }
            }

            // Secondary Helpful Actions: "Noisy Room? Show Picture Cards" & "Next Story"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onToggleNoisyRoomCards,
                    modifier = Modifier.testTag("trigger_low_snr_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = "Switch to picture tap cards",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isVisualFallbackActive) "Hide Picture Cards" else "Noisy Room? Tap Cards")
                }

                OutlinedButton(
                    onClick = onNextStoryTask,
                    modifier = Modifier.testTag("next_story_button")
                ) {
                    Text("Next Riddle")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next story riddle",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MultimodalVisualFallbackSection(
    task: SocraticTask,
    onSelectCard: (VisualCardOption) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.large)
            .testTag("visual_fallback_cards_container")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = "Tap your answer",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Column {
                    Text(
                        text = "Tap Your Answer on the Screen!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "\"I'm having a little trouble hearing you. Can you tap your answer on the screen?\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                task.fallbackOptions.forEach { option ->
                    Card(
                        onClick = { onSelectCard(option) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(136.dp)
                            .testTag("fallback_card_${option.id}")
                            .semantics { contentDescription = option.spokenLabel }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = option.emojiVisual,
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = option.spokenLabel,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickChildVoiceBubblesCard(
    currentTask: SocraticTask,
    onChildUtterance: (String) -> Unit
) {
    val wrongSample = when (currentTask.id) {
        "TASK-NUM-01" -> "Three"
        "TASK-LIT-01" -> "Ma"
        "TASK-NUM-02" -> "Two"
        "TASK-LIT-02" -> "One clap"
        else -> "Two pinecones"
    }
    val rightSample = when (currentTask.id) {
        "TASK-NUM-01" -> "Five apples!"
        "TASK-LIT-01" -> "BO!"
        "TASK-NUM-02" -> "Four fireflies!"
        "TASK-LIT-02" -> "Two claps!"
        else -> "Six has more!"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Quick Voice Replies (Tap to answer aloud):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onChildUtterance(rightSample) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("simulate_correct_answer_button")
                ) {
                    Text("🗣️ \"$rightSample\"")
                }

                FilledTonalButton(
                    onClick = { onChildUtterance(wrongSample) },
                    modifier = Modifier.testTag("simulate_wrong_answer_button")
                ) {
                    Text("🤔 \"$wrongSample\"")
                }

                FilledTonalButton(
                    onClick = { onChildUtterance("Can you give me a clue?") }
                ) {
                    Text("🦉 \"Give me a clue!\"")
                }
            }
        }
    }
}

@Composable
private fun CalmClosingRitualCard(
    onParentResetSession: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calm_closing_ritual_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = "Calm Closing Ritual",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
            Text(
                text = "See You Tomorrow, Little Explorer!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = "\"You worked so hard today! Our adventure is done for now. Time to rest your ears!\"",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Kiko is sleeping in the treehouse until tomorrow's 15-minute story time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = onParentResetSession,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("parent_unlock_session_button")
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Parent: Start New Daily Session")
            }
        }
    }
}

@Composable
private fun StoryConversationCard(
    dialogueHistory: List<DialogueMessage>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("socratic_dialogue_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Our Story Conversation",
                style = MaterialTheme.typography.titleLarge
            )

            dialogueHistory.takeLast(5).forEachIndexed { idx, msg ->
                val isKiko = msg.speaker == "Kiko"
                val bgColor = when {
                    msg.isHintStep -> MaterialTheme.colorScheme.secondaryContainer
                    isKiko -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialogue_bubble_$idx")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isKiko) "🦉 Kiko" else "🗣️ Learner",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            msg.subtitle?.let { sub ->
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
