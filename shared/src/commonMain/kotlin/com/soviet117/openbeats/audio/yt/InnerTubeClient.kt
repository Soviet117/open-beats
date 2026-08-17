package com.soviet117.openbeats.audio.yt

import androidx.compose.ui.graphics.Color
import com.soviet117.openbeats.ui.data.Song
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object InnerTubeClient {

    private const val SEARCH_URL =
        "https://music.youtube.com/youtubei/v1/search?key=AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
    private const val PLAYER_URL =
        "https://music.youtube.com/youtubei/v1/player?key=AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30"
    private const val USER_AGENT =
        "com.google.android.apps.youtube.music/7.27.52 (Linux; U; Android 12; US) gzip"

    private val json = Json { ignoreUnknownKeys = true }

    data class SearchResult(
        val videoId: String,
        val title: String,
        val artist: String,
        val album: String,
        val durationMs: Long,
        val thumbnailUrl: String?,
    )

    data class StreamInfo(
        val url: String,
        val mimeType: String,
        val bitrate: Long,
        val contentLength: Long,
        val durationMs: Long,
        val title: String,
        val artist: String,
        val thumbnailUrl: String?,
    )

    suspend fun search(query: String, limit: Int = 15): List<SearchResult> {
        val queryJson = JsonPrimitive(query).toString()
        val body = """
        {
            "context": {
                "client": {
                    "clientName": "ANDROID_MUSIC",
                    "clientVersion": "7.27.52",
                    "androidSdkVersion": 31,
                    "hl": "es",
                    "gl": "MX"
                }
            },
            "query": $queryJson,
            "params": "EgWKAQIIA"
        }
        """.trimIndent()

        val response = httpPost(
            url = SEARCH_URL,
            headers = mapOf("User-Agent" to USER_AGENT),
            body = body,
        )
        return parseSearchResults(response, limit)
    }

    suspend fun resolveStream(videoId: String): StreamInfo? {
        val videoIdJson = JsonPrimitive(videoId).toString()
        val body = """
        {
            "context": {
                "client": {
                    "clientName": "ANDROID_MUSIC",
                    "clientVersion": "7.27.52",
                    "androidSdkVersion": 31,
                    "hl": "es",
                    "gl": "MX"
                }
            },
            "videoId": $videoIdJson
        }
        """.trimIndent()

        val response = httpPost(
            url = PLAYER_URL,
            headers = mapOf("User-Agent" to USER_AGENT),
            body = body,
        )
        return parseStreamInfo(response)
    }

    fun streamToSong(info: StreamInfo, videoId: String): Song {
        return Song(
            id = info.url,
            title = info.title,
            artist = info.artist,
            album = "YouTube Music",
            durationMs = info.durationMs,
            colors = listOf(Color(0xFFFF0000), Color(0xFF282828)),
        )
    }

    private fun parseSearchResults(response: String, limit: Int): List<SearchResult> {
        return try {
            val root = json.parseToJsonElement(response) as JsonObject
            val contents = root["contents"] as? JsonObject ?: return emptyList()
            val tabbed = contents["tabbedSearchResultsRenderer"] as? JsonObject ?: return emptyList()
            val tabs = tabbed["tabs"] as? JsonArray ?: return emptyList()

            val results = mutableListOf<SearchResult>()

            for (tab in tabs) {
                val tabObj = tab as? JsonObject ?: continue
                val tabRenderer = tabObj["tabRenderer"] as? JsonObject ?: continue
                val content = tabRenderer["content"] as? JsonObject ?: continue
                val sectionList = content["sectionListRenderer"] as? JsonObject ?: continue
                val sections = sectionList["contents"] as? JsonArray ?: continue

                for (section in sections) {
                    val sectionObj = section as? JsonObject ?: continue
                    val musicShelf = sectionObj["musicShelfRenderer"] as? JsonObject ?: continue
                    val items = musicShelf["contents"] as? JsonArray ?: continue

                    for (item in items) {
                        val itemObj = item as? JsonObject ?: continue
                        val renderer = itemObj["musicResponsiveListItemRenderer"] as? JsonObject ?: continue
                        val result = parseSearchItem(renderer) ?: continue
                        results.add(result)
                        if (results.size >= limit) return results
                    }
                }
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseSearchItem(renderer: JsonObject): SearchResult? {
        val videoId = extractVideoId(renderer) ?: return null
        val flexColumns = renderer["flexColumns"] as? JsonArray ?: return null
        if (flexColumns.size < 2) return null

        val title = extractRunsText(flexColumns[0] as? JsonObject ?: return null)
        if (title.isEmpty()) return null

        val subtitleRuns = extractRuns(flexColumns[1] as? JsonObject ?: return null) ?: emptyList()
        val subtitleParts = subtitleRuns.filter { it.isNotEmpty() && it != "•" }

        val artist = subtitleParts.getOrElse(0) { "Desconocido" }
        val album = subtitleParts.getOrElse(1) { "YouTube Music" }
        val durationMs = extractDuration(renderer)
        val thumbnailUrl = extractThumbnail(renderer)

        return SearchResult(videoId, title, artist, album, durationMs, thumbnailUrl)
    }

    private fun extractRunsText(column: JsonObject): String {
        val runs = getRuns(column) ?: return ""
        val sb = StringBuilder()
        for (run in runs) {
            val text = (run["text"] as? JsonPrimitive)?.content ?: ""
            sb.append(text)
        }
        return sb.toString().trim()
    }

    private fun extractRuns(column: JsonObject): List<String>? {
        val renderer = column["musicResponsiveListItemFlexColumnRenderer"] as? JsonObject ?: return null
        val textObj = renderer["text"] as? JsonObject ?: return null
        val runs = textObj["runs"] as? JsonArray ?: return null
        return runs.mapNotNull { val obj = it as? JsonObject; (obj?.get("text") as? JsonPrimitive)?.content }
    }

    private fun getRuns(column: JsonObject): List<JsonObject>? {
        val renderer = column["musicResponsiveListItemFlexColumnRenderer"] as? JsonObject ?: return null
        val textObj = renderer["text"] as? JsonObject ?: return null
        val runs = textObj["runs"] as? JsonArray ?: return null
        return runs.mapNotNull { it as? JsonObject }
    }

    private fun extractVideoId(renderer: JsonObject): String? {
        val overlay1 = renderer["overlay"] as? JsonObject ?: return null
        val overlay2 = overlay1["musicItemThumbnailOverlayRenderer"] as? JsonObject ?: return null
        val overlay3 = overlay2["content"] as? JsonObject ?: return null
        val overlay4 = overlay3["musicPlayButtonRenderer"] as? JsonObject ?: return null
        val overlay5 = overlay4["playNavigationEndpoint"] as? JsonObject ?: return null
        val watchEndpoint = overlay5["watchEndpoint"] as? JsonObject ?: return null
        return (watchEndpoint["videoId"] as? JsonPrimitive)?.content
    }

    private fun extractDuration(renderer: JsonObject): Long {
        val flexColumns = renderer["flexColumns"] as? JsonArray ?: return 0L
        for (col in flexColumns) {
            val colObj = col as? JsonObject ?: continue
            val runs = getRuns(colObj) ?: continue
            for (run in runs) {
                val text = run["text"]?.toString()?.trim('"') ?: continue
                if (text.matches(Regex("\\d+:\\d+"))) {
                    val parts = text.split(":")
                    if (parts.size == 2) {
                        val min = parts[0].toLongOrNull() ?: 0L
                        val sec = parts[1].toLongOrNull() ?: 0L
                        return min * 60_000 + sec * 1_000
                    }
                }
            }
        }
        return 0L
    }

    private fun extractThumbnail(renderer: JsonObject): String? {
        val thumbObj = (renderer["thumbnail"] as? JsonObject)
            ?.get("musicThumbnailRenderer") as? JsonObject
            ?: return null
        val thumbInner = (thumbObj["thumbnail"] as? JsonObject) ?: return null
        val thumbnails = thumbInner["thumbnails"] as? JsonArray ?: return null
        if (thumbnails.isEmpty()) return null
        val last = thumbnails[thumbnails.size - 1] as? JsonObject ?: return null
        return (last["url"] as? JsonPrimitive)?.content
    }

    private fun parseStreamInfo(response: String): StreamInfo? {
        return try {
            val root = json.parseToJsonElement(response) as JsonObject

            val videoDetails = root["videoDetails"] as? JsonObject
            val title = (videoDetails?.get("title") as? JsonPrimitive)?.content ?: "Sin título"
            val artist = (videoDetails?.get("author") as? JsonPrimitive)?.content ?: "Desconocido"
            val lengthSeconds = ((videoDetails?.get("lengthSeconds") as? JsonPrimitive)?.content
                ?: "0").toLongOrNull() ?: 0L

            val streamingData = root["streamingData"] as? JsonObject ?: return null
            val formats = streamingData["adaptiveFormats"] as? JsonArray ?: return null

            var bestBitrate = 0L
            var bestAudioUrl: String? = null
            var bestMimeType = ""
            var bestContentLength = 0L

            for (fmt in formats) {
                val fmtObj = fmt as? JsonObject ?: continue
                val mimeType = (fmtObj["mimeType"] as? JsonPrimitive)?.content ?: continue
                if (!mimeType.startsWith("audio/")) continue
                val url = (fmtObj["url"] as? JsonPrimitive)?.content ?: continue
                val bitrate = ((fmtObj["bitrate"] as? JsonPrimitive)?.content ?: "0").toLongOrNull() ?: 0L
                if (bestAudioUrl == null || bitrate > bestBitrate) {
                    bestBitrate = bitrate
                    bestAudioUrl = url
                    bestMimeType = mimeType
                    bestContentLength = ((fmtObj["contentLength"] as? JsonPrimitive)?.content
                        ?: "0").toLongOrNull() ?: 0L
                }
            }

            val streamUrl = bestAudioUrl ?: return null

            StreamInfo(
                url = streamUrl,
                mimeType = bestMimeType,
                bitrate = bestBitrate,
                contentLength = bestContentLength,
                durationMs = lengthSeconds * 1000,
                title = title,
                artist = artist,
                thumbnailUrl = null,
            )
        } catch (_: Exception) {
            null
        }
    }
}
