package com.soviet117.openbeats.audio.yt

import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun YouTubeWebView(
    videoId: String,
    modifier: Modifier,
    onReady: () -> Unit,
    onStateChange: (Int) -> Unit,
    onError: (Int) -> Unit,
) {
    AndroidView(
        factory = { context ->
            CookieManager.getInstance().apply { setAcceptCookie(true) }
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.setSupportZoom(false)
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 12; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Mobile Safari/537.36"
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadUrl("https://m.youtube.com/watch?v=$videoId")
                onReady()
            }
        },
        modifier = modifier,
    )
}
