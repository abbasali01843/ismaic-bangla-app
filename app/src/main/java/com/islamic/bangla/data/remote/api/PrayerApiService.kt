package com.islamic.bangla.data.remote.api

import com.islamic.bangla.data.remote.dto.AladhanResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * api.aladhan.com — prayer timings.
 * method=1 (University of Islamic Sciences, Karachi), widely used for Bangladesh.
 */
interface PrayerApiService {

    @GET("timingsByCity")
    suspend fun getTimingsByCity(
        @Query("city") city: String,
        @Query("country") country: String = "Bangladesh",
        @Query("method") method: Int = 1
    ): AladhanResponse
}
