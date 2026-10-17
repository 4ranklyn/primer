package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.OfflineBundleEntity
import com.example.data.SocraticCurriculumBank
import com.example.ui.theme.AmberWarmSun
import com.example.ui.theme.CalmSageOlive
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CardWarmParchment
import com.example.ui.theme.SageContainerLight
import com.example.ui.theme.SocraticCoral
import com.example.ui.theme.TextEspresso
import com.example.ui.theme.TextMutedClay

@Composable
fun CurriculumBundlesScreen(
    offlineBundles: List<OfflineBundleEntity>,
    onToggleBundleDownload: (OfflineBundleEntity) -> Unit,
    onLaunchTaskInCompanion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val downloadedMb = offlineBundles.filter { it.isDownloaded }.sumOf { it.sizeMb.toDouble() }.toFloat()
    val maxCapMb = 50.0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Storybook Hero Banner with Calm Tactile Styling
        Card(
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.5.dp, CardBorderSubtle),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_storybook_1791699444317),
                    contentDescription = "Rumah Pohon Belajar Kiko",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Kurikulum Calistung Socratic Kiko",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextEspresso
                    )
                    Text(
                        text = "Paket audio cerita berbobot rendah (<50MB) yang dapat diunduh melalui Wi-Fi untuk belajar luring tanpa kuota seluler.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedClay
                    )
                }
            }
        }

        // Offline Quota Indicator
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, CardBorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("offline_storage_quota_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Penyimpanan Paket Suara Luring",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextEspresso
                    )
                    Text(
                        text = "${"%.1f".format(downloadedMb)} MB / 50 MB",
                        style = MaterialTheme.typography.labelLarge,
                        color = CalmSageOlive,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { (downloadedMb / maxCapMb).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = CalmSageOlive,
                    trackColor = Color(0xFFE5DDD0)
                )
                Text(
                    text = "Hemat kuota dan latensi suara <1000ms dengan aset prompt ter-cache lokal.",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMutedClay
                )
            }
        }

        Text(
            text = "Paket Pelajaran Suara (Wi-Fi)",
            style = MaterialTheme.typography.headlineMedium,
            color = TextEspresso
        )

        offlineBundles.forEach { bundle ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, CardBorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bundle_card_${bundle.bundleId}")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bundle.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextEspresso
                            )
                            Text(
                                text = "${bundle.category} • ${bundle.promptCount} Cerita • ${bundle.sizeMb} MB",
                                style = MaterialTheme.typography.labelMedium,
                                color = CalmSageOlive
                            )
                        }
                        FilledTonalButton(
                            onClick = { onToggleBundleDownload(bundle) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (bundle.isDownloaded) SageContainerLight else Color(0xFFFBE8C8),
                                contentColor = if (bundle.isDownloaded) CalmSageOlive else SocraticCoral
                            ),
                            modifier = Modifier.testTag("toggle_bundle_${bundle.bundleId}")
                        ) {
                            Icon(
                                imageVector = if (bundle.isDownloaded) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (bundle.isDownloaded) "Tersimpan" else "Unduh")
                        }
                    }
                    Text(
                        text = bundle.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedClay
                    )
                }
            }
        }

        Text(
            text = "Aktivitas Calistung Terarah",
            style = MaterialTheme.typography.headlineMedium,
            color = TextEspresso
        )

        SocraticCurriculumBank.tasks.forEachIndexed { index, task ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, CardBorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("curriculum_task_card_$index")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${task.title} (${task.ageTarget})",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextEspresso
                            )
                            Text(
                                text = task.domain,
                                style = MaterialTheme.typography.labelMedium,
                                color = SocraticCoral
                            )
                        }
                        Button(
                            onClick = { onLaunchTaskInCompanion(index) },
                            colors = ButtonDefaults.buttonColors(containerColor = CalmSageOlive),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("launch_task_button_$index")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mulai")
                        }
                    }

                    Text(
                        text = "\"${task.openingQuestion}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextEspresso
                    )

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = AmberWarmSun,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tangga Bimbingan Socratic Kiko:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextEspresso
                                )
                            }
                            Text("1. \"${task.stepDown1Question}\"", style = MaterialTheme.typography.bodyMedium, color = TextMutedClay)
                            Text("2. \"${task.stepDown2Question}\"", style = MaterialTheme.typography.bodyMedium, color = TextMutedClay)
                            Text("3. \"${task.stepDown3Scaffold}\"", style = MaterialTheme.typography.bodyMedium, color = TextMutedClay)
                        }
                    }
                }
            }
        }
    }
}
