package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.islamic.bangla.data.model.PrayerDay
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerLogDao {
    @Upsert
    suspend fun upsert(day: PrayerDay)

    @Query("SELECT * FROM prayer_log WHERE date = :date")
    fun getDay(date: String): Flow<PrayerDay?>

    @Query("SELECT * FROM prayer_log WHERE date = :date")
    suspend fun getDayOnce(date: String): PrayerDay?

    @Query("SELECT * FROM prayer_log ORDER BY date DESC LIMIT :limit")
    fun getRecentDays(limit: Int): Flow<List<PrayerDay>>
}
