package com.example.data

object CalistungDatabaseSeeder {

    suspend fun seedInitialDataIfNeeded(dao: CalistungDao) {
        val initialProfiles = listOf(
            ChildProfileEntity(
                id = 1,
                name = "Bima",
                age = 5,
                avatarEmoji = "🦉",
                subscriptionTier = "Paket Standar Keluarga",
                dailyMinutesUsedToday = 7,
                urrScorePercent = 76,
                medianTcrSeconds = 13,
                rbsRetentionPercent = 86,
                monthlyInferenceIdr = 14850,
                childToCompanionWordRatio = 1.62f,
                unlockedStoryClues = 4,
                scheduledDailyTime = "4:30 PM"
            ),
            ChildProfileEntity(
                id = 2,
                name = "Alya",
                age = 4,
                avatarEmoji = "🦊",
                subscriptionTier = "Paket Penjelajah Cilik",
                dailyMinutesUsedToday = 5,
                urrScorePercent = 70,
                medianTcrSeconds = 15,
                rbsRetentionPercent = 82,
                monthlyInferenceIdr = 11200,
                childToCompanionWordRatio = 1.38f,
                unlockedStoryClues = 3,
                scheduledDailyTime = "4:00 PM"
            ),
            ChildProfileEntity(
                id = 3,
                name = "Raka",
                age = 6,
                avatarEmoji = "🐻",
                subscriptionTier = "Paket Multi-Anak Plus",
                dailyMinutesUsedToday = 11,
                urrScorePercent = 84,
                medianTcrSeconds = 11,
                rbsRetentionPercent = 91,
                monthlyInferenceIdr = 18400,
                childToCompanionWordRatio = 1.85f,
                unlockedStoryClues = 5,
                scheduledDailyTime = "5:00 PM"
            )
        )
        initialProfiles.forEach { dao.insertProfile(it) }

        val bundles = listOf(
            OfflineBundleEntity(
                bundleId = "BUNDLE-NUM-CORE",
                title = "Cerita Berhitung & Angka Hutan",
                category = "Berhitung & Angka • Usia 3–6",
                description = "Petualangan cerita berhitung interaktif yang hangat, membandingkan jumlah, dan penjumlahan ceria bersama Kiko.",
                sizeMb = 14.2f,
                promptCount = 48,
                isDownloaded = true,
                cachedPromptSavingsPercent = 44
            ),
            OfflineBundleEntity(
                bundleId = "BUNDLE-LIT-SYLLABLES",
                title = "Batu Sungai Bunyi & Suku Kata",
                category = "Membaca & Fonik Awal • Usia 3–6",
                description = "Tepuk tangan berirama untuk suku kata (BO-LA, KU-DA), pengenalan bunyi huruf awal, dan permainan kosakata lisan.",
                sizeMb = 16.8f,
                promptCount = 56,
                isDownloaded = true,
                cachedPromptSavingsPercent = 41
            ),
            OfflineBundleEntity(
                bundleId = "BUNDLE-RBS-SPIRAL",
                title = "Peti Ingatan Rumah Pohon",
                category = "Ulasan & Penemuan 2 Minggu • Usia 4–6",
                description = "Cerita ulasan bertahap yang membantu anak mengingat dan menerapkan kata serta angka yang dipelajari dua minggu lalu.",
                sizeMb = 11.4f,
                promptCount = 32,
                isDownloaded = false,
                cachedPromptSavingsPercent = 39
            )
        )
        bundles.forEach { dao.insertOfflineBundle(it) }

        dao.insertConsentLog(
            ConsentAuditEntity(
                id = 1,
                parentUserId = "Orang Tua / Wali",
                timestampUtc = "2026-10-11 14:00",
                policyVersion = "Perisai Privasi Anak v1.0",
                explicitAffirmationFlag = true,
                noticeText = "Kami merekam audio suara sementara hanya untuk bercakap-cakap dengan anak Anda selama sesi aktif. Rekaman audio mentah dihapus otomatis dalam 24 jam dan tidak pernah digunakan untuk iklan atau pelatihan model kecerdasan buatan."
            )
        )

        val sampleTelemetry = listOf(
            TelemetryEventEntity(
                idempotencyKey = "session-log-001",
                childId = 1,
                taskId = "TASK-NUM-01",
                taskTitle = "Lima Apel di Hutan",
                category = "Berhitung & Angka",
                urrUnassisted = 1,
                stepDownCount = 0,
                tcrSeconds = 11,
                rbsMasteryScore = 88,
                snrDb = 26.4f,
                fallbackCardUsed = false,
                turnLatencyMs = 420,
                childWordsSpoken = 14,
                companionWordsSpoken = 9,
                timestampMillis = System.currentTimeMillis() - 86400_000L
            ),
            TelemetryEventEntity(
                idempotencyKey = "session-log-002",
                childId = 1,
                taskId = "TASK-LIT-01",
                taskTitle = "Tepuk Suku Kata: BO-LA",
                category = "Bunyi & Suku Kata",
                urrUnassisted = 1,
                stepDownCount = 1,
                tcrSeconds = 15,
                rbsMasteryScore = 84,
                snrDb = 22.0f,
                fallbackCardUsed = false,
                turnLatencyMs = 510,
                childWordsSpoken = 12,
                companionWordsSpoken = 10,
                timestampMillis = System.currentTimeMillis() - 43200_000L
            )
        )
        sampleTelemetry.forEach { dao.insertTelemetryEvent(it) }

        val now = System.currentTimeMillis()
        dao.insertAudioBufferLog(
            EphemeralAudioBufferEntity(
                chunkId = "Sesi Suara Aktif",
                childName = "Bima",
                turnSummary = "Audio suara sementara di memori • Terhapus otomatis dalam 24 jam",
                createdAtMillis = now - 120_000L,
                expiresAtMillis = now + (23 * 3600_000L),
                volatileRamOnly = true,
                biometricStripped = true,
                status = "Dijadwalkan untuk Terhapus Otomatis"
            )
        )
    }
}
