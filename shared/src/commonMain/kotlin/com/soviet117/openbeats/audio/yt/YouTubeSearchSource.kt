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
            println("YTResolve: Resolving videoId=$videoId")
            val streamInfo = InnerTubeClient.resolveStream(videoId)
            println("YTResolve: streamInfo=${streamInfo?.title}, url=${streamInfo?.url?.take(80)}")
            if (streamInfo != null) {
                val song = InnerTubeClient.streamToSong(streamInfo, videoId)
                println("YTResolve: Song created: ${song.title}, id=${song.id.take(80)}")
                song
            } else null
        } catch (e: Exception) {
            println("YTResolve: Error: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
