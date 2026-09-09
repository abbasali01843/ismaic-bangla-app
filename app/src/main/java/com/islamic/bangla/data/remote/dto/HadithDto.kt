package com.islamic.bangla.data.remote.dto

import com.google.gson.JsonElement

/**
 * Response models for fawazahmed0/hadith-api (jsdelivr CDN).
 * GET /editions/{lang}-{book}.min.json
 *
 * Note: [HadithDto.arabicnumber] is mixed-type (Int in some books,
 * String like "8.01" in others), hence [JsonElement].
 */
data class HadithEditionResponse(
    val metadata: HadithMetadataDto? = null,
    val hadiths: List<HadithDto> = emptyList()
)

data class HadithMetadataDto(
    val name: String = "",
    // Single-section endpoint uses "section", full-edition uses "sections".
    val section: Map<String, String>? = null,
    val sections: Map<String, String>? = null
) {
    fun allSections(): Map<String, String> = sections ?: section.orEmpty()
}

data class HadithDto(
    val hadithnumber: Int = 0,
    val arabicnumber: JsonElement? = null,
    val text: String = "",
    val grades: List<JsonElement>? = null,
    val reference: HadithReferenceDto? = null
) {
    fun arabicNumberText(): String = try {
        when {
            arabicnumber == null || arabicnumber.isJsonNull -> ""
            arabicnumber.isJsonPrimitive -> arabicnumber.asString
            else -> ""
        }
    } catch (e: Exception) {
        ""
    }

    /** Best-effort grade extraction; grade objects vary per book. */
    fun gradeText(): String? = try {
        grades.orEmpty().firstNotNullOfOrNull { element ->
            if (!element.isJsonObject) return@firstNotNullOfOrNull null
            val obj = element.asJsonObject
            val grade = if (obj.has("grade")) obj.get("grade")?.asString else null
            val name = if (obj.has("name")) obj.get("name")?.asString else null
            (grade ?: name)?.takeIf { it.isNotBlank() }
        }
    } catch (e: Exception) {
        null
    }
}

data class HadithReferenceDto(
    val book: Int = 0,
    val hadith: Int = 0
)
