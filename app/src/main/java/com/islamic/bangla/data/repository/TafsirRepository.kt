package com.islamic.bangla.data.repository

import androidx.core.text.HtmlCompat
import com.islamic.bangla.data.local.dao.TafsirDao
import com.islamic.bangla.data.model.TafsirCache
import com.islamic.bangla.data.remote.api.TafsirApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TafsirRepository @Inject constructor(
    private val tafsirDao: TafsirDao,
    private val tafsirApi: TafsirApiService
) {
    /**
     * Cache-first per ayah: instant offline re-reads, API fetch on first open.
     * Throws on network/API failure or empty text.
     */
    suspend fun getTafsir(surahNumber: Int, ayahNumber: Int, slug: String): String {
        tafsirDao.getTafsir(slug, surahNumber, ayahNumber)?.let { return it.text }
        val response = tafsirApi.getTafsir(slug, "$surahNumber:$ayahNumber")
        val text = response.tafsir?.text?.let { stripHtml(it) }.orEmpty()
        if (text.isBlank()) throw IllegalStateException("Empty tafsir response")
        tafsirDao.insertTafsir(
            TafsirCache(
                slug = slug,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                text = text
            )
        )
        return text
    }

    /** Some tafsir texts contain HTML tags/footnotes — render as plain text. */
    private fun stripHtml(html: String): String =
        HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
}
