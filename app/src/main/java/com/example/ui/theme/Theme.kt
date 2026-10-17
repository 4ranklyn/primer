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

private val LightColorScheme = lightColorScheme(
    primary = CalmSageOlive,
    onPrimary = Color.White,
    primaryContainer = SageContainerLight,
    onPrimaryContainer = CalmSageOlive,
    secondary = SocraticCoral,
    onSecondary = Color.White,
    secondaryContainer = CardOption1Bg,
    onSecondaryContainer = SocraticCoral,
    tertiary = AmberWarmSun,
    onTertiary = TextEspresso,
    tertiaryContainer = AmberWarmContainer,
    onTertiaryContainer = TextEspresso,
    background = SoftSandCream,
    onBackground = TextEspresso,
    surface = CardWarmParchment,
    onSurface = TextEspresso,
    surfaceVariant = Color(0xFFEFE6D7),
    onSurfaceVariant = TextMutedClay,
    outline = CardBorderSubtle,
    outlineVariant = Color(0xFFDED1BE),
    error = SocraticCoral
)

private val DarkColorScheme = darkColorScheme(
    primary = CalmMintGlow,
    onPrimary = TwilightDeepIndigo,
    primaryContainer = CalmSageOlive,
    onPrimaryContainer = Color.White,
    secondary = SocraticCoral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A2518),
    onSecondaryContainer = Color(0xFFFFD5C4),
    tertiary = MoonGlowYellow,
    onTertiary = TwilightDeepIndigo,
    tertiaryContainer = Color(0xFF4D3811),
    onTertiaryContainer = MoonGlowYellow,
    background = DarkBgSurface,
    onBackground = Color(0xFFF3ECE0),
    surface = DarkSurfaceCard,
    onSurface = Color(0xFFF3ECE0),
    surfaceVariant = Color(0xFF26352C),
    onSurfaceVariant = Color(0xFFADC0B4),
    outline = DarkBorder,
    outlineVariant = Color(0xFF384B3F),
    error = Color(0xFFFF8566)
)

val CalmScaffoldingShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
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
