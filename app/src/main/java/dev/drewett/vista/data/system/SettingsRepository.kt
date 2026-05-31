package dev.drewett.vista.data.system

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.drewett.vista.domain.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** Persists Vista's own preferences — currently the day/night theme override for the projector. */
class SettingsRepository(private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> =
        context.settingsDataStore.data.map { prefs ->
            runCatching { ThemeMode.valueOf(prefs[themeKey] ?: "") }.getOrDefault(ThemeMode.AUTO)
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[themeKey] = mode.name }
    }
}
