package com.islamic.bangla.data.remote.dto

import com.google.gson.JsonElement

/**
 * Response models for fawazahmed0/hadith-api (jsdelivr CDN).
 * GET /editions/{lang}-{book}.min.json
 *
 * hadith-api curates each book separately, so fields arrive with inconsistent
 * JSON types per book (e.g. arabicnumber is Int in some books, String like
 * "8.01" in others). Everything that varies is parsed leniently, so one odd
 * entry can never fail a whole 5–15 MB edition download.
 */
data class HadithEditionResponse(
    val metadata: HadithMetadataDto? = null,
    val hadiths: List<HadithDto> = emptyList()
)

data class HadithMetadataDto(
    val name: String = "",
    // Single-section endpoint uses "section", full-edition uses "sections".
    // Values are JsonElement: a single non-string value must not fail the
    // whole edition parse.
    val section: Map<String, JsonElement>? = null,
    val sections: Map<String, JsonElement>? = null
) {
    fun allSections(): Map<String, String> =
        (sections ?: section).orEmpty().mapValues { it.value.stringOrEmpty() }
}

/** Lenient Int reader for fields the API types inconsistently per book. */
private fun JsonElement?.intOrZero(): Int = try {
    when {
        this == null || isJsonNull -> 0
        !isJsonPrimitive -> 0
        else -> with(asJsonPrimitive) {
            when {
                isNumber -> asInt
                isString -> asString.filter { it.isDigit() }.toIntOrNull() ?: 0
                else -> 0
            }
        }
    }
} catch (e: Exception) {
    0
}

private fun JsonElement?.stringOrEmpty(): String = try {
    when {
        this == null || isJsonNull -> ""
        isJsonPrimitive -> asString
        else -> ""
    }
} catch (e: Exception) {
    ""
}

data class HadithDto(
    // Int in most books, but hadith-api curation is inconsistent per book
    // (like arabicnumber); parse leniently so one odd entry cannot fail
    // the whole edition download.
    val hadithnumber: JsonElement? = null,
    val arabicnumber: JsonElement? = null,
    // Nullable: an explicit null must not poison the Room insert later.
    val text: String? = null,
    val grades: List<JsonElement>? = null,
    val reference: HadithReferenceDto? = null
) {
    /** Stable numeric id of this hadith (0 when the API omits it). */
    fun number(): Int = hadithnumber.intOrZero()

    fun arabicNumberText(): String = arabicnumber.stringOrEmpty()

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
    // Lenient for the same per-book inconsistency as hadithnumber.
    val book: JsonElement? = null,
    val hadith: JsonElement? = null
) {
    fun bookNumber(): Int = book.intOrZero()

    fun hadithNumber(): Int = hadith.intOrZero()
}
