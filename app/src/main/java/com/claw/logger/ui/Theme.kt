package com.claw.logger.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B5D5B),
    onPrimary = Color.White,
    secondary = Color(0xFFB85C38),
    onSecondary = Color.White,
    background = Color(0xFFF5F1E8),
    onBackground = Color(0xFF1F1A16),
    surface = Color(0xFFFFFBF5),
    onSurface = Color(0xFF1F1A16),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FD0CB),
    onPrimary = Color(0xFF003735),
    secondary = Color(0xFFFFB18D),
    onSecondary = Color(0xFF5C2200),
    background = Color(0xFF161312),
    onBackground = Color(0xFFECE1D8),
    surface = Color(0xFF211D1B),
    onSurface = Color(0xFFECE1D8),
)

@Composable
fun ClawLoggerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
