package com.islamic.bangla.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays the azan at prayer time (alarm audio stream).
 * Downloads once from Wikimedia Commons, then plays the cached file offline.
 */
@Singleton
class AzanPlayer @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var player: MediaPlayer? = null

    fun play(context: Context) {
        scope.launch {
            try {
                stopInternal()
                val file = File(context.applicationContext.filesDir, AZAN_FILE)
                if (!file.exists() || file.length() == 0L) {
                    downloadAzan(file)
                }
                val useCache = file.exists() && file.length() > 0L
                val mp = MediaPlayer()
                try {
                    mp.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    if (useCache) {
                        mp.setDataSource(file.absolutePath)
                    } else {
                        mp.setDataSource(AZAN_URL)
                    }
                    mp.setOnCompletionListener { stop() }
                    mp.setOnErrorListener { _, _, _ -> stop(); true }
                    mp.prepare()
                    player = mp
                    mp.start()
                } catch (e: Exception) {
                    try {
                        mp.release()
                    } catch (_: Exception) {
                    }
                    throw e
                }
            } catch (_: Exception) {
                // Silent fallback: the notification is already shown.
            }
        }
    }

    fun stop() {
        scope.launch { stopInternal() }
    }

    private fun stopInternal() {
        try {
            player?.let {
                try {
                    if (it.isPlaying) it.stop()
                } catch (_: Exception) {
                }
                try {
                    it.reset()
                } catch (_: Exception) {
                }
                try {
                    it.release()
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        } finally {
            player = null
        }
    }

    private fun downloadAzan(file: File) {
        val connection = URL(AZAN_URL).openConnection()
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        connection.getInputStream().use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
    }

    companion object {
        /** CC BY-SA 4.0 azan by Andrewler, via Wikimedia Commons. */
        const val AZAN_URL = "https://upload.wikimedia.org/wikipedia/commons/8/86/Azan.ogg"
        private const val AZAN_FILE = "azan.ogg"
    }
}
