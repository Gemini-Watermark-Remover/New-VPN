package com.ciphervpn.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val SELECTED_SERVER_KEY = stringPreferencesKey("SELECTED_SERVER")
    }

    val selectedServerFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SELECTED_SERVER_KEY] ?: "United_States"
        }

    suspend fun saveSelectedServer(server: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_SERVER_KEY] = server
        }
    }
}
