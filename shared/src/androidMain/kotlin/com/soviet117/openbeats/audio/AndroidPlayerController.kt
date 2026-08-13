package com.soviet117.openbeats.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
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

class AndroidPlayerController(context: Context) : PlayerController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus = */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        .setWakeMode(C.WAKE_MODE_LOCAL)
        .build()

    private val _state = MutableStateFlow(PlayerState())
    override val state = _state.asStateFlow()

    private var tickerJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                update { it.copy(isPlaying = isPlaying, positionMs = player.currentPosition) }
                runTicker(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                update { it.copy(durationMs = player.duration.coerceAtLeast(0L)) }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                update {
                    it.copy(
                        currentIndex = player.currentMediaItemIndex,
                        positionMs = 0L,
                        durationMs = player.duration.coerceAtLeast(0L),
                    )
                }
            }
        })
    }

    override fun setQueue(songs: List<Song>, startIndex: Int) {
        val items = songs.map { MediaItem.fromUri(it.id) }
        player.setMediaItems(items, startIndex.coerceIn(0, songs.size - 1), 0L)
        player.prepare()
        applyShuffleAndRepeat()
        player.play()
        val previousState = _state.value
        update {
            PlayerState(
                queue = songs,
                currentIndex = startIndex.coerceIn(0, songs.size - 1),
                isPlaying = true,
                positionMs = 0L,
                durationMs = songs.getOrNull(startIndex)?.durationMs ?: 0L,
                shuffle = previousState.shuffle,
                repeatMode = previousState.repeatMode,
            )
        }
    }

    override fun playPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    override fun next() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    override fun previous() {
        if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L)))
        update { it.copy(positionMs = positionMs.coerceAtLeast(0L)) }
    }

    override fun toggleShuffle() {
        val next = !_state.value.shuffle
        player.shuffleModeEnabled = next
        update { it.copy(shuffle = next) }
    }

    override fun cycleRepeat() {
        val next = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        player.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        update { it.copy(repeatMode = next) }
    }

    private fun applyShuffleAndRepeat() {
        player.shuffleModeEnabled = _state.value.shuffle
        player.repeatMode = when (_state.value.repeatMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    private fun runTicker(isPlaying: Boolean) {
        tickerJob?.cancel()
        if (!isPlaying) return
        tickerJob = scope.launch {
            while (isActive) {
                update { it.copy(positionMs = player.currentPosition) }
                delay(500)
            }
        }
    }

    private fun update(transform: (PlayerState) -> PlayerState) {
        _state.value = transform(_state.value)
    }

    fun release() {
        tickerJob?.cancel()
        player.release()
    }
}
