package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.AyahDao
import com.islamic.bangla.data.model.Ayah
import com.islamic.bangla.data.remote.api.QuranApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth: Room cache.
 * [refreshAyahs] implements API-first: fetch from network, then cache.
 * UI keeps showing the cache (fallback) while refresh runs or fails.
 */
@Singleton
class AyahRepository @Inject constructor(
    private val ayahDao: AyahDao,
    private val quranApi: QuranApiService
) {
    fun getAyahsBySurah(surahNumber: Int): Flow<List<Ayah>> = ayahDao.getAyahsBySurah(surahNumber)

    fun getAyahById(ayahId: Int): Flow<Ayah> = ayahDao.getAyahById(ayahId)

    fun searchAyahs(searchText: String): Flow<List<Ayah>> = ayahDao.searchAyahs(searchText)

    fun getAyahByNumber(surahNumber: Int, ayahNumber: Int): Flow<Ayah> =
        ayahDao.getAyahByNumber(surahNumber, ayahNumber)

    suspend fun insertAyah(ayah: Ayah) = ayahDao.insertAyah(ayah)

    suspend fun insertAllAyahs(ayahList: List<Ayah>) = ayahDao.insertAllAyahs(ayahList)

    suspend fun deleteAyahsBySurah(surahNumber: Int) = ayahDao.deleteAyahsBySurah(surahNumber)

    suspend fun deleteAllAyahs() = ayahDao.deleteAllAyahs()

    /**
     * Fetch Arabic (Uthmani) + Bangla (Muhiuddin Khan) ayahs from the API
     * and cache them in Room. Throws on network/API failure.
     */
    suspend fun refreshAyahs(surahNumber: Int) {
        val response = quranApi.getSurahEditions(surahNumber, EDITIONS)
        val byEdition = response.data.associateBy { it.edition?.identifier }
        val arabic = byEdition[ARABIC_EDITION]?.ayahs.orEmpty()
            .associateBy { it.numberInSurah }
        val bangla = byEdition[BANGLA_EDITION]?.ayahs.orEmpty()
            .associateBy { it.numberInSurah }
        if (arabic.isEmpty()) throw IllegalStateException("Empty API response")

        val ayahs = arabic.map { (numberInSurah, ayah) ->
            Ayah(
                id = surahNumber * 1000 + numberInSurah,
                surahNumber = surahNumber,
                ayahNumber = numberInSurah,
                globalNumber = ayah.number,
                arabicText = ayah.text,
                banglaTranslation = bangla[numberInSurah]?.text.orEmpty(),
                englishTranslation = ""
            )
        }
        ayahDao.insertAllAyahs(ayahs)
    }

    companion object {
        private const val ARABIC_EDITION = "quran-uthmani"
        private const val BANGLA_EDITION = "bn.bengali"
        private const val EDITIONS = "$ARABIC_EDITION,$BANGLA_EDITION"
    }
}
