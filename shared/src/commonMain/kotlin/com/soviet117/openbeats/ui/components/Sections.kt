package com.soviet117.openbeats.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.ui.data.Genre
import com.soviet117.openbeats.ui.data.Playlist
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.theme.BrandSoft
import com.soviet117.openbeats.ui.theme.SurfaceHigh
import com.soviet117.openbeats.ui.theme.TextPrimary
import com.soviet117.openbeats.ui.theme.TextSecondary

@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
                color = BrandSoft,
            )
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.width(150.dp).clickable(onClick = onClick),
    ) {
        Artwork(
            colors = playlist.colors,
            modifier = Modifier.size(150.dp),
            corner = 14.dp,
            noteSize = 38.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = playlist.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun GenreCard(
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(genre.colors))
            .height(96.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            text = genre.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.padding(14.dp),
        )
    }
}

@Composable
fun RecentTile(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    song: Song? = null,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceHigh)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (song != null) {
                SongArtwork(
                    song = song,
                    modifier = Modifier.size(56.dp),
                    corner = 8.dp,
                    noteSize = 22.dp,
                    solid = true,
                )
            } else {
                Artwork(
                    colors = playlist.colors,
                    modifier = Modifier.size(56.dp),
                    corner = 8.dp,
                    noteSize = 22.dp,
                    solid = true,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = song?.title ?: playlist.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 10.dp),
            )
        }
    }
}

@Composable
fun Chip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val background = if (selected) BrandSoft.copy(alpha = 0.18f) else SurfaceHigh
    val foreground = if (selected) BrandSoft else TextSecondary
    val border = if (selected) BrandSoft.copy(alpha = 0.55f) else Color.Transparent
    Surface(
        modifier = modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = background,
        border = BorderStroke(1.dp, border),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = foreground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}
