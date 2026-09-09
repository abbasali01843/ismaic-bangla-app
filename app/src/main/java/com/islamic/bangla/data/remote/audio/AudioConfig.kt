package com.islamic.bangla.data.remote.audio

/** A verse-by-verse reciter on cdn.islamic.network (same provider as the Quran text API). */
data class Reciter(
    val edition: String,
    val banglaName: String,
    val englishName: String
)

object AudioConfig {
    val reciters = listOf(
        Reciter("ar.alafasy", "মিশারী আল-আফাসী", "Mishary Alafasy"),
        Reciter("ar.husary", "মাহমুদ খলিল আল-হুসারী", "Mahmud Khalil Al-Husary"),
        Reciter("ar.abdurrahmaansudais", "আব্দুর রহমান আস-সুদাইস", "Abdur-Rahman As-Sudais"),
        Reciter("ar.mahermuaiqly", "মাহের আল-মুয়াইকলি", "Maher Al-Muaiqly"),
        Reciter("ar.ahmedajamy", "আহমাদ আল-আজামী", "Ahmed Al-Ajamy")
    )

    const val DEFAULT_RECITER = "ar.alafasy"

    fun reciterName(edition: String): String =
        reciters.find { it.edition == edition }?.banglaName ?: edition

    /**
     * Verified pattern (from /v1/ayah/{n}/{edition} -> audio field):
     * https://cdn.islamic.network/quran/audio/128/{edition}/{globalAyah}.mp3
     */
    fun ayahAudioUrl(globalAyahNumber: Int, reciterEdition: String = DEFAULT_RECITER): String =
        "https://cdn.islamic.network/quran/audio/128/$reciterEdition/$globalAyahNumber.mp3"

    // ---- Offline downloads (per reciter) ----

    /** filesDir/audio/{edition}/{surah}/{ayah}.mp3 */
    fun localAyahFile(
        context: android.content.Context,
        edition: String,
        surahNumber: Int,
        ayahNumber: Int
    ): java.io.File =
        java.io.File(context.filesDir, "audio/$edition/$surahNumber/$ayahNumber.mp3")

    fun downloadedAyahCount(
        context: android.content.Context,
        edition: String,
        surahNumber: Int,
        totalAyahs: Int
    ): Int =
        (1..totalAyahs).count { localAyahFile(context, edition, surahNumber, it).exists() }

    fun deleteSurahAudio(
        context: android.content.Context,
        edition: String,
        surahNumber: Int
    ): Boolean =
        java.io.File(context.filesDir, "audio/$edition/$surahNumber").deleteRecursively()
}
