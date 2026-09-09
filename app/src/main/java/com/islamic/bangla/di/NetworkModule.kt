package com.islamic.bangla.di

import com.islamic.bangla.data.remote.api.HadithApiService
import com.islamic.bangla.data.remote.api.PrayerApiService
import com.islamic.bangla.data.remote.api.QuranApiService
import com.islamic.bangla.data.remote.api.TafsirApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private fun buildRetrofit(client: OkHttpClient, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    @Named("alquran")
    fun provideAlquranRetrofit(client: OkHttpClient): Retrofit =
        buildRetrofit(client, "https://api.alquran.cloud/v1/")

    @Singleton
    @Provides
    @Named("aladhan")
    fun provideAladhanRetrofit(client: OkHttpClient): Retrofit =
        buildRetrofit(client, "https://api.aladhan.com/v1/")

    @Singleton
    @Provides
    @Named("hadith")
    fun provideHadithRetrofit(client: OkHttpClient): Retrofit =
        buildRetrofit(client, "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/")

    @Singleton
    @Provides
    @Named("quranCom")
    fun provideQuranComRetrofit(client: OkHttpClient): Retrofit =
        buildRetrofit(client, "https://api.quran.com/api/v4/")

    @Singleton
    @Provides
    fun provideQuranApi(@Named("alquran") retrofit: Retrofit): QuranApiService =
        retrofit.create(QuranApiService::class.java)

    @Singleton
    @Provides
    fun providePrayerApi(@Named("aladhan") retrofit: Retrofit): PrayerApiService =
        retrofit.create(PrayerApiService::class.java)

    @Singleton
    @Provides
    fun provideHadithApi(@Named("hadith") retrofit: Retrofit): HadithApiService =
        retrofit.create(HadithApiService::class.java)

    @Singleton
    @Provides
    fun provideTafsirApi(@Named("quranCom") retrofit: Retrofit): TafsirApiService =
        retrofit.create(TafsirApiService::class.java)
}
