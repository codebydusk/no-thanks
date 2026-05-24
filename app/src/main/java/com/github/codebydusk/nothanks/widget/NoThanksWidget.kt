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
import com.github.codebydusk.nothanks.data.ExcuseRepository
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.datastore.preferences.core.Preferences

class NoThanksWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 40.dp), // 4x1 roughly
            DpSize(180.dp, 100.dp) // 4x2 roughly
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = ExcuseRepository(context)
        
        // Fetch current state
        var currentText = repository.getCurrentExcuse()
        if (currentText == null) {
            currentText = repository.getNextExcuse() ?: "Tap to fetch"
        }

        val prefs = getAppWidgetState<Preferences>(context, id)
        val isCopied = prefs[IS_COPIED_KEY] ?: false
        
        provideContent {
            val theme by repository.themeFlow.collectAsState(initial = ExcuseRepository.THEME_MATERIAL)
            val cornerStyle by repository.cornerStyleFlow.collectAsState(initial = ExcuseRepository.CORNER_ROUND)
            val darkMode by repository.darkModeFlow.collectAsState(initial = null)
            val copyMechanism by repository.copyMechanismFlow.collectAsState(initial = ExcuseRepository.COPY_TAP)
            
            val displayTheme = if (darkMode == true) true else if (darkMode == false) false else isSystemInDarkTheme(LocalContext.current)

            WidgetContent(
                text = if (isCopied) "Copied!" else currentText,
                theme = theme,
                cornerStyle = cornerStyle,
                isDark = displayTheme,
                copyMechanism = copyMechanism
            )
        }
    }

    @Composable
    private fun WidgetContent(
        text: String,
        theme: String,
        cornerStyle: String,
        isDark: Boolean,
        copyMechanism: String
    ) {
        val backgroundColor = if (isDark) ComposeColor.Black else ComposeColor.White
        val foregroundColor = if (isDark) ComposeColor.White else ComposeColor.Black
        
        val cornerRadius = if (cornerStyle == ExcuseRepository.CORNER_ROUND) 16.dp else 0.dp
        
        val padding = when (theme) {
            ExcuseRepository.THEME_NOTHING -> 8.dp
            ExcuseRepository.THEME_SAMSUNG -> 12.dp
            else -> 10.dp
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(cornerRadius)
                .padding(padding)
        ) {
            // Background for corners (Glance doesn't have clipToOutline easily, but we can use background with radius if supported in newer Glance or just use a Shape)
            // Actually GlanceModifier.background(color, shape) exists in some versions.
            
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Navigation Buttons
                    Image(
                        provider = ImageProvider(android.R.drawable.ic_media_previous),
                        contentDescription = "Previous",
                        modifier = GlanceModifier
                            .size(24.dp)
                            .clickable(actionRunCallback<HistoryAction>(
                                actionParametersOf(HistoryAction.DIRECTION_KEY to HistoryAction.PREVIOUS)
                            ))
                    )
                    
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    
                    val textModifier = GlanceModifier.defaultWeight()
                    val finalModifier = if (copyMechanism == ExcuseRepository.COPY_TAP) {
                        textModifier.clickable(actionRunCallback<CopyAction>())
                    } else {
                        textModifier
                    }

                    Box(modifier = finalModifier, contentAlignment = Alignment.Center) {
                        LazyColumn(modifier = GlanceModifier.fillMaxHeight()) {
                            item {
                                Text(
                                    text = text,
                                    style = TextStyle(
                                        color = ColorProvider(foregroundColor),
                                        fontSize = 16.sp,
                                        fontWeight = if (theme == ExcuseRepository.THEME_NOTHING) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.width(8.dp))
                    
                    if (copyMechanism == ExcuseRepository.COPY_BUTTON) {
                        Image(
                            provider = ImageProvider(android.R.drawable.ic_menu_edit), // Placeholder for copy
                            contentDescription = "Copy",
                            modifier = GlanceModifier
                                .size(24.dp)
                                .clickable(actionRunCallback<CopyAction>())
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                    }

                    Image(
                        provider = ImageProvider(android.R.drawable.ic_media_next),
                        contentDescription = "Next",
                        modifier = GlanceModifier
                            .size(24.dp)
                            .clickable(actionRunCallback<RefreshAction>())
                    )
                }
            }
        }
    }

    @Composable
    private fun isSystemInDarkTheme(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}
