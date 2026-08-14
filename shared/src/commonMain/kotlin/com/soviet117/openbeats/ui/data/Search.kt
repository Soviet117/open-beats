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
        val artistKey = normalize(artist)
        if (artistKey.isNotEmpty()) {
            ARTIST_RULES[artistKey]?.let { return it }
            for ((key, genre) in ARTIST_RULES) {
                if (key.length >= 4 && " $key " in " $artistKey ") return genre
            }
        }
        val spaced = " ${normalize("$title $artist $album")} "
        if (spaced.isBlank()) return null
        for ((keyword, genre) in RULES) {
            if (" $keyword " in spaced) return genre
        }
        return null
    }

    fun resolve(taggedGenre: String?, title: String, artist: String = "", album: String = ""): String? {
        normalizeGenre(taggedGenre)?.let { return it }
        return detect(title, artist, album)
    }

    fun normalizeGenre(name: String?): String? {
        val key = normalize(name ?: return null)
        if (key.isEmpty()) return null
        return CANONICAL_GENRES[key]
    }

    private fun normalize(value: String): String {
        val key = MetadataCleaner.normalizeSearchKey(value)
        return Regex("[^a-z0-9]").replace(key, " ").trim().replace(Regex("\\s+"), " ")
    }

    private val CANONICAL_GENRES = mapOf(
        "heavy metal" to "Metal",
        "nu metal" to "Metal",
        "metal core" to "Metal",
        "metalcore" to "Metal",
        "thrash metal" to "Metal",
        "thrash" to "Metal",
        "death metal" to "Metal",
        "black metal" to "Metal",
        "screamo" to "Metal",
        "metal" to "Metal",
        "hard rock" to "Rock",
        "classic rock" to "Rock",
        "rock and roll" to "Rock",
        "rock n roll" to "Rock",
        "rock roll" to "Rock",
        "rock en espanol" to "Rock",
        "rock pop" to "Rock",
        "pop rock" to "Rock",
        "punk rock" to "Rock",
        "grunge" to "Rock",
        "grunge rock" to "Rock",
        "alternative rock" to "Indie / Alternativo",
        "rock" to "Rock",
        "pop" to "Pop",
        "synth pop" to "Electrónica",
        "synthpop" to "Electrónica",
        "electronic" to "Electrónica",
        "electronica" to "Electrónica",
        "edm" to "Electrónica",
        "dance" to "Electrónica",
        "reggaeton" to "Reggaetón",
        "reggaetón" to "Reggaetón",
        "regueton" to "Reggaetón",
        "trap" to "Trap",
        "hip hop" to "Hip-Hop / Rap",
        "hiphop" to "Hip-Hop / Rap",
        "rap" to "Hip-Hop / Rap",
        "r and b" to "R&B / Soul",
        "rnb" to "R&B / Soul",
        "soul" to "R&B / Soul",
        "k pop" to "K-Pop",
        "kpop" to "K-Pop",
        "cumbia" to "Cumbia",
        "salsa" to "Salsa",
        "merengue" to "Merengue",
        "bachata" to "Bachata",
        "regional mexicano" to "Regional Mexicano",
        "corridos" to "Regional Mexicano",
        "norteno" to "Regional Mexicano",
        "lofi" to "Lo-fi",
        "lo fi" to "Lo-fi",
        "jazz" to "Jazz",
        "classical" to "Clásica",
        "clasica" to "Clásica",
        "acoustic" to "Acústico",
        "acustico" to "Acústico",
        "country" to "Country / Folk",
        "folk" to "Country / Folk",
        "reggae" to "Reggae",
        "balada" to "Balada / Romántica",
        "romantica" to "Balada / Romántica",
        "latina" to "Latina",
        "latino" to "Latina",
        "latin" to "Latina",
        "afrobeat" to "Afrobeat",
        "afrobeats" to "Afrobeat",
    )

    private val ARTIST_RULES = mapOf(
        "slipknot" to "Metal",
        "ghost" to "Metal",
        "metallica" to "Metal",
        "korn" to "Metal",
        "iron maiden" to "Metal",
        "rammstein" to "Metal",
        "powerwolf" to "Metal",
        "marilyn manson" to "Metal",
        "system of a down" to "Metal",
        "warcry" to "Metal",
        "xandria" to "Metal",
        "ozzy osbourne" to "Metal",
        "sabaton" to "Metal",
        "gwar" to "Metal",
        "angeles del infierno" to "Metal",
        "alcoholika" to "Metal",
        "quiet riot" to "Metal",
        "zarpa" to "Metal",
        "pantera" to "Metal",
        "megadeth" to "Metal",
        "judas priest" to "Metal",
        "black sabbath" to "Metal",
        "dream theater" to "Metal",
        "motley crue" to "Metal",
        "three days grace" to "Rock",
        "nirvana" to "Rock",
        "linkin park" to "Rock",
        "queen" to "Rock",
        "pxndx" to "Rock",
        "mago de oz" to "Rock",
        "ac dc" to "Rock",
        "acdc" to "Rock",
        "green day" to "Rock",
        "the rolling stones" to "Rock",
        "rolling stones" to "Rock",
        "red hot chili peppers" to "Rock",
        "chili peppers" to "Rock",
        "pink floyd" to "Rock",
        "fall out boy" to "Rock",
        "paramore" to "Rock",
        "guns n roses" to "Rock",
        "guns nu0027 roses" to "Rock",
        "imagine dragons" to "Rock",
        "twenty one pilots" to "Rock",
        "coldplay" to "Rock",
        "mana" to "Rock",
        "hombres g" to "Rock",
        "enanitos verdes" to "Rock",
        "los prisioneros" to "Rock",
        "el cuarteto de nos" to "Rock",
        "the rasmus" to "Rock",
        "airbag" to "Rock",
        "los rancheros" to "Rock",
        "george thorogood" to "Rock",
        "gearge thoro good" to "Rock",
        "muse" to "Indie / Alternativo",
        "radiohead" to "Indie / Alternativo",
        "arctic monkeys" to "Indie / Alternativo",
        "the strokes" to "Indie / Alternativo",
        "gorillaz" to "Indie / Alternativo",
        "foo fighters" to "Rock",
        "the killers" to "Rock",
        "the cure" to "Rock",
        "u2" to "Rock",
        "blink 182" to "Rock",
        "michael jackson" to "Pop",
        "adele" to "Pop",
        "morat" to "Pop",
        "reik" to "Pop",
        "camila" to "Pop",
        "the weeknd" to "R&B / Soul",
        "bruno mars" to "Pop",
        "ed sheeran" to "Pop",
        "taylor swift" to "Pop",
        "rihanna" to "Pop",
        "katy perry" to "Pop",
        "dua lipa" to "Pop",
        "shakira" to "Pop",
        "sin bandera" to "Pop",
        "beret" to "Pop",
        "juanes" to "Pop",
        "camilo" to "Pop",
        "sebastian yatra" to "Pop",
        "zack tabudlo" to "Pop",
        "jp cooper" to "Pop",
        "lauv" to "Pop",
        "avicii" to "Electrónica",
        "daft punk" to "Electrónica",
        "marshmello" to "Electrónica",
        "alan walker" to "Electrónica",
        "skrillex" to "Electrónica",
        "zedd" to "Electrónica",
        "calvin harris" to "Electrónica",
        "the chainsmokers" to "Electrónica",
        "bts" to "K-Pop",
        "blackpink" to "K-Pop",
        "zxtentation" to "Hip-Hop / Rap",
        "grupo 5" to "Salsa",
        "eddy herrera" to "Merengue",
        "cumbia chelera" to "Cumbia",
        "los tigres del norte" to "Regional Mexicano",
        "bad bunny" to "Reggaetón",
        "j balvin" to "Reggaetón",
        "ozuna" to "Reggaetón",
        "don omar" to "Reggaetón",
        "daddy yankee" to "Reggaetón",
        "karol g" to "Reggaetón",
        "maluma" to "Reggaetón",
        "anuel aa" to "Reggaetón",
        "romeo santos" to "Bachata",
        "prince royce" to "Bachata",
        "aventura" to "Bachata",
        "juan gabriel" to "Balada / Romántica",
        "jose jose" to "Balada / Romántica",
        "luis miguel" to "Balada / Romántica",
        "carlos vives" to "Latina",
    )

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
        "heavy metal" to "Metal",
        "nu metal" to "Metal",
        "metalcore" to "Metal",
        "screamo" to "Metal",
        "thrash" to "Metal",
        "death metal" to "Metal",
        "metal" to "Metal",
        "heavy" to "Metal",
        "hard rock" to "Rock",
        "classic rock" to "Rock",
        "rock and roll" to "Rock",
        "rock n roll" to "Rock",
        "grunge" to "Rock",
        "punk rock" to "Rock",
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
