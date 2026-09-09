package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.HadithDao
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.data.remote.HadithCollections
import com.islamic.bangla.data.remote.api.HadithApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth: Room cache.
 * [refreshCollection] implements API-first: fetch Bangla + Arabic editions
 * in parallel, then cache. UI keeps showing the cache (fallback).
 */
@Singleton
class HadithRepository @Inject constructor(
    private val hadithDao: HadithDao,
    private val hadithApi: HadithApiService
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

    /**
     * Fetch a full collection (Bangla + Arabic) from the API and cache it.
     * Throws on network/API failure.
     */
    suspend fun refreshCollection(collectionKey: String) {
        val info = HadithCollections.byKey(collectionKey)
            ?: throw IllegalArgumentException("Unknown collection: $collectionKey")

        val (ben, ara) = coroutineScope {
            val benDeferred = async { hadithApi.getEdition("ben-$collectionKey") }
            val araDeferred = async { hadithApi.getEdition("ara-$collectionKey") }
            benDeferred.await() to araDeferred.await()
        }
        if (ben.hadiths.isEmpty()) throw IllegalStateException("Empty API response")

        val sections = ben.metadata?.allSections().orEmpty()
        val arabicByNumber = ara.hadiths.associateBy { it.hadithnumber }

        val entities = ben.hadiths.map { hadith ->
            val bookNo = hadith.reference?.book ?: 0
            Hadith(
                hadithId = info.index * 1_000_000 + hadith.hadithnumber,
                collection = collectionKey,
                bookName = sections[bookNo.toString()]
                    .orEmpty()
                    .ifBlank { if (bookNo > 0) "Book $bookNo" else "" },
                hadithNumber = hadith.hadithnumber.toString(),
                arabicText = arabicByNumber[hadith.hadithnumber]?.text.orEmpty(),
                banglaTranslation = hadith.text,
                englishTranslation = "",
                gradeOfAuthenticity = hadith.gradeText() ?: info.defaultGrade
            )
        }
        hadithDao.insertAllHadiths(entities)
    }
}
