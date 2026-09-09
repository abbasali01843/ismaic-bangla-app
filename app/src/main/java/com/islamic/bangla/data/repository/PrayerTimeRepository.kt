package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.PrayerTimeDao
import com.islamic.bangla.data.model.PrayerTime
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PrayerTimeRepository @Inject constructor(
    private val prayerTimeDao: PrayerTimeDao
) {
    fun getPrayerTimesByCity(city: String): Flow<List<PrayerTime>> = 
        prayerTimeDao.getPrayerTimesByCity(city)

    fun getTodayPrayerTime(city: String, date: String): Flow<PrayerTime> = 
        prayerTimeDao.getTodayPrayerTime(city, date)

    fun getAllPrayerTimes(): Flow<List<PrayerTime>> = prayerTimeDao.getAllPrayerTimes()

    suspend fun insertPrayerTime(prayerTime: PrayerTime) = prayerTimeDao.insertPrayerTime(prayerTime)

    suspend fun insertAllPrayerTimes(prayerTimeList: List<PrayerTime>) = 
        prayerTimeDao.insertAllPrayerTimes(prayerTimeList)

    suspend fun deletePrayerTimesByCity(city: String) = prayerTimeDao.deletePrayerTimesByCity(city)

    suspend fun deleteAllPrayerTimes() = prayerTimeDao.deleteAllPrayerTimes()
}
