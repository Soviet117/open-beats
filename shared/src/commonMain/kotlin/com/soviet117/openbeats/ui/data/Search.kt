package com.soviet117.openbeats.ui.data

data class SearchResults(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<LibraryArtist> = emptyList(),
) {
    val isEmpty: Boolean get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty()
}

fun searchLibrary(query: String, songs: List<Song>): SearchResults {
    val tokens = MetadataCleaner.normalizeSearchKey(query)
        .split(" ")
        .filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return SearchResults()

    fun Song.matches(): Boolean {
        val haystack = MetadataCleaner.normalizeSearchKey("$title $artist $album $genre")
        return tokens.all { it in haystack }
    }

    val matchedSongs = songs.filter { it.matches() }
    val albums = deriveAlbums(songs)
        .filter { album ->
            tokens.any { token ->
                token in MetadataCleaner.normalizeSearchKey(album.name) ||
                    album.songs.any { song ->
                        token in MetadataCleaner.normalizeSearchKey(song.title) ||
                            token in MetadataCleaner.normalizeSearchKey(song.artist)
                    }
            }
        }
    val artists = deriveArtists(songs)
        .filter { artist ->
            tokens.any { token ->
                token in MetadataCleaner.normalizeSearchKey(artist.name) ||
                    artist.songs.any { song ->
                        token in MetadataCleaner.normalizeSearchKey(song.title) ||
                            token in MetadataCleaner.normalizeSearchKey(song.album)
                    }
            }
        }

    val firstToken = tokens.first()
    fun relevance(song: Song): Int {
        val key = MetadataCleaner.normalizeSearchKey(song.title)
        return when {
            key.startsWith(firstToken) -> 0
            firstToken in key -> 1
            else -> 2
        }
    }
    val orderedSongs = matchedSongs.sortedWith(compareBy({ relevance(it) }, { it.title.lowercase() }))
    return SearchResults(
        songs = orderedSongs,
        albums = albums.sortedBy { it.name.lowercase() },
        artists = artists.sortedBy { it.name.lowercase() },
    )
}

object GenreDetector {

    fun detect(song: Song): String? = detect(song.title, song.artist, song.album)

    fun detect(title: String, artist: String = "", album: String = ""): String? {
        val spaced = " ${normalize("$title $artist $album")} "
        if (spaced.isBlank()) return null
        for ((keyword, genre) in RULES) {
            if (" $keyword " in spaced) return genre
        }
        return null
    }

    private fun normalize(value: String): String {
        val key = MetadataCleaner.normalizeSearchKey(value)
        return Regex("[^a-z0-9]").replace(key, " ").trim().replace(Regex("\\s+"), " ")
    }

    private val RULES = listOf(
        "lofi" to "Lo-fi",
        "lo fi" to "Lo-fi",
        "reggaeton" to "Reggaetón",
        "dembow" to "Reggaetón",
        "bachata" to "Bachata",
        "salsa" to "Salsa",
        "cumbia" to "Cumbia",
        "merengue" to "Merengue",
        "corridos" to "Regional Mexicano",
        "corrido" to "Regional Mexicano",
        "norteno" to "Regional Mexicano",
        "grupera" to "Regional Mexicano",
        "banda" to "Regional Mexicano",
        "kpop" to "K-Pop",
        "k pop" to "K-Pop",
        "trap" to "Trap",
        "drill" to "Trap",
        "hip hop" to "Hip-Hop / Rap",
        "hiphop" to "Hip-Hop / Rap",
        "rap" to "Hip-Hop / Rap",
        "electronic" to "Electrónica",
        "electronica" to "Electrónica",
        "techno" to "Electrónica",
        "tech house" to "Electrónica",
        "edm" to "Electrónica",
        "trance" to "Electrónica",
        "dubstep" to "Electrónica",
        "electro" to "Electrónica",
        "house" to "Electrónica",
        "metal" to "Metal",
        "heavy" to "Metal",
        "grunge" to "Rock",
        "punk" to "Rock",
        "rock" to "Rock",
        "indie" to "Indie / Alternativo",
        "alternativo" to "Indie / Alternativo",
        "alternative" to "Indie / Alternativo",
        "pop" to "Pop",
        "acoustic" to "Acústico",
        "acustico" to "Acústico",
        "unplugged" to "Acústico",
        "jazz" to "Jazz",
        "classical" to "Clásica",
        "clasica" to "Clásica",
        "orchestra" to "Clásica",
        "piano" to "Clásica",
        "r b" to "R&B / Soul",
        "rnb" to "R&B / Soul",
        "soul" to "R&B / Soul",
        "funk" to "R&B / Soul",
        "country" to "Country / Folk",
        "folk" to "Country / Folk",
        "reggae" to "Reggae",
        "afrobeat" to "Afrobeat",
        "afrobeats" to "Afrobeat",
        "balada" to "Balada / Romántica",
        "romantica" to "Balada / Romántica",
        "latina" to "Latina",
        "latino" to "Latina",
        "urbano" to "Latina",
        "latin" to "Latina",
    )
}
