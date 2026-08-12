package com.soviet117.openbeats.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.audio.LocalAudioLibrary
import com.soviet117.openbeats.audio.decodeImage
import com.soviet117.openbeats.ui.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SongArtwork(
    song: Song,
    modifier: Modifier = Modifier,
    corner: Dp = 12.dp,
    noteSize: Dp = 26.dp,
) {
    val library = LocalAudioLibrary.current
    var bitmap by remember(song.id) { mutableStateOf(decodeImage(song.artwork)) }
    LaunchedEffect(song.id, library) {
        if (bitmap == null && song.artwork == null && library != null) {
            val bytes = library.loadArtwork(song.id)
            if (bytes != null) {
                bitmap = withContext(Dispatchers.Default) { decodeImage(bytes) }
            }
        }
    }
    val artworkBitmap = bitmap
    if (artworkBitmap != null) {
        Image(
            bitmap = artworkBitmap,
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(corner)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Artwork(
            colors = song.colors,
            modifier = modifier,
            corner = corner,
            noteSize = noteSize,
        )
    }
}

@Composable
fun Artwork(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    corner: Dp = 12.dp,
    noteSize: Dp = 26.dp,
    noteAlpha: Float = 0.45f,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = noteAlpha),
            modifier = Modifier.size(noteSize),
        )
    }
}

@Composable
fun Avatar(
    name: String,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(1),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}
