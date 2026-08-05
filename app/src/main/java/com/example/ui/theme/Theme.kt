package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DolphinCyanAccent,
    onPrimary = DolphinNavyBg,
    primaryContainer = DolphinBlueDark,
    onPrimaryContainer = Color.White,
    secondary = DolphinBluePrimary,
    onSecondary = Color.White,
    background = DolphinNavyBg,
    onBackground = TextPrimaryLight,
    surface = DolphinNavyCard,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = DolphinBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0EDFF),
    onPrimaryContainer = DolphinBlueDark,
    secondary = DolphinBlueDark,
    onSecondary = Color.White,
    background = DolphinLightBg,
    onBackground = TextPrimaryDark,
    surface = DolphinLightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun DolphinFiberTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
