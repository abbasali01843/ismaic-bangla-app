package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.islamic.bangla.data.model.LastRead
import kotlinx.coroutines.flow.Flow

@Dao
interface LastReadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(lastRead: LastRead)

    @Query("SELECT * FROM last_read WHERE id = 1")
    fun get(): Flow<LastRead?>

    @Query("DELETE FROM last_read")
    suspend fun clear()
}
