package com.islamic.bangla.data.remote.dto

/**
 * Response models for api.alquran.cloud
 * GET /v1/surah/{surah}/editions/{editions}
 */
data class QuranEditionsResponse(
    val code: Int = 0,
    val status: String = "",
    val data: List<SurahEditionDto> = emptyList()
)

data class SurahEditionDto(
    val number: Int = 0,
    val ayahs: List<AyahDto> = emptyList(),
    val edition: EditionDto? = null
)

data class EditionDto(
    val identifier: String = ""
)

data class AyahDto(
    val number: Int = 0,
    val text: String = "",
    val numberInSurah: Int = 0
)
