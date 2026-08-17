package com.soviet117.openbeats.audio.yt

expect suspend fun httpPost(
    url: String,
    headers: Map<String, String> = emptyMap(),
    body: String,
): String
