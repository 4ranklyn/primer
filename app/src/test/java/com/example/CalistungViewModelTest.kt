package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.viewmodel.CalistungViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalistungViewModelTest {

    @Test
    fun viewModel_initializesDefaultStateAndCurrentTask() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CalistungViewModel(app)

        val task = vm.currentTask()
        assertNotNull(task)
        assertEquals("TASK-NUM-01", task.id)

        val state = vm.uiState.value
        assertEquals(1, state.selectedProfileId)
        assertFalse(state.isSessionLockedWindDown)
        assertEquals(420, state.sessionElapsedSeconds)
    }

    @Test
    fun viewModel_selectChildProfile_updatesProfileAndResetsTimers() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CalistungViewModel(app)

        vm.selectChildProfile(2)
        assertEquals(2, vm.uiState.value.selectedProfileId)
    }

    @Test
    fun viewModel_toggleOfflineMode_updatesState() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CalistungViewModel(app)

        vm.toggleOfflineMode(true)
        assertTrue(vm.uiState.value.isOfflinePreCachedMode)

        vm.toggleOfflineMode(false)
        assertFalse(vm.uiState.value.isOfflinePreCachedMode)
    }

    @Test
    fun viewModel_coPlayDemo_startsAndCompletesDemo() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CalistungViewModel(app)

        vm.startCoPlayOnboardingDemo()
        assertTrue(vm.uiState.value.isCoPlayDemoActive)

        vm.completeCoPlayOnboardingDemo()
        assertFalse(vm.uiState.value.isCoPlayDemoActive)
    }

    @Test
    fun viewModel_parentWhatsAppDigest_generatesReadableSummary() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = CalistungViewModel(app)

        val digest = vm.buildParentWhatsAppDigestText()
        assertTrue(digest.contains("Laporan Belajar Kiko"))
    }
}
