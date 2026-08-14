package com.soviet117.openbeats.ui.data

interface MusicSearchSource {
    suspend fun search(query: String, limit: Int = 20): List<Song>
}
