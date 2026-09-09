package com.islamic.bangla.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.islamic.bangla.data.audio.AzanPlayer
import com.islamic.bangla.data.preferences.SettingsStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/** Fires at each prayer time: shows the notification + plays the azan if enabled. */
@AndroidEntryPoint
class PrayerAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var azanPlayer: AzanPlayer

    @Inject
    lateinit var settingsStore: SettingsStore

    override fun onReceive(context: Context, intent: Intent) {
        val key = intent.getStringExtra(EXTRA_PRAYER_KEY) ?: return
        val name = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: key
        val appContext = context.applicationContext
        PrayerNotificationHelper.showPrayerNotification(appContext, key, name)

        val pending = goAsync()
        Thread {
            try {
                if (runBlocking { settingsStore.azanEnabled.first() }) {
                    azanPlayer.play(appContext)
                }
            } catch (_: Exception) {
                // Notification already shown; azan is best-effort.
            } finally {
                pending.finish()
            }
        }.start()
    }

    companion object {
        const val EXTRA_PRAYER_KEY = "prayer_key"
        const val EXTRA_PRAYER_NAME = "prayer_name"
    }
}
