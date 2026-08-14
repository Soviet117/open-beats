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
        names.putIfAbsent(key, if (synthetic) song.artist else song.album)
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
        names.putIfAbsent(key, song.artist)
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
}

fun songsLabel(count: Int): String = if (count == 1) "1 canción" else "$count canciones"
