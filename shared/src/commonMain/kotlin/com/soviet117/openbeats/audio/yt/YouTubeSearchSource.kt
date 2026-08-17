package com.soviet117.openbeats.audio.yt

import androidx.compose.ui.graphics.Color
import com.soviet117.openbeats.ui.data.Song

class YouTubeSearchSource {

    suspend fun search(query: String, limit: Int = 20): List<Song> {
        if (query.isBlank()) return emptyList()
        return try {
            val results = InnerTubeClient.search(query, limit)
            results.map { result ->
                Song(
                    id = "yt:${result.videoId}",
                    title = result.title,
                    artist = result.artist,
                    album = result.album,
                    durationMs = result.durationMs,
                    colors = listOf(
                        Color(0xFFFF0000),
                        Color(0xFF282828),
                    ),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun resolveAndPlay(videoId: String): Song? {
        return try {
            val streamInfo = InnerTubeClient.resolveStream(videoId) ?: return null
            InnerTubeClient.streamToSong(streamInfo, videoId)
        } catch (_: Exception) {
            null
        }
    }
}
