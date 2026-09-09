package com.islamic.bangla.data

import com.islamic.bangla.data.remote.api.HadithApiService
import com.islamic.bangla.data.remote.dto.HadithDto
import com.islamic.bangla.data.remote.dto.HadithEditionResponse
import com.islamic.bangla.data.remote.dto.HadithMetadataDto

/** Scriptable [HadithApiService] fake; records every requested edition. */
class FakeHadithApi : HadithApiService {
    val requestedEditions = mutableListOf<String>()

    /** Return value / exception per edition, e.g. "ben-bukhari". */
    var handler: (String) -> HadithEditionResponse = { HadithEditionResponse() }

    override suspend fun getEdition(edition: String): HadithEditionResponse {
        requestedEditions += edition
        return handler(edition)
    }

    companion object {
        fun editionOf(vararg hadiths: HadithDto): HadithEditionResponse =
            HadithEditionResponse(metadata = HadithMetadataDto(), hadiths = hadiths.toList())
    }
}
