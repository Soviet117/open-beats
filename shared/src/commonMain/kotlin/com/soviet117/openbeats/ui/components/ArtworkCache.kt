package com.soviet117.openbeats.ui.components

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import com.soviet117.openbeats.audio.AudioLibrary
import com.soviet117.openbeats.audio.decodeImage
import com.soviet117.openbeats.ui.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

object ArtworkCache {

    private val bitmaps = mutableStateMapOf<String, ImageBitmap>()
    private val requested = mutableSetOf<String>()

    val size: Int
        get() = bitmaps.size

    fun get(songId: String): ImageBitmap? = bitmaps[songId]

    suspend fun preload(songs: List<Song>, library: AudioLibrary) {
        val pending = songs.filter { it.id !in requested }
        if (pending.isEmpty()) return
        requested += pending.map { it.id }
        val semaphore = Semaphore(2)
        coroutineScope {
            pending.map { song ->
                async(Dispatchers.Default) {
                    semaphore.withPermit {
                        val bytes = song.artwork ?: library.loadArtwork(song.id)
                        bytes?.let { decodeImage(it) }?.let { bitmaps[song.id] = it }
                    }
                }
            }.awaitAll()
        }
    }
}
