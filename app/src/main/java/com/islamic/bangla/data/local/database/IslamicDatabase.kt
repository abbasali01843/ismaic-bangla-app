package com.islamic.bangla.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.islamic.bangla.data.local.dao.AyahDao
import com.islamic.bangla.data.local.dao.BookmarkDao
import com.islamic.bangla.data.local.dao.DuaDao
import com.islamic.bangla.data.local.dao.HadithDao
import com.islamic.bangla.data.local.dao.LastReadDao
import com.islamic.bangla.data.local.dao.NoteDao
import com.islamic.bangla.data.local.dao.PrayerLogDao
import com.islamic.bangla.data.local.dao.PrayerTimeDao
import com.islamic.bangla.data.local.dao.QuranDao
import com.islamic.bangla.data.local.dao.TafsirDao
import com.islamic.bangla.data.model.Ayah
import com.islamic.bangla.data.model.Bookmark
import com.islamic.bangla.data.model.Dua
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.data.model.LastRead
import com.islamic.bangla.data.model.Note
import com.islamic.bangla.data.model.PrayerDay
import com.islamic.bangla.data.model.PrayerTime
import com.islamic.bangla.data.model.Quran
import com.islamic.bangla.data.model.TafsirCache

// NOTE: Dhikr is intentionally NOT a Room entity — dhikrs load from assets via DhikrRepository.

@Database(
    entities = [
        Quran::class,
        Ayah::class,
        Hadith::class,
        Dua::class,
        PrayerTime::class,
        PrayerDay::class,
        Bookmark::class,
        LastRead::class,
        Note::class,
        TafsirCache::class
    ],
    version = 6,
    exportSchema = true
)
abstract class IslamicDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun ayahDao(): AyahDao
    abstract fun hadithDao(): HadithDao
    abstract fun duaDao(): DuaDao
    abstract fun prayerTimeDao(): PrayerTimeDao
    abstract fun prayerLogDao(): PrayerLogDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun lastReadDao(): LastReadDao
    abstract fun noteDao(): NoteDao
    abstract fun tafsirDao(): TafsirDao
}
