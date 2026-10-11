package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ChildProfileEntity
import com.example.data.ParentSurveyEntity
import com.example.data.TelemetryEventEntity

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
        Text(
            text = "Weekly Learning Progress",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "See how your child is discovering syllables, counting stories, and speaking with confidence.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (activeProfile != null) {
            TriadTelemetrySection(profile = activeProfile)
            TriadBenchmarkChartCard(profile = activeProfile)
        }

        // Family Learner Profiles Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("multi_child_profiles_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Your Children's Profiles",
                    style = MaterialTheme.typography.titleLarge
                )
                profiles.forEach { child ->
                    Surface(
                        color = if (child.id == selectedProfileId) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${child.avatarEmoji} ${child.name} (Age ${child.age})",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "First-Try Answers: ${child.urrScorePercent}% • Thinking Pace: ${child.medianTcrSeconds}s • Story Clues: ${child.unlockedStoryClues}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                text = child.scheduledDailyTime,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Monthly Parent Reflection Check-In
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("monthly_parent_survey_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Monthly Parent Check-In",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(
                    text = "Share how your child is progressing at home so Kiko can tailor future story adventures.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "1. Speaks in full sentences during story play: ${sentencesScore.toInt()} / 5",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = sentencesScore,
                    onValueChange = { sentencesScore = it },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Text(
                    text = "2. Puts the phone down calmly after the 15-minute wind-down: ${calmReturnScore.toInt()} / 5",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = calmReturnScore,
                    onValueChange = { calmReturnScore = it },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Text(
                    text = "3. Claps syllables & counts objects independently: ${syllablesScore.toInt()} / 5",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = syllablesScore,
                    onValueChange = { syllablesScore = it },
                    valueRange = 1f..5f,
                    steps = 3
                )

                OutlinedTextField(
                    value = surveyNotes,
                    onValueChange = { surveyNotes = it },
                    label = { Text("Favorite words or stories this week (optional)") },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_survey_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Monthly Check-In")
                }

                if (surveySavedBanner || surveys.isNotEmpty()) {
                    Text(
                        text = "Saved Check-Ins (${surveys.size}) • Last calm wind-down rating: ${surveys.firstOrNull()?.returnsPhoneCalmlyScore ?: 5}/5",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Recent Completed Story Activities
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("telemetry_event_log_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Story Sessions",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                telemetryEvents.take(6).forEach { ev ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${ev.taskTitle} • ${ev.category}",
                                style = MaterialTheme.typography.labelLarge
                            )
                            val helpLabel = if (ev.stepDownCount == 0) {
                                "Solved on first try!"
                            } else {
                                "Solved with ${ev.stepDownCount} gentle hint(s)"
                            }
                            Text(
                                text = "$helpLabel • Answered in ${ev.tcrSeconds}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Independent", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${profile.urrScorePercent}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Solved without hints",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Thinking Time", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${profile.medianTcrSeconds}s",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Average reply pace",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("2-Week Memory", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${profile.rbsRetentionPercent}%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Skills remembered",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun TriadBenchmarkChartCard(profile: ChildProfileEntity) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${profile.name}'s Calistung Growth",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Active Voice Ratio: ${profile.name} speaks ${"%.1f".format(profile.childToCompanionWordRatio)}x more words than Kiko!",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val barHeight = 22.dp.toPx()
                val gap = 16.dp.toPx()
                val maxW = size.width

                val metrics = listOf(
                    Triple(profile.urrScorePercent / 100f, primaryColor, 0),
                    Triple((30 - profile.medianTcrSeconds).coerceIn(5, 25) / 25f, secondaryColor, 1),
                    Triple(profile.rbsRetentionPercent / 100f, tertiaryColor, 2)
                )

                metrics.forEach { (ratio, color, idx) ->
                    val y = idx * (barHeight + gap) + 8.dp.toPx()
                    drawRoundRect(
                        color = trackColor,
                        topLeft = Offset(0f, y),
                        size = Size(maxW, barHeight),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(0f, y),
                        size = Size(maxW * ratio.coerceIn(0.1f, 1f), barHeight),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("■ First-Try (${profile.urrScorePercent}%)", style = MaterialTheme.typography.labelMedium)
                Text("■ Pace (${profile.medianTcrSeconds}s)", style = MaterialTheme.typography.labelMedium)
                Text("■ Memory (${profile.rbsRetentionPercent}%)", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
