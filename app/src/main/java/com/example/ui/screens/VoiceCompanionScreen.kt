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
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.CalmOutlineVariant
import com.example.ui.theme.CalmPrimary
import com.example.ui.theme.CalmPrimaryContainer
import com.example.ui.theme.CalmSecondary
import com.example.ui.theme.CalmSecondaryContainer
import com.example.ui.theme.CalmSurfaceContainer
import com.example.ui.theme.CalmSurfaceContainerHigh
import com.example.ui.theme.CalmSurfaceContainerLow
import com.example.ui.theme.CalmSurfaceContainerLowest
import com.example.ui.theme.CalmTertiary
import com.example.ui.theme.CalmTertiaryFixedDim
import com.example.ui.theme.CardBorderTactile
import com.example.ui.theme.CardLipBottom
import com.example.ui.theme.DisplayChildLgMobile
import com.example.ui.theme.EspressoMuted
import com.example.ui.theme.EspressoSlate
import com.example.ui.theme.MoonGlow
import com.example.ui.theme.SurfaceAlmond
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.SurfaceSandSubtle
import com.example.ui.theme.TerracottaPrimary
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

    var showHintBadge by remember { mutableStateOf(false) }

    // Pulsing aura animation for active listening/speaking
    val infiniteTransition = rememberInfiniteTransition(label = "kiko_aura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = if (isSpeaking || isListening) 1.14f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking || isListening) 700 else 2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("kiko_character_stage"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 3-Minute Co-Play Onboarding Guided Banner (PRD Section 8.2 & 12.1)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🤝", fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mode Demo Co-Play (3 Menit)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EspressoSlate
                            )
                            Text(
                                text = "Orang tua & anak mendengarkan dan menjawab bersama Kiko",
                                style = MaterialTheme.typography.bodySmall,
                                color = EspressoMuted
                            )
                        }
                    }
                    Text(
                        text = "Dampingi anak Anda menjawab dengan suara lantang. Perhatikan bagaimana Kiko tidak pernah menyalahkan dan membimbing langkah demi langkah!",
                        style = MaterialTheme.typography.bodyMedium,
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
        // 1. Session Status & Warm Motivation Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = CalmSurfaceContainerLow,
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.dp, CardBorderTactile)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ManageSearch,
                        contentDescription = null,
                        tint = CalmTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Sisa $remMin Menit",
                        style = MaterialTheme.typography.labelLarge,
                        color = EspressoSlate,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Surface(
                color = CalmSurfaceContainerLow,
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.dp, CardBorderTactile)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = CalmTertiaryFixedDim,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "3 Bintang",
                        style = MaterialTheme.typography.labelLarge,
                        color = EspressoSlate,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Socratic Dialogue Bubble
        Card(
            colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLowest),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, CardBorderTactile),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "🍎",
                        fontSize = 28.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Column {
                        Text(
                            text = if (uiState.currentStepDownLevel > 0) {
                                "Let's count them together! What number comes after one, two, three?"
                            } else {
                                currentTask.openingQuestion
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = EspressoSlate,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.currentStepDownLevel > 0) {
                                "Step-Down Ladder: ${uiState.currentStepDownLevel} / 3 • Kiko menemanimu berhitung pelan-pelan!"
                            } else {
                                "Ayo kita hitung sama-sama!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.currentStepDownLevel > 0) CalmSecondary else EspressoMuted,
                            fontWeight = if (uiState.currentStepDownLevel > 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Surface(
                    onClick = onSpeakCurrentPrompt,
                    color = CalmSurfaceContainerLow,
                    shape = CircleShape,
                    border = BorderStroke(1.dp, CardBorderTactile),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("replay_kiko_voice_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Ulangi Pertanyaan",
                            tint = CalmPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 3. Central Living Companion Anchor (Kiko)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer pulsing aura
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(CalmSecondary.copy(alpha = if (isSpeaking || isListening) 0.30f else 0.12f))
                )

                // Companion circular frame
                Surface(
                    color = CalmSurfaceContainerHigh,
                    shape = CircleShape,
                    border = BorderStroke(3.dp, CalmSurfaceContainerLowest),
                    shadowElevation = 6.dp,
                    modifier = Modifier.size(96.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_companion_kiko_1791699428835),
                        contentDescription = "Kiko the Owl Companion",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Mini state indicator pin
                Surface(
                    color = CalmSecondaryContainer,
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, Color.White),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = "Status Kiko",
                            tint = CalmSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Companion listening/speaking status pill & audio rhythm bars
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = CalmSurfaceContainerLow,
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.dp, CardBorderTactile)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 4 rhythm wave bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(14.dp)
                    ) {
                        val barHeights = if (isSpeaking || isListening) {
                            listOf(0.6f, 1.0f, 0.75f, 0.45f)
                        } else {
                            listOf(0.4f, 0.6f, 0.4f, 0.3f)
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
                            isListening -> "Kiko lagi mendengarkan..."
                            else -> "Kiko siap mendengarkan..."
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = CalmSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 4. Visual Montessori Challenge Zone (Apples Concrete Counter)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Group 1: 3 Red Apples
            Card(
                colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLowest),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, CardBorderTactile),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = CalmSurfaceContainerLow,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🍎 🍎 🍎", fontSize = 24.sp)
                        }
                    }
                    Surface(
                        color = CalmSurfaceContainer,
                        shape = RoundedCornerShape(9999.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CalmPrimary)
                            )
                            Text(
                                text = "3 di meja",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EspressoSlate
                            )
                        }
                    }
                }
            }

            // Group 2: 2 Green Apples
            Card(
                colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLowest),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, CardBorderTactile),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = CalmSurfaceContainerLow,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🍏 🍏", fontSize = 24.sp)
                        }
                    }
                    Surface(
                        color = CalmSurfaceContainer,
                        shape = RoundedCornerShape(9999.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CalmSecondary)
                            )
                            Text(
                                text = "2 di piring",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = EspressoSlate
                            )
                        }
                    }
                }
            }
        }

        // 5. Primary Voice Control Cluster
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pill: Dengar
            Surface(
                onClick = onSpeakCurrentPrompt,
                color = CalmSurfaceContainer,
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.5.dp, CardBorderTactile),
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Dengar Contoh",
                        tint = CalmPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Dengar",
                        style = MaterialTheme.typography.labelLarge,
                        color = EspressoSlate,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Giant Dominant CTA: Bicara Sekarang
            Surface(
                onClick = onRequestMicAndListen,
                color = if (isListening) CalmSecondary else CalmPrimary,
                shape = RoundedCornerShape(9999.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(76.dp)
                    .testTag("request_mic_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Bicara Sekarang",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = if (isListening) "Mendengarkan" else "Bicara",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = if (isListening) "Katakan Jawabanmu" else "Sekarang",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Right Pill: Petunjuk
            Surface(
                onClick = { showHintBadge = !showHintBadge },
                color = CalmSurfaceContainer,
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.5.dp, CardBorderTactile),
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Beri Petunjuk",
                        tint = CalmTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Petunjuk",
                        style = MaterialTheme.typography.labelLarge,
                        color = EspressoSlate,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        if (showHintBadge) {
            Surface(
                color = CalmSurfaceContainerLow,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CalmTertiary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = CalmTertiary
                    )
                    Text(
                        text = "Petunjuk: Coba hitung 3 apel di meja, lalu tambahkan 2 apel di piring!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EspressoSlate
                    )
                }
            }
        }

        // Fallback Cards if toggled or in low-SNR mode
        if (uiState.isVisualFallbackActive) {
            TactileFallbackChoiceSection(
                task = currentTask,
                onSelectCard = onSelectVisualFallbackCard
            )
        }

        // Calm Closing Ritual Card if locked or triggered
        if (uiState.isSessionLockedWindDown) {
            CalmClosingRitualCard(
                onParentResetSession = onParentResetSession
            )
        }

        // Pedagogical & Testing Affordance Row (Supports Robolectric CUJ Verification)
        Surface(
            color = CalmSurfaceContainerLowest,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CardBorderTactile),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Aksi Simulasi Pendampingan",
                    style = MaterialTheme.typography.labelMedium,
                    color = EspressoMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onChildVoiceUtterance("Three") },
                        shape = RoundedCornerShape(9999.dp),
                        border = BorderStroke(1.dp, CardBorderTactile),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("simulate_wrong_answer_button")
                    ) {
                        Text("Jawab Tiga", fontSize = 11.sp, color = CalmPrimary)
                    }

                    OutlinedButton(
                        onClick = onToggleNoisyRoomCards,
                        shape = RoundedCornerShape(9999.dp),
                        border = BorderStroke(1.dp, CardBorderTactile),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("trigger_low_snr_button")
                    ) {
                        Text(
                            text = if (uiState.isVisualFallbackActive) "Tutup Kartu" else "Kartu Pilihan",
                            fontSize = 11.sp,
                            color = CalmSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = onTriggerWindDownRitual,
                        shape = RoundedCornerShape(9999.dp),
                        border = BorderStroke(1.dp, CardBorderTactile),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("trigger_wind_down_button")
                    ) {
                        Text("Waktu Tidur", fontSize = 11.sp, color = CalmTertiary)
                    }

                    OutlinedButton(
                        onClick = {
                            if (uiState.isCoPlayDemoActive) onCompleteCoPlayDemo() else onStartCoPlayDemo()
                        },
                        shape = RoundedCornerShape(9999.dp),
                        border = BorderStroke(1.dp, CardBorderTactile),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("trigger_co_play_demo_button")
                    ) {
                        Text(
                            text = if (uiState.isCoPlayDemoActive) "Tutup Demo" else "Demo Co-Play",
                            fontSize = 11.sp,
                            color = EspressoSlate
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tactile Calistung Fallback Choice Tiles
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
                    text = "Akses Orang Tua (Buka Kunci)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
