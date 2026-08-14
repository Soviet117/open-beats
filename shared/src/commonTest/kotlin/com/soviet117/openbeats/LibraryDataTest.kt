package com.soviet117.openbeats

import androidx.compose.ui.graphics.Color
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.data.deriveAlbums
import com.soviet117.openbeats.ui.data.deriveArtists
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryDataTest {

    private val violet = listOf(Color(0xFF7C3AED), Color(0xFFEC4899))

    private fun song(id: String, title: String, artist: String, album: String) =
        Song(id, title, artist, album, 180_000, colors = violet)

    @Test
    fun albumsGroupByAlbumName() {
        val albums = deriveAlbums(
            listOf(
                song("1", "Thriller", "Michael Jackson", "Thriller"),
                song("2", "Billie Jean", "Michael Jackson", "Thriller"),
                song("3", "Beat It", "Michael Jackson", "Thriller"),
                song("4", "Bohemian Rhapsody", "Queen", "A Night at the Opera"),
            ),
        )
        assertEquals(2, albums.size)
        val thriller = albums.first { it.name == "Thriller" }
        assertEquals("Álbum", thriller.subtitle)
        assertEquals(3, thriller.songs.size)
    }

    @Test
    fun songsWithoutAlbumGroupByArtistAsSencillos() {
        val albums = deriveAlbums(
            listOf(
                song("1", "Song A", "Artist X", ""),
                song("2", "Song B", "Artist X", ""),
                song("3", "Song C", "Artist Y", ""),
            ),
        )
        assertEquals(2, albums.size)
        val sencillos = albums.first { it.name == "Artist X" }
        assertEquals("Sencillos", sencillos.subtitle)
        assertEquals(2, sencillos.songs.size)
    }

    @Test
    fun artistNamesAreMergedWhenOnlyCaseDiffers() {
        val artists = deriveArtists(
            listOf(
                song("1", "A", "Michael Jackson", "Thriller"),
                song("2", "B", "michael jackson", "Bad"),
            ),
        )
        assertEquals(1, artists.size)
        assertEquals(2, artists.first().songs.size)
    }

    @Test
    fun artistsGroupByArtist() {
        val artists = deriveArtists(
            listOf(
                song("1", "A", "Queen", "A"),
                song("2", "B", "Queen", "B"),
                song("3", "C", "ABBA", "C"),
            ),
        )
        assertEquals(2, artists.size)
        val queen = artists.first { it.name == "Queen" }
        assertEquals(2, queen.songs.size)
    }

    @Test
    fun albumsAreSortedAlphabetically() {
        val albums = deriveAlbums(
            listOf(
                song("1", "A", "X", "Zeta"),
                song("2", "B", "X", "Alpha"),
            ),
        )
        assertEquals(listOf("Alpha", "Zeta"), albums.map { it.name })
    }
}
