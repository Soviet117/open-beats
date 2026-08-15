package com.soviet117.openbeats.audio

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.soviet117.openbeats.shared.R
import com.soviet117.openbeats.ui.data.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

class AndroidPlayerController(
    context: Context,
) : PlayerController {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mainExecutor = Executor { command -> Handler(Looper.getMainLooper()).post(command) }
    private val sessionToken =
        SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
    private val notificationArtwork: ByteArray? by lazy {
        runCatching {
            val bitmap = BitmapFactory.decodeResource(appContext.resources, R.drawable.logo_app_oscuro)
                ?: return@lazy null
            ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                out.toByteArray()
            }
        }.getOrNull()
    }

    private var mediaController: MediaController? = null
    private var connectInFlight = false
    private var currentQueue: List<Song> = emptyList()
    private var pendingQueue: Pair<List<Song>, Int>? = null
    private var tickerJob: Job? = null
    private var queueJob: Job? = null

    private val _state = MutableStateFlow(PlayerState())
    override val state = _state.asStateFlow()

    private val sessionListener = object : MediaController.Listener {
        override fun onDisconnected(controller: MediaController) {
            if (mediaController === controller) {
                mediaController = null
                mainExecutor.execute { runCatching { controller.release() } }
            }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            update { it.copy(isPlaying = isPlaying) }
            runTicker(isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaController?.let { controller ->
                update {
                    it.copy(
                        currentIndex = controller.currentMediaItemIndex,
                        positionMs = 0L,
                        durationMs = controller.duration.coerceAtLeast(0L),
                    )
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            mediaController?.let { controller ->
                update { it.copy(durationMs = controller.duration.coerceAtLeast(0L)) }
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            update {
                it.copy(
                    currentIndex = mediaController?.currentMediaItemIndex ?: it.currentIndex,
                    positionMs = newPosition.positionMs,
                    durationMs = mediaController?.duration?.coerceAtLeast(0L) ?: it.durationMs,
                )
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            update { it.copy(shuffle = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            update { it.copy(repeatMode = repeatMode.toRepeatMode()) }
        }
    }

    init {
        connect()
    }

    override fun setQueue(songs: List<Song>, startIndex: Int) {
        if (songs.isEmpty()) return
        currentQueue = songs
        update {
            it.copy(
                queue = songs,
                currentIndex = startIndex.coerceIn(0, songs.size - 1),
                isPlaying = true,
                positionMs = 0L,
                durationMs = songs.getOrNull(startIndex)?.durationMs ?: 0L,
            )
        }
        val controller = mediaController
        if (controller != null && controller.isConnected) {
            applyQueue(controller, songs, startIndex)
        } else {
            pendingQueue = songs to startIndex
            connect()
        }
    }

    override fun skipToIndex(index: Int) {
        val controller = mediaController ?: return
        if (index !in 0 until controller.mediaItemCount) return
        controller.seekTo(index, C.TIME_UNSET)
        update { it.copy(currentIndex = index, positionMs = 0L) }
    }

    override fun playPause() {
        mediaController?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    override fun next() {
        mediaController?.let {
            if (it.hasNextMediaItem()) it.seekToNextMediaItem()
        }
    }

    override fun previous() {
        mediaController?.let {
            if (it.hasPreviousMediaItem()) it.seekToPreviousMediaItem()
        }
    }

    override fun seekTo(positionMs: Long) {
        mediaController?.let { controller ->
            val clamped = positionMs.coerceIn(0L, controller.duration.coerceAtLeast(0L))
            controller.seekTo(clamped)
            update { it.copy(positionMs = clamped) }
        }
    }

    override fun toggleShuffle() {
        val next = !_state.value.shuffle
        mediaController?.shuffleModeEnabled = next
        update { it.copy(shuffle = next) }
    }

    override fun cycleRepeat() {
        val next = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        mediaController?.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        update { it.copy(repeatMode = next) }
    }

    fun release() {
        queueJob?.cancel()
        tickerJob?.cancel()
        val controller = mediaController
        mediaController = null
        controller?.let { runCatching { it.release() } }
    }

    private fun connect() {
        if (connectInFlight) return
        connectInFlight = true
        val future = MediaController.Builder(appContext, sessionToken)
            .setListener(sessionListener)
            .buildAsync()
        Futures.addCallback(
            future,
            object : FutureCallback<MediaController> {
                override fun onSuccess(result: MediaController) {
                    connectInFlight = false
                    mediaController = result
                    result.addListener(playerListener)
                    syncFromController(result)
                    pendingQueue?.let { (songs, index) ->
                        pendingQueue = null
                        applyQueue(result, songs, index)
                    }
                }

                override fun onFailure(t: Throwable) {
                    connectInFlight = false
                    Log.w(TAG, "No se pudo conectar con PlaybackService", t)
                }
            },
            mainExecutor,
        )
    }

    private fun applyQueue(controller: MediaController, songs: List<Song>, startIndex: Int) {
        queueJob?.cancel()
        queueJob = scope.launch {
            val items = songs.map { it.toMediaItem() }
            controller.setMediaItems(items, startIndex.coerceIn(0, songs.size - 1), 0L)
            controller.prepare()
            controller.play()
        }
    }

    private fun syncFromController(controller: MediaController) {
        if (currentQueue.isEmpty()) {
            val restored = controller.restoredQueue()
            if (restored.isNotEmpty()) currentQueue = restored
        }
        val queue = currentQueue
        val index = controller.currentMediaItemIndex
        val duration = controller.duration.coerceAtLeast(0L).takeIf { it > 0 }
        update {
            it.copy(
                queue = queue,
                currentIndex = if (index in queue.indices) index else -1,
                isPlaying = controller.isPlaying,
                positionMs = controller.currentPosition,
                durationMs = duration ?: queue.getOrNull(index)?.durationMs ?: 0L,
                shuffle = controller.shuffleModeEnabled,
                repeatMode = controller.repeatMode.toRepeatMode(),
            )
        }
        runTicker(controller.isPlaying)
    }

    private fun MediaController.restoredQueue(): List<Song> {
        val count = mediaItemCount
        if (count == 0) return emptyList()
        return (0 until count).map { index -> getMediaItemAt(index).toSong(index) }
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setDurationMs(durationMs)
        notificationArtwork?.let {
            metadata.setArtworkData(it, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
        }
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(id)
            .setMediaMetadata(metadata.build())
            .build()
    }

    private fun MediaItem.toSong(index: Int): Song {
        val meta = mediaMetadata
        return Song(
            id = mediaId,
            title = meta.title?.toString() ?: "Sin título",
            artist = meta.artist?.toString() ?: "Artista desconocido",
            album = meta.albumTitle?.toString() ?: "Álbum desconocido",
            durationMs = meta.durationMs?.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L,
            colors = fallbackPalette[index % fallbackPalette.size],
        )
    }

    private fun runTicker(isPlaying: Boolean) {
        tickerJob?.cancel()
        if (!isPlaying) return
        tickerJob = scope.launch {
            while (isActive) {
                val controller = mediaController
                if (controller != null) {
                    update {
                        it.copy(
                            positionMs = controller.currentPosition,
                            durationMs = controller.duration.coerceAtLeast(0L),
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun update(transform: (PlayerState) -> PlayerState) {
        _state.value = transform(_state.value)
    }

    private fun Int.toRepeatMode(): RepeatMode = when (this) {
        Player.REPEAT_MODE_OFF -> RepeatMode.OFF
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> RepeatMode.ONE
    }

    private val fallbackPalette = listOf(
        listOf(Color(0xFF7C3AED), Color(0xFFEC4899)),
        listOf(Color(0xFF06B6D4), Color(0xFF3B82F6)),
        listOf(Color(0xFFF59E0B), Color(0xFFEF4444)),
        listOf(Color(0xFF10B981), Color(0xFF3B82F6)),
        listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
    )

    private companion object {
        const val TAG = "AndroidPlayerController"
    }
}
