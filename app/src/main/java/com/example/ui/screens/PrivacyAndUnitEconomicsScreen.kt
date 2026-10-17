package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ChildProfileEntity
import com.example.data.ConsentAuditEntity
import com.example.data.EphemeralAudioBufferEntity
import com.example.ui.theme.CalmSageOlive
import com.example.ui.theme.CardBorderSubtle
import com.example.ui.theme.CardWarmParchment
import com.example.ui.theme.SageContainerLight
import com.example.ui.theme.SocraticCoral
import com.example.ui.theme.TextEspresso
import com.example.ui.theme.TextMutedClay

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pengaturan Keluarga & Privasi Anak",
            style = MaterialTheme.typography.headlineMedium,
            color = TextEspresso
        )
        Text(
            text = "Kelola paket langganan keluarga, mode luring Wi-Fi, serta proteksi penghapusan suara 24 jam.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMutedClay
        )

        // Offline Mode Switch
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, CardBorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = CalmSageOlive
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Mode Paket Cerita Luring",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextEspresso
                        )
                        Text(
                            text = "Gunakan prompt audio lokal tanpa menguras kuota seluler.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMutedClay
                        )
                    }
                }
                Switch(
                    checked = isOfflineMode,
                    onCheckedChange = onToggleOfflineMode,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CalmSageOlive
                    ),
                    modifier = Modifier.testTag("offline_mode_switch")
                )
            }
        }

        // Paket Langganan Keluarga
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, CardBorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("unit_economics_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardMembership, contentDescription = null, tint = CalmSageOlive)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Paket Langganan Keluarga",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextEspresso
                    )
                }

                Surface(
                    color = SageContainerLight,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = activeProfile?.subscriptionTier ?: "Paket Standar Keluarga (IDR 99.000/bln)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CalmSageOlive
                        )
                        Text(
                            text = "Termasuk sesi harian suara 1-on-1 Socratic 15 menit, kurikulum bertingkat, dan hingga 3 profil anak.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextEspresso
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Promo Early Explorer: IDR 70.000/bln",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMutedClay
                    )
                    Text(
                        text = "Advanced (3 Anak): IDR 199.000/bln",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMutedClay
                    )
                }
            }
        }

        // Proteksi Privasi Suara 24 Jam
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, CardBorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audio_purge_center_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SocraticCoral
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Proteksi Privasi Suara 24 Jam",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextEspresso
                    )
                }

                Text(
                    text = "Suara anak Anda hanya diproses saat sesi tanya jawab aktif dan otomatis dihapus permanen dalam 24 jam. Nol pelacakan pihak ketiga dan tanpa iklan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMutedClay
                )

                if (audioBuffers.isEmpty()) {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ Semua rekaman sementara telah dibersihkan bersih (Zero Retention).",
                            style = MaterialTheme.typography.labelLarge,
                            color = CalmSageOlive,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    audioBuffers.take(3).forEach { buf ->
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CardBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${buf.chunkId} • ${buf.childName}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextEspresso
                                )
                                Text(
                                    text = buf.turnSummary,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextMutedClay
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onExecute24hAudioPurge,
                    colors = ButtonDefaults.buttonColors(containerColor = SocraticCoral),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("execute_audio_purge_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Memori Suara Sekarang")
                }
            }
        }

        // Persetujuan Orang Tua
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWarmParchment),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, CardBorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("parental_consent_audit_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = CalmSageOlive)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Persetujuan Suara Orang Tua",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextEspresso
                    )
                }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${stringResource(id = R.string.consent_notice_verbatim)}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = TextEspresso,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                FilledTonalButton(
                    onClick = onOpenConsentModal,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SageContainerLight,
                        contentColor = CalmSageOlive
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_consent_modal_button")
                ) {
                    Icon(Icons.Default.Policy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Perbarui Persetujuan Orang Tua")
                }

                consentLogs.firstOrNull()?.let { latest ->
                    Text(
                        text = "Disetujui oleh: ${latest.parentUserId} (${latest.timestampUtc})",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMutedClay
                    )
                }
            }
        }
    }
}
