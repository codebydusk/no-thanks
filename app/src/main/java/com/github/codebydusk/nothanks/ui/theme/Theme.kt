package com.github.codebydusk.nothanks.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Golden Silence dark: charcoal background, gold text / borders ───────────
private val GoldenSilenceDark = darkColorScheme(
    primary = GoldenYellow,
    onPrimary = DarkCharcoal,
    primaryContainer = Color(0xFF3A3820),
    onPrimaryContainer = GoldenYellow,
    secondary = GoldenYellow,
    onSecondary = DarkCharcoal,
    secondaryContainer = Color(0xFF3A3820),
    onSecondaryContainer = GoldenYellow,
    tertiary = GoldenYellow,
    onTertiary = DarkCharcoal,
    background = DarkCharcoal,
    onBackground = GoldenYellow,
    surface = DarkCharcoalLight,
    onSurface = GoldenYellow,
    surfaceVariant = DarkCharcoalLight,
    onSurfaceVariant = GoldenMuted,
    surfaceContainerLow = DarkCharcoalLight,
    surfaceContainer = DarkCharcoalLight,
    surfaceContainerHigh = DarkCharcoalLight,
    outline = GoldenYellow,
    outlineVariant = Color(0xFF5A4A1A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

// ── Golden Silence light: gold background, charcoal text / borders ──────────
private val GoldenSilenceLight = lightColorScheme(
    primary = DarkCharcoal,
    onPrimary = GoldenYellow,
    primaryContainer = DarkCharcoal,
    onPrimaryContainer = GoldenYellow,
    secondary = DarkCharcoal,
    onSecondary = GoldenYellow,
    secondaryContainer = DarkCharcoal,
    onSecondaryContainer = GoldenYellow,
    tertiary = DarkCharcoal,
    onTertiary = GoldenYellow,
    background = GoldenYellow,
    onBackground = DarkCharcoal,
    surface = GoldenYellowLight,
    onSurface = DarkCharcoal,
    surfaceVariant = GoldenYellowLight,
    onSurfaceVariant = CharcoalMuted,
    surfaceContainerLow = GoldenYellowLight,
    surfaceContainer = GoldenYellowLight,
    surfaceContainerHigh = GoldenYellowLight,
    outline = DarkCharcoal,
    outlineVariant = Color(0xFF5A5830),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
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