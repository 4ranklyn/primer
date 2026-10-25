package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ChildProfileEntity
import com.example.data.ConsentAuditEntity
import com.example.data.EphemeralAudioBufferEntity
import com.example.ui.theme.CalmOutlineVariant
import com.example.ui.theme.CalmPrimary
import com.example.ui.theme.CalmPrimaryContainer
import com.example.ui.theme.CalmPrimaryFixed
import com.example.ui.theme.CalmSecondary
import com.example.ui.theme.CalmSecondaryContainer
import com.example.ui.theme.CalmSurfaceContainer
import com.example.ui.theme.CalmSurfaceContainerHigh
import com.example.ui.theme.CalmSurfaceContainerHighest
import com.example.ui.theme.CalmSurfaceContainerLow
import com.example.ui.theme.CalmSurfaceContainerLowest
import com.example.ui.theme.CalmTertiary
import com.example.ui.theme.CalmTertiaryContainer
import com.example.ui.theme.CalmTertiaryFixed
import com.example.ui.theme.CardBorderTactile
import com.example.ui.theme.EspressoMuted
import com.example.ui.theme.EspressoSlate
import com.example.ui.theme.MoonGlow
import com.example.ui.theme.OnPrimaryFixed
import com.example.ui.theme.OnSecondaryContainer
import com.example.ui.theme.PrivacySageSoft
import com.example.ui.theme.SurfaceAlmond
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.SurfaceSandSubtle
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.TwilightAmber
import com.example.ui.theme.TwilightIndigo
import kotlinx.coroutines.delay

@Composable
fun PrivacyAndUnitEconomicsScreen(
    activeProfile: ChildProfileEntity?,
    isOfflineMode: Boolean,
    consentLogs: List<ConsentAuditEntity>,
    audioBuffers: List<EphemeralAudioBufferEntity>,
    onToggleOfflineMode: (Boolean) -> Unit,
    onExecute24hAudioPurge: () -> Unit,
    onOpenConsentModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isLullabyPlaying by remember { mutableStateOf(false) }
    var purgeFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // 3-second hold to unlock parent mode
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var gateUnlocked by remember { mutableStateOf(false) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            while (isHolding && holdProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed / 3000f).coerceIn(0f, 1f)
                delay(50)
            }
            if (holdProgress >= 1f) {
                gateUnlocked = true
                onOpenConsentModal()
            }
        } else {
            holdProgress = 0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Waktu Istirahat (Closing Ritual Night Sky Card from Stitch Screen 3)
        Card(
            colors = CardDefaults.cardColors(containerColor = TwilightIndigo),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.5.dp, TwilightAmber.copy(alpha = 0.35f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Concentric glowing moon container
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(CalmSurfaceContainerHighest.copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(CalmSurfaceContainerHighest.copy(alpha = 0.25f))
                    )
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(TwilightAmber.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = "Bulan Waktu Istirahat",
                            tint = MoonGlow,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // Mini Zzz badge
                    Surface(
                        color = CalmTertiaryContainer,
                        shape = RoundedCornerShape(9999.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 6.dp)
                    ) {
                        Text(
                            text = "Zzz",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Waktunya Istirahat 🌙",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Telingamu hebat hari ini. Istirahat dulu ya!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CalmSurfaceContainerLow,
                    textAlign = TextAlign.Center
                )

                // Lullaby audio button
                Surface(
                    onClick = { isLullabyPlaying = !isLullabyPlaying },
                    color = if (isLullabyPlaying) CalmSecondaryContainer else CalmTertiaryFixed,
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = if (isLullabyPlaying) CalmSecondary else CalmPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isLullabyPlaying) "Memutar Musik..." else "Lagu Tidur",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLullabyPlaying) OnSecondaryContainer else CalmPrimary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = if (isLullabyPlaying) CalmSecondary else CalmPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 2. Stats Tiles (Batas Sehat & Capaian Tenang)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainer),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderTactile),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CalmSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = CalmSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "BATAS SEHAT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalmSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "15 Menit Suara",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = EspressoSlate
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainer),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderTactile),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CalmPrimaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = CalmPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CAPAIAN TENANG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalmPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "4 Selesai",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = EspressoSlate
                        )
                    }
                }
            }
        }

        // 3. Tomorrow's Adventure Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerHigh),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, CardBorderTactile),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CalmSurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🦊", fontSize = 24.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "PETUALANGAN ESOK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalmTertiary,
                            letterSpacing = 0.5.sp
                        )
                        Text(text = "🦊", fontSize = 12.sp)
                    }
                    Text(
                        text = "Petualangan Rubah Merah",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EspressoSlate
                    )
                    Text(
                        text = "Sampai jumpa saat mentari terbit",
                        style = MaterialTheme.typography.bodySmall,
                        color = EspressoMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CalmSurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbTwilight,
                        contentDescription = null,
                        tint = EspressoMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 4. 3-Second Hold Parental Gate Bar
        Surface(
            color = CalmSurfaceContainerHigh,
            shape = RoundedCornerShape(9999.dp),
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            tryAwaitRelease()
                            isHolding = false
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                // Animated progress fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(holdProgress)
                        .height(48.dp)
                        .background(if (holdProgress >= 1f) CalmSecondary else CalmPrimaryContainer)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (gateUnlocked) Icons.Default.Check else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (holdProgress > 0.5f) Color.White else EspressoSlate,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            gateUnlocked -> "Mode Orang Tua Terbuka ✓"
                            isHolding -> "Tahan... ${(3 * (1f - holdProgress)).toInt() + 1}s"
                            else -> "Akses Orang Tua (Tahan 3 Detik)"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (holdProgress > 0.5f) Color.White else EspressoSlate
                    )
                }
            }
        }

        // Audio privacy status indicator
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = CalmSecondary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Audio lokal telah aman dibersihkan",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = EspressoMuted
            )
        }

        // 5. Embedded Privacy & Unit Economics Cards (Supports Tests)
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceAlmond),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, CardBorderTactile),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audio_purge_center_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Privasi Audio Suara (POL-DATA-02)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoSlate
                )
                Text(
                    text = "Buffer lokal aktif: ${audioBuffers.size} rekaman sementara. Semua audio lokal otomatis dihapus dalam 24 jam.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EspressoMuted
                )
                Button(
                    onClick = {
                        onExecute24hAudioPurge()
                        purgeFeedbackMessage = "Semua rekaman audio lokal telah berhasil dihapus!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CalmSecondary),
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier.testTag("execute_purge_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bersihkan Audio Sekarang")
                }
                if (purgeFeedbackMessage != null) {
                    Text(
                        text = purgeFeedbackMessage!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = CalmSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCream),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, CardBorderTactile),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("unit_economics_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Langganan & Kebijakan Anak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoSlate
                )
                Text(
                    text = "Langganan Kiko: IDR 25.000 / bulan. Menjamin biaya operasional suara mandiri tanpa iklan atau pelacakan profil.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EspressoMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Moda Mandiri (Hemat Kuota)",
                        style = MaterialTheme.typography.labelLarge,
                        color = EspressoSlate
                    )
                    Switch(
                        checked = isOfflineMode,
                        onCheckedChange = onToggleOfflineMode,
                        colors = SwitchDefaults.colors(checkedThumbColor = CalmSecondary)
                    )
                }
            }
        }
    }
}
