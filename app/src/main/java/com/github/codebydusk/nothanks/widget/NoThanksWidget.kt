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
        // 4x1 compact layout
        private val SIZE_4x1 = DpSize(180.dp, 40.dp)
        // 4x2 expanded layout
        private val SIZE_4x2 = DpSize(180.dp, 100.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SIZE_4x1, SIZE_4x2)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = ExcuseRepository(context)
        
        // Seed widget state on first load if no text is stored yet
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
            val isCopied = state[IS_COPIED_KEY] ?: false

            val theme by repository.themeFlow.collectAsState(initial = ExcuseRepository.THEME_MATERIAL)
            val cornerStyle by repository.cornerStyleFlow.collectAsState(initial = ExcuseRepository.CORNER_ROUND)
            val darkModeSetting by repository.darkModeFlow.collectAsState(initial = ExcuseRepository.DARK_MODE_SYSTEM)
            val copyMechanism by repository.copyMechanismFlow.collectAsState(initial = ExcuseRepository.COPY_TAP)
            val showPrevButton by repository.showPrevButtonFlow.collectAsState(initial = true)
            
            val isDark = resolveDarkMode(darkModeSetting, LocalContext.current)
            val size = LocalSize.current

            WidgetContent(
                text = if (isCopied) "Copied!" else currentText,
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
        val colors = resolveColors(theme, isDark)
        val backgroundColor = colors.first
        val foregroundColor = colors.second
        
        val cornerRadius = if (cornerStyle == ExcuseRepository.CORNER_ROUND) 16.dp else 0.dp
        
        val padding = when (theme) {
            ExcuseRepository.THEME_NOTHING -> 8.dp
            ExcuseRepository.THEME_SAMSUNG -> 12.dp
            else -> 10.dp
        }

        val fontSize = when (theme) {
            ExcuseRepository.THEME_NOTHING -> 14.sp
            ExcuseRepository.THEME_SAMSUNG -> 15.sp
            else -> 16.sp
        }

        val fontWeight = when (theme) {
            ExcuseRepository.THEME_NOTHING -> FontWeight.Bold
            else -> FontWeight.Normal
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(cornerRadius)
                .padding(padding)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button (conditionally shown)
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
                        // 4x2 mode: scrollable text
                        Box(modifier = finalModifier, contentAlignment = Alignment.CenterStart) {
                            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = text,
                                        style = TextStyle(
                                            color = ColorProvider(foregroundColor),
                                            fontSize = fontSize,
                                            fontWeight = fontWeight
                                        ),
                                        maxLines = 6
                                    )
                                }
                            }
                        }
                    } else {
                        // 4x1 mode: compact single-area text
                        Box(modifier = finalModifier, contentAlignment = Alignment.CenterStart) {
                            Text(
                                text = text,
                                style = TextStyle(
                                    color = ColorProvider(foregroundColor),
                                    fontSize = fontSize,
                                    fontWeight = fontWeight
                                ),
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(4.dp))
                    
                    // Copy button (when in button mode)
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

                    // Refresh button
                    Image(
                        provider = ImageProvider(R.drawable.ic_refresh),
                        contentDescription = "Refresh",
                        colorFilter = ColorFilter.tint(ColorProvider(foregroundColor)),
                        modifier = GlanceModifier
                            .size(24.dp)
                            .clickable(actionRunCallback<RefreshAction>())
                    )
                }
            }
        }
    }

    /**
     * Resolves background and foreground colors based on theme and dark mode.
     * Returns Pair(backgroundColor, foregroundColor).
     */
    private fun resolveColors(theme: String, isDark: Boolean): Pair<ComposeColor, ComposeColor> {
        return when (theme) {
            ExcuseRepository.THEME_NOTHING -> {
                // Nothing OS: pure black & white
                if (isDark) Pair(ComposeColor.Black, ComposeColor.White)
                else Pair(ComposeColor.White, ComposeColor.Black)
            }
            ExcuseRepository.THEME_SAMSUNG -> {
                // Samsung One UI: warmer off-tones
                if (isDark) Pair(ComposeColor(0xFF1A1A1A), ComposeColor(0xFFF7F7F7))
                else Pair(ComposeColor(0xFFF7F7F7), ComposeColor(0xFF1A1A1A))
            }
            else -> {
                // Material You: standard surface colors
                if (isDark) Pair(ComposeColor(0xFF1C1B1F), ComposeColor(0xFFE6E1E5))
                else Pair(ComposeColor(0xFFFFFBFE), ComposeColor(0xFF1C1B1F))
            }
        }
    }

    /**
     * Resolves dark mode setting string to boolean.
     */
    private fun resolveDarkMode(setting: String, context: Context): Boolean {
        return when (setting) {
            ExcuseRepository.DARK_MODE_DARK -> true
            ExcuseRepository.DARK_MODE_LIGHT -> false
            else -> {
                // System default
                val uiMode = context.resources.configuration.uiMode
                (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
}
