package com.islamic.bangla.util

import android.content.Context
import android.content.Intent
import com.islamic.bangla.data.model.Ayah
import com.islamic.bangla.data.model.Dua
import com.islamic.bangla.data.model.Hadith

private val banglaDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

private fun String.toBanglaDigits(): String =
    map { if (it in '0'..'9') banglaDigits[it - '0'] else it }.joinToString("")

/** Formats ayahs/hadiths/duas as shareable Bangla text + opens the share sheet. */
object ShareHelper {
    private const val APP_TAG = "\n\n— ইসলামিক বাংলা অ্যাপ"

    fun ayahText(surahName: String, ayah: Ayah): String = buildString {
        appendLine("$surahName • আয়াত ${ayah.ayahNumber.toString().toBanglaDigits()}")
        appendLine()
        appendLine(ayah.arabicText)
        if (ayah.banglaTranslation.isNotBlank()) {
            appendLine()
            append(ayah.banglaTranslation)
        }
        append(APP_TAG)
    }

    fun hadithText(collectionName: String, hadith: Hadith): String = buildString {
        appendLine("$collectionName • হাদিস নং ${hadith.hadithNumber.toBanglaDigits()}")
        appendLine()
        if (hadith.arabicText.isNotBlank()) {
            appendLine(hadith.arabicText)
            appendLine()
        }
        append(hadith.banglaTranslation)
        append(APP_TAG)
    }

    fun duaText(dua: Dua): String = buildString {
        appendLine(dua.duaNameBangla)
        appendLine()
        appendLine(dua.arabicText)
        appendLine()
        append(dua.banglaTranslation)
        append(APP_TAG)
    }

    fun share(context: Context, text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, "শেয়ার করুন"))
    }
}
