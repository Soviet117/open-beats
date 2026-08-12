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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.ui.components.PlaylistCard
import com.soviet117.openbeats.ui.components.RecentTile
import com.soviet117.openbeats.ui.components.SectionHeader
import com.soviet117.openbeats.ui.components.SongRow
import com.soviet117.openbeats.ui.data.Mock
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.theme.BrandGradient
import com.soviet117.openbeats.ui.theme.BrandViolet
import com.soviet117.openbeats.ui.theme.TextPrimary
import com.soviet117.openbeats.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    songs: List<Song>,
    onPlay: (Song) -> Unit,
    loading: Boolean = false,
    modifier: Modifier = Modifier,
) {
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
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Mock.recentTiles.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowItems.forEach { playlist ->
                            RecentTile(
                                playlist = playlist,
                                modifier = Modifier.weight(1f),
                                onClick = { if (songs.isNotEmpty()) onPlay(songs.first()) },
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
        item {
            SectionHeader(title = "Hecho para ti", action = "Ver todo")
        }
        item {
            Spacer(Modifier.height(12.dp))
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(Mock.playlists.take(6)) { playlist ->
                    PlaylistCard(playlist = playlist)
                }
            }
        }
        item {
            Spacer(Modifier.height(28.dp))
        }
        item {
            SectionHeader(title = "Lo más escuchado", action = "Ver todo")
        }
        item {
            Spacer(Modifier.height(4.dp))
        }
        itemsIndexed(songs) { index, song ->
            SongRow(
                song = song,
                index = index + 1,
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick = { onPlay(song) },
            )
        }
    }
}
