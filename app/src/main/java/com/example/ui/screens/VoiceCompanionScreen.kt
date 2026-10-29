package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.CalmPrimary
import com.example.ui.theme.CalmPrimaryContainer
import com.example.ui.theme.CalmSecondary
import com.example.ui.theme.CalmSecondaryContainer
import com.example.ui.theme.CalmSurfaceContainerHigh
import com.example.ui.theme.CalmSurfaceContainerLow
import com.example.ui.theme.CalmSurfaceContainerLowest
import com.example.ui.theme.CardBorderTactile
import com.example.ui.theme.EspressoMuted
import com.example.ui.theme.EspressoSlate
import com.example.ui.theme.MoonGlow
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.SurfaceSandSubtle
import com.example.ui.theme.TwilightAmber
import com.example.ui.theme.TwilightIndigo
import com.example.viewmodel.CompanionUiState

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
    onStartCoPlayDemo: () -> Unit = {},
    onCompleteCoPlayDemo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val remainingSeconds = (900 - uiState.sessionElapsedSeconds).coerceAtLeast(0)
    val remMin = remainingSeconds / 60

    val infiniteTransition = rememberInfiniteTransition(label = "kiko_aura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = if (isSpeaking || isListening) 1.15f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking || isListening) 650 else 2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("kiko_character_stage"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Guided Co-Play Banner (PRD Section 8.2)
        if (uiState.isCoPlayDemoActive) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CalmPrimaryContainer),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, CalmPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("co_play_onboarding_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🤝 Mode Demo Co-Play (3 Menit)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EspressoSlate
                    )
                    Text(
                        text = "Dampingi anak menjawab lantang. Kiko membimbing langkah demi langkah tanpa menyalahkan!",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoSlate
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onCompleteCoPlayDemo,
                            colors = ButtonDefaults.buttonColors(containerColor = CalmPrimary),
                            shape = RoundedCornerShape(9999.dp)
                        ) {
                            Text("Selesai Demo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Kiko Character Avatar
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(165.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(CalmSecondary.copy(alpha = if (isSpeaking || isListening) 0.35f else 0.12f))
            )

            Surface(
                color = CalmSurfaceContainerHigh,
                shape = CircleShape,
                border = BorderStroke(4.dp, CalmSurfaceContainerLowest),
                shadowElevation = 8.dp,
                modifier = Modifier.size(140.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_companion_kiko_1791699428835),
                    contentDescription = "Teman Belajar Kiko",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Surface(
                color = CalmSecondaryContainer,
                shape = CircleShape,
                border = BorderStroke(2.dp, Color.White),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = "Status Kiko",
                        tint = CalmSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Status Mood Pill
        Surface(
            color = CalmSurfaceContainerLow,
            shape = RoundedCornerShape(9999.dp),
            border = BorderStroke(1.dp, CardBorderTactile)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(14.dp)
                ) {
                    val barHeights = if (isSpeaking || isListening) {
                        listOf(0.5f, 1.0f, 0.7f, 0.4f)
                    } else {
                        listOf(0.3f, 0.5f, 0.3f, 0.2f)
                    }
                    barHeights.forEach { fraction ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((14 * fraction).dp)
                                .clip(RoundedCornerShape(9999.dp))
                                .background(CalmSecondary)
                        )
                    }
                }

                Text(
                    text = when {
                        isSpeaking -> "Kiko sedang berbicara..."
                        isListening -> "Kiko mendengarkanmu..."
                        else -> "Sisa $remMin menit hari ini"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = EspressoSlate,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 3. Clear Socratic Question Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLowest),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, CardBorderTactile),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.currentStepDownLevel > 0) {
                            "Ayo kita hitung bersama! Angka berapa yang datang setelah satu, dua, tiga? Let's count them together! What number comes after one, two, three?"
                        } else {
                            currentTask.openingQuestion
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = EspressoSlate,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (uiState.currentStepDownLevel > 0) {
                            "Tangga Petunjuk: ${uiState.currentStepDownLevel} / 3 • Kiko membimbingmu pelan-pelan! Step-Down Ladder: ${uiState.currentStepDownLevel} / 3"
                        } else {
                            "Ayo jawab dengan suara lantang ya!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.currentStepDownLevel > 0) CalmSecondary else EspressoMuted,
                        fontWeight = if (uiState.currentStepDownLevel > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    onClick = onSpeakCurrentPrompt,
                    color = CalmSurfaceContainerLow,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, CardBorderTactile),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("replay_kiko_voice_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Ulangi Pertanyaan",
                            tint = CalmPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 4. Large Dominant "Bicara Sekarang" Mic CTA
        Surface(
            onClick = onRequestMicAndListen,
            color = if (isListening) CalmSecondary else CalmPrimary,
            shape = RoundedCornerShape(9999.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .testTag("request_mic_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Bicara Sekarang",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = if (isListening) "Kiko Mendengarkan..." else "Tekan & Bicara",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isListening) "Katakan jawabanmu sekarang" else "Sebutkan jawabanmu dengan lantang",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 5. Tactile Fallback Cards (Appears ONLY when SNR is low or explicitly needed)
        if (uiState.isVisualFallbackActive) {
            TactileFallbackChoiceSection(
                task = currentTask,
                onSelectCard = onSelectVisualFallbackCard
            )
        }

        // 6. Calm Closing Ritual Card (Appears after 15 minutes)
        if (uiState.isSessionLockedWindDown) {
            CalmClosingRitualCard(
                onParentResetSession = onParentResetSession
            )
        }

        // 7. Hidden / Subtle Verification Row for Testing
        Surface(
            color = CalmSurfaceContainerLowest,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CardBorderTactile.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onChildVoiceUtterance("Three") },
                    shape = RoundedCornerShape(9999.dp),
                    border = BorderStroke(0.5.dp, CardBorderTactile),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_wrong_answer_button")
                ) {
                    Text("Jawab Tiga", fontSize = 10.sp, color = EspressoMuted)
                }

                OutlinedButton(
                    onClick = onToggleNoisyRoomCards,
                    shape = RoundedCornerShape(9999.dp),
                    border = BorderStroke(0.5.dp, CardBorderTactile),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("trigger_low_snr_button")
                ) {
                    Text(
                        text = if (uiState.isVisualFallbackActive) "Tutup Kartu" else "Kartu Pilihan",
                        fontSize = 10.sp,
                        color = EspressoMuted
                    )
                }

                OutlinedButton(
                    onClick = onTriggerWindDownRitual,
                    shape = RoundedCornerShape(9999.dp),
                    border = BorderStroke(0.5.dp, CardBorderTactile),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("trigger_wind_down_button")
                ) {
                    Text("Waktu Tidur", fontSize = 10.sp, color = EspressoMuted)
                }

                OutlinedButton(
                    onClick = {
                        if (uiState.isCoPlayDemoActive) onCompleteCoPlayDemo() else onStartCoPlayDemo()
                    },
                    shape = RoundedCornerShape(9999.dp),
                    border = BorderStroke(0.5.dp, CardBorderTactile),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("trigger_co_play_demo_button")
                ) {
                    Text(
                        text = if (uiState.isCoPlayDemoActive) "Tutup Demo" else "Demo",
                        fontSize = 10.sp,
                        color = EspressoMuted
                    )
                }
            }
        }
    }
}

/**
 * Tactile Fallback Cards (Noise / SNR Fallback)
 */
@Composable
private fun TactileFallbackChoiceSection(
    task: SocraticTask,
    onSelectCard: (VisualCardOption) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLowest),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, CardBorderTactile),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
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
                Surface(
                    color = SurfaceSandSubtle,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Pilih gambar",
                            tint = CalmPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Sentuh Kartu Jawabanmu",
                        style = MaterialTheme.typography.titleMedium,
                        color = EspressoSlate,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ruangan agak bising. Sentuh pilihan di bawah ya:",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoMuted
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                task.fallbackOptions.forEach { option ->
                    Card(
                        onClick = { onSelectCard(option) },
                        colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLow),
                        border = BorderStroke(1.5.dp, CardBorderTactile),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(112.dp)
                            .testTag("fallback_card_${option.id}")
                            .semantics { contentDescription = option.spokenLabel }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = option.emojiVisual,
                                fontSize = 30.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = option.spokenLabel,
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                color = EspressoSlate,
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
 * Calm Closing Ritual Card (Waktu Istirahat)
 */
@Composable
fun CalmClosingRitualCard(
    onParentResetSession: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TwilightIndigo),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.dp, TwilightAmber.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calm_closing_ritual_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = TwilightIndigo.copy(alpha = 0.8f),
                shape = CircleShape,
                border = BorderStroke(2.dp, MoonGlow),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Bulan Waktu Istirahat",
                        tint = MoonGlow,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Text(
                text = "Waktunya Istirahat 🌙",
                style = MaterialTheme.typography.headlineMedium,
                color = SurfaceCream,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "You worked so hard today! Our adventure is done for now. Time to rest your ears! Telingamu hebat hari ini. Istirahat dulu ya!",
                style = MaterialTheme.typography.bodyMedium,
                color = SurfaceCream.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Button(
                onClick = onParentResetSession,
                colors = ButtonDefaults.buttonColors(containerColor = TwilightAmber),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier.testTag("parent_unlock_reset_button")
            ) {
                Text(
                    text = "Akses Orang Tua (Mulai Sesi Baru)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
