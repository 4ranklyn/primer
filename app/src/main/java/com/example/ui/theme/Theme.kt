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
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = SageContainer,
    onPrimaryContainer = OnSageContainer,
    secondary = TerracottaSecondary,
    onSecondary = Color.White,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,
    tertiary = HoneyGoldTertiary,
    onTertiary = Color.White,
    tertiaryContainer = HoneyGoldContainer,
    onTertiaryContainer = OnHoneyGoldContainer,
    background = StorybookCreamBg,
    onBackground = DeepEspressoText,
    surface = StorybookSurface,
    onSurface = DeepEspressoText,
    surfaceVariant = ParchmentCard,
    onSurfaceVariant = MutedWalnutText,
    outline = WarmBorder,
    error = SnrDangerCoral
)

private val DarkColorScheme = darkColorScheme(
    primary = MintPrimaryDark,
    onPrimary = OnSageContainer,
    primaryContainer = ForestGreenPrimary,
    onPrimaryContainer = SageContainer,
    secondary = WarmApricotDark,
    onSecondary = OnTerracottaContainer,
    secondaryContainer = Color(0xFF5C230E),
    onSecondaryContainer = TerracottaContainer,
    tertiary = SoftGoldDark,
    onTertiary = OnHoneyGoldContainer,
    tertiaryContainer = Color(0xFF573800),
    onTertiaryContainer = HoneyGoldContainer,
    background = TwilightBg,
    onBackground = Color(0xFFF3ECE4),
    surface = TwilightSurface,
    onSurface = Color(0xFFF3ECE4),
    surfaceVariant = TwilightCard,
    onSurfaceVariant = Color(0xFFC5B9AD),
    outline = Color(0xFF3F5249),
    error = Color(0xFFFF8A80)
)

val StorybookShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
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
        shapes = StorybookShapes,
        content = content
    )
}
