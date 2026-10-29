package com.example

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CalmBackground
import com.example.ui.theme.CalmPrimary
import com.example.ui.theme.CalmScaffoldingShapes
import com.example.ui.theme.CalmSecondary
import com.example.ui.theme.SurfaceCream
import com.example.ui.theme.Typography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ThemeTest {

    @Test
    fun themePalettes_defineCalmInclusiveColors() {
        assertNotNull(CalmPrimary)
        assertNotNull(CalmSecondary)
        assertNotNull(CalmBackground)
        assertNotNull(SurfaceCream)
    }

    @Test
    fun shapes_complyWithTactileChildFriendlyRadii() {
        assertNotNull(CalmScaffoldingShapes)
        assertEquals(RoundedCornerShape(8.dp), CalmScaffoldingShapes.small)
        assertEquals(RoundedCornerShape(16.dp), CalmScaffoldingShapes.medium)
        assertEquals(RoundedCornerShape(24.dp), CalmScaffoldingShapes.large)
        assertEquals(RoundedCornerShape(32.dp), CalmScaffoldingShapes.extraLarge)
    }

    @Test
    fun typography_definesLegibleChildFirstScales() {
        assertNotNull(Typography.titleLarge)
        assertNotNull(Typography.bodyLarge)
        assertNotNull(Typography.labelMedium)
    }
}
