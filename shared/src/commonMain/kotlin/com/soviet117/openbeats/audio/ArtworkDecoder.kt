package com.soviet117.openbeats.audio

import androidx.compose.ui.graphics.ImageBitmap

expect fun decodeImage(bytes: ByteArray?): ImageBitmap?
