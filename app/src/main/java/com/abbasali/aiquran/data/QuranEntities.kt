package com.abbasali.aiquran.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local cache schema only. Do not populate it from ordinary Quran Foundation
 * API responses when Content Sync is available for that resource.
 */
@Entity(
    tableName = "quran_verses",
    indices = [Index(value = ["surahNumber", "ayahNumber"], unique = true)]
)
data class QuranVerseEntity(
    @PrimaryKey val verseKey: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val juzNumber: Int? = null,
    val pageNumber: Int? = null,
    val contentSource: String,
    val contentVersion: String
)

@Entity(
    tableName = "translations",
    indices = [Index(value = ["verseKey", "languageCode", "editionId"], unique = true)]
)
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val verseKey: String,
    val languageCode: String,
    val editionId: String,
    val editionName: String,
    val translationText: String,
    val licenseId: String,
    val attribution: String
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val verseKey: String,
    val createdAtEpochMillis: Long,
    val note: String? = null
)
