package com.soviet117.openbeats

import androidx.compose.ui.graphics.Color
import com.soviet117.openbeats.audio.MockPlayerController
import com.soviet117.openbeats.audio.PlayerState
import com.soviet117.openbeats.audio.RepeatMode
import com.soviet117.openbeats.ui.data.Song
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerControllerTest {

    private val violet = listOf(Color(0xFF7C3AED), Color(0xFFEC4899))

    private fun song(id: String) = Song(id, "Titulo $id", "Artista", "Álbum", 180_000, colors = violet)

    private fun controller() = MockPlayerController(
        PlayerState(
            queue = listOf(song("1"), song("2"), song("3")),
            currentIndex = 0,
            isPlaying = true,
            durationMs = 180_000,
        ),
    )

    @Test
    fun skipToIndexMovesToRequestedSong() {
        val controller = controller()
        controller.skipToIndex(2)
        val state = controller.state.value
        assertEquals(2, state.currentIndex)
        assertEquals("Titulo 3", state.currentSong?.title)
        assertEquals(0L, state.positionMs)
        assertTrue(state.isPlaying)
    }

    @Test
    fun skipToIndexOutOfRangeIsIgnored() {
        val controller = controller()
        controller.skipToIndex(5)
        controller.skipToIndex(-1)
        assertEquals(0, controller.state.value.currentIndex)
    }

    @Test
    fun setQueueKeepsRepeatAndShuffle() {
        val controller = MockPlayerController(
            PlayerState(
                queue = listOf(song("1")),
                currentIndex = 0,
                shuffle = true,
                repeatMode = RepeatMode.ONE,
            ),
        )
        controller.setQueue(listOf(song("a"), song("b")), 1)
        val state = controller.state.value
        assertEquals(1, state.currentIndex)
        assertTrue(state.shuffle)
        assertEquals(RepeatMode.ONE, state.repeatMode)
    }

    @Test
    fun cycleRepeatCyclesThroughModes() {
        val controller = MockPlayerController(PlayerState())
        assertEquals(RepeatMode.OFF, controller.state.value.repeatMode)
        controller.cycleRepeat()
        assertEquals(RepeatMode.ALL, controller.state.value.repeatMode)
        controller.cycleRepeat()
        assertEquals(RepeatMode.ONE, controller.state.value.repeatMode)
        controller.cycleRepeat()
        assertEquals(RepeatMode.OFF, controller.state.value.repeatMode)
    }

    @Test
    fun previousAtStartStopsPlayback() {
        val controller = controller()
        controller.previous()
        assertFalse(controller.state.value.isPlaying)
    }
}
