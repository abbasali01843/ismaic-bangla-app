package com.islamic.bangla.data.audio

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.islamic.bangla.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground service wrapper around the app-scoped player:
 * keeps recitation playing in the background with notification,
 * lock-screen and headset controls (handled by Media3).
 */
@AndroidEntryPoint
class QuranPlaybackService : MediaSessionService() {

    @Inject
    lateinit var audioPlayer: QuranAudioPlayer

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        session = MediaSession.Builder(this, audioPlayer.exoPlayer)
            .setSessionActivity(sessionActivity)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        session

    override fun onDestroy() {
        session?.release()
        session = null
        super.onDestroy()
    }
}
