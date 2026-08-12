package com.soviet117.openbeats

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.soviet117.openbeats.audio.AndroidPlayerController
import com.soviet117.openbeats.audio.MediaStoreAudioLibrary
import com.soviet117.openbeats.data.SharedPreferencesFavoritesStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            var permissionGranted by remember { mutableStateOf(hasAudioPermission(this)) }
            val library = remember { MediaStoreAudioLibrary(applicationContext) }
            val player = remember { AndroidPlayerController(applicationContext) }
            val favoritesStore = remember { SharedPreferencesFavoritesStore(applicationContext) }
            DisposableEffect(player) {
                onDispose { player.release() }
            }
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                permissionGranted = granted
            }
            App(
                permissionGranted = permissionGranted,
                onRequestPermission = {
                    launcher.launch(audioPermissionName())
                },
                audioLibrary = library,
                playerController = player,
                favoritesStore = favoritesStore,
            )
        }
    }
}

private fun audioPermissionName(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

private fun hasAudioPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, audioPermissionName()) ==
        PackageManager.PERMISSION_GRANTED

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
