package com.soviet117.openbeats.audio

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun decodeImage(bytes: ByteArray?): ImageBitmap? {
    if (bytes == null || bytes.isEmpty()) return null
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight)
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun calculateSampleSize(width: Int, height: Int): Int {
    if (width <= 0 || height <= 0) return 1
    val maxDim = maxOf(width, height)
    var sample = 1
    while (maxDim / (sample * 2) >= 1024) {
        sample *= 2
    }
    return sample
}
