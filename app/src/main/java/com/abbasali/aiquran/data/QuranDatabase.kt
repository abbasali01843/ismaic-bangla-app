package com.abbasali.aiquran.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [QuranVerseEntity::class, TranslationEntity::class, BookmarkEntity::class],
    version = 1,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var instance: QuranDatabase? = null

        fun getInstance(context: Context): QuranDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "ai-quran.db"
                ).build().also { instance = it }
            }
    }
}
