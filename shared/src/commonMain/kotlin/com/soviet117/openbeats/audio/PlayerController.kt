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
    val shuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
) {
    val currentSong: Song?
        get() = if (currentIndex in queue.indices) queue[currentIndex] else null
}

enum class RepeatMode { OFF, ALL, ONE }

interface PlayerController {
    val state: StateFlow<PlayerState>
    fun setQueue(songs: List<Song>, startIndex: Int)
    fun skipToIndex(index: Int)
    fun playPause()
    fun next()
    fun previous()
    fun seekTo(positionMs: Long)
    fun toggleShuffle()
    fun cycleRepeat()
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
            shuffle = _state.value.shuffle,
            repeatMode = _state.value.repeatMode,
        )
    }

    override fun skipToIndex(index: Int) {
        val state = _state.value
        if (index !in state.queue.indices) return
        _state.value = state.copy(
            currentIndex = index,
            positionMs = 0L,
            durationMs = state.queue[index].durationMs,
            isPlaying = true,
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

    override fun toggleShuffle() {
        _state.value = _state.value.copy(shuffle = !_state.value.shuffle)
    }

    override fun cycleRepeat() {
        val next = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _state.value = _state.value.copy(repeatMode = next)
    }

    private fun move(step: Int) {
        val state = _state.value
        if (state.queue.isEmpty()) return
        if (state.shuffle && state.queue.size > 1) {
            val randomIndex = state.queue.indices.filter { it != state.currentIndex }.random()
            _state.value = state.copy(
                currentIndex = randomIndex,
                positionMs = 0L,
                durationMs = state.queue[randomIndex].durationMs,
            )
            return
        }
        val nextIndex = state.currentIndex + step
        when {
            nextIndex in state.queue.indices -> _state.value = state.copy(
                currentIndex = nextIndex,
                positionMs = 0L,
                durationMs = state.queue[nextIndex].durationMs,
            )
            state.repeatMode == RepeatMode.ALL -> {
                val wrapped = ((nextIndex % state.queue.size) + state.queue.size) % state.queue.size
                _state.value = state.copy(
                    currentIndex = wrapped,
                    positionMs = 0L,
                    durationMs = state.queue[wrapped].durationMs,
                )
            }
            state.repeatMode == RepeatMode.ONE -> _state.value = state.copy(positionMs = 0L)
            else -> _state.value = state.copy(isPlaying = false)
        }
    }
}
