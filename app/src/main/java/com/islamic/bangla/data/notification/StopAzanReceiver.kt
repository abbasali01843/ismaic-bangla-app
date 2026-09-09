package com.islamic.bangla.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.islamic.bangla.data.audio.AzanPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Stops azan playback from the notification action button. */
@AndroidEntryPoint
class StopAzanReceiver : BroadcastReceiver() {

    @Inject
    lateinit var azanPlayer: AzanPlayer

    override fun onReceive(context: Context, intent: Intent) {
        azanPlayer.stop()
        NotificationManagerCompat.from(context)
            .cancel(intent.getIntExtra(EXTRA_NOTIF_ID, 0))
    }

    companion object {
        const val EXTRA_NOTIF_ID = "notif_id"
    }
}
