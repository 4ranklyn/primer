package com.example

import com.example.data.SocraticCurriculumBank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SocraticCurriculumTest {

    @Test
    fun socraticCurriculumBank_containsValidPedagogicalLadderTasks() {
        val tasks = SocraticCurriculumBank.tasks
        assertTrue("Curriculum must contain curated tasks", tasks.isNotEmpty())

        tasks.forEach { task ->
            assertNotNull(task.id)
            assertTrue(task.title.isNotBlank())
            assertTrue(task.openingQuestion.isNotBlank())
            assertTrue("Expected keywords must not be empty", task.expectedKeywords.isNotEmpty())
            assertTrue("Step-Down 1 question must be present", task.stepDown1Question.isNotBlank())
            assertTrue("Step-Down 2 question must be present", task.stepDown2Question.isNotBlank())
            assertTrue("Step-Down 3 scaffold must be present", task.stepDown3Scaffold.isNotBlank())
            assertTrue("Each task must have at least 2 visual card options", task.fallbackOptions.size >= 2)
            assertTrue("Exactly one fallback card must be correct", task.fallbackOptions.count { it.isCorrect } == 1)
        }
    }

    @Test
    fun socraticCurriculumBank_firstTask_matchesFiveApplesInForest() {
        val firstTask = SocraticCurriculumBank.tasks[0]
        assertEquals("TASK-NUM-01", firstTask.id)
        assertTrue(firstTask.title.contains("Lima Apel"))
        assertTrue(firstTask.expectedKeywords.contains("lima"))
        assertTrue(firstTask.fallbackOptions.any { it.spokenLabel.contains("Apel") && it.isCorrect })
    }
}
