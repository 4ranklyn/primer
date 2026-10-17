package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
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
import com.example.ui.theme.AmberWarmSun
import com.example.ui.theme.CalmMintGlow
import com.example.ui.theme.CalmSageOlive
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CardOption1Bg
import com.example.ui.theme.CardOption1Border
import com.example.ui.theme.CardOption2Bg
import com.example.ui.theme.CardOption2Border
import com.example.ui.theme.CardWarmParchment
import com.example.ui.theme.MoonGlowYellow
import com.example.ui.theme.SageContainerLight
import com.example.ui.theme.SocraticCoral
import com.example.ui.theme.StarLavender
import com.example.ui.theme.StarLavenderMuted
import com.example.ui.theme.TextEspresso
import com.example.ui.theme.TextMutedClay
import com.example.ui.theme.TwilightBorder
import com.example.ui.theme.TwilightCardSurface
import com.example.ui.theme.TwilightDeepIndigo
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
        targetValue = if (isSpeaking || isListening) 1.09f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking || isListening) 650 else 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val remainingSeconds = (900 - uiState.sessionElapsedSeconds).coerceAtLeast(0)
    val remMin = remainingSeconds / 60
    val remSec = remainingSeconds % 60

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Learner Bar & Daily 15-Minute Calm Budget (Design System Invariant 1)
        ChildSessionHeader(
            profiles = profiles,
            selectedProfileId = uiState.selectedProfileId,
            remainingMinutes = remMin,
            remainingSeconds = remSec,
            sessionProgress = (uiState.sessionElapsedSeconds / 900f).coerceIn(0f, 1f),
            isLocked = uiState.isSessionLockedWindDown,
            onSelectProfile = onSelectProfile,
            onTriggerWindDownRitual = onTriggerWindDownRitual
        )

        // Screen 3: Waktu Istirahat Closing Ritual (Deep Twilight Indigo, Calm Bedtime Theme)
        if (uiState.isSessionLockedWindDown) {
            WaktuIstirahatClosingRitualCard(
                onParentResetSession = onParentResetSession
            )
        } else {
            // Screen 1: Suara Harian Socratic (Warm Organic Sage & Terracotta Coral)
            SuaraHarianSocraticStageCard(
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

            // Screen 2: Kartu Pilihan Fallback (Tactile, High-Contrast Tiles when ambient SNR is low)
            if (uiState.isVisualFallbackActive) {
                KartuPilihanFallbackSection(
                    task = currentTask,
                    onSelectCard = onSelectVisualFallbackCard
                )
            }

            // Quick Voice Reply Chips (Accessible testing & child co-play)
            QuickChildVoiceRepliesCard(
                currentTask = currentTask,
                onChildUtterance = onChildVoiceUtterance
            )
        }

        // Live Socratic Dialogue Stream
        SocraticConversationLogCard(
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
    remainingSeconds: Int,
    sessionProgress: Float,
    isLocked: Boolean,
    onSelectProfile: (Int) -> Unit,
    onTriggerWindDownRitual: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.5.dp, CardBorderSubtle),
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
                                containerColor = if (isSelected) SageContainerLight else Color(0xFFF9F5EC),
                                labelColor = if (isSelected) CalmSageOlive else TextMutedClay
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CalmSageOlive else CardBorderSubtle
                            ),
                            shape = RoundedCornerShape(14.dp),
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
                                contentDescription = "Wind down",
                                tint = SocraticCoral,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Waktu Istirahat", color = SocraticCoral) },
                        border = BorderStroke(1.dp, SocraticCoral.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("trigger_wind_down_button")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Bedtime else Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isLocked) MoonGlowYellow else AmberWarmSun,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLocked) {
                            "15 Menit Belajar Selesai (Waktu Istirahat)"
                        } else {
                            "Waktu Belajar Suara: %02d:%02d tersisa".format(remainingMinutes, remainingSeconds)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isLocked) SocraticCoral else TextEspresso
                    )
                }
                Text(
                    text = "Batas 15 Menit",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMutedClay
                )
            }

            LinearProgressIndicator(
                progress = { sessionProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("daily_session_progress"),
                color = if (isLocked) SocraticCoral else CalmSageOlive,
                trackColor = Color(0xFFE5DDD0)
            )
        }
    }
}

@Composable
private fun SuaraHarianSocraticStageCard(
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
    val currentPromptText = when (stepDownLevel) {
        0 -> currentTask.openingQuestion
        1 -> currentTask.stepDown1Question
        2 -> currentTask.stepDown2Question
        else -> currentTask.stepDown3Scaffold
    }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
        border = BorderStroke(1.5.dp, CardBorderSubtle),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                            SageContainerLight.copy(alpha = 0.5f),
                            CardWarmParchment
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Task Topic Ribbon & Scaffolding Step Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SageContainerLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${currentTask.domain} • ${currentTask.ageTarget}",
                        style = MaterialTheme.typography.labelLarge,
                        color = CalmSageOlive,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Surface(
                    color = if (stepDownLevel > 0) Color(0xFFFBE8C8) else Color(0xFFEBE3D5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("step_down_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (stepDownLevel > 0) SocraticCoral else TextMutedClay,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (stepDownLevel == 0) {
                                "Tahap 1: Eksplorasi Mandiri"
                            } else {
                                "Step-Down Ladder: $stepDownLevel / 3"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (stepDownLevel > 0) SocraticCoral else TextMutedClay
                        )
                    }
                }
            }

            // Kiko Breathing Owl Stage Avatar (Voice Pulse Ring in CalmMintGlow)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Expanding animated breathing aura
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isListening || isSpeaking) CalmMintGlow.copy(alpha = 0.45f) else AmberWarmSun.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.img_companion_kiko_1791699428835),
                    contentDescription = "Kiko the Socratic Owl Companion",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(122.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .border(3.5.dp, CalmSageOlive, CircleShape)
                )
            }

            Text(
                text = currentTask.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextEspresso,
                textAlign = TextAlign.Center
            )

            // Tactile Story Card & Speech Bubble
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, CardBorderSubtle),
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
                        text = "\"$currentPromptText\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextEspresso,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = companionMoodText,
                        style = MaterialTheme.typography.labelMedium,
                        color = CalmSageOlive,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Primary Voice Action Button (Warm Coral, 56dp Touch Target)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartListening,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) CalmSageOlive else SocraticCoral
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("speak_to_kiko_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Bicara pada Kiko"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isListening) "Kiko Mendengarkan..." else "Bicara pada Kiko",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                FilledTonalButton(
                    onClick = onSpeakPrompt,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SageContainerLight,
                        contentColor = CalmSageOlive
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("replay_kiko_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Dengarkan suara Kiko"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSpeaking) "Bicara..." else "Dengar")
                }
            }

            // Bottom Actions: Room Noise Fallback & Next Activity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onToggleNoisyRoomCards,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardBorderSubtle),
                    modifier = Modifier.testTag("trigger_low_snr_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = if (isVisualFallbackActive) SocraticCoral else TextMutedClay,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isVisualFallbackActive) "Tutup Kartu Pilihan" else "Ruangan Berisik? Kartu Gambar",
                        color = if (isVisualFallbackActive) SocraticCoral else TextMutedClay
                    )
                }

                OutlinedButton(
                    onClick = onNextStoryTask,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardBorderSubtle),
                    modifier = Modifier.testTag("next_story_button")
                ) {
                    Text("Cerita Lanjut", color = CalmSageOlive)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CalmSageOlive,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Screen 2: Kartu Pilihan Fallback (High-Contrast, Tactile Picture Tap Targets for Ambient Noise / SNR Fallback)
 */
@Composable
private fun KartuPilihanFallbackSection(
    task: SocraticTask,
    onSelectCard: (VisualCardOption) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(2.dp, CardOption1Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visual_fallback_cards_container")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = CardOption1Bg,
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Pilih gambar",
                            tint = SocraticCoral,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Ketuk Jawaban di Layar",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextEspresso
                    )
                    Text(
                        text = "\"Kiko agak sulit mendengar suaramu. Yuk ketuk kartu jawaban di bawah!\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedClay
                    )
                }
            }

            // High-Contrast Choice Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                task.fallbackOptions.forEachIndexed { idx, option ->
                    val tileBg = if (idx == 0) CardOption1Bg else CardOption2Bg
                    val tileBorder = if (idx == 0) CardOption1Border else CardOption2Border

                    Card(
                        onClick = { onSelectCard(option) },
                        colors = CardDefaults.cardColors(containerColor = tileBg),
                        border = BorderStroke(2.dp, tileBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(144.dp)
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
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = option.spokenLabel,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = TextEspresso,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen 3: Waktu Istirahat Closing Ritual (Deep Twilight Indigo, Calm Sleep Transition)
 */
@Composable
private fun WaktuIstirahatClosingRitualCard(
    onParentResetSession: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TwilightDeepIndigo),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.dp, TwilightBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calm_closing_ritual_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Glowing Moon Illustration Container
            Surface(
                color = TwilightCardSurface,
                shape = CircleShape,
                border = BorderStroke(2.dp, TwilightBorder),
                modifier = Modifier.size(92.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Bulan Waktu Istirahat",
                        tint = MoonGlowYellow,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Text(
                text = "Waktu Istirahat Tiba, Sahabat Cilik!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Surface(
                color = TwilightCardSurface,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, TwilightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"Kamu sudah belajar dengan sangat hebat hari ini! Petualangan kita selesai untuk hari ini. Saatnya mengistirahatkan telingamu!\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = MoonGlowYellow,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Text(
                text = "Kiko sekarang beristirahat di rumah pohon sampai jadwal belajar besok. Kembalikan ponsel kepada ayah atau ibu dengan tenang.",
                style = MaterialTheme.typography.bodyMedium,
                color = StarLavender,
                textAlign = TextAlign.Center
            )

            OutlinedButton(
                onClick = onParentResetSession,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, MoonGlowYellow),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MoonGlowYellow
                ),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("parent_unlock_session_button")
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Orang Tua: Mulai Sesi Baru")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickChildVoiceRepliesCard(
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
        colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AmberWarmSun,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pilihan Suara Anak (Ketuk untuk menjawab):",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextEspresso
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onChildUtterance(rightSample) },
                    colors = ButtonDefaults.buttonColors(containerColor = CalmSageOlive),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("simulate_correct_answer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🗣️ \"$rightSample\"")
                }

                FilledTonalButton(
                    onClick = { onChildUtterance(wrongSample) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFFBE8C8),
                        contentColor = SocraticCoral
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("simulate_wrong_answer_button")
                ) {
                    Text("🤔 \"$wrongSample\"")
                }

                FilledTonalButton(
                    onClick = { onChildUtterance("Minta petunjuk Kiko") },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SageContainerLight,
                        contentColor = CalmSageOlive
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("🦉 \"Minta Petunjuk!\"")
                }
            }
        }
    }
}

@Composable
private fun SocraticConversationLogCard(
    dialogueHistory: List<DialogueMessage>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, CardBorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("socratic_dialogue_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Percakapan Cerita Socratic",
                style = MaterialTheme.typography.titleLarge,
                color = TextEspresso
            )

            dialogueHistory.takeLast(5).forEachIndexed { idx, msg ->
                val isKiko = msg.speaker == "Kiko"
                val bgColor = when {
                    msg.isHintStep -> Color(0xFFFBE8C8)
                    isKiko -> Color.White
                    else -> SageContainerLight
                }
                Surface(
                    color = bgColor,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardBorderSubtle),
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
                                text = if (isKiko) "🦉 Kiko Pendamping" else "🗣️ Anak",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isKiko) CalmSageOlive else SocraticCoral
                            )
                            msg.subtitle?.let { sub ->
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextMutedClay
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextEspresso
                        )
                    }
                }
            }
        }
    }
}
