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
        val COPY_PREFIX_KEY = booleanPreferencesKey("copy_prefix")
        val TEXT_SIZE_KEY = stringPreferencesKey("text_size")

        // Dark mode values
        const val DARK_MODE_SYSTEM = "SYSTEM"
        const val DARK_MODE_LIGHT = "LIGHT"
        const val DARK_MODE_DARK = "DARK"

        // Theme values — 3 themes only
        const val THEME_NOTHING = "NOTHING"
        const val THEME_GOLDEN = "GOLDEN"
        const val THEME_SYSTEM = "SYSTEM_THEME"

        // Corner style values
        const val CORNER_ROUND = "ROUND"
        const val CORNER_SQUARE = "SQUARE"
        const val CORNER_SHARP = "SHARP"

        // Copy mechanism values
        const val COPY_TAP = "TAP"
        const val COPY_BUTTON = "BUTTON"

        // Text size values
        const val TEXT_SIZE_SMALL = "SMALL"
        const val TEXT_SIZE_NORMAL = "NORMAL"
        const val TEXT_SIZE_LARGE = "LARGE"

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

        // Hilarious messages shown when text is copied to clipboard
        val COPIED_MESSAGES = listOf(
            "That's a copy, Houston. 📋",
            "Snagged! Use it wisely. 🎯",
            "Ctrl+C executed. Godspeed. ⚡",
            "In your clipboard. No refunds. 📱",
            "Excuse extracted with prejudice. 🔪",
            "Pasted into your soul. 🌀",
            "That excuse is now legally yours. ⚖️",
            "Copy secured. Mission complete. ✅",
            "Yours now. Don't abuse it. 🤐",
            "Clipboard hijacked. You're welcome. 😎"
        )

        fun getRandomCopiedMessage(): String = COPIED_MESSAGES[Random.nextInt(COPIED_MESSAGES.size)]

        // Theme-specific hilarious loading messages
        private val NOTHING_LOADING = listOf(
            "● ● ● loading ● ● ●",
            "📡 signal: searching...",
            "🔊 beep. boop. thinking.",
            "// excuse.render() 💻",
            "⏳ nothing to show yet...",
            "✨ glyphs assembling...",
            "🖥️ dot matrix active!",
            "⚙️ glyph interface busy..."
        )

        private val GOLDEN_LOADING = listOf(
            "⚜️ summoning ancient wisdom...",
            "🏰 the scroll is unrolling...",
            "📜 consulting the archives...",
            "🗡️ forging your refusal...",
            "👑 the royal decree approaches...",
            "🔔 the bell tolls for thee...",
            "⚗️ brewing golden words...",
            "🕯️ illuminating the manuscript..."
        )

        private val SYSTEM_LOADING = listOf(
            "⏳ fetching your excuse...",
            "🔄 loading something clever...",
            "💭 thinking of a good one...",
            "📡 connecting to excuse server...",
            "✨ generating brilliance...",
            "🧠 processing refusal...",
            "🎯 targeting the perfect excuse..."
        )

        /**
         * Returns a random hilarious loading message appropriate for the given theme.
         */
        fun getRandomLoadingMessage(theme: String): String {
            val messages = when (theme) {
                THEME_NOTHING -> NOTHING_LOADING
                THEME_GOLDEN -> GOLDEN_LOADING
                THEME_SYSTEM -> SYSTEM_LOADING
                else -> SYSTEM_LOADING
            }
            return messages[Random.nextInt(messages.size)]
        }
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

    /** Reads the currently saved theme setting (used by widget actions). */
    suspend fun getThemeSetting(): String =
        context.dataStore.data.first()[THEME_KEY] ?: THEME_NOTHING

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

    // Flows for settings observation — default theme is Nothing OS
    val darkModeFlow: Flow<String> = context.dataStore.data.map { it[DARK_MODE_KEY] ?: DARK_MODE_SYSTEM }
    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: THEME_NOTHING }
    val cornerStyleFlow: Flow<String> = context.dataStore.data.map { it[CORNER_STYLE_KEY] ?: CORNER_ROUND }
    val copyMechanismFlow: Flow<String> = context.dataStore.data.map { it[COPY_MECHANISM_KEY] ?: COPY_TAP }
    val showPrevButtonFlow: Flow<Boolean> = context.dataStore.data.map { it[SHOW_PREV_BUTTON_KEY] ?: true }
    val copyPrefixFlow: Flow<Boolean> = context.dataStore.data.map { it[COPY_PREFIX_KEY] ?: false }
    val textSizeFlow: Flow<String> = context.dataStore.data.map { it[TEXT_SIZE_KEY] ?: TEXT_SIZE_NORMAL }

    suspend fun updateSetting(key: Preferences.Key<*>, value: Any) {
        context.dataStore.edit { preferences ->
            @Suppress("UNCHECKED_CAST")
            when (key) {
                DARK_MODE_KEY -> preferences[DARK_MODE_KEY] = value as String
                THEME_KEY -> preferences[THEME_KEY] = value as String
                CORNER_STYLE_KEY -> preferences[CORNER_STYLE_KEY] = value as String
                COPY_MECHANISM_KEY -> preferences[COPY_MECHANISM_KEY] = value as String
                SHOW_PREV_BUTTON_KEY -> preferences[SHOW_PREV_BUTTON_KEY] = value as Boolean
                COPY_PREFIX_KEY -> preferences[COPY_PREFIX_KEY] = value as Boolean
                TEXT_SIZE_KEY -> preferences[TEXT_SIZE_KEY] = value as String
            }
        }
    }
}
