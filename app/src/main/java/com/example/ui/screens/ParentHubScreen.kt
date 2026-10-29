package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PsychologyAlt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChildProfileEntity
import com.example.data.ParentSurveyEntity
import com.example.data.TelemetryEventEntity
import com.example.ui.theme.CalmPrimary
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
import com.example.ui.theme.MetricTabularLg
import com.example.ui.theme.OnSecondaryContainer
import com.example.ui.theme.SurfaceAlmond

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

    var cloudPurgeStatus by remember { mutableStateOf("Hapus Cloud") }
    var syncStatus by remember { mutableStateOf("Perbarui Materi") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Child Scope & Profile Selector Bar (Stitch Screen 4)
        Surface(
            color = CalmSurfaceContainer,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, CardBorderTactile),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CalmSurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CalmPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${activeProfile?.name ?: "Amina"} (${activeProfile?.age ?: 5}th)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = EspressoSlate
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = EspressoMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Konsol Pendamping Aktif",
                            fontSize = 11.sp,
                            color = EspressoMuted
                        )
                    }
                }

                Surface(
                    color = CalmSecondaryContainer,
                    shape = RoundedCornerShape(9999.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = CalmSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "TERENKRIPSI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSecondaryContainer,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // 2. Learning Telemetry Triad (URR, TCR, RBS from Stitch Screen 4)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("triad_telemetry_cards"),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // URR Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderTactile),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "URR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EspressoMuted,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = CalmSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${activeProfile?.urrScorePercent ?: 72}%",
                        style = MetricTabularLg,
                        color = CalmPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mandiri",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EspressoSlate
                    )
                    Text(
                        text = "+18% MoM",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalmSecondary
                    )
                }
            }

            // TCR Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderTactile),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TCR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EspressoMuted,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CalmSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${activeProfile?.medianTcrSeconds ?: 14}s",
                        style = MetricTabularLg,
                        color = CalmTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sintesis",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EspressoSlate
                    )
                    Text(
                        text = "-9.8s respon",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalmSecondary
                    )
                }
            }

            // RBS Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderTactile),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RBS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EspressoMuted,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = CalmSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${activeProfile?.rbsRetentionPercent ?: 84}%",
                        style = MetricTabularLg,
                        color = CalmSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daya Ingat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EspressoSlate
                    )
                    Text(
                        text = "14-Hari Siklus",
                        fontSize = 9.sp,
                        color = EspressoMuted
                    )
                }
            }
        }

        // 3. Developmental Trajectory Progress
        Card(
            colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLowest),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, CardBorderTactile),
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
                    Text(
                        text = "Trajektori Kognitif & Literasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EspressoSlate
                    )
                    Text(
                        text = "Fase Pra-Sekolah",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalmPrimary
                    )
                }

                // Metric 1: Phonemic
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fonemik & Suku Kata",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EspressoSlate
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = CalmSecondaryContainer,
                                shape = RoundedCornerShape(9999.dp)
                            ) {
                                Text(
                                    text = "Mahir",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "92%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EspressoSlate
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { 0.92f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(9999.dp)),
                        color = CalmSecondary,
                        trackColor = CalmSurfaceContainerHigh
                    )
                }

                // Metric 2: Counting
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Berhitung Objek (1-10)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EspressoSlate
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = CalmTertiaryFixed,
                                shape = RoundedCornerShape(9999.dp)
                            ) {
                                Text(
                                    text = "Berkembang",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CalmPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "78%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EspressoSlate
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { 0.78f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(9999.dp)),
                        color = CalmTertiary,
                        trackColor = CalmSurfaceContainerHigh
                    )
                }

                // Metric 3: Socratic Curiosity
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nalar Sokratik & Rasa Ingin Tahu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EspressoSlate
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = CalmPrimaryFixed,
                                shape = RoundedCornerShape(9999.dp)
                            ) {
                                Text(
                                    text = "Sangat Aktif",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CalmPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = "88%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EspressoSlate
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { 0.88f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(9999.dp)),
                        color = CalmPrimary,
                        trackColor = CalmSurfaceContainerHigh
                    )
                }
            }
        }

        // 4. Privacy & Data Sovereignty Ledger (2 Columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voice Purge Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderTactile),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
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
                            text = "POL-DATA-02",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalmSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Privasi Audio Suara",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EspressoSlate
                    )
                    Surface(
                        color = CalmSecondaryContainer,
                        shape = RoundedCornerShape(9999.dp)
                    ) {
                        Text(
                            text = "Auto-Purge 24 Jam",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    FilledTonalButton(
                        onClick = { cloudPurgeStatus = "Audio Terhapus!" },
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = CalmSurfaceContainerHighest),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = cloudPurgeStatus, fontSize = 10.sp, color = EspressoSlate)
                    }
                }
            }

            // Offline Material Pack Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CalmSurfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderTactile),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = CalmTertiary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "MODA MANDIRI",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CalmTertiary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Tanpa Internet",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = EspressoSlate
                    )
                    Text(
                        text = "34 MB tersimpan lokal",
                        fontSize = 10.sp,
                        color = EspressoMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    FilledTonalButton(
                        onClick = { syncStatus = "Materi Siap ✓" },
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = CalmSurfaceContainerHighest),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = syncStatus, fontSize = 10.sp, color = EspressoSlate)
                    }
                }
            }
        }

        // 5. Pedagogical Reflection Card
        Surface(
            color = CalmSecondaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CalmSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PsychologyAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Catatan Pedagogis Hari Ini",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Refleksi: Jeda berpikir 3.8 detik menandakan pembentukan logika mandiri yang tenang, bukan keraguan.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = OnSecondaryContainer
                    )
                }
            }
        }

        // 6. Monthly Parental Developmental Survey Form
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
                Text(
                    text = "Refleksi Bulanan Orang Tua",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EspressoSlate
                )

                Text(
                    text = "1. Mengulang kata & kalimat dengan jelas: ${sentencesScore.toInt()} / 5",
                    style = MaterialTheme.typography.bodySmall,
                    color = EspressoSlate
                )
                Slider(
                    value = sentencesScore,
                    onValueChange = { sentencesScore = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = CalmSecondary,
                        activeTrackColor = CalmSecondary
                    )
                )

                Text(
                    text = "2. Mengembalikan ponsel dengan tenang: ${calmReturnScore.toInt()} / 5",
                    style = MaterialTheme.typography.bodySmall,
                    color = EspressoSlate
                )
                Slider(
                    value = calmReturnScore,
                    onValueChange = { calmReturnScore = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = CalmPrimary,
                        activeTrackColor = CalmPrimary
                    )
                )

                OutlinedTextField(
                    value = surveyNotes,
                    onValueChange = { surveyNotes = it },
                    label = { Text("Catatan perilaku anak (opsional)") },
                    shape = RoundedCornerShape(14.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = CalmSecondary),
                    shape = RoundedCornerShape(9999.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_survey_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan Evaluasi Bulanan")
                }

                if (surveySavedBanner || surveys.isNotEmpty()) {
                    Text(
                        text = "Tersimpan: ${surveys.size} evaluasi tercatat.",
                        style = MaterialTheme.typography.labelMedium,
                        color = CalmSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
