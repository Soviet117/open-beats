package com.soviet117.openbeats.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.ui.components.Artwork
import com.soviet117.openbeats.ui.components.PlayButton
import com.soviet117.openbeats.ui.components.SongRow
import com.soviet117.openbeats.ui.data.LibraryGenre
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.data.songsLabel
import com.soviet117.openbeats.ui.theme.TextPrimary
import com.soviet117.openbeats.ui.theme.TextSecondary

@Composable
fun GenreDetailScreen(
    genre: LibraryGenre,
    likedIds: Set<String>,
    onBack: () -> Unit,
    onPlay: (List<Song>, Int) -> Unit,
    onToggleLike: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary,
                        )
                    }
                }
                Artwork(
                    colors = genre.colors,
                    modifier = Modifier.size(160.dp),
                    corner = 32.dp,
                    noteSize = 56.dp,
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = genre.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = songsLabel(genre.songs.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(20.dp))
                PlayButton(
                    size = 64.dp,
                    playing = false,
                    onClick = { onPlay(genre.songs, 0) },
                )
                Spacer(Modifier.height(12.dp))
            }
        }
        itemsIndexed(genre.songs, key = { _, song -> song.id }) { index, song ->
            SongRow(
                song = song,
                index = index + 1,
                isLiked = song.id in likedIds,
                onClick = { onPlay(genre.songs, index) },
                onToggleLike = { onToggleLike(song.id) },
            )
        }
    }
}
