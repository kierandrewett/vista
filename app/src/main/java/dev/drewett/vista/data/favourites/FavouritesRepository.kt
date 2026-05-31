package dev.drewett.vista.data.favourites

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favouritesDataStore by preferencesDataStore(name = "favourites")

/** User-curated pinned apps, persisted across restarts via DataStore. */
class FavouritesRepository(private val context: Context) {

    private val key = stringSetPreferencesKey("packages")

    val favourites: Flow<Set<String>> =
        context.favouritesDataStore.data.map { prefs -> prefs[key] ?: emptySet() }

    suspend fun toggle(packageName: String) {
        context.favouritesDataStore.edit { prefs ->
            val current = prefs[key]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(packageName)) current.remove(packageName)
            prefs[key] = current
        }
    }
}
