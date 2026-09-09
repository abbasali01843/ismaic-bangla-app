package com.islamic.bangla.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Saved bookmark: an ayah, hadith or dua. */
@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["type", "refId"], unique = true)]
)
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** "ayah", "hadith" or "dua". */
    val type: String,
    /** Ayah.id (surah*1000+ayah), Hadith.hadithId or Dua.duaId. */
    val refId: Int,
    val title: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

/** Single-row "continue reading" pointer (id is always 1). */
@Entity(tableName = "last_read")
data class LastRead(
    @PrimaryKey
    val id: Int = 1,
    /** "surah" for now (extendable). */
    val type: String,
    val refId: Int,
    val title: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * One personal note per item, keyed by (type, refId).
 * Same type/refId convention as [Bookmark].
 */
@Entity(
    tableName = "notes",
    indices = [Index(value = ["type", "refId"], unique = true)]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val refId: Int,
    val title: String = "",
    val noteText: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
