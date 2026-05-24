package com.github.codebydusk.nothanks.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.random.Random

val Context.dataStore by preferencesDataStore(name = "settings")

class ExcuseRepository(private val context: Context) {

    private val api = Retrofit.Builder()
        .baseUrl("https://naas.isalman.dev/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ExcuseApi::class.java)

    private val gson = Gson()

    companion object {
        private val HISTORY_KEY = stringPreferencesKey("history")
        private val CURRENT_INDEX_KEY = intPreferencesKey("current_index")
        
        // Settings keys
        val DARK_MODE_KEY = stringPreferencesKey("dark_mode_v2")
        val THEME_KEY = stringPreferencesKey("theme")
        val CORNER_STYLE_KEY = stringPreferencesKey("corner_style")
        val COPY_MECHANISM_KEY = stringPreferencesKey("copy_mechanism")
        val SHOW_PREV_BUTTON_KEY = booleanPreferencesKey("show_prev_button")

        // Dark mode values
        const val DARK_MODE_SYSTEM = "SYSTEM"
        const val DARK_MODE_LIGHT = "LIGHT"
        const val DARK_MODE_DARK = "DARK"

        // Theme values
        const val THEME_NOTHING = "NOTHING"
        const val THEME_SAMSUNG = "SAMSUNG"
        const val THEME_MATERIAL = "MATERIAL"

        // Corner style values
        const val CORNER_ROUND = "ROUND"
        const val CORNER_SQUARE = "SQUARE"

        // Copy mechanism values
        const val COPY_TAP = "TAP"
        const val COPY_BUTTON = "BUTTON"

        // Sarcastic fallback messages for API failure
        val FALLBACK_MESSAGES = listOf(
            "We ran out of excuses today. Please try again.",
            "Even our excuse generator needs a break sometimes.",
            "The excuse factory workers are on strike. Check back later.",
            "Our excuse hamster stopped running. Give it a moment.",
            "Error 404: Excuses not found. The irony is not lost on us.",
            "The internet ate your excuse. Classic.",
            "Looks like we need an excuse for not having excuses.",
            "Our server said 'No, thanks!' to your request."
        )
    }

    suspend fun getNextExcuse(): String {
        return try {
            val response = api.getExcuse()
            val reason = response.reason
            addToHistory(reason)
            reason
        } catch (e: Exception) {
            val fallback = FALLBACK_MESSAGES[Random.nextInt(FALLBACK_MESSAGES.size)]
            fallback
        }
    }

    private suspend fun addToHistory(reason: String) {
        context.dataStore.edit { preferences ->
            val historyJson = preferences[HISTORY_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<String>>() {}.type
            val history: MutableList<String> = gson.fromJson(historyJson, type)
            
            history.add(reason)
            if (history.size > 10) {
                history.removeAt(0)
            }
            
            preferences[HISTORY_KEY] = gson.toJson(history)
            preferences[CURRENT_INDEX_KEY] = history.size - 1
        }
    }

    suspend fun getPreviousExcuse(): String? {
        var result: String? = null
        context.dataStore.edit { preferences ->
            val historyJson = preferences[HISTORY_KEY] ?: "[]"
            val type = object : TypeToken<List<String>>() {}.type
            val history: List<String> = gson.fromJson(historyJson, type)
            val currentIndex = preferences[CURRENT_INDEX_KEY] ?: -1
            
            if (currentIndex > 0) {
                val newIndex = currentIndex - 1
                preferences[CURRENT_INDEX_KEY] = newIndex
                result = history[newIndex]
            } else if (currentIndex == 0) {
                result = history[0]
            }
        }
        return result
    }

    suspend fun getNextFromHistory(): String? {
        var result: String? = null
        context.dataStore.edit { preferences ->
            val historyJson = preferences[HISTORY_KEY] ?: "[]"
            val type = object : TypeToken<List<String>>() {}.type
            val history: List<String> = gson.fromJson(historyJson, type)
            val currentIndex = preferences[CURRENT_INDEX_KEY] ?: -1
            
            if (currentIndex < history.size - 1 && currentIndex != -1) {
                val newIndex = currentIndex + 1
                preferences[CURRENT_INDEX_KEY] = newIndex
                result = history[newIndex]
            }
        }
        return result
    }

    suspend fun getCurrentExcuse(): String? {
        val preferences = context.dataStore.data.first()
        val historyJson = preferences[HISTORY_KEY] ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        val history: List<String> = gson.fromJson(historyJson, type)
        val currentIndex = preferences[CURRENT_INDEX_KEY] ?: -1
        
        return if (currentIndex in history.indices) {
            history[currentIndex]
        } else {
            null
        }
    }

    suspend fun getCurrentIndex(): Int {
        val preferences = context.dataStore.data.first()
        return preferences[CURRENT_INDEX_KEY] ?: -1
    }

    suspend fun getHistorySize(): Int {
        val preferences = context.dataStore.data.first()
        val historyJson = preferences[HISTORY_KEY] ?: "[]"
        val type = object : TypeToken<List<String>>() {}.type
        val history: List<String> = gson.fromJson(historyJson, type)
        return history.size
    }

    // Flows for settings observation
    val darkModeFlow: Flow<String> = context.dataStore.data.map { it[DARK_MODE_KEY] ?: DARK_MODE_SYSTEM }
    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: THEME_MATERIAL }
    val cornerStyleFlow: Flow<String> = context.dataStore.data.map { it[CORNER_STYLE_KEY] ?: CORNER_ROUND }
    val copyMechanismFlow: Flow<String> = context.dataStore.data.map { it[COPY_MECHANISM_KEY] ?: COPY_TAP }
    val showPrevButtonFlow: Flow<Boolean> = context.dataStore.data.map { it[SHOW_PREV_BUTTON_KEY] ?: true }

    suspend fun updateSetting(key: Preferences.Key<*>, value: Any) {
        context.dataStore.edit { preferences ->
            @Suppress("UNCHECKED_CAST")
            when (key) {
                DARK_MODE_KEY -> preferences[DARK_MODE_KEY] = value as String
                THEME_KEY -> preferences[THEME_KEY] = value as String
                CORNER_STYLE_KEY -> preferences[CORNER_STYLE_KEY] = value as String
                COPY_MECHANISM_KEY -> preferences[COPY_MECHANISM_KEY] = value as String
                SHOW_PREV_BUTTON_KEY -> preferences[SHOW_PREV_BUTTON_KEY] = value as Boolean
            }
        }
    }
}
