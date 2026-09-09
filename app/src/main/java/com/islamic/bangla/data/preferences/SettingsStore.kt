package com.islamic.bangla.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.islamic.bangla.data.remote.Tafsirs
import com.islamic.bangla.data.remote.audio.AudioConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Theme choices persisted with DataStore. */
enum class ThemeMode(val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromValue(value: Int): ThemeMode =
            entries.firstOrNull { it.value == value } ?: SYSTEM
    }
}

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val themeModeKey = intPreferencesKey("theme_mode")
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")
    private val azanKey = booleanPreferencesKey("azan_enabled")
    private val prayerCityKey = stringPreferencesKey("prayer_city")
    private val tasbihTotalKey = longPreferencesKey("tasbih_total")
    private val lastDhikrKey = intPreferencesKey("tasbih_last_dhikr")
    private val reciterKey = stringPreferencesKey("reciter_edition")
    private val tafsirSlugKey = stringPreferencesKey("tafsir_slug")
    private val recentSearchesKey = stringPreferencesKey("recent_searches")
    private val arabicFontKey = floatPreferencesKey("arabic_font_scale")
    private val banglaFontKey = floatPreferencesKey("bangla_font_scale")

    // ---- Theme ----

    val themeMode: Flow<ThemeMode> = context.settingsDataStore.data
        .map { prefs -> ThemeMode.fromValue(prefs[themeModeKey] ?: ThemeMode.SYSTEM.value) }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { prefs -> prefs[themeModeKey] = mode.value }
    }

    // ---- Hadith sync ----

    /** Last successful API sync (epoch millis) per hadith collection key. */
    suspend fun getLastSync(collectionKey: String): Long? {
        val key = longPreferencesKey("sync_$collectionKey")
        return context.settingsDataStore.data.map { prefs -> prefs[key] }.first()
    }

    suspend fun setLastSync(collectionKey: String, timestamp: Long = System.currentTimeMillis()) {
        val key = longPreferencesKey("sync_$collectionKey")
        context.settingsDataStore.edit { prefs -> prefs[key] = timestamp }
    }

    // ---- Prayer notifications ----

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[notificationsKey] ?: false }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs -> prefs[notificationsKey] = enabled }
    }

    /** Play the azan aloud when a prayer notification fires. Default OFF. */
    val azanEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { prefs -> prefs[azanKey] ?: false }

    suspend fun setAzanEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { prefs -> prefs[azanKey] = enabled }
    }

    /** Per-prayer toggle, default ON (keys: fajr, dhuhr, asr, maghrib, isha). */
    fun prayerEnabled(prayerKey: String): Flow<Boolean> =
        context.settingsDataStore.data
            .map { prefs -> prefs[booleanPreferencesKey("notify_$prayerKey")] ?: true }

    suspend fun setPrayerEnabled(prayerKey: String, enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[booleanPreferencesKey("notify_$prayerKey")] = enabled
        }
    }

    val prayerCity: Flow<String> = context.settingsDataStore.data
        .map { prefs -> prefs[prayerCityKey] ?: "Dhaka" }

    suspend fun setPrayerCity(city: String) {
        context.settingsDataStore.edit { prefs -> prefs[prayerCityKey] = city }
    }

    // ---- Tasbih counter ----

    val tasbihTotal: Flow<Long> = context.settingsDataStore.data
        .map { prefs -> prefs[tasbihTotalKey] ?: 0L }

    fun tasbihCount(dhikrId: Int): Flow<Int> =
        context.settingsDataStore.data
            .map { prefs -> prefs[intPreferencesKey("tasbih_count_$dhikrId")] ?: 0 }

    suspend fun incrementTasbih(dhikrId: Int) {
        context.settingsDataStore.edit { prefs ->
            val countKey = intPreferencesKey("tasbih_count_$dhikrId")
            prefs[countKey] = (prefs[countKey] ?: 0) + 1
            prefs[tasbihTotalKey] = (prefs[tasbihTotalKey] ?: 0L) + 1
        }
    }

    suspend fun resetTasbih(dhikrId: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[intPreferencesKey("tasbih_count_$dhikrId")] = 0
        }
    }

    val lastDhikrId: Flow<Int> = context.settingsDataStore.data
        .map { prefs -> prefs[lastDhikrKey] ?: 1 }

    suspend fun setLastDhikrId(dhikrId: Int) {
        context.settingsDataStore.edit { prefs -> prefs[lastDhikrKey] = dhikrId }
    }

    // ---- Audio reciter ----

    /** Edition id of the selected reciter (see [AudioConfig]). */
    val reciter: Flow<String> = context.settingsDataStore.data
        .map { prefs -> prefs[reciterKey] ?: AudioConfig.DEFAULT_RECITER }

    suspend fun setReciter(edition: String) {
        context.settingsDataStore.edit { prefs -> prefs[reciterKey] = edition }
    }

    // ---- Tafsir ----

    /** Slug of the selected Bangla tafsir (see [Tafsirs]). */
    val tafsirSlug: Flow<String> = context.settingsDataStore.data
        .map { prefs -> prefs[tafsirSlugKey] ?: Tafsirs.defaultSlug }

    suspend fun setTafsirSlug(slug: String) {
        context.settingsDataStore.edit { prefs -> prefs[tafsirSlugKey] = slug }
    }

    // ---- Recent searches ----

    /** Newest-first recent global searches (max 10), stored newline-separated. */
    val recentSearches: Flow<List<String>> = context.settingsDataStore.data
        .map { prefs ->
            prefs[recentSearchesKey]?.split("\n")?.filter { it.isNotBlank() }
                ?: emptyList()
        }

    suspend fun addRecentSearch(query: String) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[recentSearchesKey]
                ?.split("\n")
                ?.filter { it.isNotBlank() }
                .orEmpty()
            prefs[recentSearchesKey] = (listOf(query) + current)
                .distinct()
                .take(10)
                .joinToString("\n")
        }
    }

    suspend fun clearRecentSearches() {
        context.settingsDataStore.edit { prefs -> prefs.remove(recentSearchesKey) }
    }

    // ---- Font sizes ----

    /** Arabic text scale, 0.8..1.4 (default 1.0). */
    val arabicFontScale: Flow<Float> = context.settingsDataStore.data
        .map { prefs -> prefs[arabicFontKey] ?: 1f }

    suspend fun setArabicFontScale(scale: Float) {
        context.settingsDataStore.edit { prefs -> prefs[arabicFontKey] = scale }
    }

    /** Bangla translation scale, 0.8..1.4 (default 1.0). */
    val banglaFontScale: Flow<Float> = context.settingsDataStore.data
        .map { prefs -> prefs[banglaFontKey] ?: 1f }

    suspend fun setBanglaFontScale(scale: Float) {
        context.settingsDataStore.edit { prefs -> prefs[banglaFontKey] = scale }
    }
}
