package com.github.codebydusk.nothanks.widget

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.layout.*
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.github.codebydusk.nothanks.R
import com.github.codebydusk.nothanks.data.ExcuseRepository
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.datastore.preferences.core.Preferences

class NoThanksWidget : GlanceAppWidget() {

    companion object {
        private val SIZE_4x1 = DpSize(180.dp, 40.dp)
        private val SIZE_4x2 = DpSize(180.dp, 100.dp)
    }

    override val sizeMode = SizeMode.Responsive(setOf(SIZE_4x1, SIZE_4x2))

    /**
     * Holds resolved colors for a given theme + dark-mode combination.
     * background = widget background
     * foreground = text colour
     * navColor   = back/previous button colour (may differ from text, e.g. Nothing OS grey)
     * accent     = refresh button colour
     */
    private data class ThemeColors(
        val background: ComposeColor,
        val foreground: ComposeColor,
        val navColor: ComposeColor,
        val accent: ComposeColor
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = ExcuseRepository(context)

        // Seed widget state on first load
        val prefs = getAppWidgetState<Preferences>(context, id)
        if (prefs[CURRENT_TEXT_KEY] == null) {
            val initialText = repository.getCurrentExcuse() ?: repository.getNextExcuse()
            updateAppWidgetState(context, id) { mutablePrefs ->
                mutablePrefs[CURRENT_TEXT_KEY] = initialText
            }
        }

        provideContent {
            val state = currentState<Preferences>()
            val currentText = state[CURRENT_TEXT_KEY] ?: "Tap ↻ to fetch"
            val isCopied  = state[IS_COPIED_KEY]  ?: false
            val isLoading = state[IS_LOADING_KEY] ?: false
            val copiedMsg = state[COPIED_MSG_KEY]  ?: "Clipboard hijacked. You're welcome."

            val theme by repository.themeFlow.collectAsState(initial = ExcuseRepository.THEME_NOTHING)
            val cornerStyle by repository.cornerStyleFlow.collectAsState(initial = ExcuseRepository.CORNER_ROUND)
            val darkModeSetting by repository.darkModeFlow.collectAsState(initial = ExcuseRepository.DARK_MODE_SYSTEM)
            val copyMechanism by repository.copyMechanismFlow.collectAsState(initial = ExcuseRepository.COPY_TAP)
            val showPrevButton by repository.showPrevButtonFlow.collectAsState(initial = true)
            val textSize by repository.textSizeFlow.collectAsState(initial = ExcuseRepository.TEXT_SIZE_NORMAL)
            val fontStyle by repository.fontStyleFlow.collectAsState(initial = ExcuseRepository.FONT_STYLE_MONOSPACE)
            val includeCopyPrefix by repository.copyPrefixFlow.collectAsState(initial = false)

            val isDark = resolveDarkMode(darkModeSetting, LocalContext.current)
            val size = LocalSize.current

            WidgetContent(
                // Prepend "No, thanks!" only to real API text — not loading quips or copy confirmations
                text = when {
                    isCopied  -> copiedMsg
                    isLoading -> currentText
                    else      -> if (includeCopyPrefix) "No, thanks! $currentText" else currentText
                },
                theme = theme,
                cornerStyle = cornerStyle,
                isDark = isDark,
                copyMechanism = copyMechanism,
                isExpanded = size.height >= SIZE_4x2.height,
                showPrevButton = showPrevButton,
                textSize = textSize,
                fontStyle = fontStyle,
                context = LocalContext.current
            )
        }
    }

    @Composable
    private fun WidgetContent(
        text: String,
        theme: String,
        cornerStyle: String,
        isDark: Boolean,
        copyMechanism: String,
        isExpanded: Boolean,
        showPrevButton: Boolean,
        textSize: String,
        fontStyle: String,
        context: Context
    ) {
        val themeColors = resolveThemeColors(theme, isDark, context)
        val backgroundColor = themeColors.background
        val foregroundColor = themeColors.foreground  // text
        val navColor = themeColors.navColor           // back/prev button
        val accentColor = themeColors.accent           // refresh button

        val padding = 10.dp
        // Extra horizontal breathing room so text never butts against the edges
        val hPadding = when {
            cornerStyle == ExcuseRepository.CORNER_ROUND -> padding + 16.dp
            else -> padding + 8.dp
        }

        val fontSize = when (theme) {
            ExcuseRepository.THEME_NOTHING -> 16.sp
            ExcuseRepository.THEME_GOLDEN  -> 17.sp
            else -> 16.sp
        }

        // Apply text size scale multiplier
        val scaledFontSize = when (textSize) {
            ExcuseRepository.TEXT_SIZE_SMALL -> fontSize * 0.85f
            ExcuseRepository.TEXT_SIZE_LARGE -> fontSize * 1.25f
            else -> fontSize  // TEXT_SIZE_NORMAL
        }

        val fontWeight = when (theme) {
            ExcuseRepository.THEME_NOTHING -> FontWeight.Bold
            ExcuseRepository.THEME_GOLDEN  -> FontWeight.Bold
            else -> FontWeight.Normal
        }

        // Use the selected font style
        val fontFamily = when (fontStyle) {
            ExcuseRepository.FONT_STYLE_MONOSPACE -> FontFamily.Monospace
            else -> FontFamily.SansSerif
        }

        // Pill (CORNER_ROUND) = 50dp → truly pill-shaped at typical widget heights
        // Rounded (CORNER_SQUARE) = 8dp → gentle rounding
        // Sharp (CORNER_SHARP) = 0dp → true square with no rounding
        val cornerRadius = when (cornerStyle) {
            ExcuseRepository.CORNER_ROUND  -> 50.dp
            ExcuseRepository.CORNER_SQUARE -> 8.dp
            else /* CORNER_SHARP */         -> 0.dp
        }

        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(cornerRadius)

        Box(modifier = baseModifier) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(horizontal = hPadding, vertical = padding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button (conditionally shown) — nav colour
                    if (showPrevButton) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_chevron_left),
                            contentDescription = "Previous",
                            colorFilter = ColorFilter.tint(ColorProvider(navColor)),
                            modifier = GlanceModifier
                                .size(24.dp)
                                .clickable(actionRunCallback<HistoryAction>(
                                    actionParametersOf(HistoryAction.DIRECTION_KEY to HistoryAction.PREVIOUS)
                                ))
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                    }

                    // Text area
                    val textModifier = GlanceModifier.defaultWeight()
                    val finalModifier = if (copyMechanism == ExcuseRepository.COPY_TAP) {
                        textModifier.clickable(actionRunCallback<CopyAction>())
                    } else {
                        textModifier
                    }

                    if (isExpanded) {
                        // 4x2 mode: unlimited lines — LazyColumn handles scrolling
                        Box(modifier = finalModifier, contentAlignment = Alignment.CenterStart) {
                            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = text,
                                        style = TextStyle(
                                            color = ColorProvider(foregroundColor),
                                            fontSize = scaledFontSize,
                                            fontWeight = fontWeight,
                                            fontFamily = fontFamily
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // 4x1 mode: allow up to 3 lines before truncating
                        Box(modifier = finalModifier, contentAlignment = Alignment.CenterStart) {
                            Text(
                                text = text,
                                style = TextStyle(
                                    color = ColorProvider(foregroundColor),
                                    fontSize = scaledFontSize,
                                    fontWeight = fontWeight,
                                    fontFamily = fontFamily
                                ),
                                maxLines = 3
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(4.dp))

                    // Copy button — nav colour (matches back button)
                    if (copyMechanism == ExcuseRepository.COPY_BUTTON) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_content_copy),
                            contentDescription = "Copy",
                            colorFilter = ColorFilter.tint(ColorProvider(navColor)),
                            modifier = GlanceModifier
                                .size(20.dp)
                                .clickable(actionRunCallback<CopyAction>())
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                    }

                    // Refresh button — accent colour (brand/theme highlight)
                    Image(
                        provider = ImageProvider(R.drawable.ic_refresh),
                        contentDescription = "Refresh",
                        colorFilter = ColorFilter.tint(ColorProvider(accentColor)),
                        modifier = GlanceModifier
                            .size(24.dp)
                            .clickable(actionRunCallback<RefreshAction>())
                    )
                }
            }
        }
    }

    /**
     * Returns ThemeColors(background, foreground, navColor, accent) for the given theme + dark-mode.
     *
     * Theme philosophy:
     *  Nothing OS     → #1b1b1d/#fdfbff body, grey #5e5e62 nav, Nothing red #d71921 refresh
     *  Golden Silence → #1E1E24/#FFCB47 duo-tone, everything gold/charcoal
     *  System         → device dynamic colors on Android 12+, neutral M3 fallback on older
     */
    private fun resolveThemeColors(theme: String, isDark: Boolean, context: Context): ThemeColors = when (theme) {
        ExcuseRepository.THEME_NOTHING -> if (isDark) ThemeColors(
            background = ComposeColor(0xFF1B1B1D),
            foreground = ComposeColor(0xFFFDFBFF),
            navColor   = ComposeColor(0xFF5E5E62),
            accent     = ComposeColor(0xFFD71921)
        ) else ThemeColors(
            background = ComposeColor(0xFFFDFBFF),
            foreground = ComposeColor(0xFF1B1B1D),
            navColor   = ComposeColor(0xFF5E5E62),
            accent     = ComposeColor(0xFFD71921)
        )

        ExcuseRepository.THEME_GOLDEN -> if (isDark) ThemeColors(
            background = ComposeColor(0xFF1E1E24),
            foreground = ComposeColor(0xFFFFCB47),
            navColor   = ComposeColor(0xFFFFCB47),
            accent     = ComposeColor(0xFFFFCB47)
        ) else ThemeColors(
            background = ComposeColor(0xFFFFCB47),
            foreground = ComposeColor(0xFF1E1E24),
            navColor   = ComposeColor(0xFF1E1E24),
            accent     = ComposeColor(0xFF1E1E24)
        )

        // System theme: use device's Material You dynamic colors on Android 12+
        else -> resolveSystemThemeColors(isDark, context)
    }

    /**
     * Reads the device's wallpaper-derived dynamic colors (Android 12+).
     * Falls back to neutral Material3 defaults on older Android versions.
     */
    private fun resolveSystemThemeColors(isDark: Boolean, context: Context): ThemeColors {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return try {
                if (isDark) {
                    ThemeColors(
                        background = ComposeColor(context.getColor(android.R.color.system_neutral1_900)),
                        foreground = ComposeColor(context.getColor(android.R.color.system_neutral1_100)),
                        navColor   = ComposeColor(context.getColor(android.R.color.system_neutral1_500)),
                        accent     = ComposeColor(context.getColor(android.R.color.system_accent1_200))
                    )
                } else {
                    ThemeColors(
                        background = ComposeColor(context.getColor(android.R.color.system_neutral1_50)),
                        foreground = ComposeColor(context.getColor(android.R.color.system_neutral1_900)),
                        navColor   = ComposeColor(context.getColor(android.R.color.system_neutral1_500)),
                        accent     = ComposeColor(context.getColor(android.R.color.system_accent1_500))
                    )
                }
            } catch (e: Exception) {
                // Fallback if system color resources are unavailable
                fallbackSystemColors(isDark)
            }
        }
        return fallbackSystemColors(isDark)
    }

    private fun fallbackSystemColors(isDark: Boolean): ThemeColors = if (isDark) ThemeColors(
        background = ComposeColor(0xFF1C1B1F),
        foreground = ComposeColor(0xFFE6E1E5),
        navColor   = ComposeColor(0xFF938F99),
        accent     = ComposeColor(0xFFCAC4D0)
    ) else ThemeColors(
        background = ComposeColor(0xFFFFFBFE),
        foreground = ComposeColor(0xFF1C1B1F),
        navColor   = ComposeColor(0xFF79747E),
        accent     = ComposeColor(0xFF625B71)
    )

    private fun resolveDarkMode(setting: String, context: Context): Boolean = when (setting) {
        ExcuseRepository.DARK_MODE_DARK  -> true
        ExcuseRepository.DARK_MODE_LIGHT -> false
        else -> {
            val uiMode = context.resources.configuration.uiMode
            (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
    }
}
