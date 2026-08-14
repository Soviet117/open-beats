package com.soviet117.openbeats

import androidx.compose.ui.graphics.Color
import com.soviet117.openbeats.ui.data.GenreDetector
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.data.deriveGenres
import com.soviet117.openbeats.ui.data.searchLibrary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchTest {

    private val violet = listOf(Color(0xFF7C3AED), Color(0xFFEC4899))

    private fun song(id: String, title: String, artist: String, album: String, genre: String = "") =
        Song(id, title, artist, album, 180_000, colors = violet, genre = genre)

    @Test
    fun searchBySongTitle() {
        val results = searchLibrary("thriller", listOf(song("1", "Thriller", "Michael Jackson", "Thriller")))
        assertEquals(1, results.songs.size)
        assertEquals("Thriller", results.songs.first().title)
    }

    @Test
    fun searchIsAccentInsensitive() {
        val songs = listOf(
            song("1", "Baila Conmigo", "Ritmo Solar", "Fuego Latino"),
            song("2", "Otra Cosa", "Otro", "Álbum"),
        )
        val byAccent = searchLibrary("baila", songs)
        assertEquals(listOf("Baila Conmigo"), byAccent.songs.map { it.title })

        val byArtist = searchLibrary("Ritmo", songs)
        assertEquals(listOf("Baila Conmigo"), byArtist.songs.map { it.title })

        val accentInQuery = searchLibrary("Reggaetón", listOf(song("1", "X", "Y", "Z", genre = "reggaetón")))
        assertEquals(1, accentInQuery.songs.size)
    }

    @Test
    fun searchByGenre() {
        val results = searchLibrary("lo-fi", listOf(song("1", "Focus", "A", "B", genre = "Lo-Fi")))
        assertEquals(1, results.songs.size)
    }

    @Test
    fun searchMatchesAlbumAndArtist() {
        val songs = listOf(
            song("1", "Billie Jean", "Michael Jackson", "Thriller"),
            song("2", "Beat It", "Michael Jackson", "Thriller"),
            song("3", "Mamma Mia", "ABBA", "Arrival"),
        )
        val byAlbum = searchLibrary("thriller", songs)
        assertEquals(2, byAlbum.songs.size)

        val byArtist = searchLibrary("michael", songs)
        assertEquals(2, byArtist.songs.size)
        assertEquals(1, byArtist.artists.size)
        assertEquals("Michael Jackson", byArtist.artists.first().name)

        val byAlbumName = searchLibrary("arrival", songs)
        assertEquals(1, byAlbumName.albums.size)
        assertEquals("Arrival", byAlbumName.albums.first().name)
    }

    @Test
    fun searchRequiresAllTokensForASong() {
        val results = searchLibrary("michael queen", listOf(song("1", "A", "Michael Jackson", "X")))
        assertTrue(results.songs.isEmpty())
    }

    @Test
    fun emptyOrBlankQueryReturnsEmptyResults() {
        assertTrue(searchLibrary("", listOf(song("1", "A", "B", "C"))).isEmpty)
        assertTrue(searchLibrary("   ", listOf(song("1", "A", "B", "C"))).isEmpty)
    }

    @Test
    fun deriveGenresGroupsByNormalizedName() {
        val genres = deriveGenres(
            listOf(
                song("1", "A", "X", "Y", genre = "Reggaetón"),
                song("2", "B", "X", "Y", genre = "Reggaetón"),
                song("3", "C", "Z", "W", genre = "reggaetón"),
                song("4", "D", "P", "Q", genre = "Rock"),
            ),
        )
        assertEquals(2, genres.size)
        val reggaeton = genres.first { it.songs.size == 3 }
        assertEquals(3, reggaeton.songs.size)
        val rock = genres.first { it.name == "Rock" }
        assertEquals(1, rock.songs.size)
    }

    @Test
    fun deriveGenresSkipsBlankAndUnknown() {
        val genres = deriveGenres(
            listOf(
                song("1", "A", "X", "Y", genre = ""),
                song("2", "B", "X", "Y", genre = "   "),
            ),
        )
        assertTrue(genres.isEmpty())
    }

    @Test
    fun deriveGenresColorsAreDeterministic() {
        val genres = deriveGenres(listOf(song("1", "A", "X", "Y", genre = "Jazz")))
        val again = deriveGenres(listOf(song("1", "A", "X", "Y", genre = "Jazz")))
        assertEquals(genres.first().colors, again.first().colors)
    }

    @Test
    fun genreDetectorFindsKnownKeywords() {
        assertEquals("Lo-fi", GenreDetector.detect("Lo-fi Beats To Study To", "Some Artist"))
        assertEquals("Rock", GenreDetector.detect("Rock N' Roll Star", ""))
        assertEquals("K-Pop", GenreDetector.detect("Dynamite", "BTS", "K-Pop Hits"))
        assertEquals("Trap", GenreDetector.detect("Trap Queen", "Fetty Wap"))
        assertEquals("Regional Mexicano", GenreDetector.detect("", "Los Tigres del Norte", "Corridos"))
    }

    @Test
    fun genreDetectorIsAccentInsensitive() {
        assertEquals("Reggaetón", GenreDetector.detect("Corazón de Reggaetón"))
        assertEquals("Clásica", GenreDetector.detect("Piano Clásico"))
    }

    @Test
    fun genreDetectorReturnsNullWhenNoMatch() {
        assertEquals(null, GenreDetector.detect("Something Very Ordinary", "Random Artist"))
    }
}
