package com.example

import com.example.data.CalistungDao
import com.example.data.CalistungDatabaseSeeder
import com.example.data.ChildProfileEntity
import com.example.data.ConsentAuditEntity
import com.example.data.EphemeralAudioBufferEntity
import com.example.data.OfflineBundleEntity
import com.example.data.ParentSurveyEntity
import com.example.data.TelemetryEventEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeCalistungDao : CalistungDao {
    val profiles = mutableListOf<ChildProfileEntity>()
    val bundles = mutableListOf<OfflineBundleEntity>()
    val consentLogs = mutableListOf<ConsentAuditEntity>()
    val telemetry = mutableListOf<TelemetryEventEntity>()
    val audioBuffers = mutableListOf<EphemeralAudioBufferEntity>()
    val surveys = mutableListOf<ParentSurveyEntity>()

    override fun getAllProfiles(): Flow<List<ChildProfileEntity>> = flowOf(profiles)
    override suspend fun insertProfile(profile: ChildProfileEntity): Long {
        profiles.add(profile)
        return profile.id.toLong()
    }
    override suspend fun updateProfile(profile: ChildProfileEntity) {
        val idx = profiles.indexOfFirst { it.id == profile.id }
        if (idx >= 0) profiles[idx] = profile else profiles.add(profile)
    }

    override fun getTelemetryForChild(childId: Int): Flow<List<TelemetryEventEntity>> =
        flowOf(telemetry.filter { it.childId == childId })
    override fun getAllTelemetry(): Flow<List<TelemetryEventEntity>> = flowOf(telemetry)
    override suspend fun insertTelemetryEvent(event: TelemetryEventEntity) {
        telemetry.add(event)
    }

    override fun getAllOfflineBundles(): Flow<List<OfflineBundleEntity>> = flowOf(bundles)
    override suspend fun insertOfflineBundle(bundle: OfflineBundleEntity) {
        bundles.add(bundle)
    }
    override suspend fun updateOfflineBundle(bundle: OfflineBundleEntity) {
        val idx = bundles.indexOfFirst { it.bundleId == bundle.bundleId }
        if (idx >= 0) bundles[idx] = bundle else bundles.add(bundle)
    }

    override fun getConsentAuditLogs(): Flow<List<ConsentAuditEntity>> = flowOf(consentLogs)
    override suspend fun insertConsentLog(log: ConsentAuditEntity) {
        consentLogs.add(log)
    }

    override fun getAudioBufferLogs(): Flow<List<EphemeralAudioBufferEntity>> = flowOf(audioBuffers)
    override suspend fun insertAudioBufferLog(log: EphemeralAudioBufferEntity) {
        audioBuffers.add(log)
    }
    override suspend fun purgeAllAudioBuffers() {
        audioBuffers.clear()
    }
    override suspend fun deleteExpiredAudioBuffers(currentMillis: Long) {
        audioBuffers.removeAll { it.expiresAtMillis <= currentMillis }
    }

    override fun getParentSurveys(): Flow<List<ParentSurveyEntity>> = flowOf(surveys)
    override suspend fun insertParentSurvey(survey: ParentSurveyEntity) {
        surveys.add(survey)
    }
}

class CalistungDatabaseSeederTest {

    @Test
    fun seedInitialDataIfNeeded_populatesAllRequiredCatalogsAndAuditLogs() = runBlocking {
        val fakeDao = FakeCalistungDao()
        CalistungDatabaseSeeder.seedInitialDataIfNeeded(fakeDao)

        assertEquals(3, fakeDao.profiles.size)
        assertEquals("Bima", fakeDao.profiles[0].name)
        assertEquals("Alya", fakeDao.profiles[1].name)
        assertEquals("Raka", fakeDao.profiles[2].name)

        assertEquals(3, fakeDao.bundles.size)
        assertTrue(fakeDao.bundles.any { it.bundleId == "BUNDLE-NUM-CORE" })
        assertTrue(fakeDao.bundles.any { it.bundleId == "BUNDLE-LIT-SYLLABLES" })

        assertEquals(1, fakeDao.consentLogs.size)
        assertTrue(fakeDao.consentLogs.first().explicitAffirmationFlag)

        assertEquals(2, fakeDao.telemetry.size)
        assertEquals(1, fakeDao.audioBuffers.size)
    }
}
