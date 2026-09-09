package com.islamic.bangla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.islamic.bangla.data.model.Note
import kotlinx.coroutines.flow.Flow

/** One personal note per item, keyed by (type, refId). */
@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM notes WHERE type = :type AND refId = :refId")
    suspend fun deleteNote(type: String, refId: Int)

    @Query("SELECT * FROM notes WHERE type = :type AND refId = :refId")
    fun getNote(type: String, refId: Int): Flow<Note?>

    @Query("SELECT type || ':' || refId FROM notes")
    fun noteIds(): Flow<List<String>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}
