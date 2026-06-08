package com.github.codebydusk.nothanks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import androidx.compose.ui.graphics.Color
import com.github.codebydusk.nothanks.data.ExcuseRepository
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

// ── Nothing OS ──────────────────
private val NothingDark = darkColorScheme(
    primary = Color(0xFFD71921),
    background = Color(0xFF1B1B1D),
    surface = Color(0xFF1B1B1D),
    surfaceContainerLow = Color(0xFF2D2D30),
    onPrimary = Color.White,
    onBackground = Color(0xFFFDFBFF),
    onSurface = Color(0xFFFDFBFF),
    outline = Color(0xFF5E5E62)
)

private val NothingLight = lightColorScheme(
    primary = Color(0xFFD71921),
    background = Color(0xFFFDFBFF),
    surface = Color(0xFFFDFBFF),
    surfaceContainerLow = Color(0xFFF0F0F0),
    onPrimary = Color.White,
    onBackground = Color(0xFF1B1B1D),
    onSurface = Color(0xFF1B1B1D),
    outline = Color(0xFF5E5E62)
)

// ── OLED ──────────────────
private val OledDark = darkColorScheme(
    primary = Color(0xFFCAFE48),
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF111111),
    onPrimary = Color(0xFF111111),
    onBackground = Color(0xFFF7F7F9),
    onSurface = Color(0xFFF7F7F9),
    outline = Color(0xFF555555)
)

@Composable
fun NoThanksTheme(
    theme: String = ExcuseRepository.THEME_SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = when (theme) {
        ExcuseRepository.THEME_GOLDEN -> if (darkTheme) GoldenSilenceDark else GoldenSilenceLight
        ExcuseRepository.THEME_NOTHING -> if (darkTheme) NothingDark else NothingLight
        ExcuseRepository.THEME_OLED -> OledDark // Always dark
        else -> {
            // System dynamic colors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) NothingDark else NothingLight
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}