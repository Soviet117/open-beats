package com.soviet117.openbeats.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharedPreferencesFavoritesStore(context: Context) : FavoritesStore {

    private val prefs = context.getSharedPreferences("open_beats_favorites", Context.MODE_PRIVATE)

    override suspend fun load(): Set<String> = withContext(Dispatchers.IO) {
        prefs.getStringSet(KEY, emptySet())?.toSet() ?: emptySet()
    }

    override suspend fun save(ids: Set<String>) = withContext(Dispatchers.IO) {
        prefs.edit().putStringSet(KEY, ids.toSet()).apply()
    }

    private companion object {
        const val KEY = "liked_song_ids"
    }
}
