package com.soviet117.openbeats.ui.data

import androidx.compose.ui.graphics.Color

data class Song(
    val id: Int,
    val title: String,
    val artist: String,
    val durationSec: Int,
    val colors: List<Color>,
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

fun formatDuration(sec: Int): String {
    val minutes = sec / 60
    val seconds = sec % 60
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
        Song(1, "Noche de Cristal", "Luna Radiante", 222, cVioletPink),
        Song(2, "Midnight Drive", "Neon Harbor", 198, cCyanBlue),
        Song(3, "Baila Conmigo", "Ritmo Solar", 185, cAmberRed),
        Song(4, "Electric Dreams", "Nova Wave", 241, cPinkViolet),
        Song(5, "Café en Domingo", "Los Buenos Días", 167, cTealLime),
        Song(6, "Gravity", "Astra Nova", 213, cVioletCyan),
        Song(7, "Olas del Mar", "Brisa Tropical", 236, cEmeraldBlue),
        Song(8, "City Lights", "Neon Harbor", 179, cCyanBlue),
        Song(9, "Corazón Rebelde", "Luna Radiante", 201, cOrangeRose),
        Song(10, "Pixel Hearts", "Nova Wave", 191, cPinkViolet),
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
