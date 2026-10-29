package com.example

import com.example.data.SocraticCurriculumBank
import com.example.network.GeminiCandidate
import com.example.network.GeminiContent
import com.example.network.GeminiGenerationConfig
import com.example.network.GeminiPart
import com.example.network.GeminiRequest
import com.example.network.GeminiResponse
import com.example.network.GeminiSocraticService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiSocraticServiceTest {

    private val sampleTask = SocraticCurriculumBank.tasks.first()

    @Test
    fun evaluateChildResponse_correctAnswerReturnsAffirmationAndZeroStepDown() = runBlocking {
        val result = GeminiSocraticService.evaluateChildResponse(
            task = sampleTask,
            childUtterance = "Ada lima apel!",
            currentStepDownLevel = 0,
            isOfflineMode = true
        )

        assertTrue(result.isCorrect)
        assertEquals(0, result.nextStepDownLevel)
        assertTrue(result.usedCachedPrompt)
        assertEquals(sampleTask.celebrationAffirmation, result.companionSpokenReply)
    }

    @Test
    fun evaluateChildResponse_incorrectAnswerTriggersStepDownLadderSequentially() = runBlocking {
        // Level 0 -> 1
        val step1 = GeminiSocraticService.evaluateChildResponse(
            task = sampleTask,
            childUtterance = "Dua",
            currentStepDownLevel = 0,
            isOfflineMode = true
        )
        assertFalse(step1.isCorrect)
        assertEquals(1, step1.nextStepDownLevel)
        assertEquals(sampleTask.stepDown1Question, step1.companionSpokenReply)

        // Level 1 -> 2
        val step2 = GeminiSocraticService.evaluateChildResponse(
            task = sampleTask,
            childUtterance = "Nol",
            currentStepDownLevel = 1,
            isOfflineMode = true
        )
        assertFalse(step2.isCorrect)
        assertEquals(2, step2.nextStepDownLevel)
        assertEquals(sampleTask.stepDown2Question, step2.companionSpokenReply)

        // Level 2 -> 3
        val step3 = GeminiSocraticService.evaluateChildResponse(
            task = sampleTask,
            childUtterance = "Sepuluh",
            currentStepDownLevel = 2,
            isOfflineMode = true
        )
        assertFalse(step3.isCorrect)
        assertEquals(3, step3.nextStepDownLevel)
        assertEquals(sampleTask.stepDown3Scaffold, step3.companionSpokenReply)

        // Level 3 capped at 3
        val step4 = GeminiSocraticService.evaluateChildResponse(
            task = sampleTask,
            childUtterance = "Masih salah",
            currentStepDownLevel = 3,
            isOfflineMode = true
        )
        assertEquals(3, step4.nextStepDownLevel)
    }

    @Test
    fun geminiDataContracts_instantiateCorrectly() {
        val part = GeminiPart(text = "Halo Kiko")
        val content = GeminiContent(role = "user", parts = listOf(part))
        val config = GeminiGenerationConfig(temperature = 0.5f, maxOutputTokens = 120)
        val request = GeminiRequest(contents = listOf(content), generationConfig = config)
        val response = GeminiResponse(candidates = listOf(GeminiCandidate(content = content)))

        assertNotNull(request)
        assertEquals("user", content.role)
        assertEquals("Halo Kiko", response.candidates?.first()?.content?.parts?.first()?.text)
    }
}
