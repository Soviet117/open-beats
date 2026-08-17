package com.soviet117.openbeats.audio

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.soviet117.openbeats.shared.R
import okhttp3.OkHttpClient

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val okHttpClient = OkHttpClient.Builder().build()
        val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setDefaultRequestProperties(
                mapOf(
                    "User-Agent" to "com.google.android.apps.youtube.music/7.27.52 (Linux; U; Android 12; US) gzip",
                    "Referer" to "https://www.youtube.com/",
                    "Origin" to "https://www.youtube.com",
                ),
            )
        val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)
        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
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
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent())
            .build()
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .build()
                .apply { setSmallIcon(R.drawable.ic_stat_openbeats) },
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.let {
            it.player.release()
            it.release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private fun sessionActivityPendingIntent(): PendingIntent {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(packageName)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
