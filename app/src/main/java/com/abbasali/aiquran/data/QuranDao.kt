package com.abbasali.aiquran.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM quran_verses WHERE surahNumber = :surah ORDER BY ayahNumber")
    fun observeVerses(surah: Int): Flow<List<QuranVerseEntity>>

    @Query("SELECT * FROM quran_verses WHERE verseKey = :verseKey LIMIT 1")
    suspend fun getVerse(verseKey: String): QuranVerseEntity?

    @Query("SELECT * FROM translations WHERE verseKey = :verseKey ORDER BY languageCode, editionName")
    fun observeTranslations(verseKey: String): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translations WHERE languageCode = :languageCode AND translationText LIKE '%' || :query || '%' LIMIT 100")
    suspend fun searchTranslations(query: String, languageCode: String): List<TranslationEntity>

    @Query("SELECT * FROM bookmarks ORDER BY createdAtEpochMillis DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE verseKey = :verseKey")
    suspend fun deleteBookmark(verseKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<QuranVerseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationEntity>)
}
