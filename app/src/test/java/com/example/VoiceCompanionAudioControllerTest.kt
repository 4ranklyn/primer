package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.audio.VoiceCompanionAudioController
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceCompanionAudioControllerTest {

    @Test
    fun audioController_initializesAndHandlesLifecycleSafely() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        var speechResult = ""
        var speechError = ""
        var lastRms = 0f

        val controller = VoiceCompanionAudioController(
            context = app,
            onSpeechResult = { speechResult = it },
            onSpeechErrorOrSilence = { speechError = it },
            onRmsVolumeChanged = { lastRms = it }
        )

        assertNotNull(controller)
        assertFalse(controller.isSpeaking.value)
        assertFalse(controller.isListening.value)

        // Test speak & stopSpeaking
        controller.speak("Halo Kiko!")
        // In headless test environments, speak gracefully completes or resets
        controller.stopSpeaking()
        assertFalse(controller.isSpeaking.value)

        // Test voice capture in test environment (graceful degradation)
        controller.startRealVoiceCapture()
        controller.stopListening()
        assertFalse(controller.isListening.value)

        // Test release
        controller.release()
    }
}
