package com.islamic.bangla.data.model

/** One of the 99 Names of Allah (bundled in allah_names.json). */
data class AllahName(
    val id: Int,
    val arabic: String,
    val banglaName: String,
    val meaningBangla: String,
    val meaningEnglish: String
)
