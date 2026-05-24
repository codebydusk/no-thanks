package com.github.codebydusk.nothanks.widget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.github.codebydusk.nothanks.data.ExcuseRepository
import kotlinx.coroutines.delay

val IS_COPIED_KEY = booleanPreferencesKey("is_copied")
val CURRENT_TEXT_KEY = stringPreferencesKey("current_text")

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val repository = ExcuseRepository(context)
        val theme = repository.getThemeSetting()

        // Show a hilarious theme-appropriate loading message immediately
        val loadingMsg = ExcuseRepository.getRandomLoadingMessage(theme)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[CURRENT_TEXT_KEY] = loadingMsg
        }
        NoThanksWidget().update(context, glanceId)

        // Fetch new excuse from API, then replace the loading message
        val newText = repository.getNextExcuse()
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[CURRENT_TEXT_KEY] = newText
        }
        NoThanksWidget().update(context, glanceId)
    }
}

class HistoryAction : ActionCallback {
    companion object {
        val DIRECTION_KEY = ActionParameters.Key<String>("direction")
        const val PREVIOUS = "PREVIOUS"
        const val NEXT = "NEXT"
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val direction = parameters[DIRECTION_KEY] ?: return
        val repository = ExcuseRepository(context)

        val newText = if (direction == PREVIOUS) {
            repository.getPreviousExcuse()
        } else {
            repository.getNextFromHistory()
        }
        if (newText != null) {
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[CURRENT_TEXT_KEY] = newText
            }
        }
        NoThanksWidget().update(context, glanceId)
    }
}

class CopyAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val repository = ExcuseRepository(context)
        // Read from history, not widget state — avoids copying a loading message
        val text = repository.getCurrentExcuse() ?: return

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("No, Thanks! Excuse", text)
        clipboard.setPrimaryClip(clip)

        // Show "Copied!" feedback for 2 seconds
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[IS_COPIED_KEY] = true
        }
        NoThanksWidget().update(context, glanceId)

        try {
            delay(2000)
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[IS_COPIED_KEY] = false
            }
            NoThanksWidget().update(context, glanceId)
        } catch (_: Exception) {
            // Widget may have been removed during delay — ignore gracefully
        }
    }
}
