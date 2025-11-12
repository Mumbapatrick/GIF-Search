package com.giphy.giphysearchapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.giphy.giphysearchapp.data.model.GifItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("favorites_prefs")

class FavoritesManager(private val context: Context) {

    private val FAVORITES_KEY = stringSetPreferencesKey("favorites_list")

    val favoritesFlow: Flow<List<GifItem>> = context.dataStore.data.map { prefs ->
        prefs[FAVORITES_KEY]?.mapNotNull { json ->
            runCatching { Json.decodeFromString<GifItem>(json) }.getOrNull()
        } ?: emptyList()
    }

    suspend fun addFavorite(gif: GifItem) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY]?.toMutableSet() ?: mutableSetOf()
            current.add(Json.encodeToString(gif))
            prefs[FAVORITES_KEY] = current
        }
    }

    suspend fun removeFavorite(gif: GifItem) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY]?.toMutableSet() ?: mutableSetOf()
            current.remove(Json.encodeToString(gif))
            prefs[FAVORITES_KEY] = current
        }
    }
}

