package com.soviet117.openbeats.audio.yt

actual suspend fun httpPost(
    url: String,
    headers: Map<String, String>,
    body: String,
): String {
    throw UnsupportedOperationException("YouTube streaming not yet implemented for iOS")
}
