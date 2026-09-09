package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.NoteDao
import com.islamic.bangla.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getNote(type: String, refId: Int): Flow<Note?> = noteDao.getNote(type, refId)

    /** Ids as "type:refId" for fast has-note lookups. */
    fun noteIds(): Flow<Set<String>> = noteDao.noteIds().map { it.toSet() }

    suspend fun saveNote(type: String, refId: Int, title: String, noteText: String) {
        if (noteText.isBlank()) {
            noteDao.deleteNote(type, refId)
        } else {
            noteDao.deleteNote(type, refId)
            noteDao.insertNote(
                Note(
                    type = type,
                    refId = refId,
                    title = title,
                    noteText = noteText.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteNote(type: String, refId: Int) = noteDao.deleteNote(type, refId)
}
