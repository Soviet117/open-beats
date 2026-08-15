package com.soviet117.openbeats.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soviet117.openbeats.audio.RepeatMode
import com.soviet117.openbeats.ui.components.PlayButton
import com.soviet117.openbeats.ui.components.SongArtwork
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.data.formatDuration
import com.soviet117.openbeats.ui.theme.BrandViolet
import com.soviet117.openbeats.ui.theme.Obsidian
import com.soviet117.openbeats.ui.theme.SurfaceHigh
import com.soviet117.openbeats.ui.theme.TextMuted
import com.soviet117.openbeats.ui.theme.TextPrimary
import com.soviet117.openbeats.ui.theme.TextSecondary

@Composable
fun NowPlayingScreen(
    song: Song,
    playing: Boolean,
    isLiked: Boolean,
    positionMs: Long = 0L,
    durationMs: Long = song.durationMs,
    shuffle: Boolean = false,
    repeatMode: RepeatMode = RepeatMode.OFF,
    queue: List<Song> = emptyList(),
    currentIndex: Int = 0,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onTogglePlay: () -> Unit = {},
    onToggleLike: () -> Unit = {},
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    onSkipToIndex: (Int) -> Unit = {},
) {
    var showQueue by remember { mutableStateOf(false) }
    val backdrop = Brush.verticalGradient(
        listOf(song.colors.first().copy(alpha = 0.55f), Obsidian, Obsidian),
    )
    val sliderDuration = durationMs.coerceAtLeast(1L).toFloat()
    val sliderPosition = positionMs.coerceIn(0L, durationMs.coerceAtLeast(1L)).toFloat()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backdrop)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {}
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextPrimary,
                    )
                }
                Text(
                    text = "EN REPRODUCCIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showQueue = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Ver cola",
                        tint = TextPrimary,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            SongArtwork(
                song = song,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                corner = 24.dp,
                noteSize = 72.dp,
            )
            Spacer(Modifier.height(36.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onToggleLike) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) BrandViolet else TextSecondary,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Slider(
                value = sliderPosition,
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..sliderDuration,
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = BrandViolet,
                    inactiveTrackColor = SurfaceHigh,
                ),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formatDuration(positionMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatDuration(durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = null,
                        tint = if (shuffle) BrandViolet else TextMuted,
                        modifier = Modifier.size(22.dp),
                    )
                }
                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(42.dp),
                    )
                }
                PlayButton(
                    size = 76.dp,
                    playing = playing,
                    onClick = onTogglePlay,
                )
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(42.dp),
                    )
                }
                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        imageVector = if (repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = null,
                        tint = if (repeatMode != RepeatMode.OFF) BrandViolet else TextMuted,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Devices,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Escuchar en este dispositivo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showQueue = true }) {
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = "Ver cola",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }

    if (showQueue) {
        QueueSheet(
            queue = queue,
            currentIndex = currentIndex,
            onDismiss = { showQueue = false },
            onSkipToIndex = { index ->
                onSkipToIndex(index)
                showQueue = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueueSheet(
    queue: List<Song>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onSkipToIndex: (Int) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceHigh,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = "Cola de reproducción",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            if (queue.isEmpty()) {
                Text(
                    text = "La cola está vacía",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 520.dp)) {
                    itemsIndexed(queue, key = { _, song -> song.id }) { index, song ->
                        QueueRow(
                            song = song,
                            index = index,
                            isCurrent = index == currentIndex,
                            onClick = { onSkipToIndex(index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    song: Song,
    index: Int,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(26.dp), contentAlignment = Alignment.Center) {
            if (isCurrent) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = BrandViolet,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        SongArtwork(
            song = song,
            modifier = Modifier.size(40.dp),
            corner = 8.dp,
            noteSize = 16.dp,
            solid = true,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isCurrent) BrandViolet else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = formatDuration(song.durationMs),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
        )
    }
}
