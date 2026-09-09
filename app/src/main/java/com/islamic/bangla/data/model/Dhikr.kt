package com.islamic.bangla.data.model

/** A dhikr for the tasbih counter (bundled in assets, not stored in Room). */
data class Dhikr(
    val id: Int,
    val arabicText: String,
    val banglaText: String,
    val transliteration: String,
    val targetCount: Int,
    val virtueBangla: String
)
