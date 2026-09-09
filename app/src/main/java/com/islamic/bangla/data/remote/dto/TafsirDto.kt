package com.islamic.bangla.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Response of GET tafsirs/{slug}/by_ayah/{surah}:{ayah} on api.quran.com v4. */
data class TafsirResponse(
    val tafsir: TafsirDto?
)

data class TafsirDto(
    @SerializedName("resource_name")
    val resourceName: String?,
    val slug: String?,
    val text: String?
)
