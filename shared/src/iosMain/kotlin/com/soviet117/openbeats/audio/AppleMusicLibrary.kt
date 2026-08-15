package com.soviet117.openbeats.audio

import androidx.compose.ui.graphics.Color
import com.soviet117.openbeats.ui.data.GenreDetector
import com.soviet117.openbeats.ui.data.Song
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.MediaPlayer.MPMediaItemPropertyAlbumTitle
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyGenre
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPMediaQuery
import platform.MediaPlayer.MPMediaTypeAnyAudio
import platform.MediaPlayer.MPMediaTypeAudioBook
import platform.MediaPlayer.MPMediaTypePodcast
import platform.posix.memcpy
import platform.UIKit.UIImageJPEGRepresentation

@OptIn(ExperimentalForeignApi::class)
class AppleMusicLibrary : AudioLibrary {

    override suspend fun loadSongs(): List<Song> = withContext(Dispatchers.Default) {
        val items = MPMediaQuery.songsQuery().items.orEmpty()
        val songs = mutableListOf<Song>()
        var index = 0
        for (item in items) {
            val url = item.assetURL ?: continue
            val durationMs = (item.playbackDuration * 1000.0).toLong()
            if (durationMs < 30_000) continue
            val mediaType = item.mediaType
            val isAudio = (mediaType and MPMediaTypeAnyAudio) != 0uL
            val isBookOrPodcast = (mediaType and (MPMediaTypePodcast or MPMediaTypeAudioBook)) != 0uL
            if (!isAudio || isBookOrPodcast) continue
            val title = item.valueForProperty(MPMediaItemPropertyTitle) as? String ?: "Sin título"
            val artist = item.valueForProperty(MPMediaItemPropertyArtist) as? String ?: "Artista desconocido"
            val album = item.valueForProperty(MPMediaItemPropertyAlbumTitle) as? String ?: "Álbum desconocido"
            val genreTag = (item.valueForProperty(MPMediaItemPropertyGenre) as? String).orEmpty().trim()
            songs += Song(
                id = url.absoluteString ?: "ios-${item.persistentID}",
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                colors = palette[index % palette.size],
                genre = GenreDetector.resolve(genreTag.ifEmpty { null }, title, artist, album).orEmpty(),
            )
            index++
        }
        songs.sortedBy { it.title.lowercase() }
    }

    override suspend fun loadArtwork(songId: String): ByteArray? = withContext(Dispatchers.Default) {
        val items = MPMediaQuery.songsQuery().items.orEmpty()
        val item = items.firstOrNull { candidate ->
            candidate.assetURL?.absoluteString == songId || "ios-${candidate.persistentID}" == songId
        } ?: return@withContext null
        val artwork = item.artwork ?: return@withContext null
        val image = artwork.imageWithSize(CGSizeMake(512.0, 512.0)) ?: return@withContext null
        UIImageJPEGRepresentation(image, 0.9)?.toByteArray()
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).usePinned { pinned ->
        if (length > 0uL) {
            memcpy(pinned.addressOf(0), bytes, length)
        }
        pinned.get()
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
