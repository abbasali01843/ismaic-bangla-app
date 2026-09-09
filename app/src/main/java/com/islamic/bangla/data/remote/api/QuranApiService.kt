package com.islamic.bangla.data.remote.api

import com.islamic.bangla.data.remote.dto.QuranEditionsResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * api.alquran.cloud — Quran text + translations.
 * Example: surah/114/editions/quran-uthmani,bn.bengali
 */
interface QuranApiService {

    @GET("surah/{surah}/editions/{editions}")
    suspend fun getSurahEditions(
        @Path("surah") surah: Int,
        // Comma-separated list must not be percent-encoded.
        @Path(value = "editions", encoded = true) editions: String
    ): QuranEditionsResponse
}
