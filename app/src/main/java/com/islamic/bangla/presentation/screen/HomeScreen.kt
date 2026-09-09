package com.islamic.bangla.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamic.bangla.data.model.LastRead
import com.islamic.bangla.presentation.navigation.Screen
import com.islamic.bangla.presentation.viewmodel.QuranViewModel
import com.islamic.bangla.presentation.viewmodel.ReadingViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Feature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    quranViewModel: QuranViewModel = hiltViewModel(),
    readingViewModel: ReadingViewModel = hiltViewModel()
) {
    val quranState by quranViewModel.uiState.collectAsStateWithLifecycle()
    val lastRead by readingViewModel.lastRead.collectAsStateWithLifecycle()
    val bookmarkCount by readingViewModel.bookmarkCount.collectAsStateWithLifecycle()
    val today = SimpleDateFormat("d MMMM yyyy", Locale("bn")).format(Date())

    val features = listOf(
        Feature("আল-কুরআন", "তিলাওয়াত ও অনুবাদ", Icons.Filled.MenuBook, Screen.QuranList.route),
        Feature("হাদিস", "বাংলা অনুবাদসহ", Icons.Filled.LibraryBooks, Screen.HadithList.route),
        Feature("দোয়া ও যিকির", "দৈনন্দিন মাসনূন দোয়া", Icons.Filled.Favorite, Screen.DuaList.route),
        Feature("নামাজের সময়", "আজকের সময়সূচি", Icons.Filled.AccessTime, Screen.PrayerTimes.route),
        Feature("তাসবিহ", "যিকির কাউন্টার", Icons.Filled.Timer, Screen.Tasbih.route),
        Feature("কিবলা", "কিবলার দিক", Icons.Filled.Explore, Screen.Qibla.route),
        Feature("নামাজ শিক্ষা", "ওযু, রাকাত ও ধাপ", Icons.Filled.School, Screen.NamazGuide.route),
        Feature("৯৯ নাম", "আল্লাহর নামসমূহ", Icons.Filled.Star, Screen.AllahNames.route),
        Feature("৬ কালেমা", "আরবি ও অর্থসহ", Icons.Filled.Article, Screen.Kalima.route),
        Feature("যাকাত", "যাকাত হিসাব", Icons.Filled.Calculate, Screen.Zakat.route),
        Feature(
            title = "বুকমার্ক",
            subtitle = if (bookmarkCount > 0) "${bookmarkCount}টি সংরক্ষিত" else "সংরক্ষিত আয়াত",
            icon = Icons.Filled.Bookmark,
            route = Screen.Bookmarks.route
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ইসলামিক বাংলা") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "খুঁজুন")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "সেটিংস")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "আসসালামু আলাইকুম",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = today,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (quranState.surahs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${quranState.surahs.size}টি সূরা পড়ার জন্য প্রস্তুত",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            lastRead?.let { read ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LastReadCard(
                        lastRead = read,
                        onContinue = {
                            onNavigate(Screen.SurahDetail.createRoute(read.refId))
                        }
                    )
                }
            }
            items(features) { feature ->
                Card(onClick = { onNavigate(feature.route) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = feature.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = feature.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "অফলাইনে পড়ুন • বাংলায় শিখুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LastReadCard(
    lastRead: LastRead,
    onContinue: () -> Unit
) {
    Card(onClick = onContinue) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "সর্বশেষ পড়া",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lastRead.title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (lastRead.subtitle.isNotBlank()) {
                    Text(
                        text = lastRead.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "পড়া চালিয়ে যান",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
