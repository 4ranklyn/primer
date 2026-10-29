package com.example.viewmodel

import com.example.data.CalistungDao
import com.example.data.ChildProfileEntity

object ChildProgressTracker {

    fun buildParentWhatsAppDigestText(profile: ChildProfileEntity?): String {
        val childName = profile?.name ?: "Si Kecil"
        val urr = profile?.urrScorePercent ?: 76
        val tcr = profile?.medianTcrSeconds ?: 13
        return """
            🦉 *Laporan Belajar Kiko (Sesi 15 Menit)*
            Halo Bunda / Ayah! $childName baru saja menyelesaikan petualangan Calistung hari ini:
            
            ✅ *Skor Pemahaman Mandiri (URR):* $urr%
            ⚡ *Kecepatan Respon (Median TCR):* $tcr detik
            ✨ *Petunjuk Cerita Terbuka:* ${profile?.unlockedStoryClues ?: 4} Petunjuk
            
            🍽️ *Pertanyaan Meja Makan Hari Ini:*
            "Tadi bersama Kiko, kata apa yang berima dengan 'MATA' dan 'BATA' ya?"
        """.trimIndent()
    }

    suspend fun updateChildAggregateStats(
        dao: CalistungDao,
        currentProfile: ChildProfileEntity?,
        sessionElapsedSeconds: Int,
        solvedUnassisted: Boolean,
        tcrSeconds: Int,
        costIdr: Int,
        childWords: Int,
        companionWords: Int,
        rbsMastery: Int
    ) {
        if (currentProfile == null) return
        val newUrr = if (solvedUnassisted) {
            (currentProfile.urrScorePercent + 2).coerceAtMost(98)
        } else {
            currentProfile.urrScorePercent
        }
        val newTcr = ((currentProfile.medianTcrSeconds + tcrSeconds) / 2).coerceIn(8, 30)
        val newSpend = (currentProfile.monthlyInferenceIdr + costIdr).coerceAtMost(25000)
        val newClues = if (solvedUnassisted) currentProfile.unlockedStoryClues + 1 else currentProfile.unlockedStoryClues

        val currentRatio = currentProfile.childToCompanionWordRatio
        val turnRatio = if (companionWords > 0) childWords.toFloat() / companionWords.toFloat() else currentRatio
        val newRatio = ((currentRatio * 0.8f) + (turnRatio * 0.2f)).coerceIn(0.5f, 3.0f)

        val currentRbs = currentProfile.rbsRetentionPercent
        val newRbs = ((currentRbs * 0.85f) + (rbsMastery * 0.15f)).toInt().coerceIn(50, 99)

        dao.updateProfile(
            currentProfile.copy(
                urrScorePercent = newUrr,
                medianTcrSeconds = newTcr,
                monthlyInferenceIdr = newSpend,
                unlockedStoryClues = newClues,
                dailyMinutesUsedToday = (sessionElapsedSeconds / 60).coerceAtMost(15),
                childToCompanionWordRatio = newRatio,
                rbsRetentionPercent = newRbs
            )
        )
    }
}
