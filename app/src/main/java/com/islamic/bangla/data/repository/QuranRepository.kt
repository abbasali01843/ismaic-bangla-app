package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.QuranDao
import com.islamic.bangla.data.model.Quran
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepository @Inject constructor(
    private val quranDao: QuranDao
) {
    fun getAllSurahs(): Flow<List<Quran>> = quranDao.getAllSurahs()

    fun getSurahById(surahNumber: Int): Flow<Quran> = quranDao.getSurahById(surahNumber)

    fun searchSurahs(searchText: String): Flow<List<Quran>> = quranDao.searchSurahs(searchText)

    suspend fun insertQuran(quran: Quran) = quranDao.insertQuran(quran)

    suspend fun insertAllQuran(quranList: List<Quran>) = quranDao.insertAllQuran(quranList)

    suspend fun deleteAllSurahs() = quranDao.deleteAllSurahs()
}
