package com.codex.animestream.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFFF4D6D),
    secondary = Color(0xFF66D9E8),
    tertiary = Color(0xFFFFD166),
    background = Color(0xFF090A0F),
    surface = Color(0xFF11131B),
    surfaceVariant = Color(0xFF1A1D29),
    onPrimary = Color.White,
    onBackground = Color(0xFFF5F6FA),
    onSurface = Color(0xFFF5F6FA),
)

@Composable
fun AnimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = Typography(),
        content = content,
    )
}
