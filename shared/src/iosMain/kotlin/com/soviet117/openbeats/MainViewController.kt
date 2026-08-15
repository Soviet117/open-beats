package com.soviet117.openbeats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.soviet117.openbeats.audio.AppleMusicLibrary
import com.soviet117.openbeats.audio.ApplePlayerController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.MediaPlayer.MPMediaLibrary
import platform.MediaPlayer.MPMediaLibraryAuthorizationStatusAuthorized
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController = ComposeUIViewController {
    val library = remember { AppleMusicLibrary() }
    val player = remember { ApplePlayerController() }
    var permissionGranted by remember { mutableStateOf(hasLibraryPermission()) }
    val appVersion = remember {
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
    }

    App(
        permissionGranted = permissionGranted,
        onRequestPermission = {
            MPMediaLibrary.requestAuthorization { status ->
                permissionGranted = status == MPMediaLibraryAuthorizationStatusAuthorized
            }
        },
        audioLibrary = library,
        playerController = player,
        appVersion = appVersion,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun hasLibraryPermission(): Boolean =
    MPMediaLibrary.authorizationStatus() == MPMediaLibraryAuthorizationStatusAuthorized
