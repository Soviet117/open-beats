package com.soviet117.openbeats.audio

import com.soviet117.openbeats.ui.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
) {
    val currentSong: Song?
        get() = if (currentIndex in queue.indices) queue[currentIndex] else null
}

interface PlayerController {
    val state: StateFlow<PlayerState>
    fun setQueue(songs: List<Song>, startIndex: Int)
    fun playPause()
    fun next()
    fun previous()
    fun seekTo(positionMs: Long)
}

class MockPlayerController(initial: PlayerState) : PlayerController {
    private val _state = MutableStateFlow(initial)
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    override fun setQueue(songs: List<Song>, startIndex: Int) {
        _state.value = PlayerState(
            queue = songs,
            currentIndex = startIndex,
            isPlaying = true,
            positionMs = 0L,
            durationMs = songs.getOrNull(startIndex)?.durationMs ?: 0L,
        )
    }

    override fun playPause() {
        _state.value = _state.value.copy(isPlaying = !_state.value.isPlaying)
    }

    override fun next() = move(1)

    override fun previous() = move(-1)

    override fun seekTo(positionMs: Long) {
        _state.value = _state.value.copy(positionMs = positionMs)
    }

    private fun move(step: Int) {
        val state = _state.value
        val nextIndex = state.currentIndex + step
        if (nextIndex in state.queue.indices) {
            _state.value = state.copy(
                currentIndex = nextIndex,
                positionMs = 0L,
                durationMs = state.queue[nextIndex].durationMs,
            )
        }
    }
}
