package com.soviet117.openbeats.ui.data

data class ParsedSong(
    val title: String,
    val artist: String,
    val album: String,
)

data class ParsedFileName(
    val title: String,
    val artist: String,
    val album: String,
)

object MetadataCleaner {

    const val UNKNOWN_ARTIST = "Artista desconocido"

    private val KNOWN_EXTENSIONS = setOf(
        "mp3", "m4a", "wav", "flac", "ogg", "aac", "opus", "mp4", "wma", "webm",
    )

    private val JUNK_KEYS = setOf(
        "unknown", "unknown artist", "unknown album", "untitled",
        "desconocido", "artista desconocido", "album desconocido",
        "sin titulo", "track", "audio", "title", "titled",
    )

    private val TRACKY_KEY = Regex("""(track|audio)\s*\d*""")

    private val SUFFIX_TOKENS =
        "official video|official audio|official music video|lyrics|lyric video|" +
            "official|audio|video|hd|4k|8k|remastered|remaster|extended version|" +
            "original mix|clean|explicit|visualizer|youtube|mp3"

    private val SUFFIX_PAREN = Regex("""\s*[(\[]\s*(?:$SUFFIX_TOKENS)\s*[)\]]\s*$""", RegexOption.IGNORE_CASE)
    private val SUFFIX_DASH = Regex("""\s*-\s*(?:$SUFFIX_TOKENS)\s*$""", RegexOption.IGNORE_CASE)
    private val BITRATE_PAREN = Regex("""\s*\(?\s*\d{2,4}\s*kbps?\s*\)?\s*$""", RegexOption.IGNORE_CASE)

    private val TRACK_PREFIX_DOT = Regex("""^\d{1,3}\s*[.)\]]\s*""")
    private val TRACK_PREFIX_DASH = Regex("""^\d{1,3}\s*[-–—]\s*""")
    private val SEPARATOR = Regex("""\s*[-–—]\s*""")
    private val COLLAPSE_WS = Regex("""\s+""")
    private val NON_ALPHANUM = Regex("[^a-z0-9áéíóúüñ]")
    private val NUMERIC = Regex("""\d{1,3}""")

    private val PREFIXES = listOf(
        "y2mate.com", "yt1s.com", "yt5s.io", "mp3juices", "ytmp3", "mp3convert", "conv",
    )

    fun infer(
        mediaTitle: String?,
        mediaArtist: String?,
        mediaAlbum: String?,
        fileName: String?,
    ): ParsedSong {
        val parsed = parseFileName(fileName)
        return ParsedSong(
            title = if (looksReal(mediaTitle)) mediaTitle!!.trim() else parsed.title,
            artist = when {
                looksReal(mediaArtist) -> mediaArtist!!.trim()
                parsed.artist.isNotBlank() -> parsed.artist
                else -> UNKNOWN_ARTIST
            },
            album = when {
                looksReal(mediaAlbum) -> mediaAlbum!!.trim()
                parsed.album.isNotBlank() -> parsed.album
                else -> ""
            },
        )
    }

    fun looksReal(value: String?): Boolean = !isGarbage(value)

    fun isGarbage(value: String?): Boolean {
        if (value == null) return true
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return true
        if (!trimmed.any { it.isLetter() }) return true
        val key = normalizeKey(trimmed)
        if (key in JUNK_KEYS) return true
        if (TRACKY_KEY.matches(key)) return true
        if (key.length >= 20 && !key.contains(" ")) return true
        return false
    }

    fun normalizeKey(value: String): String {
        val spaced = NON_ALPHANUM.replace(value.trim().lowercase(), " ")
        return COLLAPSE_WS.replace(spaced, " ").trim()
    }

    fun parseFileName(fileName: String?): ParsedFileName {
        var name = fileName?.trim() ?: return ParsedFileName("", "", "")
        name = name.removeSuffixByExtension()
        if (!name.contains(' ') && name.contains('_')) {
            name = name.replace('_', ' ')
        }
        name = name.replace(Regex("[–—―‒]"), "-")
        for (prefix in PREFIXES) {
            name = name.replace(Regex("^$prefix\\s*[-:]\\s*", RegexOption.IGNORE_CASE), "").trim()
        }
        repeat(3) {
            val before = name
            name = name.replace(BITRATE_PAREN, "").trim()
            name = name.replace(SUFFIX_PAREN, "").trim()
            name = name.replace(SUFFIX_DASH, "").trim()
            if (name == before) return@repeat
        }
        name = name.replace(TRACK_PREFIX_DOT, "").trim()
        name = name.replace(TRACK_PREFIX_DASH, "").trim()

        val parts = name.split(SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return ParsedFileName("", "", "")

        val title: String
        val artist: String
        val album: String
        when {
            parts.size == 1 -> {
                artist = ""
                album = ""
                title = parts[0]
            }
            parts.size == 2 -> {
                artist = parts[0]
                album = ""
                title = parts[1]
            }
            else -> {
                artist = parts.first()
                title = parts.last()
                album = parts.subList(1, parts.size - 1)
                    .filterNot { NUMERIC.matches(it) }
                    .joinToString(" ")
            }
        }
        return ParsedFileName(
            title = title.clean(),
            artist = artist.clean(),
            album = album.clean(),
        )
    }

    private fun String.removeSuffixByExtension(): String {
        val idx = lastIndexOf('.')
        if (idx <= 0) return this
        val extension = substring(idx + 1).lowercase()
        return if (extension in KNOWN_EXTENSIONS) substring(0, idx) else this
    }

    private fun String.clean(): String =
        COLLAPSE_WS.replace(trim(), " ").trim().trim('.').trim()
}
