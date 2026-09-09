package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.AyahDao
import com.islamic.bangla.data.model.Ayah
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AyahRepository @Inject constructor(
    private val ayahDao: AyahDao
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
}
