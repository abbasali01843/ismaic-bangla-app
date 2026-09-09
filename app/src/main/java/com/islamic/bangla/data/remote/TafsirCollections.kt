package com.islamic.bangla.data.remote

/** Bengali tafsirs verified on api.quran.com v4 (resources/tafsirs). */
data class TafsirInfo(
    val slug: String,
    val banglaName: String
)

object Tafsirs {
    val all = listOf(
        TafsirInfo("bn-tafsir-ahsanul-bayaan", "আহসানুল বায়ান"),
        TafsirInfo("bn-tafseer-ibn-e-kaseer", "ইবনে কাসীর"),
        TafsirInfo("bn-tafsir-abu-bakr-zakaria", "আবু বকর যাকারিয়া"),
        TafsirInfo("tafisr-fathul-majid-bn", "ফাতহুল মাজীদ")
    )

    val defaultSlug: String = all.first().slug

    fun banglaName(slug: String): String =
        all.find { it.slug == slug }?.banglaName ?: slug
}
