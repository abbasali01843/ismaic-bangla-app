package com.islamic.bangla.data.remote.api

import com.islamic.bangla.data.remote.dto.HadithEditionResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * fawazahmed0/hadith-api over jsdelivr CDN.
 * Example: editions/ben-bukhari.min.json
 */
interface HadithApiService {

    @GET("editions/{edition}.min.json")
    suspend fun getEdition(
        @Path("edition") edition: String
    ): HadithEditionResponse
}
