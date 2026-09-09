package com.islamic.bangla.data.local.seed

/** Curated display order + Bangla labels for dua category keys. */
object DuaCategories {
    private val names = mapOf(
        "morning-evening" to "সকাল-সন্ধ্যা",
        "sleep" to "ঘুম",
        "food" to "খাবার",
        "home" to "বাড়ি",
        "mosque" to "মসজিদ",
        "travel" to "সফর",
        "distress" to "বিপদ ও দুশ্চিন্তা",
        "prayer" to "নামাজ",
        "ramadan" to "রমজান",
        "weather" to "প্রকৃতি",
        "health" to "রোগ-ব্যাধি",
        "daily" to "দৈনন্দিন"
    )

    fun banglaName(key: String): String = names[key] ?: key

    /** Orders DB keys in the curated order; unknown keys go last. */
    fun ordered(keys: List<String>): List<String> {
        val order = names.keys.toList()
        return keys.sortedBy { key ->
            order.indexOf(key).let { if (it == -1) Int.MAX_VALUE else it }
        }
    }
}
