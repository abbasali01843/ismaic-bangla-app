package com.islamic.bangla.data.remote

/**
 * Hadith collections with verified Bengali editions on hadith-api
 * (editions "ben-{key}" + "ara-{key}").
 */
data class HadithCollectionInfo(
    val key: String,
    val banglaName: String,
    /** Unique numeric prefix for Room primary keys. */
    val index: Int,
    /** Default grade when the API provides none. */
    val defaultGrade: String
)

object HadithCollections {
    val all = listOf(
        HadithCollectionInfo("bukhari", "সহীহ বুখারী", 1, "সহীহ"),
        HadithCollectionInfo("muslim", "সহীহ মুসলিম", 2, "সহীহ"),
        HadithCollectionInfo("abudawud", "সুনান আবু দাউদ", 3, ""),
        HadithCollectionInfo("tirmidhi", "জামি আত-তিরমিযী", 4, ""),
        HadithCollectionInfo("nasai", "সুনান আন-নাসাঈ", 5, ""),
        HadithCollectionInfo("ibnmajah", "সুনান ইবনে মাজাহ", 6, "")
    )

    fun byKey(key: String): HadithCollectionInfo? = all.find { it.key == key }
}
