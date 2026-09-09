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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.data.remote.HadithCollections
import com.islamic.bangla.data.repository.HadithLoadError
import com.islamic.bangla.presentation.components.EmptyState
import com.islamic.bangla.presentation.components.NoteEditorDialog
import com.islamic.bangla.presentation.components.NoteTarget
import com.islamic.bangla.presentation.components.OfflineBanner
import com.islamic.bangla.presentation.theme.ScaledArabicTextStyle
import com.islamic.bangla.presentation.theme.ScaledBanglaTextStyle
import com.islamic.bangla.presentation.viewmodel.HadithViewModel
import com.islamic.bangla.presentation.viewmodel.ReadingViewModel
import com.islamic.bangla.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HadithListScreen(
    viewModel: HadithViewModel = hiltViewModel(),
    readingViewModel: ReadingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bookmarkedIds by readingViewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val noteIds by readingViewModel.noteIds.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var noteTarget by remember { mutableStateOf<NoteTarget?>(null) }

    val visibleHadiths =
        if (query.isBlank()) uiState.hadiths else uiState.searchResults
    val selectedName = uiState.collections
        .find { it.key == uiState.selectedCollection }?.banglaName.orEmpty()

    Scaffold(
        topBar = { TopAppBar(title = { Text("হাদিস") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.collections, key = { it.key }) { collection ->
                    FilterChip(
                        selected = uiState.selectedCollection == collection.key,
                        onClick = { viewModel.selectCollection(collection.key) },
                        label = { Text(collection.banglaName) }
                    )
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    if (it.isBlank()) viewModel.clearSearch() else viewModel.searchHadiths(it)
                },
                label = { Text("হাদিস খুঁজুন") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "ইন্টারনেট থেকে $selectedName ডাউনলোড হচ্ছে…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            val syncError = uiState.syncError
            if (syncError != null && uiState.hadiths.isNotEmpty()) {
                OfflineBanner(message = syncError.bannerMessage())
            }
            if (uiState.arabicMissing && uiState.hadiths.isNotEmpty()) {
                OfflineBanner(message = "আরবি পাঠ লোড হয়নি — শুধু বাংলা দেখছেন")
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
                visibleHadiths.isEmpty() && !uiState.isSyncing -> {
                    EmptyState(
                        title = when {
                            query.isNotBlank() -> "কোনো হাদিস পাওয়া যায়নি"
                            syncError != null -> syncError.title()
                            else -> "কোনো হাদিস পাওয়া যায়নি"
                        },
                        message = when {
                            query.isNotBlank() -> "অন্য শব্দে খুঁজে দেখুন"
                            syncError != null -> syncError.message()
                            else -> "পুনরায় চেষ্টা করুন"
                        },
                        actionLabel = "আবার চেষ্টা করুন",
                        onAction = {
                            uiState.selectedCollection?.let { viewModel.selectCollection(it) }
                        }
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (query.isBlank() && visibleHadiths.isNotEmpty()) {
                            item {
                                Text(
                                    text = "$selectedName • ${visibleHadiths.size} হাদিস",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }
                        }
                        items(visibleHadiths, key = { it.hadithId }) { hadith ->
                            val collectionName =
                                HadithCollections.byKey(hadith.collection)?.banglaName
                                    ?: hadith.collection
                            val hadithTitle =
                                "$collectionName • হাদিস নং ${hadith.hadithNumber}"
                            HadithCard(
                                hadith = hadith,
                                isBookmarked = bookmarkedIds.contains("hadith:${hadith.hadithId}"),
                                hasNote = noteIds.contains("hadith:${hadith.hadithId}"),
                                onBookmarkClick = {
                                    readingViewModel.toggleBookmark(
                                        type = "hadith",
                                        refId = hadith.hadithId,
                                        title = hadithTitle,
                                        subtitle = hadith.bookName
                                    )
                                },
                                onNoteClick = {
                                    noteTarget = NoteTarget(
                                        type = "hadith",
                                        refId = hadith.hadithId,
                                        title = hadithTitle
                                    )
                                },
                                onShareClick = {
                                    ShareHelper.share(
                                        context,
                                        ShareHelper.hadithText(collectionName, hadith)
                                    )
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
private fun HadithCard(
    hadith: Hadith,
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "হাদিস নং ${hadith.hadithNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (hadith.bookName.isNotBlank()) {
                        Text(
                            text = hadith.bookName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (hadith.gradeOfAuthenticity.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        label = { Text(hadith.gradeOfAuthenticity) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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
            if (hadith.arabicText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hadith.arabicText,
                    style = ScaledArabicTextStyle(),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = hadith.banglaTranslation,
                style = ScaledBanglaTextStyle()
            )
        }
    }
}

/** Empty-state title for each refresh failure — never blames the internet. */
private fun HadithLoadError.title(): String = when (this) {
    HadithLoadError.NoInternet -> "ইন্টারনেট সংযোগ নেই"
    is HadithLoadError.ServerError -> "সার্ভার থেকে তথ্য পাওয়া যায়নি"
    HadithLoadError.DataFormatError -> "তথ্য পড়তে সমস্যা হয়েছে"
    HadithLoadError.UnknownCollection -> "অজানা সংগ্রহ"
    HadithLoadError.EmptyCollection -> "সংগ্রহে কোনো হাদিস নেই"
    is HadithLoadError.Unexpected -> "কিছু ভুল হয়েছে"
}

private fun HadithLoadError.message(): String = when (this) {
    HadithLoadError.NoInternet -> "সংযোগ ফিরে এলে আবার চেষ্টা করুন"
    is HadithLoadError.ServerError -> "সার্ভার ত্রুটি (কোড $httpCode) — পরে আবার চেষ্টা করুন"
    HadithLoadError.DataFormatError -> "সার্ভারের তথ্য বোঝা যায়নি — পরে আবার চেষ্টা করুন"
    HadithLoadError.UnknownCollection -> "এই হাদিস সংগ্রহটি পাওয়া যায়নি"
    HadithLoadError.EmptyCollection -> "পরে আবার চেষ্টা করুন"
    is HadithLoadError.Unexpected -> "পরে আবার চেষ্টা করুন"
}

/** Short banner shown above cached hadiths when a refresh failed. */
private fun HadithLoadError.bannerMessage(): String = when (this) {
    HadithLoadError.NoInternet -> "অফলাইন — সংরক্ষিত তথ্য দেখছেন"
    is HadithLoadError.ServerError -> "সার্ভার ত্রুটি — সংরক্ষিত তথ্য দেখছেন"
    HadithLoadError.DataFormatError -> "নতুন তথ্য পড়া যায়নি — সংরক্ষিত তথ্য দেখছেন"
    HadithLoadError.UnknownCollection -> "সংগ্রহ পাওয়া যায়নি — সংরক্ষিত তথ্য দেখছেন"
    HadithLoadError.EmptyCollection -> "সংগ্রহ খালি — সংরক্ষিত তথ্য দেখছেন"
    is HadithLoadError.Unexpected -> "সংরক্ষিত তথ্য দেখছেন"
}
