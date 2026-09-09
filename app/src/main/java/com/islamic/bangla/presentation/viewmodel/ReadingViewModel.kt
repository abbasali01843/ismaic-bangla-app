package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.Bookmark
import com.islamic.bangla.data.model.LastRead
import com.islamic.bangla.data.model.Note
import com.islamic.bangla.data.repository.BookmarkRepository
import com.islamic.bangla.data.repository.LastReadRepository
import com.islamic.bangla.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Bookmarks, last-read position and personal notes for ayahs, hadiths and duas.
 * Bookmark/note ids are exposed as "type:refId" strings for fast UI lookups.
 */
@HiltViewModel
class ReadingViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val lastReadRepository: LastReadRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    val bookmarks: StateFlow<List<Bookmark>> = bookmarkRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val bookmarkedIds: StateFlow<Set<String>> = bookmarkRepository.getAll()
        .map { list -> list.map { "${it.type}:${it.refId}" }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    val bookmarkCount: StateFlow<Int> = bookmarkRepository.getAll()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val lastRead: StateFlow<LastRead?> = lastReadRepository.get()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val notes: StateFlow<List<Note>> = noteRepository.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val noteIds: StateFlow<Set<String>> = noteRepository.noteIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    val noteCount: StateFlow<Int> = notes
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun toggleBookmark(type: String, refId: Int, title: String, subtitle: String) {
        viewModelScope.launch {
            bookmarkRepository.toggle(type, refId, title, subtitle)
        }
    }

    fun saveLastRead(type: String, refId: Int, title: String, subtitle: String) {
        viewModelScope.launch {
            lastReadRepository.save(
                LastRead(type = type, refId = refId, title = title, subtitle = subtitle)
            )
        }
    }

    fun getNote(type: String, refId: Int): Flow<Note?> =
        noteRepository.getNote(type, refId)

    fun saveNote(type: String, refId: Int, title: String, noteText: String) {
        viewModelScope.launch {
            noteRepository.saveNote(type, refId, title, noteText)
        }
    }

    fun deleteNote(type: String, refId: Int) {
        viewModelScope.launch {
            noteRepository.deleteNote(type, refId)
        }
    }
}
