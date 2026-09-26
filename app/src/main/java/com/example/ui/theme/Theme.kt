package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBluePrimary,
    onPrimary = Color(0xFF001F2B),
    primaryContainer = ElectricBlueContainer,
    onPrimaryContainer = ElectricBlueOnContainer,
    secondary = SafetyOrange,
    onSecondary = Color(0xFF2C0E00),
    secondaryContainer = SafetyOrangeContainer,
    onSecondaryContainer = SafetyOrangeLight,
    tertiary = SuccessGreen,
    onTertiary = Color(0xFF002111),
    tertiaryContainer = SuccessGreenContainer,
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedContainer,
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun ParkMarkTheme(
    content: @Composable () -> Unit
) {
    // ParkMark uses high-contrast dark mode by default for outdoor/glare visibility
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
