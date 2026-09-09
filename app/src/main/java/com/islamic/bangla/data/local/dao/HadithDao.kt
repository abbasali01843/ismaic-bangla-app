package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.islamic.bangla.data.model.Hadith
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHadith(hadith: Hadith)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHadiths(hadithList: List<Hadith>)

    @Query("SELECT * FROM hadith ORDER BY collection ASC")
    fun getAllHadiths(): Flow<List<Hadith>>

    @Query("SELECT * FROM hadith WHERE collection = :collection ORDER BY hadithNumber ASC")
    fun getHadithsByCollection(collection: String): Flow<List<Hadith>>

    @Query("SELECT * FROM hadith WHERE hadithId = :hadithId")
    fun getHadithById(hadithId: Int): Flow<Hadith>

    @Query("SELECT * FROM hadith WHERE banglaTranslation LIKE '%' || :searchText || '%' OR arabicText LIKE '%' || :searchText || '%' ORDER BY collection ASC")
    fun searchHadiths(searchText: String): Flow<List<Hadith>>

    @Query("SELECT * FROM hadith WHERE gradeOfAuthenticity = :grade ORDER BY collection ASC")
    fun getHadithsByGrade(grade: String): Flow<List<Hadith>>

    @Query("SELECT DISTINCT collection FROM hadith")
    fun getHadithCollections(): Flow<List<String>>

    @Query("DELETE FROM hadith WHERE collection = :collection")
    suspend fun deleteHadithsByCollection(collection: String)

    @Query("DELETE FROM hadith")
    suspend fun deleteAllHadiths()
}
