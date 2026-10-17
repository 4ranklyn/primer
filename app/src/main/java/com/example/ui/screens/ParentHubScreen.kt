package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ChildProfileEntity
import com.example.data.ParentSurveyEntity
import com.example.data.TelemetryEventEntity
import com.example.ui.theme.AmberWarmSun
import com.example.ui.theme.CalmSageOlive
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CardWarmParchment
import com.example.ui.theme.MetricPositiveGreen
import com.example.ui.theme.MetricSpiralPurple
import com.example.ui.theme.MetricWarningClay
import com.example.ui.theme.SageContainerLight
import com.example.ui.theme.SocraticCoral
import com.example.ui.theme.TextEspresso
import com.example.ui.theme.TextMutedClay

/**
 * Screen 4: Laporan Orang Tua Konsol (Analytical Clean, Muted Sage & Deep Navy, Zero Fluff)
 */
@Composable
fun ParentHubScreen(
    profiles: List<ChildProfileEntity>,
    selectedProfileId: Int,
    telemetryEvents: List<TelemetryEventEntity>,
    surveys: List<ParentSurveyEntity>,
    onSubmitSurvey: (Int, Int, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeProfile = profiles.firstOrNull { it.id == selectedProfileId } ?: profiles.firstOrNull()

    var sentencesScore by remember { mutableFloatStateOf(5f) }
    var calmReturnScore by remember { mutableFloatStateOf(5f) }
    var syllablesScore by remember { mutableFloatStateOf(4f) }
    var surveyNotes by remember { mutableStateOf("") }
    var surveySavedBanner by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Konsol Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Konsol Perkembangan Anak",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextEspresso
                )
                Text(
                    text = "Analisis penalaran Calistung & adaptasi kognitif harian",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMutedClay
                )
            }
            Surface(
                color = SageContainerLight,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Laporan Mingguan",
                    style = MaterialTheme.typography.labelMedium,
                    color = CalmSageOlive,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Triad Kognitif Utama (Unassisted Response Rate, Time-to-Correct, Retention Benchmark Spiral)
        if (activeProfile != null) {
            TriadTelemetrySection(profile = activeProfile)
            TriadBenchmarkChartCard(profile = activeProfile)
        }

        // Profil Anak dalam Rumah Tangga
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, CardBorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("multi_child_profiles_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = CalmSageOlive,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Profil Anak Aktif (Maks. 3 Profil)",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextEspresso
                    )
                }
                profiles.forEach { child ->
                    val isSelected = child.id == selectedProfileId
                    Surface(
                        color = if (isSelected) Color.White else Color(0xFFF9F5EC),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CalmSageOlive else CardBorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${child.avatarEmoji} ${child.name} (Usia ${child.age} Tahun)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextEspresso
                                )
                                Text(
                                    text = "Mandiri: ${child.urrScorePercent}% • Respons: ${child.medianTcrSeconds}s • Retensi: ${child.rbsRetentionPercent}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMutedClay
                                )
                            }
                            Surface(
                                color = if (isSelected) SageContainerLight else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = child.scheduledDailyTime,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CalmSageOlive,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Formulir Evaluasi Observasi Bulanan Orang Tua
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, CardBorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monthly_parent_survey_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = SocraticCoral,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Observasi Perkembangan di Rumah",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextEspresso
                    )
                }
                Text(
                    text = "Konfirmasi perilaku anak tanpa dopamin/layar pasif untuk penyesuaian kurikulum Kiko.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMutedClay
                )

                Text(
                    text = "1. Berbicara dengan kalimat lengkap saat bermain: ${sentencesScore.toInt()} / 5",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextEspresso
                )
                Slider(
                    value = sentencesScore,
                    onValueChange = { sentencesScore = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = CalmSageOlive,
                        activeTrackColor = CalmSageOlive
                    )
                )

                Text(
                    text = "2. Mengembalikan ponsel dengan tenang setelah 15 menit [Ritual Istirahat]: ${calmReturnScore.toInt()} / 5",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextEspresso
                )
                Slider(
                    value = calmReturnScore,
                    onValueChange = { calmReturnScore = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = SocraticCoral,
                        activeTrackColor = SocraticCoral
                    )
                )

                Text(
                    text = "3. Mandiri menebak suku kata & membandingkan jumlah benda: ${syllablesScore.toInt()} / 5",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextEspresso
                )
                Slider(
                    value = syllablesScore,
                    onValueChange = { syllablesScore = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberWarmSun,
                        activeTrackColor = AmberWarmSun
                    )
                )

                OutlinedTextField(
                    value = surveyNotes,
                    onValueChange = { surveyNotes = it },
                    label = { Text("Catatan perilaku atau kata baru anak (opsional)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("survey_notes_input")
                )

                Button(
                    onClick = {
                        onSubmitSurvey(
                            sentencesScore.toInt(),
                            calmReturnScore.toInt(),
                            syllablesScore.toInt(),
                            surveyNotes
                        )
                        surveyNotes = ""
                        surveySavedBanner = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CalmSageOlive),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_survey_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Evaluasi Bulanan")
                }

                if (surveySavedBanner || surveys.isNotEmpty()) {
                    Text(
                        text = "Tersimpan: ${surveys.size} evaluasi • Skor tenang mengembalikan ponsel: ${surveys.firstOrNull()?.returnsPhoneCalmlyScore ?: 5}/5",
                        style = MaterialTheme.typography.labelMedium,
                        color = MetricPositiveGreen
                    )
                }
            }
        }

        // Riwayat Sesi Percakapan Socratic Terakhir
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, CardBorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("telemetry_event_log_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = CalmSageOlive, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Riwayat Aktivitas Suara Terbaru",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextEspresso
                    )
                }
                telemetryEvents.take(6).forEach { ev ->
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ev.taskTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextEspresso,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${ev.tcrSeconds} detik",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CalmSageOlive
                                )
                            }
                            val helpLabel = if (ev.stepDownCount == 0) {
                                "Berhasil mandiri tanpa bantuan"
                            } else {
                                "Berhasil dengan ${ev.stepDownCount} petunjuk Socratic"
                            }
                            Text(
                                text = "${ev.category} • $helpLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedClay
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TriadTelemetrySection(profile: ChildProfileEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("triad_telemetry_cards"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // URR Metric Tile
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, CalmSageOlive.copy(alpha = 0.3f)),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MetricPositiveGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Respon Mandiri",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMutedClay
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${profile.urrScorePercent}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MetricPositiveGreen
                )
                Text(
                    text = "Target: >=70%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedClay
                )
            }
        }

        // TCR Metric Tile
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, SocraticCoral.copy(alpha = 0.3f)),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MetricWarningClay,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Waktu Deduksi",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMutedClay
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${profile.medianTcrSeconds}s",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MetricWarningClay
                )
                Text(
                    text = "Target: <=15s",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedClay
                )
            }
        }

        // RBS Metric Tile
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, MetricSpiralPurple.copy(alpha = 0.3f)),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MetricSpiralPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Retensi 14 Hari",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMutedClay
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${profile.rbsRetentionPercent}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MetricSpiralPurple
                )
                Text(
                    text = "Target: >=80%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedClay
                )
            }
        }
    }
}

@Composable
private fun TriadBenchmarkChartCard(profile: ChildProfileEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.5.dp, CardBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Kurva Daya Tangkap Calistung (${profile.name})",
                style = MaterialTheme.typography.titleLarge,
                color = TextEspresso
            )
            Text(
                text = "Rasio Berbicara: ${profile.name} aktif bersuara ${"%.1f".format(profile.childToCompanionWordRatio)}x lebih banyak dari Kiko",
                style = MaterialTheme.typography.labelLarge,
                color = CalmSageOlive
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val barHeight = 20.dp.toPx()
                val gap = 16.dp.toPx()
                val maxW = size.width

                val metrics = listOf(
                    Triple(profile.urrScorePercent / 100f, MetricPositiveGreen, 0),
                    Triple((30 - profile.medianTcrSeconds).coerceIn(5, 25) / 25f, MetricWarningClay, 1),
                    Triple(profile.rbsRetentionPercent / 100f, MetricSpiralPurple, 2)
                )

                metrics.forEach { (ratio, color, idx) ->
                    val y = idx * (barHeight + gap) + 6.dp.toPx()
                    drawRoundRect(
                        color = Color(0xFFF1EAE0),
                        topLeft = Offset(0f, y),
                        size = Size(maxW, barHeight),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(0f, y),
                        size = Size(maxW * ratio.coerceIn(0.1f, 1f), barHeight),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("■ Mandiri (${profile.urrScorePercent}%)", style = MaterialTheme.typography.labelMedium, color = MetricPositiveGreen)
                Text("■ Kecepatan (${profile.medianTcrSeconds}s)", style = MaterialTheme.typography.labelMedium, color = MetricWarningClay)
                Text("■ Spiral 14d (${profile.rbsRetentionPercent}%)", style = MaterialTheme.typography.labelMedium, color = MetricSpiralPurple)
            }
        }
    }
}
