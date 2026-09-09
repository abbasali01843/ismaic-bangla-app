package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.PrayerTimeDao
import com.islamic.bangla.data.model.PrayerTime
import com.islamic.bangla.data.remote.api.PrayerApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth: Room cache.
 * [refreshToday] implements API-first: fetch from Aladhan, then cache.
 * UI keeps showing the cache (fallback) while refresh runs or fails.
 */
@Singleton
class PrayerTimeRepository @Inject constructor(
    private val prayerTimeDao: PrayerTimeDao,
    private val prayerApi: PrayerApiService
) {
    fun getPrayerTimesByCity(city: String): Flow<List<PrayerTime>> =
        prayerTimeDao.getPrayerTimesByCity(city)

    fun getTodayPrayerTime(city: String, date: String): Flow<PrayerTime?> =
        prayerTimeDao.getTodayPrayerTime(city, date)

    fun getAllPrayerTimes(): Flow<List<PrayerTime>> = prayerTimeDao.getAllPrayerTimes()

    suspend fun insertPrayerTime(prayerTime: PrayerTime) = prayerTimeDao.insertPrayerTime(prayerTime)

    suspend fun insertAllPrayerTimes(prayerTimeList: List<PrayerTime>) =
        prayerTimeDao.insertAllPrayerTimes(prayerTimeList)

    suspend fun deletePrayerTimesByCity(city: String) = prayerTimeDao.deletePrayerTimesByCity(city)

    suspend fun deleteAllPrayerTimes() = prayerTimeDao.deleteAllPrayerTimes()

    /**
     * Fetch today's timings + Hijri date from the API and cache them.
     * Throws on network/API failure.
     */
    suspend fun refreshToday(city: String) {
        val data = prayerApi.getTimingsByCity(city).data
            ?: throw IllegalStateException("Empty API response")
        val timings = data.timings ?: throw IllegalStateException("Empty API response")

        val storedDate = data.date?.gregorian?.date.orEmpty().toIsoDate()
        val hijri = data.date?.hijri
        val hijriDate = listOfNotNull(
            hijri?.day?.takeIf { it.isNotBlank() },
            hijri?.month?.en?.takeIf { it.isNotBlank() },
            hijri?.year?.takeIf { it.isNotBlank() }?.let { "$it AH" }
        ).joinToString(" ")

        prayerTimeDao.insertPrayerTime(
            PrayerTime(
                id = "${city}_$storedDate".hashCode(),
                date = storedDate,
                fajr = timings.Fajr.cleanTime(),
                dhuhr = timings.Dhuhr.cleanTime(),
                asr = timings.Asr.cleanTime(),
                maghrib = timings.Maghrib.cleanTime(),
                isha = timings.Isha.cleanTime(),
                imsak = timings.Imsak.cleanTime(),
                city = city,
                country = "Bangladesh",
                hijriDate = hijriDate
            )
        )
    }

    /** "05:22 (+06)" -> "05:22" */
    private fun String.cleanTime(): String = substringBefore(" ").trim()

    /** "09-09-2026" (DD-MM-YYYY) -> "2026-09-09" (ISO). */
    private fun String.toIsoDate(): String {
        val parts = split("-")
        return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else this
    }
}
