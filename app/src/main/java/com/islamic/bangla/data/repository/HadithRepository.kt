package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.HadithDao
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.data.remote.HadithCollections
import com.islamic.bangla.data.remote.api.HadithApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth: Room cache.
 * [refreshCollection] implements API-first: fetch Bangla + Arabic editions
 * sequentially (Arabic best-effort), then cache. UI keeps showing the
 * cache (fallback).
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
     *
     * Never throws for API problems — every failure is returned as a typed
     * [HadithRefreshResult.Failure] so the UI can say what actually went
     * wrong instead of always blaming the internet connection.
     *
     * The two editions download sequentially (not in parallel): each file is
     * 5–15 MB and parallel parsing doubles the transient memory spike on
     * low-end devices. Arabic is best-effort — if only the Arabic edition
     * fails, the Bangla hadiths are still cached and the result reports
     * [HadithRefreshResult.Success.arabicComplete] == false.
     *
     * The Room cache is only touched after a successful download+map, so a
     * failed refresh always leaves previously cached hadiths intact.
     */
    suspend fun refreshCollection(collectionKey: String): HadithRefreshResult {
        val info = HadithCollections.byKey(collectionKey)
            ?: return HadithRefreshResult.Failure(HadithLoadError.UnknownCollection)

        val ben = try {
            hadithApi.getEdition("ben-$collectionKey")
        } catch (e: Exception) {
            return HadithRefreshResult.Failure(e.toHadithLoadError())
        }
        if (ben.hadiths.isEmpty()) {
            return HadithRefreshResult.Failure(HadithLoadError.EmptyCollection)
        }

        var arabicError: HadithLoadError? = null
        val ara = try {
            hadithApi.getEdition("ara-$collectionKey")
        } catch (e: Exception) {
            arabicError = e.toHadithLoadError()
            null
        }

        val sections = ben.metadata?.allSections().orEmpty()
        val arabicByNumber = ara?.hadiths.orEmpty().associateBy { it.number() }

        val entities = ben.hadiths.mapIndexed { index, hadith ->
            val number = hadith.number()
            val bookNo = hadith.reference?.bookNumber() ?: 0
            Hadith(
                // index keeps the key unique even if the API omits numbers.
                hadithId = info.index * 1_000_000 + index,
                collection = collectionKey,
                bookName = sections[bookNo.toString()]
                    .orEmpty()
                    .ifBlank { if (bookNo > 0) "Book $bookNo" else "" },
                hadithNumber = number.toString(),
                arabicText = arabicByNumber[number]?.text.orEmpty(),
                banglaTranslation = hadith.text.orEmpty(),
                englishTranslation = "",
                gradeOfAuthenticity = hadith.gradeText() ?: info.defaultGrade
            )
        }
        try {
            // Delete-then-insert keeps re-sync idempotent (primary keys are
            // positional, so REPLACE alone would leave stale rows behind).
            hadithDao.deleteHadithsByCollection(collectionKey)
            hadithDao.insertAllHadiths(entities)
        } catch (e: Exception) {
            return HadithRefreshResult.Failure(
                HadithLoadError.Unexpected(e.message)
            )
        }
        return HadithRefreshResult.Success(
            arabicComplete = ara != null,
            arabicError = arabicError
        )
    }
}
