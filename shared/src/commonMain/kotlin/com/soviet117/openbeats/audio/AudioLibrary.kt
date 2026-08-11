package com.soviet117.openbeats.audio

import com.soviet117.openbeats.ui.data.Song

interface AudioLibrary {
    suspend fun loadSongs(): List<Song>
}
