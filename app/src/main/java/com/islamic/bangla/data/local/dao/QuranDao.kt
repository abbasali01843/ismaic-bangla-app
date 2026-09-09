package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.islamic.bangla.data.model.Quran
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Insert
    suspend fun insertQuran(quran: Quran)

    @Insert
    suspend fun insertAllQuran(quranList: List<Quran>)

    @Query("SELECT * FROM quran ORDER BY surahNumber ASC")
    fun getAllSurahs(): Flow<List<Quran>>

    @Query("SELECT * FROM quran WHERE surahNumber = :surahNumber")
    fun getSurahById(surahNumber: Int): Flow<Quran>

    @Query("SELECT * FROM quran WHERE surahNameBangla LIKE '%' || :searchText || '%'")
    fun searchSurahs(searchText: String): Flow<List<Quran>>

    @Query("DELETE FROM quran")
    suspend fun deleteAllSurahs()
}
