package com.github.codebydusk.nothanks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ── Golden Silence dark: charcoal bg, EVERYTHING else gold ──────────────────
private val GoldenSilenceDark = darkColorScheme(
    primary = GoldenYellow,
    onPrimary = DarkCharcoal,
    primaryContainer = DarkCharcoal,
    onPrimaryContainer = GoldenYellow,
    secondary = GoldenYellow,
    onSecondary = DarkCharcoal,
    secondaryContainer = DarkCharcoal,
    onSecondaryContainer = GoldenYellow,
    tertiary = GoldenYellow,
    onTertiary = DarkCharcoal,
    background = DarkCharcoal,
    onBackground = GoldenYellow,
    surface = DarkCharcoal,
    onSurface = GoldenYellow,
    surfaceVariant = DarkCharcoal,
    onSurfaceVariant = GoldenYellow,
    surfaceContainerLow = DarkCharcoal,
    surfaceContainer = DarkCharcoal,
    surfaceContainerHigh = DarkCharcoal,
    outline = GoldenYellow,
    outlineVariant = GoldenYellow,
    error = GoldenYellow,
    onError = DarkCharcoal,
)

// ── Golden Silence light: gold bg, EVERYTHING else charcoal ─────────────────
private val GoldenSilenceLight = lightColorScheme(
    primary = DarkCharcoal,
    onPrimary = GoldenYellow,
    primaryContainer = GoldenYellow,
    onPrimaryContainer = DarkCharcoal,
    secondary = DarkCharcoal,
    onSecondary = GoldenYellow,
    secondaryContainer = GoldenYellow,
    onSecondaryContainer = DarkCharcoal,
    tertiary = DarkCharcoal,
    onTertiary = GoldenYellow,
    background = GoldenYellow,
    onBackground = DarkCharcoal,
    surface = GoldenYellow,
    onSurface = DarkCharcoal,
    surfaceVariant = GoldenYellow,
    onSurfaceVariant = DarkCharcoal,
    surfaceContainerLow = GoldenYellow,
    surfaceContainer = GoldenYellow,
    surfaceContainerHigh = GoldenYellow,
    outline = DarkCharcoal,
    outlineVariant = DarkCharcoal,
    error = DarkCharcoal,
    onError = GoldenYellow,
)

@Composable
fun NoThanksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) GoldenSilenceDark else GoldenSilenceLight

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}