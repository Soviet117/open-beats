package com.soviet117.openbeats.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.ui.components.ArtistRow
import com.soviet117.openbeats.ui.components.Chip
import com.soviet117.openbeats.ui.components.LibraryRow
import com.soviet117.openbeats.ui.components.PlaylistCard
import com.soviet117.openbeats.ui.components.SectionHeader
import com.soviet117.openbeats.ui.data.Mock
import com.soviet117.openbeats.ui.theme.BrandSoft
import com.soviet117.openbeats.ui.theme.TextPrimary
import com.soviet117.openbeats.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
) {
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
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null,
                    tint = TextSecondary,
                )
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Chip(text = "Playlists", selected = true)
            Chip(text = "Artistas", selected = false)
            Chip(text = "Álbumes", selected = false)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {}
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = BrandSoft,
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = "Crear nueva playlist",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
            )
        }
        Spacer(Modifier.height(4.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        ) {
            items(Mock.playlists) { playlist ->
                LibraryRow(
                    playlist = playlist,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                Spacer(Modifier.height(20.dp))
                SectionHeader(title = "Artistas")
                Spacer(Modifier.height(4.dp))
            }
            items(Mock.artists) { artist ->
                ArtistRow(
                    artist = artist,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                Spacer(Modifier.height(20.dp))
                SectionHeader(title = "Álbumes")
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(Mock.playlists.takeLast(4)) { album ->
                        PlaylistCard(playlist = album)
                    }
                }
            }
        }
    }
}
