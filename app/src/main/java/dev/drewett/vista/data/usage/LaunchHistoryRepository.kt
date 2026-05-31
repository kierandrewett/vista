package dev.drewett.vista.data.usage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launchHistoryDataStore by preferencesDataStore(name = "launch_history")

/**
 * Vista launches every app, so it can keep its own most-recently-launched history — fully
 * reliable and permission-free, unlike system UsageStats (which Google TV withholds from
 * third-party launchers). Powers the "Jump back in" row.
 */
class LaunchHistoryRepository(private val context: Context) {

    private val key = stringPreferencesKey("recent")
    private val limit = 20

    val recent: Flow<List<String>> =
        context.launchHistoryDataStore.data.map { prefs ->
            prefs[key].orEmpty().split('\n').filter { it.isNotEmpty() }
        }

    suspend fun record(packageName: String) {
        context.launchHistoryDataStore.edit { prefs ->
            val ordered = prefs[key].orEmpty().split('\n')
                .filter { it.isNotEmpty() && it != packageName }
                .toMutableList()
            ordered.add(0, packageName)
            prefs[key] = ordered.take(limit).joinToString("\n")
        }
    }
}
