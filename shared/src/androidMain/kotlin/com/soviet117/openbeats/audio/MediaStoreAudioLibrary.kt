package com.soviet117.openbeats.audio

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import com.soviet117.openbeats.ui.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class MediaStoreAudioLibrary(private val context: Context) : AudioLibrary {

    private val albumIds = ConcurrentHashMap<String, Long>()
    private val artworkCache = mutableMapOf<String, ByteArray?>()
    private val artworkMutex = Mutex()
    private val albumArtCache = mutableMapOf<Long, ByteArray?>()

    override suspend fun loadSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
        )
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            var index = 0
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val durationMs = cursor.getLong(durationCol)
                if (durationMs < 30_000) continue
                val songId = ContentUris.withAppendedId(collection, id).toString()
                albumIds[songId] = cursor.getLong(albumIdCol)
                songs += Song(
                    id = songId,
                    title = cursor.getString(titleCol) ?: "Sin título",
                    artist = cursor.getString(artistCol) ?: "Artista desconocido",
                    album = cursor.getString(albumCol) ?: "Álbum desconocido",
                    durationMs = durationMs,
                    colors = palette[index % palette.size],
                )
                index++
            }
        }
        songs
    }

    override suspend fun loadArtwork(songId: String): ByteArray? {
        val albumId = albumIds[songId] ?: return null
        artworkMutex.withLock {
            if (artworkCache.containsKey(songId)) return artworkCache[songId]
        }
        val bytes = withContext(Dispatchers.IO) { loadAlbumArt(albumId) }
        artworkMutex.withLock {
            if (!artworkCache.containsKey(songId)) artworkCache[songId] = bytes
        }
        return bytes
    }

    private fun loadAlbumArt(albumId: Long): ByteArray? {
        synchronized(albumArtCache) {
            if (albumArtCache.containsKey(albumId)) return albumArtCache[albumId]
        }
        val bytes = try {
            val artUri = Uri.parse("content://media/external/audio/albumart/$albumId")
            context.contentResolver.openInputStream(artUri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
        synchronized(albumArtCache) {
            if (!albumArtCache.containsKey(albumId)) albumArtCache[albumId] = bytes
        }
        return bytes
    }

    private val palette = listOf(
        listOf(Color(0xFF7C3AED), Color(0xFFEC4899)),
        listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
        listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
        listOf(Color(0xFF10B981), Color(0xFF3B82F6)),
        listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
        listOf(Color(0xFFF97316), Color(0xFFF43F5E)),
        listOf(Color(0xFF14B8A6), Color(0xFF84CC16)),
        listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)),
    )
}
