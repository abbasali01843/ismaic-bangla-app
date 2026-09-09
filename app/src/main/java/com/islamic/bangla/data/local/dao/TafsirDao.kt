package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.islamic.bangla.data.model.TafsirCache

@Dao
interface TafsirDao {
    @Query(
        "SELECT * FROM tafsir_cache " +
            "WHERE slug = :slug AND surahNumber = :surahNumber AND ayahNumber = :ayahNumber"
    )
    suspend fun getTafsir(slug: String, surahNumber: Int, ayahNumber: Int): TafsirCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafsir(entry: TafsirCache)

    @Query("DELETE FROM tafsir_cache")
    suspend fun deleteAll()
}
