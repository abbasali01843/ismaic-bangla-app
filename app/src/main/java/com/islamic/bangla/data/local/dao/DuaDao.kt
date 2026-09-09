package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.islamic.bangla.data.model.Dua
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDua(dua: Dua)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDuas(duaList: List<Dua>)

    @Query("SELECT * FROM dua ORDER BY duaId ASC")
    fun getAllDuas(): Flow<List<Dua>>

    @Query("SELECT * FROM dua WHERE duaId = :duaId")
    fun getDuaById(duaId: Int): Flow<Dua>

    @Query(
        """SELECT * FROM dua
           WHERE duaNameBangla LIKE '%' || :searchText || '%'
              OR banglaTranslation LIKE '%' || :searchText || '%'
              OR arabicText LIKE '%' || :searchText || '%'
              OR duaName LIKE '%' || :searchText || '%'
           ORDER BY duaId ASC"""
    )
    fun searchDuas(searchText: String): Flow<List<Dua>>

    @Query("SELECT * FROM dua WHERE category = :category ORDER BY duaId ASC")
    fun getDuasByCategory(category: String): Flow<List<Dua>>

    @Query("SELECT DISTINCT category FROM dua ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Query("DELETE FROM dua")
    suspend fun deleteAllDuas()
}
