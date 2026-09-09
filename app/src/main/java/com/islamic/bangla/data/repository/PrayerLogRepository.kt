package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.PrayerLogDao
import com.islamic.bangla.data.model.PrayerDay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerLogRepository @Inject constructor(
    private val prayerLogDao: PrayerLogDao
) {
    fun getDay(date: String): Flow<PrayerDay?> = prayerLogDao.getDay(date)

    fun getRecentDays(limit: Int): Flow<List<PrayerDay>> =
        prayerLogDao.getRecentDays(limit)

    suspend fun togglePrayer(date: String, key: String) {
        val current = prayerLogDao.getDayOnce(date) ?: PrayerDay(date)
        val updated = when (key) {
            "fajr" -> current.copy(fajr = !current.fajr)
            "dhuhr" -> current.copy(dhuhr = !current.dhuhr)
            "asr" -> current.copy(asr = !current.asr)
            "maghrib" -> current.copy(maghrib = !current.maghrib)
            else -> current.copy(isha = !current.isha)
        }
        prayerLogDao.upsert(updated)
    }
}
