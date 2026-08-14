package com.soviet117.openbeats.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.soviet117.openbeats.currentHour
import com.soviet117.openbeats.ui.components.RecentTile
import com.soviet117.openbeats.ui.components.SectionHeader
import com.soviet117.openbeats.ui.components.SongRow
import com.soviet117.openbeats.ui.data.Playlist
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.data.greetingForHour
import com.soviet117.openbeats.ui.theme.BrandSoft
import com.soviet117.openbeats.ui.theme.BrandViolet
import com.soviet117.openbeats.ui.theme.SurfaceHigh
import com.soviet117.openbeats.ui.theme.TextMuted
import com.soviet117.openbeats.ui.theme.TextPrimary
import com.soviet117.openbeats.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import open_beats.shared.generated.resources.Res
import open_beats.shared.generated.resources.ic_ob_logo

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
    appVersion: String? = null,
    recents: List<Song>? = null,
    modifier: Modifier = Modifier,
) {
    val albums = remember(songs) { deriveAlbums(songs) }
    val recentAlbums = if (recents == null) albums.take(4) else emptyList()
    val recentSongs = recents.orEmpty().take(4)
    val favorites = remember(songs, likedIds) { songs.filter { it.id in likedIds } }
    var showAbout by remember { mutableStateOf(false) }
    val greeting = remember { greetingForHour(currentHour()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item {
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_ob_logo),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Open Beats",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showAbout = true },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = "Acerca de Open Beats",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = greeting,
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
        if (recentAlbums.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    recentAlbums.chunked(2).forEach { rowItems ->
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
        if (recentSongs.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    recentSongs.chunked(2).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowItems.forEach { song ->
                                RecentTile(
                                    playlist = Playlist(
                                        id = song.id.hashCode(),
                                        name = song.title,
                                        subtitle = song.artist,
                                        songCount = 1,
                                        colors = song.colors,
                                    ),
                                    song = song,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val queueIndex = songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
                                        onPlay(songs, queueIndex)
                                    },
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

    if (showAbout) {
        AboutDialog(appVersion = appVersion, onDismiss = { showAbout = false })
    }
}

@Composable
private fun AboutDialog(
    appVersion: String?,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceHigh),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_ob_logo),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Open Beats",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Reproductor de música local, libre de anuncios y open source.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                if (appVersion != null) {
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Code,
                            contentDescription = null,
                            tint = BrandSoft,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Versión $appVersion",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = BrandSoft)
                }
            }
        }
    }
}
