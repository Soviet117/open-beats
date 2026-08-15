package com.soviet117.openbeats.audio

import com.soviet117.openbeats.ui.data.Song
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAudioSession
import platform.AVFoundation.AVAudioSessionCategoryPlayback
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.MediaPlayer.MPMediaItemPropertyAlbumTitle
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess

@OptIn(ExperimentalForeignApi::class)
class ApplePlayerController : PlayerController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(PlayerState())
    override val state = _state.asStateFlow()

    private var player: AVPlayer? = null
    private var currentQueue: List<Song> = emptyList()
    private var tickerJob: Job? = null
    private var endObserver: Any? = null

    init {
        configureAudioSession()
        setupRemoteCommands()
    }

    private fun configureAudioSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, 0u, null)
        session.setActive(true, null)
    }

    override fun setQueue(songs: List<Song>, startIndex: Int) {
        if (songs.isEmpty()) return
        currentQueue = songs
        playAt(startIndex.coerceIn(0, songs.size - 1))
    }

    override fun skipToIndex(index: Int) {
        if (index !in currentQueue.indices) return
        playAt(index)
    }

    override fun playPause() {
        val avPlayer = player ?: return
        if (avPlayer.rate > 0.0) {
            avPlayer.pause()
            update { it.copy(isPlaying = false) }
        } else {
            avPlayer.play()
            update { it.copy(isPlaying = true) }
        }
        runTicker(_state.value.isPlaying)
        updateNowPlaying()
    }

    override fun next() {
        val state = _state.value
        if (state.queue.isEmpty()) return
        val nextIndex = when {
            state.shuffle && state.queue.size > 1 ->
                state.queue.indices.filter { it != state.currentIndex }.random()
            state.currentIndex + 1 in state.queue.indices -> state.currentIndex + 1
            state.repeatMode == RepeatMode.ALL -> 0
            state.repeatMode == RepeatMode.ONE -> state.currentIndex
            else -> return
        }
        playAt(nextIndex)
    }

    override fun previous() {
        val state = _state.value
        if (state.queue.isEmpty()) return
        val nextIndex = when {
            state.shuffle && state.queue.size > 1 ->
                state.queue.indices.filter { it != state.currentIndex }.random()
            state.currentIndex - 1 in state.queue.indices -> state.currentIndex - 1
            state.repeatMode == RepeatMode.ALL -> state.queue.size - 1
            else -> state.currentIndex
        }
        playAt(nextIndex)
    }

    override fun seekTo(positionMs: Long) {
        val avPlayer = player ?: return
        val seconds = positionMs.coerceAtLeast(0L) / 1000.0
        avPlayer.seekToTime(CMTimeMakeWithSeconds(seconds, 600))
        update { it.copy(positionMs = positionMs) }
    }

    override fun toggleShuffle() {
        update { it.copy(shuffle = !it.shuffle) }
    }

    override fun cycleRepeat() {
        update {
            it.copy(
                repeatMode = when (it.repeatMode) {
                    RepeatMode.OFF -> RepeatMode.ALL
                    RepeatMode.ALL -> RepeatMode.ONE
                    RepeatMode.ONE -> RepeatMode.OFF
                },
            )
        }
    }

    fun release() {
        tickerJob?.cancel()
        endObserver?.let { NSNotificationCenter.defaultCenter().removeObserver(it) }
        endObserver = null
        player?.pause()
        player = null
    }

    private fun playAt(index: Int) {
        val song = currentQueue.getOrNull(index) ?: return
        val url = NSURL.URLWithString(song.id) ?: return
        val item = AVPlayerItem(uRL = url)
        endObserver?.let { NSNotificationCenter.defaultCenter().removeObserver(it) }
        endObserver = NSNotificationCenter.defaultCenter().addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = item,
            queue = null,
            usingBlock = { next() },
        )
        val avPlayer = player ?: AVPlayer().also { player = it }
        avPlayer.replaceCurrentItemWithPlayerItem(item)
        avPlayer.play()
        update {
            it.copy(
                queue = currentQueue,
                currentIndex = index,
                isPlaying = true,
                positionMs = 0L,
                durationMs = song.durationMs,
            )
        }
        updateNowPlaying()
        runTicker(true)
    }

    private fun runTicker(isPlaying: Boolean) {
        tickerJob?.cancel()
        if (!isPlaying) return
        tickerJob = scope.launch {
            while (isActive) {
                val avPlayer = player
                if (avPlayer != null) {
                    update {
                        it.copy(positionMs = (avPlayer.currentTime().seconds * 1000.0).toLong())
                    }
                }
                delay(500)
            }
        }
    }

    private fun updateNowPlaying() {
        val state = _state.value
        val song = state.currentSong ?: return
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = mapOf(
            MPMediaItemPropertyTitle to song.title,
            MPMediaItemPropertyArtist to song.artist,
            MPMediaItemPropertyAlbumTitle to song.album,
            MPMediaItemPropertyPlaybackDuration to song.durationMs / 1000.0,
            MPNowPlayingInfoPropertyElapsedPlaybackTime to state.positionMs / 1000.0,
            MPNowPlayingInfoPropertyPlaybackRate to if (state.isPlaying) 1.0 else 0.0,
        )
    }

    private fun setupRemoteCommands() {
        val center = MPRemoteCommandCenter.sharedCommandCenter()
        center.playCommand.addTargetWithHandler { _ ->
            playPause()
            MPRemoteCommandHandlerStatusSuccess
        }
        center.pauseCommand.addTargetWithHandler { _ ->
            playPause()
            MPRemoteCommandHandlerStatusSuccess
        }
        center.togglePlayPauseCommand.addTargetWithHandler { _ ->
            playPause()
            MPRemoteCommandHandlerStatusSuccess
        }
        center.nextTrackCommand.addTargetWithHandler { _ ->
            next()
            MPRemoteCommandHandlerStatusSuccess
        }
        center.previousTrackCommand.addTargetWithHandler { _ ->
            previous()
            MPRemoteCommandHandlerStatusSuccess
        }
    }

    private fun update(transform: (PlayerState) -> PlayerState) {
        _state.value = transform(_state.value)
    }
}
