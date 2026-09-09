package com.islamic.bangla.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamic.bangla.data.model.Bookmark
import com.islamic.bangla.data.model.Note
import com.islamic.bangla.presentation.components.EmptyState
import com.islamic.bangla.presentation.components.NoteEditorDialog
import com.islamic.bangla.presentation.components.NoteTarget
import com.islamic.bangla.presentation.viewmodel.ReadingViewModel

private fun typeLabel(type: String): String = when (type) {
    "ayah" -> "আয়াত"
    "hadith" -> "হাদিস"
    "dua" -> "দোয়া"
    else -> type
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onAyahClick: (surahNumber: Int, ayahNumber: Int) -> Unit,
    onHadithClick: () -> Unit,
    onDuaClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: ReadingViewModel = hiltViewModel()
) {
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(0) }
    var filter by rememberSaveable { mutableStateOf("all") }
    var noteTarget by remember { mutableStateOf<NoteTarget?>(null) }

    val visible = if (filter == "all") {
        bookmarks
    } else {
        bookmarks.filter { it.type == filter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("বুকমার্ক") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = tab) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text("বুকমার্ক (${bookmarks.size})") }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text("আমার নোট (${notes.size})") }
                )
            }
            if (tab == 0) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = filter == "all",
                            onClick = { filter = "all" },
                            label = { Text("সব") }
                        )
                    }
                    listOf("ayah", "hadith", "dua").forEach { type ->
                        item(key = type) {
                            FilterChip(
                                selected = filter == type,
                                onClick = { filter = type },
                                label = { Text(typeLabel(type)) }
                            )
                        }
                    }
                }
                if (visible.isEmpty()) {
                    EmptyState(
                        title = "কোনো বুকমার্ক নেই",
                        message = "আয়াত, হাদিস বা দোয়ার পাশের বুকমার্ক আইকনে চাপ দিয়ে সংরক্ষণ করুন"
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(visible, key = { it.id }) { bookmark ->
                            BookmarkCard(
                                bookmark = bookmark,
                                onClick = {
                                    when (bookmark.type) {
                                        "ayah" -> onAyahClick(
                                            bookmark.refId / 1000,
                                            bookmark.refId % 1000
                                        )
                                        "hadith" -> onHadithClick()
                                        "dua" -> onDuaClick()
                                    }
                                },
                                onDelete = {
                                    viewModel.toggleBookmark(
                                        bookmark.type,
                                        bookmark.refId,
                                        bookmark.title,
                                        bookmark.subtitle
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                if (notes.isEmpty()) {
                    EmptyState(
                        title = "কোনো নোট নেই",
                        message = "আয়াত, হাদিস বা দোয়ার পাশের নোট আইকনে চাপ দিয়ে নিজের নোট লিখুন"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = {
                                    when (note.type) {
                                        "ayah" -> onAyahClick(
                                            note.refId / 1000,
                                            note.refId % 1000
                                        )
                                        "hadith" -> onHadithClick()
                                        "dua" -> onDuaClick()
                                    }
                                },
                                onEdit = {
                                    noteTarget = NoteTarget(
                                        type = note.type,
                                        refId = note.refId,
                                        title = note.title
                                    )
                                },
                                onDelete = {
                                    viewModel.deleteNote(note.type, note.refId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    noteTarget?.let { target ->
        NoteEditorDialog(
            target = target,
            viewModel = viewModel,
            onDismiss = { noteTarget = null }
        )
    }
}

@Composable
private fun BookmarkCard(
    bookmark: Bookmark,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AssistChip(onClick = onClick, label = { Text(typeLabel(bookmark.type)) })
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (bookmark.subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = bookmark.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "মুছুন",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AssistChip(onClick = onClick, label = { Text(typeLabel(note.type)) })
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.noteText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "সম্পাদনা",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "মুছুন",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
