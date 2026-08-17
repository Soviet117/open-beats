package com.soviet117.openbeats.audio.yt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

actual suspend fun httpPost(
    url: String,
    headers: Map<String, String>,
    body: String,
): String = withContext(Dispatchers.IO) {
    val conn = URL(url).openConnection() as HttpURLConnection
    try {
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        conn.setRequestProperty("Content-Type", "application/json")
        headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
        OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
    } finally {
        conn.disconnect()
    }
}
