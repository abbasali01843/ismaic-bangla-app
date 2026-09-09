package com.islamic.bangla.di

import android.content.Context
import androidx.room.Room
import com.islamic.bangla.data.local.database.IslamicDatabase
import com.islamic.bangla.data.repository.AyahRepository
import com.islamic.bangla.data.repository.DuaRepository
import com.islamic.bangla.data.repository.HadithRepository
import com.islamic.bangla.data.repository.PrayerTimeRepository
import com.islamic.bangla.data.repository.QuranRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideIslamicDatabase(
        @ApplicationContext context: Context
    ): IslamicDatabase {
        return Room.databaseBuilder(
            context,
            IslamicDatabase::class.java,
            "islamic_database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideQuranRepository(database: IslamicDatabase): QuranRepository {
        return QuranRepository(database.quranDao())
    }

    @Singleton
    @Provides
    fun provideAyahRepository(database: IslamicDatabase): AyahRepository {
        return AyahRepository(database.ayahDao())
    }

    @Singleton
    @Provides
    fun provideHadithRepository(database: IslamicDatabase): HadithRepository {
        return HadithRepository(database.hadithDao())
    }

    @Singleton
    @Provides
    fun provideDuaRepository(database: IslamicDatabase): DuaRepository {
        return DuaRepository(database.duaDao())
    }

    @Singleton
    @Provides
    fun providePrayerTimeRepository(database: IslamicDatabase): PrayerTimeRepository {
        return PrayerTimeRepository(database.prayerTimeDao())
    }
}
