package com.islamic.bangla.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One row per day: which of the 5 prayers were offered. */
@Entity(tableName = "prayer_log")
data class PrayerDay(
    @PrimaryKey
    val date: String,
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false
) {
    fun prayedCount(): Int =
        listOf(fajr, dhuhr, asr, maghrib, isha).count { it }

    fun isComplete(): Boolean = prayedCount() == 5

    fun isPrayed(key: String): Boolean = when (key) {
        "fajr" -> fajr
        "dhuhr" -> dhuhr
        "asr" -> asr
        "maghrib" -> maghrib
        else -> isha
    }
}
