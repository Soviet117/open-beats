package com.soviet117.openbeats.ui.data

import androidx.compose.ui.graphics.Color

data class Album(
    val name: String,
    val subtitle: String,
    val songs: List<Song>,
    val colors: List<Color>,
) {
    val isSynthetic: Boolean
        get() = subtitle == SYNTHETIC_SUBTITLE
}

data class LibraryArtist(
    val name: String,
    val songs: List<Song>,
    val colors: List<Color>,
)

private const val SYNTHETIC_SUBTITLE = "Sencillos"

fun deriveAlbums(songs: List<Song>): List<Album> {
    val groups = LinkedHashMap<String, MutableList<Song>>()
    val names = LinkedHashMap<String, String>()
    for (song in songs) {
        val synthetic = song.album.isBlank()
        val key = MetadataCleaner.normalizeKey(if (synthetic) song.artist else song.album)
        groups.getOrPut(key) { mutableListOf() }.add(song)
        if (key !in names) names[key] = if (synthetic) song.artist else song.album
    }
    return groups.map { (key, albumSongs) ->
        Album(
            name = names.getValue(key),
            subtitle = if (albumSongs.any { it.album.isBlank() }) SYNTHETIC_SUBTITLE else "Álbum",
            songs = albumSongs,
            colors = albumSongs.first().colors,
        )
    }.sortedBy { it.name.lowercase() }
}

fun deriveArtists(songs: List<Song>): List<LibraryArtist> {
    val groups = LinkedHashMap<String, MutableList<Song>>()
    val names = LinkedHashMap<String, String>()
    for (song in songs) {
        val key = MetadataCleaner.normalizeKey(song.artist)
        groups.getOrPut(key) { mutableListOf() }.add(song)
        if (key !in names) names[key] = song.artist
    }
    return groups.map { (key, artistSongs) ->
        LibraryArtist(
            name = names.getValue(key),
            songs = artistSongs,
            colors = artistSongs.first().colors,
        )
    }.sortedBy { it.name.lowercase() }
}

sealed interface LibraryTarget {
    data class AlbumTarget(val album: Album) : LibraryTarget
    data class ArtistTarget(val artist: LibraryArtist) : LibraryTarget
    data class GenreTarget(val genre: LibraryGenre) : LibraryTarget
}

data class LibraryGenre(
    val name: String,
    val songs: List<Song>,
    val colors: List<Color>,
)

fun deriveGenres(songs: List<Song>): List<LibraryGenre> {
    val groups = LinkedHashMap<String, MutableList<Song>>()
    val names = LinkedHashMap<String, String>()
    for (song in songs) {
        val genre = GenreDetector.normalizeGenre(song.genre) ?: song.genre.trim()
        if (genre.isBlank()) continue
        val key = MetadataCleaner.normalizeSearchKey(genre)
        if (key.isEmpty()) continue
        groups.getOrPut(key) { mutableListOf() }.add(song)
        if (key !in names) names[key] = genre
    }
    return groups.map { (key, genreSongs) ->
        LibraryGenre(
            name = names.getValue(key),
            songs = genreSongs,
            colors = genreColors(key),
        )
    }.sortedBy { it.name.lowercase() }
}

private val genrePalette = listOf(
    listOf(Color(0xFF7C3AED), Color(0xFFEC4899)),
    listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
    listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
    listOf(Color(0xFF10B981), Color(0xFF3B82F6)),
    listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
    listOf(Color(0xFFF97316), Color(0xFFF43F5E)),
    listOf(Color(0xFF14B8A6), Color(0xFF84CC16)),
    listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4)),
)

fun genreColors(key: String): List<Color> {
    val idx = (key.hashCode() and Int.MAX_VALUE) % genrePalette.size
    return genrePalette[idx]
}

fun songsLabel(count: Int): String = if (count == 1) "1 canción" else "$count canciones"
