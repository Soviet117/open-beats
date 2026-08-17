package com.soviet117.openbeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.audio.yt.YouTubeSearchSource
import com.soviet117.openbeats.ui.components.ArtistRow
import com.soviet117.openbeats.ui.components.Chip
import com.soviet117.openbeats.ui.components.GenreCard
import com.soviet117.openbeats.ui.components.PlaylistCard
import com.soviet117.openbeats.ui.components.SectionHeader
import com.soviet117.openbeats.ui.components.SongRow
import com.soviet117.openbeats.ui.data.Album
import com.soviet117.openbeats.ui.data.Genre
import com.soviet117.openbeats.ui.data.LibraryArtist
import com.soviet117.openbeats.ui.data.LibraryGenre
import com.soviet117.openbeats.ui.data.Mock
import com.soviet117.openbeats.ui.data.Playlist
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.data.deriveGenres
import com.soviet117.openbeats.ui.data.searchLibrary
import com.soviet117.openbeats.ui.theme.BrandSoft
import com.soviet117.openbeats.ui.theme.BrandViolet
import com.soviet117.openbeats.ui.theme.SurfaceHigh
import com.soviet117.openbeats.ui.theme.TextPrimary
import com.soviet117.openbeats.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    songs: List<Song> = Mock.songs,
    likedIds: Set<String> = emptySet(),
    onPlay: (List<Song>, Int) -> Unit = { _, _ -> },
    onPlayYouTube: (Song) -> Unit = {},
    onToggleLike: (String) -> Unit = {},
    loading: Boolean = false,
    onOpenAlbum: (Album) -> Unit = {},
    onOpenArtist: (LibraryArtist) -> Unit = {},
    onOpenGenre: (LibraryGenre) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val genres = remember(songs) { deriveGenres(songs) }
    var query by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var youtubeResults by remember { mutableStateOf(emptyList<Song>()) }
    var youtubeLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val youtubeSource = remember { YouTubeSearchSource() }

    LaunchedEffect(query) {
        delay(250)
        debouncedQuery = query
    }

    LaunchedEffect(debouncedQuery) {
        if (debouncedQuery.isBlank()) {
            youtubeResults = emptyList()
            return@LaunchedEffect
        }
        youtubeLoading = true
        youtubeResults = try {
            youtubeSource.search(debouncedQuery)
        } catch (_: Exception) {
            emptyList()
        }
        youtubeLoading = false
    }

    val results = remember(debouncedQuery, songs) { searchLibrary(debouncedQuery, songs) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Buscar",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(16.dp))
            SearchField(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(16.dp))
        }

        if (loading && songs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = BrandViolet,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            return@LazyColumn
        }

        if (debouncedQuery.isBlank()) {
            if (genres.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(genres) { _, genre ->
                            Chip(
                                text = genre.name,
                                selected = false,
                                onClick = { onOpenGenre(genre) },
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(28.dp))
                SectionHeader(title = "Explora por género")
            }
            item {
                Spacer(Modifier.height(12.dp))
            }
            if (genres.isEmpty()) {
                item {
                    Text(
                        text = "Aún no hay géneros detectados en tu biblioteca",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        genres.chunked(2).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowItems.forEach { genre ->
                                    GenreCard(
                                        genre = Genre(genre.name, genre.colors),
                                        modifier = Modifier.weight(1f),
                                        onClick = { onOpenGenre(genre) },
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(20.dp))
            }
        } else {
            if (results.songs.isNotEmpty()) {
                item {
                    SectionHeader(title = "Canciones")
                }
                itemsIndexed(results.songs, key = { _, song -> "res-${song.id}" }) { index, song ->
                    SongRow(
                        song = song,
                        index = index + 1,
                        isLiked = song.id in likedIds,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        onClick = {
                            val queueIndex = songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                            onPlay(songs, queueIndex)
                        },
                        onToggleLike = { onToggleLike(song.id) },
                    )
                }
                item {
                    Spacer(Modifier.height(28.dp))
                }
            }
            if (results.albums.isNotEmpty()) {
                item {
                    SectionHeader(title = "Álbumes")
                }
                item {
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        results.albums.chunked(2).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                rowItems.forEach { album ->
                                    PlaylistCard(
                                        playlist = Playlist(
                                            id = album.name.hashCode(),
                                            name = album.name,
                                            subtitle = album.subtitle,
                                            songCount = album.songs.size,
                                            colors = album.colors,
                                        ),
                                        onClick = { onOpenAlbum(album) },
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(28.dp))
                }
            }
            if (results.artists.isNotEmpty()) {
                item {
                    SectionHeader(title = "Artistas")
                }
                item {
                    Spacer(Modifier.height(4.dp))
                }
                itemsIndexed(results.artists, key = { _, artist -> "res-art-${artist.name}" }) { _, artist ->
                    ArtistRow(
                        artist = artist,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        onClick = { onOpenArtist(artist) },
                    )
                }
                item {
                    Spacer(Modifier.height(28.dp))
                }
            }

            if (youtubeResults.isNotEmpty() || youtubeLoading) {
                item {
                    SectionHeader(title = "YouTube Music")
                }
                item {
                    Spacer(Modifier.height(4.dp))
                }
                if (youtubeLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = BrandViolet,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                } else {
                    itemsIndexed(youtubeResults, key = { _, song -> "yt-${song.id}" }) { index, song ->
                        YouTubeSongRow(
                            song = song,
                            index = index + 1,
                            modifier = Modifier.padding(horizontal = 20.dp),
                            onClick = { onPlayYouTube(song) },
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(28.dp))
                }
            }

            if (results.isEmpty && youtubeResults.isEmpty() && !youtubeLoading) {
                item {
                    Text(
                        text = "Sin resultados para \"$debouncedQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun YouTubeSongRow(
    song: Song,
    index: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceHigh,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color(0x1AFF0000),
                        shape = RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "YT",
                    style = MaterialTheme.typography.labelSmall,
                    color = androidx.compose.ui.graphics.Color(0xFFFF0000),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                )
            }
            if (song.durationMs > 0) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = com.soviet117.openbeats.ui.data.formatDuration(song.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                cursorBrush = SolidColor(BrandSoft),
                singleLine = true,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "¿Qué quieres escuchar?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (query.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Limpiar búsqueda",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
