package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.islamic.bangla.data.model.Ayah
import kotlinx.coroutines.flow.Flow

@Dao
interface AyahDao {
    @Insert
    suspend fun insertAyah(ayah: Ayah)

    @Insert
    suspend fun insertAllAyahs(ayahList: List<Ayah>)

    @Query("SELECT * FROM ayah WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun getAyahsBySurah(surahNumber: Int): Flow<List<Ayah>>

    @Query("SELECT * FROM ayah WHERE id = :ayahId")
    fun getAyahById(ayahId: Int): Flow<Ayah>

    @Query("SELECT * FROM ayah WHERE banglaTranslation LIKE '%' || :searchText || '%' ORDER BY surahNumber ASC")
    fun searchAyahs(searchText: String): Flow<List<Ayah>>

    @Query("SELECT * FROM ayah WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    fun getAyahByNumber(surahNumber: Int, ayahNumber: Int): Flow<Ayah>

    @Query("DELETE FROM ayah WHERE surahNumber = :surahNumber")
    suspend fun deleteAyahsBySurah(surahNumber: Int)

    @Query("DELETE FROM ayah")
    suspend fun deleteAllAyahs()
}
