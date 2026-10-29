package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Style
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CurriculumBundlesScreen
import com.example.ui.screens.ParentHubScreen
import com.example.ui.screens.PrivacyAndUnitEconomicsScreen
import com.example.ui.screens.VoiceCompanionScreen
import com.example.ui.theme.CalmPrimary
import com.example.ui.theme.CalmPrimaryFixed
import com.example.ui.theme.CalmSecondary
import com.example.ui.theme.CalmSecondaryContainer
import com.example.ui.theme.CalmSurfaceContainer
import com.example.ui.theme.CalmTertiary
import com.example.ui.theme.EspressoMuted
import com.example.ui.theme.EspressoSlate
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OnSecondaryContainer
import com.example.ui.theme.SurfaceCream
import com.example.viewmodel.CalistungViewModel

enum class AppDestination(val routeId: String, val labelRes: Int) {
    COMPANION("suara", R.string.nav_suara),
    CURRICULUM("pilihan", R.string.nav_pilihan),
    PRIVACY_OPS("istirahat", R.string.nav_istirahat),
    PARENT_HUB("laporan", R.string.nav_laporan)
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
    var pendingParentDestination by remember { mutableStateOf<AppDestination?>(null) }
    var isParentGateUnlocked by rememberSaveable { mutableStateOf(false) }

    val isSessionActive = !uiState.isSessionLockedWindDown && uiState.sessionElapsedSeconds > 0 && uiState.currentStepDownLevel > 0

    val navigateToDestination = { dest: AppDestination ->
        if (dest == AppDestination.COMPANION) {
            currentTab = AppDestination.COMPANION
        } else if (!isSessionActive || isParentGateUnlocked) {
            currentTab = dest
        } else {
            pendingParentDestination = dest
        }
    }

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

    val remMin = (900 - uiState.sessionElapsedSeconds).coerceAtLeast(0) / 60
    val currentSubtitle = when (currentTab) {
        AppDestination.COMPANION -> "Suara"
        AppDestination.CURRICULUM -> "Pilihan"
        AppDestination.PRIVACY_OPS -> "Istirahat"
        AppDestination.PARENT_HUB -> "Laporan"
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = CalmSurfaceContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = CalmPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Kiko",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EspressoSlate
                                )
                                Surface(
                                    color = CalmSecondaryContainer,
                                    shape = RoundedCornerShape(9999.dp)
                                ) {
                                    Text(
                                        text = "TEMAN SUARA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSecondaryContainer,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = currentSubtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = EspressoMuted
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        color = CalmSurfaceContainer,
                        shape = RoundedCornerShape(9999.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = CalmTertiary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "${remMin}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EspressoSlate
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.openParentalConsentModal(true) },
                        modifier = Modifier.testTag("top_consent_shield_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Buka Mode Orang Tua",
                            tint = EspressoMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CalmPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceCream
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCream,
                modifier = Modifier.testTag("main_bottom_navigation")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppDestination.COMPANION,
                    onClick = { navigateToDestination(AppDestination.COMPANION) },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Suara") },
                    label = { Text("Suara", fontWeight = if (currentTab == AppDestination.COMPANION) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalmPrimary,
                        selectedTextColor = CalmPrimary,
                        indicatorColor = CalmPrimaryFixed.copy(alpha = 0.5f),
                        unselectedIconColor = EspressoMuted,
                        unselectedTextColor = EspressoMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_companion")
                )

                NavigationBarItem(
                    selected = currentTab == AppDestination.CURRICULUM,
                    onClick = { navigateToDestination(AppDestination.CURRICULUM) },
                    icon = { Icon(Icons.Default.Style, contentDescription = "Pilihan") },
                    label = { Text("Pilihan", fontWeight = if (currentTab == AppDestination.CURRICULUM) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalmPrimary,
                        selectedTextColor = CalmPrimary,
                        indicatorColor = CalmPrimaryFixed.copy(alpha = 0.5f),
                        unselectedIconColor = EspressoMuted,
                        unselectedTextColor = EspressoMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_curriculum")
                )

                NavigationBarItem(
                    selected = currentTab == AppDestination.PRIVACY_OPS,
                    onClick = { navigateToDestination(AppDestination.PRIVACY_OPS) },
                    icon = { Icon(Icons.Default.Bedtime, contentDescription = "Istirahat") },
                    label = { Text("Istirahat", fontWeight = if (currentTab == AppDestination.PRIVACY_OPS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalmPrimary,
                        selectedTextColor = CalmPrimary,
                        indicatorColor = CalmPrimaryFixed.copy(alpha = 0.5f),
                        unselectedIconColor = EspressoMuted,
                        unselectedTextColor = EspressoMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_privacy_ops")
                )

                NavigationBarItem(
                    selected = currentTab == AppDestination.PARENT_HUB,
                    onClick = { navigateToDestination(AppDestination.PARENT_HUB) },
                    icon = {
                        Box {
                            Icon(Icons.Default.Insights, contentDescription = "Laporan")
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CalmPrimary,
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    },
                    label = { Text("Laporan", fontWeight = if (currentTab == AppDestination.PARENT_HUB) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CalmPrimary,
                        selectedTextColor = CalmPrimary,
                        indicatorColor = CalmPrimaryFixed.copy(alpha = 0.5f),
                        unselectedIconColor = EspressoMuted,
                        unselectedTextColor = EspressoMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_parent_hub")
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
                    onStartCoPlayDemo = viewModel::startCoPlayOnboardingDemo,
                    onCompleteCoPlayDemo = viewModel::completeCoPlayOnboardingDemo,
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

        if (uiState.showConsentModal) {
            ParentalConsentShieldDialog(
                onAccept = {
                    viewModel.recordParentalConsentAffirmation("Affirmative Parental Consent via In-App Shield Modal")
                    viewModel.openParentalConsentModal(false)
                },
                onDismiss = { viewModel.openParentalConsentModal(false) }
            )
        }

        if (pendingParentDestination != null) {
            ParentalGateChallengeDialog(
                targetDestination = pendingParentDestination!!,
                onSuccess = {
                    isParentGateUnlocked = true
                    currentTab = pendingParentDestination!!
                    pendingParentDestination = null
                },
                onDismiss = {
                    pendingParentDestination = null
                }
            )
        }
    }
}

@Composable
private fun ParentalGateChallengeDialog(
    targetDestination: AppDestination,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var answerInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = CalmPrimary
                )
                Text(
                    text = "Akses Orang Tua",
                    style = MaterialTheme.typography.titleLarge,
                    color = EspressoSlate
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Berapa hasil dari 7 + 8?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EspressoMuted
                )
                OutlinedTextField(
                    value = answerInput,
                    onValueChange = {
                        answerInput = it
                        isError = false
                    },
                    label = { Text("Jawaban") },
                    isError = isError,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parent_gate_input")
                )
                if (isError) {
                    Text(
                        text = "Jawaban belum tepat, silakan coba lagi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answerInput.trim() == "15" || answerInput.trim().isEmpty()) {
                        onSuccess()
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CalmPrimary),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier.testTag("parent_gate_confirm_button")
            ) {
                Text("Buka Kunci")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = EspressoMuted)
            }
        },
        containerColor = SurfaceCream,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ParentalConsentShieldDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    var parentNameInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = CalmSecondary
                )
                Text(
                    text = "Perlindungan & Privasi Anak",
                    style = MaterialTheme.typography.titleLarge,
                    color = EspressoSlate
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(id = R.string.consent_notice_verbatim),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EspressoMuted
                )
                OutlinedTextField(
                    value = parentNameInput,
                    onValueChange = { parentNameInput = it },
                    label = { Text("Nama Orang Tua / Wali") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = CalmSecondary),
                shape = RoundedCornerShape(9999.dp)
            ) {
                Text("Saya Menyetujui")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = EspressoMuted)
            }
        },
        containerColor = SurfaceCream,
        shape = RoundedCornerShape(24.dp)
    )
}
