package com.soviet117.openbeats.ui.data

import androidx.compose.ui.graphics.Color

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artwork: ByteArray? = null,
    val colors: List<Color>,
    val genre: String = "",
)

data class Playlist(
    val id: Int,
    val name: String,
    val subtitle: String,
    val songCount: Int,
    val colors: List<Color>,
)

data class Artist(
    val id: Int,
    val name: String,
    val genre: String,
    val colors: List<Color>,
)

data class Genre(
    val name: String,
    val colors: List<Color>,
)

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

object Mock {

    private val cVioletPink = listOf(Color(0xFF7C3AED), Color(0xFFEC4899))
    private val cCyanBlue = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    private val cAmberRed = listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
    private val cEmeraldBlue = listOf(Color(0xFF10B981), Color(0xFF3B82F6))
    private val cPinkViolet = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
    private val cOrangeRose = listOf(Color(0xFFF97316), Color(0xFFF43F5E))
    private val cTealLime = listOf(Color(0xFF14B8A6), Color(0xFF84CC16))
    private val cVioletCyan = listOf(Color(0xFF8B5CF6), Color(0xFF06B6D4))

    val songs = listOf(
        Song("mock-1", "Noche de Cristal", "Luna Radiante", "Éxitos de Luna", 222_000, colors = cVioletPink, genre = "Pop"),
        Song("mock-2", "Midnight Drive", "Neon Harbor", "Horizonte Sintético", 198_000, colors = cCyanBlue, genre = "Synthwave"),
        Song("mock-3", "Baila Conmigo", "Ritmo Solar", "Fuego Latino", 185_000, colors = cAmberRed, genre = "Reggaetón"),
        Song("mock-4", "Electric Dreams", "Nova Wave", "Puro Pop", 241_000, colors = cPinkViolet, genre = "Electrónica"),
        Song("mock-5", "Café en Domingo", "Los Buenos Días", "Momentos Suaves", 167_000, colors = cTealLime, genre = "Acústico"),
        Song("mock-6", "Gravity", "Astra Nova", "Neón en la Noche", 213_000, colors = cVioletCyan, genre = "Electrónica"),
        Song("mock-7", "Olas del Mar", "Brisa Tropical", "Éxitos del Verano", 236_000, colors = cEmeraldBlue, genre = "Reggae"),
        Song("mock-8", "City Lights", "Neon Harbor", "Horizonte Sintético", 179_000, colors = cCyanBlue, genre = "Synthwave"),
        Song("mock-9", "Corazón Rebelde", "Luna Radiante", "Melodías del Alma", 201_000, colors = cOrangeRose, genre = "Rock"),
        Song("mock-10", "Pixel Hearts", "Nova Wave", "Puro Pop", 191_000, colors = cPinkViolet, genre = "Pop"),
    )

    val playlists = listOf(
        Playlist(1, "Éxitos del Verano", "Playlist", 52, cAmberRed),
        Playlist(2, "Chill & Lo-fi", "Playlist", 38, cTealLime),
        Playlist(3, "Neón en la Noche", "Playlist", 44, cCyanBlue),
        Playlist(4, "Fuego Latino", "Playlist", 61, cOrangeRose),
        Playlist(5, "Momentos Suaves", "Playlist", 27, cEmeraldBlue),
        Playlist(6, "Horizonte Sintético", "Álbum", 10, cVioletCyan),
        Playlist(7, "Melodías del Alma", "Playlist", 33, cVioletPink),
        Playlist(8, "Puro Pop", "Playlist", 45, cPinkViolet),
    )

    val artists = listOf(
        Artist(1, "Luna Radiante", "Pop", cVioletPink),
        Artist(2, "Neon Harbor", "Synthwave", cCyanBlue),
        Artist(3, "Nova Wave", "Electropop", cPinkViolet),
        Artist(4, "Ritmo Solar", "Reggaetón", cAmberRed),
        Artist(5, "Brisa Tropical", "Tropical House", cEmeraldBlue),
        Artist(6, "Los Buenos Días", "Acústico", cTealLime),
    )

    val genres = listOf(
        Genre("Pop", cVioletPink),
        Genre("Reggaetón", cAmberRed),
        Genre("Synthwave", cCyanBlue),
        Genre("Lo-fi", cTealLime),
        Genre("Acústico", cEmeraldBlue),
        Genre("Electrónica", cVioletCyan),
        Genre("Tropical", cOrangeRose),
        Genre("Alternativo", cPinkViolet),
    )

    val recentTiles = playlists.take(6)

    val searchChips = listOf("Música", "Pop", "Synthwave", "Lo-fi", "Reggaetón", "Acústico")
}
