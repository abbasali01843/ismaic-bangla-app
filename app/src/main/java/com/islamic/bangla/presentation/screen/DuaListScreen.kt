package com.islamic.bangla.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamic.bangla.data.local.seed.DuaCategories
import com.islamic.bangla.data.model.Dua
import com.islamic.bangla.presentation.components.EmptyState
import com.islamic.bangla.presentation.components.NoteEditorDialog
import com.islamic.bangla.presentation.components.NoteTarget
import com.islamic.bangla.presentation.theme.ScaledArabicTextStyle
import com.islamic.bangla.presentation.theme.ScaledBanglaTextStyle
import com.islamic.bangla.presentation.viewmodel.DuaViewModel
import com.islamic.bangla.presentation.viewmodel.ReadingViewModel
import com.islamic.bangla.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuaListScreen(
    viewModel: DuaViewModel = hiltViewModel(),
    readingViewModel: ReadingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bookmarkedIds by readingViewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val noteIds by readingViewModel.noteIds.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var noteTarget by remember { mutableStateOf<NoteTarget?>(null) }

    val visibleDuas =
        if (query.isBlank()) uiState.duas else uiState.searchResults

    Scaffold(
        topBar = { TopAppBar(title = { Text("দোয়া ও যিকির") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    if (it.isBlank()) viewModel.clearSearch() else viewModel.searchDuas(it)
                },
                label = { Text("দোয়া খুঁজুন") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            if (uiState.categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.loadAllDuas() },
                            label = { Text("সব") }
                        )
                    }
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.loadDuasByCategory(category) },
                            label = { Text(DuaCategories.banglaName(category)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                visibleDuas.isEmpty() -> {
                    EmptyState(
                        title = "কোনো দোয়া পাওয়া যায়নি",
                        message = if (query.isNotBlank()) {
                            "অন্য শব্দে খুঁজে দেখুন"
                        } else {
                            "অন্য ক্যাটাগরি দেখুন"
                        }
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (query.isBlank()) {
                            item {
                                val label = uiState.selectedCategory
                                    ?.let { DuaCategories.banglaName(it) }
                                    ?: "সব দোয়া"
                                Text(
                                    text = "$label • ${visibleDuas.size}টি",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }
                        }
                        items(visibleDuas, key = { it.duaId }) { dua ->
                            DuaCard(
                                dua = dua,
                                isBookmarked = bookmarkedIds.contains("dua:${dua.duaId}"),
                                hasNote = noteIds.contains("dua:${dua.duaId}"),
                                onBookmarkClick = {
                                    readingViewModel.toggleBookmark(
                                        type = "dua",
                                        refId = dua.duaId,
                                        title = dua.duaNameBangla,
                                        subtitle = DuaCategories.banglaName(dua.category)
                                    )
                                },
                                onNoteClick = {
                                    noteTarget = NoteTarget(
                                        type = "dua",
                                        refId = dua.duaId,
                                        title = dua.duaNameBangla
                                    )
                                },
                                onShareClick = {
                                    ShareHelper.share(context, ShareHelper.duaText(dua))
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
            viewModel = readingViewModel,
            onDismiss = { noteTarget = null }
        )
    }
}

@Composable
private fun DuaCard(
    dua: Dua,
    isBookmarked: Boolean,
    hasNote: Boolean,
    onBookmarkClick: () -> Unit,
    onNoteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dua.duaNameBangla,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onNoteClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "নোট",
                        tint = if (hasNote) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "শেয়ার",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        imageVector = if (isBookmarked) {
                            Icons.Default.Bookmark
                        } else {
                            Icons.Default.BookmarkBorder
                        },
                        contentDescription = "বুকমার্ক",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dua.arabicText,
                style = ScaledArabicTextStyle(),
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = dua.banglaTranslation,
                style = ScaledBanglaTextStyle()
            )
            if (dua.benefitBangla.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ফজিলত: ${dua.benefitBangla}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
