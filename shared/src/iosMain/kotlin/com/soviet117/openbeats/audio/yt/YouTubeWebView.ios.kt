package com.soviet117.openbeats.audio.yt

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKUserContentController

private const val IOS_YOUTUBE_HTML_TEMPLATE = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<style>
  html, body { margin: 0; padding: 0; width: 100%%; height: 100%%; background: #000; overflow: hidden; }
  #player { width: 100%%; height: 100%%; }
</style>
</head>
<body>
<div id="player"></div>
<script>
  var tag = document.createElement('script');
  tag.src = 'https://www.youtube.com/iframe_api';
  document.head.appendChild(tag);
  var player;
  function onYouTubeIframeAPIReady() {
    player = new YT.Player('player', {
      videoId: '%s',
      playerVars: { autoplay: 1, controls: 1, modestbranding: 1, rel: 0, fs: 0 },
      events: {
        onReady: function(e) { webkit.messageHandlers.onReady.postMessage('ready'); },
        onStateChange: function(e) { webkit.messageHandlers.onStateChange.postMessage(e.data.toString()); },
        onError: function(e) { webkit.messageHandlers.onError.postMessage(e.data.toString()); }
      }
    });
  }
  function loadVideo(id) { if (player && player.loadVideoById) player.loadVideoById(id); }
  function playVideo() { if (player && player.playVideo) player.playVideo(); }
  function pauseVideo() { if (player && player.pauseVideo) player.pauseVideo(); }
  function seekTo(ms) { if (player && player.seekTo) player.seekTo(ms / 1000, true); }
</script>
</body>
</html>
"""

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun YouTubeWebView(
    videoId: String,
    modifier: Modifier,
    onReady: () -> Unit,
    onStateChange: (Int) -> Unit,
    onError: (Int) -> Unit,
) {
    val config = WKWebViewConfiguration()
    val contentController = config.userContentController

    UIKitView(
        factory = {
            val webView = WKWebView(frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = config)

            contentController.addScriptMessageHandler(
                messageHandler = object : platform.WebKit.WKScriptMessageHandlerProtocol {
                    override fun userContentController(
                        userContentController: platform.WebKit.WKUserContentController,
                        didReceiveScriptMessage: platform.WebKit.WKScriptMessage,
                    ) {
                        when (didReceiveScriptMessage.name) {
                            "onReady" -> onReady()
                            "onStateChange" -> {
                                val state = (didReceiveScriptMessage.body as? String)?.toIntOrNull() ?: return
                                onStateChange(state)
                            }
                            "onError" -> {
                                val error = (didReceiveScriptMessage.body as? String)?.toIntOrNull() ?: return
                                onError(error)
                            }
                        }
                    }
                },
                name = "onReady",
            )
            contentController.addScriptMessageHandler(
                messageHandler = object : platform.WebKit.WKScriptMessageHandlerProtocol {
                    override fun userContentController(
                        userContentController: platform.WebKit.WKUserContentController,
                        didReceiveScriptMessage: platform.WebKit.WKScriptMessage,
                    ) {
                        val state = (didReceiveScriptMessage.body as? String)?.toIntOrNull() ?: return
                        onStateChange(state)
                    }
                },
                name = "onStateChange",
            )
            contentController.addScriptMessageHandler(
                messageHandler = object : platform.WebKit.WKScriptMessageHandlerProtocol {
                    override fun userContentController(
                        userContentController: platform.WebKit.WKUserContentController,
                        didReceiveScriptMessage: platform.WebKit.WKScriptMessage,
                    ) {
                        val error = (didReceiveScriptMessage.body as? String)?.toIntOrNull() ?: return
                        onError(error)
                    }
                },
                name = "onError",
            )

            val html = IOS_YOUTUBE_HTML_TEMPLATE.format(videoId)
            webView.loadHTMLString(html, baseURL = NSURL(string = "https://www.youtube.com"))
            webView
        },
        modifier = modifier,
    )
}
