package com.islamic.bangla.di

import android.content.Context
import androidx.room.Room
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
import com.islamic.bangla.data.local.database.IslamicDatabase
import com.islamic.bangla.data.local.database.MIGRATION_5_6
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IslamicDatabase =
        Room.databaseBuilder(context, IslamicDatabase::class.java, "islamic_database")
            .addMigrations(MIGRATION_5_6)
            // Upgrades always migrate (never wipe). Only a version *downgrade*
            // (installing an older APK over a newer DB) recreates the schema.
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideQuranDao(database: IslamicDatabase): QuranDao = database.quranDao()

    @Provides
    fun provideAyahDao(database: IslamicDatabase): AyahDao = database.ayahDao()

    @Provides
    fun provideHadithDao(database: IslamicDatabase): HadithDao = database.hadithDao()

    @Provides
    fun provideDuaDao(database: IslamicDatabase): DuaDao = database.duaDao()

    @Provides
    fun providePrayerTimeDao(database: IslamicDatabase): PrayerTimeDao =
        database.prayerTimeDao()

    @Provides
    fun providePrayerLogDao(database: IslamicDatabase): PrayerLogDao =
        database.prayerLogDao()

    @Provides
    fun provideBookmarkDao(database: IslamicDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideLastReadDao(database: IslamicDatabase): LastReadDao = database.lastReadDao()

    @Provides
    fun provideNoteDao(database: IslamicDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideTafsirDao(database: IslamicDatabase): TafsirDao = database.tafsirDao()
}
