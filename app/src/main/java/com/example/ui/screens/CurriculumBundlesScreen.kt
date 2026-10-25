package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.OfflineBundleEntity
import com.example.data.SocraticCurriculumBank
import com.example.ui.theme.CalmOutlineVariant
import com.example.ui.theme.CalmPrimary
import com.example.ui.theme.CalmPrimaryContainer
import com.example.ui.theme.CalmPrimaryFixed
import com.example.ui.theme.CalmSecondary
import com.example.ui.theme.CalmSecondaryContainer
import com.example.ui.theme.CalmSurfaceContainer
import com.example.ui.theme.CalmSurfaceContainerHigh
import com.example.ui.theme.CalmSurfaceContainerLow
import com.example.ui.theme.CalmSurfaceContainerLowest
import com.example.ui.theme.CalmTertiary
import com.example.ui.theme.CalmTertiaryFixed
import com.example.ui.theme.CalmTertiaryFixedDim
import com.example.ui.theme.CardBorderTactile
import com.example.ui.theme.CardLipBottom
import com.example.ui.theme.EspressoMuted
import com.example.ui.theme.EspressoSlate
import com.example.ui.theme.OnPrimaryFixed
import com.example.ui.theme.OnSecondaryContainer
import com.example.ui.theme.SurfaceAlmond
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.SurfaceSandSubtle
import com.example.ui.theme.TerracottaPrimary

@Composable
fun CurriculumBundlesScreen(
    offlineBundles: List<OfflineBundleEntity>,
    onToggleBundleDownload: (OfflineBundleEntity) -> Unit,
    onLaunchTaskInCompanion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedOption by remember { mutableStateOf("5 Apel") }
    var hintActive by remember { mutableStateOf(false) }

    val downloadedMb = offlineBundles.filter { it.isDownloaded }.sumOf { it.sizeMb.toDouble() }.toFloat()
    val maxCapMb = 50.0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Top Calm SNR State Banner
        Surface(
            color = CalmSecondaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape(9999.dp),
            border = BorderStroke(1.dp, CalmSecondary.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(CalmSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Ruangan agak bising. Sentuh kartu jawabanmu:",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = CalmSurfaceContainerLowest.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(9999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = CalmSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "AUTO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CalmSecondary
                        )
                    }
                }
            }
        }

        // 2. Companion Audio Prompt Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLow),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, CardBorderTactile),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CalmPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "Kiko",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KIKO BERTANYA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CalmPrimary,
                            letterSpacing = 0.5.sp
                        )
                        // Calming soundwave bars
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(12.dp)
                        ) {
                            listOf(6.dp, 10.dp, 4.dp, 12.dp).forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height(h)
                                        .clip(RoundedCornerShape(9999.dp))
                                        .background(CalmPrimary.copy(alpha = 0.7f))
                                )
                            }
                        }
                    }
                    Text(
                        text = "“Ada berapa buah apel ya di piring?”",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EspressoSlate
                    )
                }
            }
        }

        // 3. Central Visual Counting Scene
        Card(
            colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainer),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, CardBorderTactile),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Surface(
                    color = CalmSurfaceContainerHigh,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🍎 🍎 🍎", fontSize = 38.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "🍏 🍏", fontSize = 34.sp)
                        }

                        // Gentle Prompt Overlay Badge
                        Surface(
                            color = CalmSurfaceContainerLowest.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(9999.dp),
                            border = BorderStroke(1.dp, CardBorderTactile),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = CalmTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Ayo hitung bersama Kiko",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EspressoSlate
                                )
                            }
                        }

                        // Subtle touch hint
                        Surface(
                            color = CalmSurfaceContainerLowest.copy(alpha = 0.85f),
                            shape = CircleShape,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = EspressoMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Tactile Fallback Montessori Cards (3 Horizontal Targets >=72pt)
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SENTUH SALAH SATU PIRING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EspressoMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Tersedia 3 Pilihan",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CalmSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val options = listOf("3 Apel", "5 Apel", "6 Apel")
                options.forEach { opt ->
                    val isSelected = opt == selectedOption
                    Card(
                        onClick = { selectedOption = opt },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CalmPrimaryFixed else CalmSurfaceContainerLow
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.5.dp,
                            color = if (isSelected) CalmPrimary else CardBorderTactile
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(104.dp)
                            .testTag(if (opt == "5 Apel") "fallback_card_OPT-5-APPLES" else "fallback_card_$opt")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            if (isSelected) {
                                Surface(
                                    color = CalmPrimary,
                                    shape = RoundedCornerShape(9999.dp),
                                    modifier = Modifier.align(Alignment.TopCenter)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "Pilihanmu",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = if (isSelected) 14.dp else 0.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (opt == "3 Apel") "🍎🍎🍎" else if (opt == "5 Apel") "🍎🍎🍎🍏🍏" else "🍎🍎🍎🍏🍏🍎",
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = opt,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) CalmPrimary else EspressoSlate
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(CalmPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = CalmOutlineVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Bottom Voice Re-engage Dock
        Surface(
            color = CalmSurfaceContainerHigh,
            shape = RoundedCornerShape(9999.dp),
            border = BorderStroke(1.dp, CardBorderTactile),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Petunjuk
                Surface(
                    onClick = { hintActive = !hintActive },
                    color = CalmSurfaceContainer,
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpCenter,
                            contentDescription = null,
                            tint = CalmTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Petunjuk",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EspressoSlate
                        )
                    }
                }

                // Center: Bicara (Seamless Voice Re-engagement)
                Surface(
                    onClick = { /* Re-engage voice */ },
                    color = CalmPrimary,
                    shape = RoundedCornerShape(9999.dp),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(CalmPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "Bicara",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                lineHeight = 14.sp
                            )
                            Text(
                                text = "Sentuh & Sebut",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 10.sp
                            )
                        }
                    }
                }

                // Right: Dengar Ulang
                Surface(
                    onClick = { /* replay */ },
                    color = CalmSurfaceContainer,
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = CalmSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Dengar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EspressoSlate
                        )
                    }
                }
            }
        }

        // 6. Offline Story Bundles (Curriculum Packs)
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceAlmond),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, CardBorderTactile),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paket Cerita Offline (${offlineBundles.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EspressoSlate
                    )
                    Text(
                        text = String.format("%.1f / %.0f MB", downloadedMb, maxCapMb),
                        style = MaterialTheme.typography.labelMedium,
                        color = CalmSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { (downloadedMb / maxCapMb).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(9999.dp)),
                    color = CalmSecondary,
                    trackColor = CardBorderTactile
                )

                offlineBundles.forEach { bundle ->
                    Surface(
                        color = CalmSurfaceContainerLowest,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorderTactile),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bundle.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = EspressoSlate
                                )
                                Text(
                                    text = "${bundle.category} • ${bundle.sizeMb} MB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EspressoMuted
                                )
                            }

                            FilledTonalButton(
                                onClick = { onToggleBundleDownload(bundle) },
                                shape = RoundedCornerShape(9999.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (bundle.isDownloaded) CalmSecondaryContainer else CalmSurfaceContainerHigh
                                ),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (bundle.isDownloaded) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = if (bundle.isDownloaded) CalmSecondary else EspressoSlate,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (bundle.isDownloaded) "Tersimpan" else "Unduh",
                                    fontSize = 11.sp,
                                    color = if (bundle.isDownloaded) CalmSecondary else EspressoSlate
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
