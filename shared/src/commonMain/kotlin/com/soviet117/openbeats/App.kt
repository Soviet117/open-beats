package com.soviet117.openbeats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.audio.AudioLibrary
import com.soviet117.openbeats.audio.LocalAudioLibrary
import com.soviet117.openbeats.audio.MockPlayerController
import com.soviet117.openbeats.audio.PlayerController
import com.soviet117.openbeats.audio.PlayerState
import com.soviet117.openbeats.ui.components.MiniPlayer
import com.soviet117.openbeats.ui.data.Mock
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.screens.HomeScreen
import com.soviet117.openbeats.ui.screens.LibraryScreen
import com.soviet117.openbeats.ui.screens.NowPlayingScreen
import com.soviet117.openbeats.ui.screens.PermissionScreen
import com.soviet117.openbeats.ui.screens.SearchScreen
import com.soviet117.openbeats.ui.theme.BrandSoft
import com.soviet117.openbeats.ui.theme.Obsidian
import com.soviet117.openbeats.ui.theme.OpenBeatsTheme
import com.soviet117.openbeats.ui.theme.TextMuted
import com.soviet117.openbeats.ui.theme.TextPrimary

private data class Tab(
    val label: String,
    val selected: ImageVector,
    val unselected: ImageVector,
)

private val Tabs = listOf(
    Tab("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    Tab("Buscar", Icons.Filled.Search, Icons.Outlined.Search),
    Tab("Biblioteca", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
)

@Composable
@Preview
fun App(
    permissionGranted: Boolean = true,
    onRequestPermission: () -> Unit = {},
    audioLibrary: AudioLibrary? = null,
    playerController: PlayerController? = null,
) {
    OpenBeatsTheme {
        var selectedTab by remember { mutableIntStateOf(0) }
        val hasLibrary = audioLibrary != null
        var songs by remember { mutableStateOf(if (hasLibrary) emptyList() else Mock.songs) }
        var loading by remember { mutableStateOf(hasLibrary) }
        var isLiked by remember { mutableStateOf(false) }
        var showPlayer by remember { mutableStateOf(false) }

        LaunchedEffect(audioLibrary) {
            val library = audioLibrary ?: return@LaunchedEffect
            runCatching { library.loadSongs() }
                .onSuccess { loaded -> if (loaded.isNotEmpty()) songs = loaded }
                .onFailure { songs = Mock.songs }
            loading = false
        }

        val controller = playerController ?: remember {
            MockPlayerController(
                PlayerState(
                    queue = Mock.songs,
                    currentIndex = 1,
                    isPlaying = true,
                    durationMs = Mock.songs[1].durationMs,
                ),
            )
        }
        val playerState by controller.state.collectAsState()
        val currentSong = playerState.currentSong

        CompositionLocalProvider(LocalAudioLibrary provides audioLibrary) {
            if (!permissionGranted) {
                PermissionScreen(onRequestPermission = onRequestPermission)
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        containerColor = Obsidian,
                        bottomBar = {
                            AnimatedVisibility(
                                visible = !showPlayer,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Column {
                                    if (currentSong != null) {
                                        MiniPlayer(
                                            song = currentSong,
                                            playing = playerState.isPlaying,
                                            onTap = { showPlayer = true },
                                            onTogglePlay = { controller.playPause() },
                                        )
                                    }
                                    NavigationBar(
                                        containerColor = Obsidian,
                                        tonalElevation = 0.dp,
                                    ) {
                                        Tabs.forEachIndexed { index, tab ->
                                            NavigationBarItem(
                                                selected = selectedTab == index,
                                                onClick = { selectedTab = index },
                                                icon = {
                                                    Icon(
                                                        imageVector = if (selectedTab == index) tab.selected else tab.unselected,
                                                        contentDescription = null,
                                                    )
                                                },
                                                label = { Text(tab.label) },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = TextPrimary,
                                                    selectedTextColor = TextPrimary,
                                                    unselectedIconColor = TextMuted,
                                                    unselectedTextColor = TextMuted,
                                                    indicatorColor = BrandSoft.copy(alpha = 0.16f),
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    ) { padding ->
                        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                            when (selectedTab) {
                                0 -> HomeScreen(
                                    songs = songs,
                                    loading = loading,
                                    onPlay = { queue, index ->
                                        controller.setQueue(queue, index)
                                        showPlayer = true
                                    },
                                )
                                1 -> SearchScreen()
                                else -> LibraryScreen()
                            }
                        }
                    }

                    if (currentSong != null) {
                        AnimatedVisibility(
                            visible = showPlayer,
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it },
                        ) {
                            NowPlayingScreen(
                                song = currentSong,
                                playing = playerState.isPlaying,
                                isLiked = isLiked,
                                positionMs = playerState.positionMs,
                                durationMs = playerState.durationMs.takeIf { it > 0 } ?: currentSong.durationMs,
                                onClose = { showPlayer = false },
                                onTogglePlay = { controller.playPause() },
                                onToggleLike = { isLiked = !isLiked },
                                onNext = { controller.next() },
                                onPrevious = { controller.previous() },
                                onSeek = { controller.seekTo(it) },
                            )
                        }
                    }
                }
            }
        }
    }
}
