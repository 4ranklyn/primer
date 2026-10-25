package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Calm Scaffolding Light Scheme
private val LightColorScheme = lightColorScheme(
    primary = CalmPrimary,
    onPrimary = CalmOnPrimary,
    primaryContainer = CalmPrimaryContainer,
    onPrimaryContainer = CalmOnPrimaryContainer,
    inversePrimary = CalmInversePrimary,
    secondary = CalmSecondary,
    onSecondary = CalmOnSecondary,
    secondaryContainer = CalmSecondaryContainer,
    onSecondaryContainer = CalmOnSecondaryContainer,
    tertiary = CalmTertiary,
    onTertiary = CalmOnTertiary,
    tertiaryContainer = CalmTertiaryContainer,
    onTertiaryContainer = CalmOnTertiaryContainer,
    background = CalmBackground,
    onBackground = CalmOnBackground,
    surface = CalmSurface,
    onSurface = CalmOnSurface,
    surfaceVariant = CalmSurfaceVariant,
    onSurfaceVariant = CalmOnSurfaceVariant,
    surfaceDim = CalmSurfaceDim,
    surfaceBright = CalmSurfaceBright,
    surfaceContainerLowest = CalmSurfaceContainerLowest,
    surfaceContainerLow = CalmSurfaceContainerLow,
    surfaceContainer = CalmSurfaceContainer,
    surfaceContainerHigh = CalmSurfaceContainerHigh,
    surfaceContainerHighest = CalmSurfaceContainerHighest,
    inverseSurface = CalmInverseSurface,
    inverseOnSurface = CalmInverseOnSurface,
    outline = CalmOutline,
    outlineVariant = CalmOutlineVariant,
    surfaceTint = CalmSurfaceTint,
    error = CalmError,
    onError = CalmOnError,
    errorContainer = CalmErrorContainer,
    onErrorContainer = CalmOnErrorContainer
)

// Calm Scaffolding Dark Scheme
private val DarkColorScheme = darkColorScheme(
    primary = CalmInversePrimary,
    onPrimary = TwilightIndigo,
    primaryContainer = CalmPrimary,
    onPrimaryContainer = Color.White,
    inversePrimary = CalmPrimary,
    secondary = CalmSecondaryFixedDim,
    onSecondary = Color.White,
    secondaryContainer = CalmSecondary,
    onSecondaryContainer = CalmSecondaryFixed,
    tertiary = CalmTertiaryFixed,
    onTertiary = TwilightIndigo,
    tertiaryContainer = CalmTertiaryContainer,
    onTertiaryContainer = CalmTertiaryFixed,
    background = TwilightIndigo,
    onBackground = SurfaceCream,
    surface = Color(0xFF2C2F3E),
    onSurface = SurfaceCream,
    surfaceVariant = Color(0xFF383B4D),
    onSurfaceVariant = CalmOutlineVariant,
    surfaceDim = Color(0xFF1F212E),
    surfaceBright = Color(0xFF383B4D),
    surfaceContainer = Color(0xFF2C2F3E),
    inverseSurface = SurfaceCream,
    inverseOnSurface = CalmOnSurface,
    outline = CalmOutline,
    outlineVariant = Color(0xFF4C5065),
    surfaceTint = CalmInversePrimary,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// Calm Scaffolding Tactile Shapes: sm=8dp, md=16dp, lg=24dp, xl=32dp
val CalmScaffoldingShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CalmScaffoldingShapes,
        content = content
    )
}
