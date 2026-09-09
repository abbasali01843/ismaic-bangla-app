package com.islamic.bangla.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.islamic.bangla.data.local.dao.AyahDao
import com.islamic.bangla.data.local.dao.HadithDao
import com.islamic.bangla.data.local.dao.DuaDao
import com.islamic.bangla.data.local.dao.PrayerTimeDao
import com.islamic.bangla.data.local.dao.QuranDao
import com.islamic.bangla.data.model.Ayah
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.data.model.Dua
import com.islamic.bangla.data.model.PrayerTime
import com.islamic.bangla.data.model.Quran

@Database(
    entities = [
        Quran::class,
        Ayah::class,
        Hadith::class,
        Dua::class,
        PrayerTime::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IslamicDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun ayahDao(): AyahDao
    abstract fun hadithDao(): HadithDao
    abstract fun duaDao(): DuaDao
    abstract fun prayerTimeDao(): PrayerTimeDao
}
