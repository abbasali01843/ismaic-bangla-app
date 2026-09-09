package com.islamic.bangla.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Offline cache of fetched tafsir text, keyed per tafsir + ayah. */
@Entity(
    tableName = "tafsir_cache",
    indices = [Index(value = ["slug", "surahNumber", "ayahNumber"], unique = true)]
)
data class TafsirCache(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val slug: String,
    val surahNumber: Int,
    val ayahNumber: Int,
    val text: String
)
