package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.CalistungDao
import com.example.data.CalistungDatabase
import com.example.data.ChildProfileEntity
import com.example.data.ConsentAuditEntity
import com.example.data.EphemeralAudioBufferEntity
import com.example.data.OfflineBundleEntity
import com.example.data.ParentSurveyEntity
import com.example.data.TelemetryEventEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalistungDatabaseTest {

    private lateinit var db: CalistungDatabase
    private lateinit var dao: CalistungDao

    @Before
    fun createDb() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(app, CalistungDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun database_singletonInstanceCreatedSuccessfully() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val instance = CalistungDatabase.getDatabase(app)
        assertNotNull(instance)
    }

    @Test
    fun dao_profileAndTelemetry_crudOperationsWork() = runBlocking {
        val profile = ChildProfileEntity(
            name = "TestChild",
            age = 5,
            avatarEmoji = "🦉"
        )
        val profileId = dao.insertProfile(profile)
        val profiles = dao.getAllProfiles().first()
        assertTrue(profiles.any { it.name == "TestChild" })

        val telemetry = TelemetryEventEntity(
            idempotencyKey = "test-uuid-001",
            childId = profileId.toInt(),
            taskId = "TASK-01",
            taskTitle = "Belajar Berhitung",
            category = "Numeracy",
            urrUnassisted = 1,
            stepDownCount = 0,
            tcrSeconds = 12,
            rbsMasteryScore = 90,
            snrDb = 22.5f,
            fallbackCardUsed = false,
            turnLatencyMs = 450,
            childWordsSpoken = 4,
            companionWordsSpoken = 10
        )
        dao.insertTelemetryEvent(telemetry)
        val fetched = dao.getTelemetryForChild(profileId.toInt()).first()
        assertEquals(1, fetched.size)
        assertEquals("test-uuid-001", fetched.first().idempotencyKey)
    }

    @Test
    fun dao_offlineBundleAndConsent_persistsAndQueries() = runBlocking {
        val bundle = OfflineBundleEntity(
            bundleId = "BUNDLE-TEST",
            title = "Test Bundle",
            category = "Literacy",
            description = "Offline literacy packet",
            sizeMb = 12.5f,
            promptCount = 20,
            isDownloaded = true
        )
        dao.insertOfflineBundle(bundle)
        val bundles = dao.getAllOfflineBundles().first()
        assertTrue(bundles.any { it.bundleId == "BUNDLE-TEST" })

        val consent = ConsentAuditEntity(
            parentUserId = "parent_001",
            timestampUtc = "2026-10-29T10:00:00Z",
            policyVersion = "v1.0",
            explicitAffirmationFlag = true,
            noticeText = "COPPA notice accepted"
        )
        dao.insertConsentLog(consent)
        val logs = dao.getConsentAuditLogs().first()
        assertTrue(logs.isNotEmpty())
    }

    @Test
    fun dao_ephemeralAudioBuffer_expirationAndPurge() = runBlocking {
        val now = System.currentTimeMillis()
        val expired = EphemeralAudioBufferEntity(
            chunkId = "chunk-01",
            childName = "TestChild",
            turnSummary = "Halo Kiko",
            createdAtMillis = now - 100_000,
            expiresAtMillis = now - 10_000
        )
        val active = EphemeralAudioBufferEntity(
            chunkId = "chunk-02",
            childName = "TestChild",
            turnSummary = "Berapa apel?",
            createdAtMillis = now,
            expiresAtMillis = now + 100_000
        )
        dao.insertAudioBufferLog(expired)
        dao.insertAudioBufferLog(active)

        dao.deleteExpiredAudioBuffers(now)
        val remaining = dao.getAudioBufferLogs().first()
        assertEquals(1, remaining.size)
        assertEquals("chunk-02", remaining.first().chunkId)

        dao.purgeAllAudioBuffers()
        val empty = dao.getAudioBufferLogs().first()
        assertTrue(empty.isEmpty())
    }

    @Test
    fun dao_parentSurvey_insertAndQuery() = runBlocking {
        val survey = ParentSurveyEntity(
            childId = 1,
            childName = "Bima",
            speaksFullSentencesScore = 4,
            returnsPhoneCalmlyScore = 5,
            readsSyllablesIndependentlyScore = 3,
            notes = "Kemajuan bagus",
            submittedAtDate = "2026-10-29"
        )
        dao.insertParentSurvey(survey)
        val surveys = dao.getParentSurveys().first()
        assertTrue(surveys.any { it.childName == "Bima" })
    }
}
