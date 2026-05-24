package com.github.codebydusk.nothanks.widget

import android.content.Context
import android.content.res.Configuration
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
     * accent = the brand highlight colour used on the refresh button.
     */
    private data class ThemeColors(
        val background: ComposeColor,
        val foreground: ComposeColor,
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

            val theme by repository.themeFlow.collectAsState(initial = ExcuseRepository.THEME_NOTHANKS)
            val cornerStyle by repository.cornerStyleFlow.collectAsState(initial = ExcuseRepository.CORNER_ROUND)
            val darkModeSetting by repository.darkModeFlow.collectAsState(initial = ExcuseRepository.DARK_MODE_SYSTEM)
            val copyMechanism by repository.copyMechanismFlow.collectAsState(initial = ExcuseRepository.COPY_TAP)
            val showPrevButton by repository.showPrevButtonFlow.collectAsState(initial = true)

            val isDark = resolveDarkMode(darkModeSetting, LocalContext.current)
            val size = LocalSize.current

            WidgetContent(
                // Prepend "No, thanks!" only to real API text — not loading quips or copy confirmations
                text = when {
                    isCopied  -> copiedMsg
                    isLoading -> currentText
                    else      -> "No, thanks! $currentText"
                },
                theme = theme,
                cornerStyle = cornerStyle,
                isDark = isDark,
                copyMechanism = copyMechanism,
                isExpanded = size.height >= SIZE_4x2.height,
                showPrevButton = showPrevButton
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
        showPrevButton: Boolean
    ) {
        val themeColors = resolveThemeColors(theme, isDark)
        val backgroundColor = themeColors.background
        val foregroundColor = themeColors.foreground  // text + nav icons
        val accentColor = themeColors.accent           // refresh button

        val padding = when (theme) {
            ExcuseRepository.THEME_NOTHING -> 8.dp
            ExcuseRepository.THEME_SAMSUNG -> 12.dp
            else -> 10.dp
        }
        // Extra horizontal breathing room so text never butts against the edges
        val hPadding = padding + 8.dp

        val fontSize = when (theme) {
            ExcuseRepository.THEME_NOTHING   -> 16.sp
            ExcuseRepository.THEME_SAMSUNG   -> 17.sp
            ExcuseRepository.THEME_NOTHANKS  -> 17.sp
            else -> 18.sp
        }

        val fontWeight = when (theme) {
            ExcuseRepository.THEME_NOTHING  -> FontWeight.Bold
            ExcuseRepository.THEME_NOTHANKS -> FontWeight.Bold
            else -> FontWeight.Normal
        }

        // Per-theme system font families
        // Nothing OS  → Monospace (dot-matrix aesthetic)
        // Samsung      → sans-serif-medium (Samsung's rounded neutral)
        // OnePlus      → SansSerif (clean OxygenOS feel)
        // Material     → SansSerif (default clean)
        val fontFamily = when (theme) {
            ExcuseRepository.THEME_NOTHING  -> FontFamily.Monospace
            ExcuseRepository.THEME_SAMSUNG  -> FontFamily("sans-serif-medium")
            ExcuseRepository.THEME_NOTHANKS -> FontFamily.SansSerif
            else -> FontFamily.SansSerif
        }

        // Pill (CORNER_ROUND) = 50dp → truly pill-shaped at typical widget heights
        // Rounded (CORNER_SQUARE) = 8dp → Samsung-style gentle rounding
        // Sharp (CORNER_SHARP) = no cornerRadius modifier applied → true 0dp square
        val baseModifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = hPadding, vertical = padding)

        val boxModifier = when (cornerStyle) {
            ExcuseRepository.CORNER_ROUND  -> baseModifier.cornerRadius(50.dp)
            ExcuseRepository.CORNER_SQUARE -> baseModifier.cornerRadius(8.dp)
            else /* CORNER_SHARP */         -> baseModifier   // no cornerRadius → true square
        }

        Box(modifier = boxModifier) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button (conditionally shown) — foreground colour
                    if (showPrevButton) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_chevron_left),
                            contentDescription = "Previous",
                            colorFilter = ColorFilter.tint(ColorProvider(foregroundColor)),
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
                                            fontSize = fontSize,
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
                                    fontSize = fontSize,
                                    fontWeight = fontWeight,
                                    fontFamily = fontFamily
                                ),
                                maxLines = 3
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(4.dp))

                    // Copy button — foreground colour (matches text)
                    if (copyMechanism == ExcuseRepository.COPY_BUTTON) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_content_copy),
                            contentDescription = "Copy",
                            colorFilter = ColorFilter.tint(ColorProvider(foregroundColor)),
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
     * Returns ThemeColors(background, foreground, accent) for the given theme + dark-mode.
     *
     * Theme philosophy:
     *  Nothing OS  → pure black/white body, Nothing red accent (both modes)
     *  Samsung      → warm off-tones, Samsung blue accent
     *  OnePlus      → near-black/white body, OnePlus red accent (both modes)
     *  Material     → standard M3 surface tones, Material purple accent
     */
    private fun resolveThemeColors(theme: String, isDark: Boolean): ThemeColors = when (theme) {
        ExcuseRepository.THEME_NOTHING -> if (isDark) ThemeColors(
            background = ComposeColor.Black,
            foreground = ComposeColor.White,
            accent     = ComposeColor(0xFFD71921)  // Nothing red
        ) else ThemeColors(
            background = ComposeColor.White,
            foreground = ComposeColor.Black,
            accent     = ComposeColor(0xFFD71921)
        )

        ExcuseRepository.THEME_SAMSUNG -> if (isDark) ThemeColors(
            background = ComposeColor(0xFF1A1A1A),
            foreground = ComposeColor(0xFFF7F7F7),
            accent     = ComposeColor(0xFF4B9FFF)  // Samsung blue (lighter for dark bg)
        ) else ThemeColors(
            background = ComposeColor(0xFFF7F7F7),
            foreground = ComposeColor(0xFF1A1A1A),
            accent     = ComposeColor(0xFF1428A0)  // Samsung blue (deep for light bg)
        )

        ExcuseRepository.THEME_ONEPLUS -> if (isDark) ThemeColors(
            background = ComposeColor(0xFF0F0F0F),
            foreground = ComposeColor(0xFFFAFAFA),
            accent     = ComposeColor(0xFFF6000D)  // OnePlus red
        ) else ThemeColors(
            background = ComposeColor(0xFFFAFAFA),
            foreground = ComposeColor(0xFF0F0F0F),
            accent     = ComposeColor(0xFFF6000D)
        )

        ExcuseRepository.THEME_NOTHANKS -> if (isDark) ThemeColors(
            // Dark: near-black navy background, light sky-blue text, vivid blue accent
            background = ComposeColor(0xFF000E24),  // Digital Blue 950
            foreground = ComposeColor(0xFFCCE0FF),  // Digital Blue 100
            accent     = ComposeColor(0xFF3385FF)   // Digital Blue 400
        ) else ThemeColors(
            // Light: softest blue background, deep navy text, brand blue accent
            background = ComposeColor(0xFFE5F0FF),  // Digital Blue 50
            foreground = ComposeColor(0xFF002966),  // Digital Blue 800
            accent     = ComposeColor(0xFF0052CC)   // Digital Blue 600
        )

        else /* THEME_MATERIAL */ -> if (isDark) ThemeColors(
            background = ComposeColor(0xFF1C1B1F),
            foreground = ComposeColor(0xFFE6E1E5),
            accent     = ComposeColor(0xFFD0BCFF)  // M3 primary in dark
        ) else ThemeColors(
            background = ComposeColor(0xFFFFFBFE),
            foreground = ComposeColor(0xFF1C1B1F),
            accent     = ComposeColor(0xFF6750A4)  // M3 primary in light
        )
    }

    private fun resolveDarkMode(setting: String, context: Context): Boolean = when (setting) {
        ExcuseRepository.DARK_MODE_DARK  -> true
        ExcuseRepository.DARK_MODE_LIGHT -> false
        else -> {
            val uiMode = context.resources.configuration.uiMode
            (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
    }
}
