package com.example

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Test
    fun appDestination_definesAllRequiredRoutes() {
        assertEquals("suara", AppDestination.COMPANION.routeId)
        assertEquals("pilihan", AppDestination.CURRICULUM.routeId)
        assertEquals("istirahat", AppDestination.PRIVACY_OPS.routeId)
        assertEquals("laporan", AppDestination.PARENT_HUB.routeId)
        assertEquals(4, AppDestination.values().size)
    }

    @Test
    fun mainActivity_launchesAndInitializesSuccessfully() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
            }
        }
    }
}
