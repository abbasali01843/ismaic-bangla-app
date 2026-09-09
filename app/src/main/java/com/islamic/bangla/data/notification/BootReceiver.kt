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

/**
 * Re-schedules prayer alarms when the schedule may be stale:
 * - after device reboot (alarms don't survive reboot)
 * - when the user changes the clock (TIME_SET)
 * - when the timezone changes (TIMEZONE_CHANGED)
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduler: PrayerAlarmScheduler

    @Inject
    lateinit var settingsStore: SettingsStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESCHEDULE_ACTIONS) return
        val pending = goAsync()
        scope.launch {
            try {
                val city = settingsStore.prayerCity.first()
                scheduler.scheduleAll(city)
            } catch (e: Exception) {
                // Scheduling will retry on next app open.
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
    }
}
