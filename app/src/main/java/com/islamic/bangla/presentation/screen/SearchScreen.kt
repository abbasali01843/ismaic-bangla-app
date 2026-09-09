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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamic.bangla.data.model.Ayah
import com.islamic.bangla.data.model.Dua
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.presentation.components.EmptyState
import com.islamic.bangla.presentation.viewmodel.SearchViewModel

private fun snippet(text: String, maxLength: Int = 150): String {
    val singleLine = text.replace('\n', ' ').trim()
    return if (singleLine.length <= maxLength) singleLine else singleLine.take(maxLength) + "…"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onAyahClick: (surahNumber: Int, ayahNumber: Int) -> Unit,
    onHadithClick: () -> Unit,
    onDuaClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recents by viewModel.recentSearches.collectAsStateWithLifecycle()
    val totalCount = uiState.ayahResults.size + uiState.hadithResults.size + uiState.duaResults.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("খুঁজুন") },
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
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("কুরআন, হাদিস, দোয়া খুঁজুন") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "মুছুন")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.submitSearch(uiState.query) }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            if (uiState.query.isBlank()) {
                if (recents.isEmpty()) {
                    EmptyState(
                        title = "সবকিছু এক জায়গায় খুঁজুন",
                        message = "উপরে লিখে কুরআনের আয়াত, হাদিস বা দোয়া খুঁজুন (বাংলা বা আরবিতে)"
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সাম্প্রতিক খোঁজ",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = viewModel::clearRecent) {
                            Text("সব মুছুন")
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(recents) { recent ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onQueryChange(recent) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = recent,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.filter == "all",
                            onClick = { viewModel.setFilter("all") },
                            label = { Text("সব ($totalCount)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filter == "ayah",
                            onClick = { viewModel.setFilter("ayah") },
                            label = { Text("আয়াত (${uiState.ayahResults.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filter == "hadith",
                            onClick = { viewModel.setFilter("hadith") },
                            label = { Text("হাদিস (${uiState.hadithResults.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filter == "dua",
                            onClick = { viewModel.setFilter("dua") },
                            label = { Text("দোয়া (${uiState.duaResults.size})") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.isSearching) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (!uiState.isSearching && totalCount == 0) {
                    EmptyState(
                        title = "কিছু পাওয়া যায়নি",
                        message = "অন্য শব্দে খুঁজে দেখুন — শুধু ডাউনলোড হওয়া কনটেন্টে খোঁজা হয়"
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (uiState.filter == "all" || uiState.filter == "ayah") {
                            if (uiState.ayahResults.isNotEmpty()) {
                                item(key = "header_ayah") {
                                    SearchSectionHeader("আয়াত")
                                }
                            }
                            items(uiState.ayahResults, key = { it.id }) { ayah ->
                                AyahResultCard(
                                    ayah = ayah,
                                    onClick = { onAyahClick(ayah.surahNumber, ayah.ayahNumber) }
                                )
                            }
                        }
                        if (uiState.filter == "all" || uiState.filter == "hadith") {
                            if (uiState.hadithResults.isNotEmpty()) {
                                item(key = "header_hadith") {
                                    SearchSectionHeader("হাদিস")
                                }
                            }
                            items(uiState.hadithResults, key = { it.hadithId }) { hadith ->
                                HadithResultCard(hadith = hadith, onClick = onHadithClick)
                            }
                        }
                        if (uiState.filter == "all" || uiState.filter == "dua") {
                            if (uiState.duaResults.isNotEmpty()) {
                                item(key = "header_dua") {
                                    SearchSectionHeader("দোয়া")
                                }
                            }
                            items(uiState.duaResults, key = { it.duaId }) { dua ->
                                DuaResultCard(dua = dua, onClick = onDuaClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AyahResultCard(ayah: Ayah, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "সূরা ${ayah.surahNumber} • আয়াত ${ayah.ayahNumber}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = snippet(ayah.banglaTranslation.ifBlank { ayah.arabicText }),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithResultCard(hadith: Hadith, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = snippet(hadith.banglaTranslation.ifBlank { hadith.arabicText }),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuaResultCard(dua: Dua, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dua.duaNameBangla,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = snippet(dua.banglaTranslation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}
