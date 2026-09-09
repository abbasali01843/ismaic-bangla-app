package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.HadithDao
import com.islamic.bangla.data.model.Hadith
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HadithRepository @Inject constructor(
    private val hadithDao: HadithDao
) {
    fun getAllHadiths(): Flow<List<Hadith>> = hadithDao.getAllHadiths()

    fun getHadithsByCollection(collection: String): Flow<List<Hadith>> = 
        hadithDao.getHadithsByCollection(collection)

    fun getHadithById(hadithId: Int): Flow<Hadith> = hadithDao.getHadithById(hadithId)

    fun searchHadiths(searchText: String): Flow<List<Hadith>> = hadithDao.searchHadiths(searchText)

    fun getHadithsByGrade(grade: String): Flow<List<Hadith>> = hadithDao.getHadithsByGrade(grade)

    fun getHadithCollections(): Flow<List<String>> = hadithDao.getHadithCollections()

    suspend fun insertHadith(hadith: Hadith) = hadithDao.insertHadith(hadith)

    suspend fun insertAllHadiths(hadithList: List<Hadith>) = hadithDao.insertAllHadiths(hadithList)

    suspend fun deleteHadithsByCollection(collection: String) = 
        hadithDao.deleteHadithsByCollection(collection)

    suspend fun deleteAllHadiths() = hadithDao.deleteAllHadiths()
}
