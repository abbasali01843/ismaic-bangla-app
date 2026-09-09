package com.islamic.bangla.data.remote.api

import com.islamic.bangla.data.remote.dto.TafsirResponse
import retrofit2.http.GET
import retrofit2.http.Path

/** Bangla tafsirs on api.quran.com v4 (free, no API key needed). */
interface TafsirApiService {
    @GET("tafsirs/{slug}/by_ayah/{ayahKey}")
    suspend fun getTafsir(
        @Path("slug") slug: String,
        @Path(value = "ayahKey", encoded = true) ayahKey: String
    ): TafsirResponse
}
