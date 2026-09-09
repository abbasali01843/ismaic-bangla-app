package com.islamic.bangla.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran")
data class Quran(
    @PrimaryKey
    val surahNumber: Int,
    val surahName: String,
    val surahNameBangla: String,
    val meaning: String,
    val meaningBangla: String,
    val totalAyahs: Int,
    val revelationType: String
)

@Entity(tableName = "ayah")
data class Ayah(
    @PrimaryKey
    val id: Int,
    val surahNumber: Int,
    val ayahNumber: Int,
    val globalNumber: Int = 0,
    val arabicText: String,
    val banglaTranslation: String,
    val englishTranslation: String
)

@Entity(tableName = "hadith")
data class Hadith(
    @PrimaryKey
    val hadithId: Int,
    val collection: String,
    val bookName: String,
    val hadithNumber: String,
    val arabicText: String,
    val banglaTranslation: String,
    val englishTranslation: String,
    val gradeOfAuthenticity: String
)

@Entity(tableName = "dua")
data class Dua(
    @PrimaryKey
    val duaId: Int,
    val category: String = "",
    val duaName: String,
    val duaNameBangla: String,
    val arabicText: String,
    val banglaTranslation: String,
    val englishTranslation: String,
    val benefit: String,
    val benefitBangla: String
)

@Entity(tableName = "prayer_times")
data class PrayerTime(
    @PrimaryKey
    val id: Int,
    val date: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val city: String,
    val country: String,
    val hijriDate: String = "",
    /** Sehri end time ("HH:mm") for the fasting countdown. */
    val imsak: String = ""
)
