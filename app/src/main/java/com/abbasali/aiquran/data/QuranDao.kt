package com.abbasali.aiquran.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM quran_verses WHERE surahNumber = :surah ORDER BY ayahNumber")
    fun observeVerses(surah: Int): Flow<List<QuranVerseEntity>>

    @Query("SELECT COUNT(*) FROM quran_verses")
    suspend fun countVerses(): Int

    @Query("SELECT * FROM quran_verses WHERE verseKey = :verseKey LIMIT 1")
    suspend fun getVerse(verseKey: String): QuranVerseEntity?

    @Query("SELECT * FROM translations WHERE verseKey = :verseKey ORDER BY languageCode, editionName")
    fun observeTranslations(verseKey: String): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translations WHERE languageCode = :languageCode AND translationText LIKE '%' || :query || '%' LIMIT 100")
    suspend fun searchTranslations(query: String, languageCode: String): List<TranslationEntity>

    @Query("SELECT * FROM bookmarks ORDER BY createdAtEpochMillis DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT quran_verses.* FROM quran_verses INNER JOIN bookmarks ON quran_verses.verseKey = bookmarks.verseKey WHERE bookmarks.note = 'saved-ayah' ORDER BY bookmarks.createdAtEpochMillis DESC")
    fun observeBookmarkedVerses(): Flow<List<QuranVerseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE verseKey = :verseKey")
    suspend fun deleteBookmark(verseKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<QuranVerseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationEntity>)

    @Transaction
    suspend fun importContent(
        verses: List<QuranVerseEntity>,
        translations: List<TranslationEntity>
    ) {
        if (verses.isNotEmpty()) insertVerses(verses)
        if (translations.isNotEmpty()) insertTranslations(translations)
    }
}
