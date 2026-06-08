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

private fun buildDuoToneColorScheme(foreground: Color, background: Color, isDark: Boolean) = if (isDark) darkColorScheme(
    primary = foreground, onPrimary = background,
    primaryContainer = background, onPrimaryContainer = foreground,
    secondary = foreground, onSecondary = background,
    secondaryContainer = background, onSecondaryContainer = foreground,
    tertiary = foreground, onTertiary = background,
    background = background, onBackground = foreground,
    surface = background, onSurface = foreground,
    surfaceVariant = background, onSurfaceVariant = foreground,
    surfaceContainerLow = background, surfaceContainer = background, surfaceContainerHigh = background,
    outline = foreground, outlineVariant = foreground,
    error = foreground, onError = background
) else lightColorScheme(
    primary = foreground, onPrimary = background,
    primaryContainer = background, onPrimaryContainer = foreground,
    secondary = foreground, onSecondary = background,
    secondaryContainer = background, onSecondaryContainer = foreground,
    tertiary = foreground, onTertiary = background,
    background = background, onBackground = foreground,
    surface = background, onSurface = foreground,
    surfaceVariant = background, onSurfaceVariant = foreground,
    surfaceContainerLow = background, surfaceContainer = background, surfaceContainerHigh = background,
    outline = foreground, outlineVariant = foreground,
    error = foreground, onError = background
)

// ── Golden Silence ──────────────────
private val GoldenSilenceDark = buildDuoToneColorScheme(GoldenYellow, DarkCharcoal, true)
private val GoldenSilenceLight = buildDuoToneColorScheme(DarkCharcoal, GoldenYellow, false)

// ── Nothing OS ──────────────────
private val NothingDark = buildDuoToneColorScheme(Color(0xFFFDFBFF), Color(0xFF1B1B1D), true)
private val NothingLight = buildDuoToneColorScheme(Color(0xFF1B1B1D), Color(0xFFFDFBFF), false)

// ── OLED ──────────────────
private val OledDark = buildDuoToneColorScheme(Color(0xFFCAFE48), Color(0xFF000000), true)

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