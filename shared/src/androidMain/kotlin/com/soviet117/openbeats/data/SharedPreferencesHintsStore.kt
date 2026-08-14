package com.soviet117.openbeats.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SharedPreferencesHintsStore(context: Context) : HintsStore {

    private val prefs = context.getSharedPreferences("open_beats_hints", Context.MODE_PRIVATE)

    override suspend fun genreTipSeen(): Boolean = withContext(Dispatchers.IO) {
        prefs.getBoolean(KEY, false)
    }

    override suspend fun markGenreTipSeen() = withContext(Dispatchers.IO) {
        prefs.edit().putBoolean(KEY, true).apply()
    }

    private companion object {
        const val KEY = "genre_tip_seen"
    }
}
