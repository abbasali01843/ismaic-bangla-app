package com.islamic.bangla.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.islamic.bangla.data.preferences.SettingsStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Fires nightly (00:10) to schedule the next day's prayer alarms. */
@AndroidEntryPoint
class DailyRefreshReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduler: PrayerAlarmScheduler

    @Inject
    lateinit var settingsStore: SettingsStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        scope.launch {
            try {
                val city = settingsStore.prayerCity.first()
                scheduler.scheduleAll(city)
            } catch (e: Exception) {
                // Keep the chain alive even on failure.
            } finally {
                pending.finish()
            }
        }
    }
}
