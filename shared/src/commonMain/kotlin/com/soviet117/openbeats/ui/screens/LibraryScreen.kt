package com.soviet117.openbeats.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.ui.components.ArtistRow
import com.soviet117.openbeats.ui.components.Chip
import com.soviet117.openbeats.ui.components.PlaylistCard
import com.soviet117.openbeats.ui.components.SongRow
import com.soviet117.openbeats.ui.data.LibraryArtist
import com.soviet117.openbeats.ui.data.Album
import com.soviet117.openbeats.ui.data.Mock
import com.soviet117.openbeats.ui.data.Playlist
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.data.deriveAlbums
import com.soviet117.openbeats.ui.data.deriveArtists
import com.soviet117.openbeats.ui.theme.BrandViolet
import com.soviet117.openbeats.ui.theme.TextPrimary
import com.soviet117.openbeats.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    songs: List<Song> = Mock.songs,
    likedIds: Set<String> = emptySet(),
    onPlay: (List<Song>, Int) -> Unit = { _, _ -> },
    onToggleLike: (String) -> Unit = {},
    loading: Boolean = false,
    onOpenSearch: () -> Unit = {},
    onOpenAlbum: (Album) -> Unit = {},
    onOpenArtist: (LibraryArtist) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val albums = remember(songs) { deriveAlbums(songs) }
    val artists = remember(songs) { deriveArtists(songs) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Tu biblioteca",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenSearch) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Buscar",
                    tint = TextSecondary,
                )
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Chip(text = "Canciones", selected = selectedTab == 0, onClick = { selectedTab = 0 })
            Chip(text = "Álbumes", selected = selectedTab == 1, onClick = { selectedTab = 1 })
            Chip(text = "Artistas", selected = selectedTab == 2, onClick = { selectedTab = 2 })
        }
        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (loading && songs.isEmpty()) {
                CircularProgressIndicator(
                    color = BrandViolet,
                    modifier = Modifier.size(32.dp).align(Alignment.Center),
                )
            } else {
                when (selectedTab) {
                    0 -> SongsTab(songs, likedIds, onPlay, onToggleLike)
                    1 -> AlbumsTab(albums, onOpenAlbum)
                    else -> ArtistsTab(artists, onOpenArtist)
                }
            }
        }
    }
}

@Composable
private fun SongsTab(
    songs: List<Song>,
    likedIds: Set<String>,
    onPlay: (List<Song>, Int) -> Unit,
    onToggleLike: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
    ) {
        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
            SongRow(
                song = song,
                index = index + 1,
                isLiked = song.id in likedIds,
                onClick = { onPlay(songs, index) },
                onToggleLike = { onToggleLike(song.id) },
            )
        }
    }
}

@Composable
private fun AlbumsTab(
    albums: List<Album>,
    onOpenAlbum: (Album) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
    ) {
        itemsIndexed(albums.chunked(2)) { _, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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

@Composable
private fun ArtistsTab(
    artists: List<LibraryArtist>,
    onOpenArtist: (LibraryArtist) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
    ) {
        itemsIndexed(artists) { index, artist ->
            ArtistRow(
                artist = artist,
                onClick = { onOpenArtist(artist) },
            )
        }
    }
}
