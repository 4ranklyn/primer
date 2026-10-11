package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import android.content.Context
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "child_profiles")
data class ChildProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val age: Int,
    val avatarEmoji: String,
    val subscriptionTier: String = "Mass Tier (IDR 99,000/mo)", // or "Promo Cohort (IDR 70,000/mo)" or "Advanced Tier (IDR 199,000/mo)"
    val dailyMinutesUsedToday: Int = 0,
    val lastSessionDate: String = "2026-10-11",
    val urrScorePercent: Int = 68,          // Unassisted Response Rate [EV-36] Target >= 70%
    val medianTcrSeconds: Int = 16,         // Median Time-to-Correct-Resolution [EV-36] Target <= 15s
    val rbsRetentionPercent: Int = 82,      // 14-Day Retention Benchmark Spiral [EV-36] Target >= 80%
    val monthlyInferenceIdr: Int = 14200,   // Cloud inference spend [EV-05] Cap <= IDR 25,000
    val childToCompanionWordRatio: Float = 1.45f,
    val unlockedStoryClues: Int = 4,
    val scheduledDailyTime: String = "16:30"
)

@Entity(tableName = "telemetry_events")
data class TelemetryEventEntity(
    @PrimaryKey val idempotencyKey: String, // UUIDv4 per PRD Section 10.2
    val childId: Int,
    val taskId: String,
    val taskTitle: String,
    val category: String, // "Literacy" or "Numeracy"
    val urrUnassisted: Int, // 1 if solved on first try without step-down, 0 otherwise
    val stepDownCount: Int, // 0..3
    val tcrSeconds: Int,
    val rbsMasteryScore: Int,
    val snrDb: Float,
    val fallbackCardUsed: Boolean,
    val turnLatencyMs: Int,
    val childWordsSpoken: Int,
    val companionWordsSpoken: Int,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_bundles")
data class OfflineBundleEntity(
    @PrimaryKey val bundleId: String,
    val title: String,
    val category: String, // "Phonemic Awareness & Syllables", "Numerical Magnitude & Counting", "RBS 14-Day Spiral"
    val description: String,
    val sizeMb: Float, // Must stay < 50MB total [FR-05]
    val promptCount: Int,
    val isDownloaded: Boolean,
    val cachedPromptSavingsPercent: Int = 42
)

@Entity(tableName = "consent_audit_logs")
data class ConsentAuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val parentUserId: String,
    val timestampUtc: String,
    val policyVersion: String,
    val explicitAffirmationFlag: Boolean,
    val noticeText: String
)

@Entity(tableName = "audio_buffer_logs")
data class EphemeralAudioBufferEntity(
    @PrimaryKey val chunkId: String,
    val childName: String,
    val turnSummary: String,
    val createdAtMillis: Long,
    val expiresAtMillis: Long, // Strictly <= 24h [FR-06, EV-32]
    val volatileRamOnly: Boolean = true,
    val biometricStripped: Boolean = true,
    val status: String = "VOLATILE_ACTIVE" // or "PURGED_ZERO_RETENTION"
)

@Entity(tableName = "parent_surveys")
data class ParentSurveyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val childId: Int,
    val childName: String,
    val speaksFullSentencesScore: Int, // 1..5
    val returnsPhoneCalmlyScore: Int,  // 1..5 [EV-07]
    val readsSyllablesIndependentlyScore: Int, // 1..5
    val notes: String,
    val submittedAtDate: String
)

@Dao
interface CalistungDao {
    @Query("SELECT * FROM child_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<ChildProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ChildProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ChildProfileEntity)

    @Query("SELECT * FROM telemetry_events WHERE childId = :childId ORDER BY timestampMillis DESC")
    fun getTelemetryForChild(childId: Int): Flow<List<TelemetryEventEntity>>

    @Query("SELECT * FROM telemetry_events ORDER BY timestampMillis DESC")
    fun getAllTelemetry(): Flow<List<TelemetryEventEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Idempotent guarantee on UUIDv4 primary key
    suspend fun insertTelemetryEvent(event: TelemetryEventEntity)

    @Query("SELECT * FROM offline_bundles ORDER BY bundleId ASC")
    fun getAllOfflineBundles(): Flow<List<OfflineBundleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineBundle(bundle: OfflineBundleEntity)

    @Update
    suspend fun updateOfflineBundle(bundle: OfflineBundleEntity)

    @Query("SELECT * FROM consent_audit_logs ORDER BY id DESC")
    fun getConsentAuditLogs(): Flow<List<ConsentAuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsentLog(log: ConsentAuditEntity)

    @Query("SELECT * FROM audio_buffer_logs ORDER BY createdAtMillis DESC")
    fun getAudioBufferLogs(): Flow<List<EphemeralAudioBufferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioBufferLog(log: EphemeralAudioBufferEntity)

    @Query("DELETE FROM audio_buffer_logs")
    suspend fun purgeAllAudioBuffers()

    @Query("SELECT * FROM parent_surveys ORDER BY id DESC")
    fun getParentSurveys(): Flow<List<ParentSurveyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParentSurvey(survey: ParentSurveyEntity)
}

@Database(
    entities = [
        ChildProfileEntity::class,
        TelemetryEventEntity::class,
        OfflineBundleEntity::class,
        ConsentAuditEntity::class,
        EphemeralAudioBufferEntity::class,
        ParentSurveyEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CalistungDatabase : RoomDatabase() {
    abstract fun dao(): CalistungDao

    companion object {
        @Volatile
        private var INSTANCE: CalistungDatabase? = null

        fun getDatabase(context: Context): CalistungDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalistungDatabase::class.java,
                    "kiko_calistung_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
