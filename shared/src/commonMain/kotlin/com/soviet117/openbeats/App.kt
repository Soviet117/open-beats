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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soviet117.openbeats.audio.AudioLibrary
import com.soviet117.openbeats.audio.LocalAudioLibrary
import com.soviet117.openbeats.audio.MockPlayerController
import com.soviet117.openbeats.audio.PlayerController
import com.soviet117.openbeats.audio.PlayerState
import com.soviet117.openbeats.data.FavoritesStore
import com.soviet117.openbeats.data.RecentStore
import com.soviet117.openbeats.ui.components.MiniPlayer
import com.soviet117.openbeats.ui.components.ArtworkCache
import com.soviet117.openbeats.ui.data.LibraryTarget
import com.soviet117.openbeats.ui.data.Mock
import com.soviet117.openbeats.ui.data.Song
import com.soviet117.openbeats.ui.screens.AlbumDetailScreen
import com.soviet117.openbeats.ui.screens.ArtistDetailScreen
import com.soviet117.openbeats.ui.screens.GenreDetailScreen
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

private const val MAX_RECENTS = 12

private val Tabs = listOf(
    Tab("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    Tab("Buscar", Icons.Filled.Search, Icons.Outlined.Search),
    Tab("Biblioteca", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
)

@Composable
@Preview
@OptIn(ExperimentalComposeUiApi::class)
fun App(
    permissionGranted: Boolean = true,
    onRequestPermission: () -> Unit = {},
    audioLibrary: AudioLibrary? = null,
    playerController: PlayerController? = null,
    favoritesStore: FavoritesStore? = null,
    recentStore: RecentStore? = null,
    appVersion: String? = null,
) {
    OpenBeatsTheme {
        var selectedTab by remember { mutableIntStateOf(0) }
        val hasLibrary = audioLibrary != null
        var songs by remember { mutableStateOf(if (hasLibrary) emptyList() else Mock.songs) }
        var loading by remember { mutableStateOf(hasLibrary) }
        var likedIds by remember { mutableStateOf(emptySet<String>()) }
        var likesLoaded by remember { mutableStateOf(false) }
        var recentIds by remember { mutableStateOf(emptyList<String>()) }
        var recentsLoaded by remember { mutableStateOf(false) }
        var showPlayer by remember { mutableStateOf(false) }
        var libraryTarget by remember { mutableStateOf<LibraryTarget?>(null) }

        LaunchedEffect(audioLibrary) {
            val library = audioLibrary ?: return@LaunchedEffect
            val loaded = runCatching { library.loadSongs() }
                .getOrElse { emptyList() }
            if (loaded.isNotEmpty()) {
                songs = loaded
                ArtworkCache.preload(loaded, library)
            } else {
                songs = Mock.songs
            }
            loading = false
        }

        LaunchedEffect(favoritesStore) {
            val store = favoritesStore ?: return@LaunchedEffect
            likedIds = runCatching { store.load() }.getOrDefault(emptySet())
            likesLoaded = true
        }

        LaunchedEffect(likedIds, likesLoaded, favoritesStore) {
            val store = favoritesStore ?: return@LaunchedEffect
            if (!likesLoaded) return@LaunchedEffect
            runCatching { store.save(likedIds) }
        }

        LaunchedEffect(recentStore) {
            val store = recentStore ?: return@LaunchedEffect
            recentIds = runCatching { store.load() }.getOrDefault(emptyList())
            recentsLoaded = true
        }

        val toggleLike: (String) -> Unit = remember {
            { songId -> likedIds = if (songId in likedIds) likedIds - songId else likedIds + songId }
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

        LaunchedEffect(currentSong?.id, recentsLoaded, recentStore) {
            val song = currentSong ?: return@LaunchedEffect
            val store = recentStore ?: return@LaunchedEffect
            if (!recentsLoaded) return@LaunchedEffect
            recentIds = (listOf(song.id) + recentIds.filter { it != song.id }).take(MAX_RECENTS)
            runCatching { store.add(song.id) }
        }

        val onPlay: (List<Song>, Int) -> Unit = remember {
            { queue, index ->
                controller.setQueue(queue, index)
                showPlayer = true
            }
        }
        val onSelectTab: (Int) -> Unit = remember { { index -> selectedTab = index; libraryTarget = null } }
        val onOpenPlayer: () -> Unit = remember { { showPlayer = true } }
        val onClosePlayer: () -> Unit = remember { { showPlayer = false } }
        val onTogglePlay: () -> Unit = remember { { controller.playPause() } }
        val onNext: () -> Unit = remember { { controller.next() } }
        val onPrevious: () -> Unit = remember { { controller.previous() } }
        val onSeek: (Long) -> Unit = remember { { position -> controller.seekTo(position) } }
        val onToggleShuffle: () -> Unit = remember { { controller.toggleShuffle() } }
        val onCycleRepeat: () -> Unit = remember { { controller.cycleRepeat() } }

        val songsById = remember(songs) { songs.associateBy { it.id } }
        val recentSongs = if (recentStore != null) recentIds.mapNotNull { songsById[it] } else null

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
                                            onTap = onOpenPlayer,
                                            onTogglePlay = onTogglePlay,
                                            onNext = onNext,
                                        )
                                    }
                                    NavigationBar(
                                        containerColor = Obsidian,
                                        tonalElevation = 0.dp,
                                    ) {
                                        Tabs.forEachIndexed { index, tab ->
                                            NavigationBarItem(
                                                selected = selectedTab == index,
                                                onClick = { onSelectTab(index) },
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
                            @Suppress("DEPRECATION")
                            BackHandler(enabled = libraryTarget != null) {
                                libraryTarget = null
                            }
                            when (selectedTab) {
                                0 -> HomeScreen(
                                    songs = songs,
                                    likedIds = likedIds,
                                    loading = loading,
                                    appVersion = appVersion,
                                    recents = recentSongs,
                                    onPlay = onPlay,
                                    onToggleLike = toggleLike,
                                )
                                1 -> SearchScreen()
                                else -> when (val target = libraryTarget) {
                                    is LibraryTarget.AlbumTarget -> AlbumDetailScreen(
                                        album = target.album,
                                        likedIds = likedIds,
                                        onBack = { libraryTarget = null },
                                        onPlay = onPlay,
                                        onToggleLike = toggleLike,
                                    )

                                    is LibraryTarget.ArtistTarget -> ArtistDetailScreen(
                                        artist = target.artist,
                                        likedIds = likedIds,
                                        onBack = { libraryTarget = null },
                                        onPlay = onPlay,
                                        onToggleLike = toggleLike,
                                    )

                                    is LibraryTarget.GenreTarget -> GenreDetailScreen(
                                        genre = target.genre,
                                        likedIds = likedIds,
                                        onBack = { libraryTarget = null },
                                        onPlay = onPlay,
                                        onToggleLike = toggleLike,
                                    )

                                    null -> LibraryScreen(
                                        songs = songs,
                                        likedIds = likedIds,
                                        loading = loading,
                                        onPlay = onPlay,
                                        onToggleLike = toggleLike,
                                        onOpenSearch = { onSelectTab(1) },
                                        onOpenAlbum = { libraryTarget = LibraryTarget.AlbumTarget(it) },
                                        onOpenArtist = { libraryTarget = LibraryTarget.ArtistTarget(it) },
                                    )
                                }
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
                                isLiked = currentSong.id in likedIds,
                                positionMs = playerState.positionMs,
                                durationMs = playerState.durationMs.takeIf { it > 0 } ?: currentSong.durationMs,
                                shuffle = playerState.shuffle,
                                repeatMode = playerState.repeatMode,
                                onClose = onClosePlayer,
                                onTogglePlay = onTogglePlay,
                                onToggleLike = { toggleLike(currentSong.id) },
                                onNext = onNext,
                                onPrevious = onPrevious,
                                onSeek = onSeek,
                                onToggleShuffle = onToggleShuffle,
                                onCycleRepeat = onCycleRepeat,
                            )
                        }
                    }
                }
            }
        }
    }
}
