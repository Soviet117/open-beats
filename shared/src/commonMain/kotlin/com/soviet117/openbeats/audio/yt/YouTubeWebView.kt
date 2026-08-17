package com.soviet117.openbeats.audio.yt

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun YouTubeWebView(
    videoId: String,
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {},
    onStateChange: (Int) -> Unit = {},
    onError: (Int) -> Unit = {},
)
