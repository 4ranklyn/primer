package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CurriculumBundlesScreen
import com.example.ui.screens.ParentHubScreen
import com.example.ui.screens.PrivacyAndUnitEconomicsScreen
import com.example.ui.screens.VoiceCompanionScreen
import com.example.ui.theme.CalmSageOlive
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CardWarmParchment
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SageContainerLight
import com.example.ui.theme.SoftSandCream
import com.example.ui.theme.SocraticCoral
import com.example.ui.theme.TextEspresso
import com.example.ui.theme.TextMutedClay
import com.example.viewmodel.CalistungViewModel

enum class AppDestination(val routeId: String, val labelRes: Int) {
    COMPANION("companion_stage", R.string.nav_companion),
    CURRICULUM("curriculum_bundles", R.string.nav_curriculum),
    PARENT_HUB("parent_hub", R.string.nav_parent_hub),
    PRIVACY_OPS("privacy_ops", R.string.nav_privacy_ops)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KikoCalistungApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KikoCalistungApp(
    viewModel: CalistungViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val telemetryEvents by viewModel.allTelemetry.collectAsStateWithLifecycle()
    val offlineBundles by viewModel.offlineBundles.collectAsStateWithLifecycle()
    val consentLogs by viewModel.consentLogs.collectAsStateWithLifecycle()
    val audioBuffers by viewModel.audioBuffers.collectAsStateWithLifecycle()
    val parentSurveys by viewModel.parentSurveys.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(AppDestination.COMPANION) }

    if (currentTab != AppDestination.COMPANION) {
        BackHandler {
            currentTab = AppDestination.COMPANION
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setMicPermissionGranted(granted)
        viewModel.startRealMicrophoneListening()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextEspresso
                        )
                        Text(
                            text = stringResource(id = R.string.tagline),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMutedClay
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openParentalConsentModal(true) },
                        modifier = Modifier.testTag("top_consent_shield_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Persetujuan Privasi Orang Tua",
                            tint = CalmSageOlive
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftSandCream
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardWarmParchment,
                modifier = Modifier.testTag("main_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppDestination.COMPANION,
                    onClick = { currentTab = AppDestination.COMPANION },
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = "Bermain dengan Kiko") },
                    label = { Text(stringResource(id = R.string.nav_companion)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CalmSageOlive,
                        indicatorColor = CalmSageOlive,
                        unselectedIconColor = TextMutedClay,
                        unselectedTextColor = TextMutedClay
                    ),
                    modifier = Modifier.testTag("nav_tab_companion")
                )
                NavigationBarItem(
                    selected = currentTab == AppDestination.CURRICULUM,
                    onClick = { currentTab = AppDestination.CURRICULUM },
                    icon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = "Cerita Belajar") },
                    label = { Text(stringResource(id = R.string.nav_curriculum)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CalmSageOlive,
                        indicatorColor = CalmSageOlive,
                        unselectedIconColor = TextMutedClay,
                        unselectedTextColor = TextMutedClay
                    ),
                    modifier = Modifier.testTag("nav_tab_curriculum")
                )
                NavigationBarItem(
                    selected = currentTab == AppDestination.PARENT_HUB,
                    onClick = { currentTab = AppDestination.PARENT_HUB },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Konsol Orang Tua") },
                    label = { Text(stringResource(id = R.string.nav_parent_hub)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CalmSageOlive,
                        indicatorColor = CalmSageOlive,
                        unselectedIconColor = TextMutedClay,
                        unselectedTextColor = TextMutedClay
                    ),
                    modifier = Modifier.testTag("nav_tab_parent_hub")
                )
                NavigationBarItem(
                    selected = currentTab == AppDestination.PRIVACY_OPS,
                    onClick = { currentTab = AppDestination.PRIVACY_OPS },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Pengaturan Keluarga") },
                    label = { Text(stringResource(id = R.string.nav_privacy_ops)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CalmSageOlive,
                        indicatorColor = CalmSageOlive,
                        unselectedIconColor = TextMutedClay,
                        unselectedTextColor = TextMutedClay
                    ),
                    modifier = Modifier.testTag("nav_tab_privacy_ops")
                )
            }
        }
    ) { innerPadding ->
        val currentTask = viewModel.currentTask()
        when (currentTab) {
            AppDestination.COMPANION -> {
                VoiceCompanionScreen(
                    uiState = uiState,
                    profiles = profiles,
                    currentTask = currentTask,
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    onSelectProfile = viewModel::selectChildProfile,
                    onNextStoryTask = viewModel::advanceToNextStoryTask,
                    onChildVoiceUtterance = viewModel::submitChildVoiceUtterance,
                    onSelectVisualFallbackCard = viewModel::selectVisualFallbackCard,
                    onToggleNoisyRoomCards = viewModel::toggleNoisyRoomTapCards,
                    onTriggerWindDownRitual = viewModel::trigger15MinuteClosingRitual,
                    onParentResetSession = viewModel::parentResetDailySessionLock,
                    onRequestMicAndListen = {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onSpeakCurrentPrompt = viewModel::speakCurrentPromptAloud,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppDestination.CURRICULUM -> {
                CurriculumBundlesScreen(
                    offlineBundles = offlineBundles,
                    onToggleBundleDownload = viewModel::toggleOfflineBundleDownload,
                    onLaunchTaskInCompanion = { idx ->
                        viewModel.selectCurriculumTask(idx)
                        currentTab = AppDestination.COMPANION
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppDestination.PARENT_HUB -> {
                ParentHubScreen(
                    profiles = profiles,
                    selectedProfileId = uiState.selectedProfileId,
                    telemetryEvents = telemetryEvents,
                    surveys = parentSurveys,
                    onSubmitSurvey = viewModel::submitMonthlyParentSurvey,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppDestination.PRIVACY_OPS -> {
                val activeProfile = profiles.firstOrNull { it.id == uiState.selectedProfileId } ?: profiles.firstOrNull()
                PrivacyAndUnitEconomicsScreen(
                    activeProfile = activeProfile,
                    isOfflineMode = uiState.isOfflinePreCachedMode,
                    consentLogs = consentLogs,
                    audioBuffers = audioBuffers,
                    onToggleOfflineMode = viewModel::toggleOfflineMode,
                    onExecute24hAudioPurge = viewModel::executeImmediate24hAudioPurge,
                    onOpenConsentModal = { viewModel.openParentalConsentModal(true) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (uiState.showConsentModal) {
        var parentNameInput by remember { mutableStateOf("Orang Tua / Wali") }
        AlertDialog(
            onDismissRequest = { viewModel.openParentalConsentModal(false) },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = CalmSageOlive
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Persetujuan Suara Orang Tua", color = TextEspresso)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = SageContainerLight,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorderSubtle)
                    ) {
                        Text(
                            text = "\"${stringResource(id = R.string.consent_notice_verbatim)}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = TextEspresso,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    OutlinedTextField(
                        value = parentNameInput,
                        onValueChange = { parentNameInput = it },
                        label = { Text("Nama Orang Tua / Wali") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_consent_id_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.recordParentalConsentAffirmation(parentNameInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = CalmSageOlive),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("confirm_parental_consent_button")
                ) {
                    Text("Saya Setuju & Aktifkan Suara")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.openParentalConsentModal(false) }
                ) {
                    Text("Tutup", color = TextMutedClay)
                }
            }
        )
    }
}
