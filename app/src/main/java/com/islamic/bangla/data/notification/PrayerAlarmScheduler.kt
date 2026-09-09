package com.islamic.bangla.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.islamic.bangla.data.model.PrayerTime
import com.islamic.bangla.data.preferences.SettingsStore
import com.islamic.bangla.data.repository.PrayerTimeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

data class PrayerDef(
    val key: String,
    val banglaName: String,
    val requestCode: Int
)

/**
 * Schedules exact alarms for the 5 daily prayers + a daily re-scheduling alarm.
 * Times come from the API first, with the Room cache as fallback.
 */
@Singleton
class PrayerAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prayerRepository: PrayerTimeRepository,
    private val settingsStore: SettingsStore
) {
    val prayers = listOf(
        PrayerDef("fajr", "ফজর", 101),
        PrayerDef("dhuhr", "যোহর", 102),
        PrayerDef("asr", "আসর", 103),
        PrayerDef("maghrib", "মাগরিব", 104),
        PrayerDef("isha", "এশা", 105)
    )

    fun banglaName(key: String): String =
        prayers.find { it.key == key }?.banglaName ?: key

    suspend fun scheduleAll(city: String) {
        if (!settingsStore.notificationsEnabled.first()) return

        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) return

        val times = getTimesForScheduling(city) ?: return

        prayers.forEach { prayer ->
            if (!settingsStore.prayerEnabled(prayer.key).first()) {
                cancel(alarmManager, prayer.requestCode)
                return@forEach
            }
            val triggerAt = triggerMillis(timeFor(times, prayer.key)) ?: return@forEach
            // Skip times that already passed (with a 30s grace window).
            if (triggerAt <= System.currentTimeMillis() + 30_000) return@forEach

            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_KEY, prayer.key)
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayer.banglaName)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, prayer.requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent
            )
        }
        scheduleDailyRefresh(alarmManager)
    }

    fun cancelAll() {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        prayers.forEach { cancel(alarmManager, it.requestCode) }
        val refreshIntent = Intent(context, DailyRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REFRESH_REQUEST_CODE, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /** Re-runs scheduling every night so the next day's alarms are always set. */
    private fun scheduleDailyRefresh(alarmManager: AlarmManager) {
        val next = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 10)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val intent = Intent(context, DailyRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REFRESH_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent
        )
    }

    private fun cancel(alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * API first, cache fallback. If even today's cache is missing, reuse the
     * latest cached day (times shift only ~1 min/day) as an approximation.
     */
    private suspend fun getTimesForScheduling(city: String): PrayerTime? {
        try {
            prayerRepository.refreshToday(city)
        } catch (e: Exception) {
            // Fall through to cache.
        }
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        prayerRepository.getTodayPrayerTime(city, today).first()?.let { return it }
        return prayerRepository.getPrayerTimesByCity(city).first().maxByOrNull { it.date }
    }

    private fun timeFor(times: PrayerTime, key: String): String = when (key) {
        "fajr" -> times.fajr
        "dhuhr" -> times.dhuhr
        "asr" -> times.asr
        "maghrib" -> times.maghrib
        else -> times.isha
    }

    /** "HH:mm" today -> epoch millis, or null if unparseable. */
    private fun triggerMillis(hhmm: String): Long? {
        val parts = hhmm.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].take(2).toIntOrNull() ?: return null
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        private const val REFRESH_REQUEST_CODE = 109
    }
}
