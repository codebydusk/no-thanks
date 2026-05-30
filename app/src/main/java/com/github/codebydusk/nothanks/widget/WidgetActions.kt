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
import com.github.codebydusk.nothanks.data.dataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

val IS_COPIED_KEY   = booleanPreferencesKey("is_copied")
val IS_LOADING_KEY  = booleanPreferencesKey("is_loading")
val CURRENT_TEXT_KEY = stringPreferencesKey("current_text")
val COPIED_MSG_KEY   = stringPreferencesKey("copied_msg")

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val repository = ExcuseRepository(context)
        val theme = repository.getThemeSetting()

        // Show a hilarious theme-appropriate loading quip — IS_LOADING_KEY=true skips "No, thanks!" prefix
        val loadingMsg = ExcuseRepository.getRandomLoadingMessage(theme)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[CURRENT_TEXT_KEY] = loadingMsg
            prefs[IS_LOADING_KEY] = true
        }
        NoThanksWidget().update(context, glanceId)

        // Fetch; IS_LOADING_KEY=false so the real result gets the "No, thanks!" prefix
        val newText = repository.getNextExcuse()
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[CURRENT_TEXT_KEY] = newText
            prefs[IS_LOADING_KEY] = false
        }
        NoThanksWidget().update(context, glanceId)
    }
}

class HistoryAction : ActionCallback {
    companion object {
        val DIRECTION_KEY = ActionParameters.Key<String>("direction")
        const val PREVIOUS = "PREVIOUS"
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
                prefs[IS_LOADING_KEY] = false
            }
        }
        NoThanksWidget().update(context, glanceId)
    }
}

class CopyAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val repository = ExcuseRepository(context)
        // Read from history so we never copy a transient loading message
        val text = repository.getCurrentExcuse() ?: return

        // Check if the user wants to include "No, thanks!" prefix in the copy
        val shouldIncludePrefix = context.dataStore.data.first()[ExcuseRepository.COPY_PREFIX_KEY] ?: false
        val textToCopy = if (shouldIncludePrefix) "No, thanks! $text" else text

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Excuse", textToCopy)
        clipboard.setPrimaryClip(clip)

        // Pick a random hilarious confirmation message, store it, show for 2 s
        val copiedMsg = ExcuseRepository.getRandomCopiedMessage()
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[IS_COPIED_KEY] = true
            prefs[COPIED_MSG_KEY] = copiedMsg
        }
        NoThanksWidget().update(context, glanceId)

        try {
            delay(2000)
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[IS_COPIED_KEY] = false
            }
            NoThanksWidget().update(context, glanceId)
        } catch (_: Exception) {
            // Widget removed during delay — ignore gracefully
        }
    }
}
