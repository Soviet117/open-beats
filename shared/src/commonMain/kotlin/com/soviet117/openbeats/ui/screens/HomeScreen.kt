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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.ui.components.RecentTile
import com.soviet117.openbeats.ui.components.SectionHeader
import com.soviet117.openbeats.ui.components.SongRow
import com.soviet117.openbeats.ui.data.Playlist
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.theme.BrandGradient
import com.soviet117.openbeats.ui.theme.BrandViolet
import com.soviet117.openbeats.ui.theme.TextPrimary
import com.soviet117.openbeats.ui.theme.TextSecondary

private data class Album(
    val playlist: Playlist,
    val songs: List<Song>,
)

private fun deriveAlbums(songs: List<Song>): List<Album> {
    val grouped = LinkedHashMap<String, MutableList<Song>>()
    for (song in songs) {
        grouped.getOrPut(song.album) { mutableListOf() }.add(song)
    }
    return grouped.map { (name, albumSongs) ->
        Album(
            playlist = Playlist(
                id = name.hashCode(),
                name = name,
                subtitle = "Álbum",
                songCount = albumSongs.size,
                colors = albumSongs.first().colors,
            ),
            songs = albumSongs,
        )
    }
}

@Composable
fun HomeScreen(
    songs: List<Song>,
    likedIds: Set<String>,
    onPlay: (List<Song>, Int) -> Unit,
    onToggleLike: (String) -> Unit,
    loading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val albums = remember(songs) { deriveAlbums(songs) }
    val recent = albums.take(4)
    val favorites = remember(songs, likedIds) { songs.filter { it.id in likedIds } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(BrandGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Open Beats",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Buenas noches",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Empieza donde te quedaste",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(20.dp))
        }
        if (loading) {
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
        if (songs.isEmpty()) {
            item {
                Text(
                    text = "Aún no hay canciones en tu dispositivo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
            return@LazyColumn
        }
        if (recent.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    recent.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowItems.forEach { album ->
                                RecentTile(
                                    playlist = album.playlist,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onPlay(album.songs, 0) },
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
            Spacer(Modifier.height(28.dp))
        }
        if (favorites.isNotEmpty()) {
            item {
                SectionHeader(title = "Tus favoritas")
            }
            item {
                Spacer(Modifier.height(4.dp))
            }
            itemsIndexed(favorites, key = { _, song -> "fav-${song.id}" }) { index, song ->
                SongRow(
                    song = song,
                    index = index + 1,
                    isLiked = true,
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
        item {
            SectionHeader(title = "Tus músicas")
        }
        item {
            Spacer(Modifier.height(4.dp))
        }
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            SongRow(
                song = song,
                index = index + 1,
                isLiked = song.id in likedIds,
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick = { onPlay(songs, index) },
                onToggleLike = { onToggleLike(song.id) },
            )
        }
    }
}
