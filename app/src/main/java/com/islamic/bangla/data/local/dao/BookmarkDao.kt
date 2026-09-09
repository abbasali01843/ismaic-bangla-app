package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.islamic.bangla.data.model.Bookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE type = :type AND refId = :refId")
    suspend fun delete(type: String, refId: Int)

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE type = :type ORDER BY timestamp DESC")
    fun getByType(type: String): Flow<List<Bookmark>>

    @Query("SELECT COUNT(*) FROM bookmarks WHERE type = :type AND refId = :refId")
    suspend fun count(type: String, refId: Int): Int
}
