package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.islamic.bangla.data.model.PrayerTime
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(prayerTime: PrayerTime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPrayerTimes(prayerTimeList: List<PrayerTime>)

    @Query("SELECT * FROM prayer_times WHERE city = :city ORDER BY date ASC")
    fun getPrayerTimesByCity(city: String): Flow<List<PrayerTime>>

    @Query("SELECT * FROM prayer_times WHERE city = :city AND date = :date")
    fun getTodayPrayerTime(city: String, date: String): Flow<PrayerTime?>

    @Query("SELECT * FROM prayer_times ORDER BY date ASC")
    fun getAllPrayerTimes(): Flow<List<PrayerTime>>

    @Query("SELECT DISTINCT city FROM prayer_times")
    fun getCities(): Flow<List<String>>

    @Query("DELETE FROM prayer_times WHERE city = :city")
    suspend fun deletePrayerTimesByCity(city: String)

    @Query("DELETE FROM prayer_times")
    suspend fun deleteAllPrayerTimes()
}
