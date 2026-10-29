package com.example.network

import com.example.BuildConfig
import com.example.data.SocraticTask
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = "user",
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float = 0.4f,
    val maxOutputTokens: Int = 140
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = GeminiGenerationConfig()
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

interface GeminiRestService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateSocraticTurn(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class SocraticTurnEvaluation(
    val isCorrect: Boolean,
    val companionSpokenReply: String,
    val nextStepDownLevel: Int,
    val latencyMs: Int,
    val usedCachedPrompt: Boolean,
    val estimatedCostIdr: Int
)

object GeminiSocraticService {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    private val api: GeminiRestService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiRestService::class.java)
    }

    suspend fun evaluateChildResponse(
        task: SocraticTask,
        childUtterance: String,
        currentStepDownLevel: Int,
        isOfflineMode: Boolean,
        idempotencyKey: String = ""
    ): SocraticTurnEvaluation = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val normalized = childUtterance.trim().lowercase()

        val matchesExpected = task.expectedKeywords.any { keyword ->
            normalized.contains(keyword.lowercase())
        }

        // Option C from PRD Section 9: Hybrid Chunk Streaming + Pre-Cached Prompts
        // When matching standard curriculum nodes or when offline, use pre-cached prompts first
        // to keep P95 latency < 1000ms [EV-31] and COGS <= IDR 25,000/mo [EV-05].
        if (matchesExpected) {
            val elapsed = (System.currentTimeMillis() - startTime).toInt()
            return@withContext SocraticTurnEvaluation(
                isCorrect = true,
                companionSpokenReply = task.celebrationAffirmation,
                nextStepDownLevel = 0,
                latencyMs = elapsed,
                usedCachedPrompt = true,
                estimatedCostIdr = 18 // Cached prompt audio saves 40%+ dynamic TTS synthesis [AS-03]
            )
        }

        // Incorrect answer -> Trigger Socratic Step-Down Ladder [FR-02, EV-34]
        // Maximum 3 step-downs per task. Never say "That is wrong" or give the answer directly.
        val nextLevel = (currentStepDownLevel + 1).coerceAtMost(3)
        val deterministicScaffold = when (nextLevel) {
            1 -> task.stepDown1Question
            2 -> task.stepDown2Question
            else -> task.stepDown3Scaffold
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val canCallCloud = !isOfflineMode &&
            apiKey.isNotBlank() &&
            apiKey != "MY_GEMINI_API_KEY" &&
            childUtterance.split(" ").size >= 3

        if (canCallCloud) {
            try {
                val sysInstruction = GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(
                            text = "Kamu adalah Kiko, burung hantu sahabat belajar Calistung (membaca, menulis, berhitung) anak usia 3-6 tahun di Indonesia. " +
                                "ATURAN KETAT: " +
                                "1. JANGAN PERNAH berkata 'salah', 'kurang tepat', 'bukan', atau 'no'. " +
                                "2. JANGAN PERNAH langsung memberikan jawaban akhir pada tingkat petunjuk 1 atau 2. " +
                                "3. Ajukan pertanyaan pemantik lembut 1-2 kalimat dalam Bahasa Indonesia yang ramah, hangat, dan mendorong anak menemukan jawabannya sendiri. " +
                                "4. Dilarang roleplay fantasi berlebihan atau keluar dari topik belajar [EV-03]. " +
                                "Pertanyaan tugas: ${task.openingQuestion}. Kata kunci jawaban: ${task.expectedKeywords.first()}. " +
                                "Referensi petunjuk bertahap: $deterministicScaffold"
                        )
                    )
                )
                val userTurn = GeminiContent(
                    role = "user",
                    parts = listOf(
                        GeminiPart(text = "Anak berkata: \"$childUtterance\". Tanggapi dengan hangat dan ceria sebagai Kiko dalam Bahasa Indonesia (maksimal 25 kata):")
                    )
                )
                val response = api.generateSocraticTurn(
                    idempotencyKey = idempotencyKey,
                    apiKey = apiKey,
                    request = GeminiRequest(
                        contents = listOf(userTurn),
                        systemInstruction = sysInstruction
                    )
                )
                val dynamicReply = response.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?.trim()

                val elapsed = (System.currentTimeMillis() - startTime).toInt()
                if (!dynamicReply.isNullOrBlank()) {
                    return@withContext SocraticTurnEvaluation(
                        isCorrect = false,
                        companionSpokenReply = dynamicReply,
                        nextStepDownLevel = nextLevel,
                        latencyMs = elapsed,
                        usedCachedPrompt = false,
                        estimatedCostIdr = 65
                    )
                }
            } catch (_: Exception) {
                // Graceful fallback to pre-cached Socratic step-down ladder on network or quota error [EV-33]
            }
        }

        val elapsed = (System.currentTimeMillis() - startTime).toInt()
        return@withContext SocraticTurnEvaluation(
            isCorrect = false,
            companionSpokenReply = deterministicScaffold,
            nextStepDownLevel = nextLevel,
            latencyMs = elapsed,
            usedCachedPrompt = true,
            estimatedCostIdr = 22
        )
    }
}
