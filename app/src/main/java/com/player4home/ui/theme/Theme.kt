package com.player4home.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = OnTeal,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealPrimary,
    secondary = AmberSecondary,
    onSecondary = OnAmber,
    secondaryContainer = AmberContainer,
    onSecondaryContainer = AmberSecondary,
    background = NavyBackground,
    onBackground = OnNavy,
    surface = NavySurface,
    onSurface = OnNavy,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = OnNavyVariant,
    error = ErrorColor,
    onError = OnTeal
)

@Composable
fun Player4HomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
