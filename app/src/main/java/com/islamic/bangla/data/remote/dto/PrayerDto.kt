package com.islamic.bangla.data.remote.dto

/**
 * Response models for api.aladhan.com
 * GET /v1/timingsByCity?city={city}&country=Bangladesh&method=1
 */
data class AladhanResponse(
    val code: Int = 0,
    val status: String = "",
    val data: AladhanDataDto? = null
)

data class AladhanDataDto(
    val timings: PrayerTimingsDto? = null,
    val date: AladhanDateDto? = null
)

data class PrayerTimingsDto(
    val Imsak: String = "",
    val Fajr: String = "",
    val Dhuhr: String = "",
    val Asr: String = "",
    val Maghrib: String = "",
    val Isha: String = ""
)

data class AladhanDateDto(
    val hijri: HijriDto? = null,
    val gregorian: GregorianDto? = null
)

data class HijriDto(
    val day: String = "",
    val month: HijriMonthDto? = null,
    val year: String = ""
)

data class HijriMonthDto(
    val en: String = ""
)

data class GregorianDto(
    // DD-MM-YYYY, e.g. "09-09-2026"
    val date: String = ""
)
