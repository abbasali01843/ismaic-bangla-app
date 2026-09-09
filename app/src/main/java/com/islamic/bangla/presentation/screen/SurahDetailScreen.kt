package com.islamic.bangla.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.islamic.bangla.data.audio.SurahDownloadState
import com.islamic.bangla.data.model.Ayah
import com.islamic.bangla.data.model.Quran
import com.islamic.bangla.presentation.components.EmptyState
import com.islamic.bangla.presentation.components.NoteEditorDialog
import com.islamic.bangla.presentation.components.NoteTarget
import com.islamic.bangla.presentation.components.OfflineBanner
import com.islamic.bangla.presentation.components.TafsirSheet
import com.islamic.bangla.presentation.components.TafsirTarget
import com.islamic.bangla.presentation.theme.ScaledArabicTextStyle
import com.islamic.bangla.presentation.theme.ScaledBanglaTextStyle
import com.islamic.bangla.presentation.viewmodel.AyahViewModel
import com.islamic.bangla.presentation.viewmodel.PlayerViewModel
import com.islamic.bangla.presentation.viewmodel.ReadingViewModel
import com.islamic.bangla.util.ShareHelper

private const val BISMILLAH = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"

private val banglaDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

private fun Int.toBanglaDigits(): String =
    toString().map { if (it in '0'..'9') banglaDigits[it - '0'] else it }.joinToString("")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    startAyah: Int = 0,
    onBack: () -> Unit,
    ayahViewModel: AyahViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    readingViewModel: ReadingViewModel = hiltViewModel()
) {
    val uiState by ayahViewModel.uiState.collectAsStateWithLifecycle()
    val playlistSurah by playerViewModel.playlistSurah.collectAsStateWithLifecycle()
    val playingAyah by playerViewModel.currentAyah.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val repeatMode by playerViewModel.repeatMode.collectAsStateWithLifecycle()
    val reciter by playerViewModel.reciter.collectAsStateWithLifecycle()
    val downloadState by playerViewModel.downloadState.collectAsStateWithLifecycle()
    val bookmarkedIds by readingViewModel.bookmarkedIds.collectAsStateWithLifecycle()
    val noteIds by readingViewModel.noteIds.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var noteTarget by remember { mutableStateOf<NoteTarget?>(null) }
    var tafsirTarget by remember { mutableStateOf<TafsirTarget?>(null) }
    val listState = rememberLazyListState()
    val hasBismillah = surahNumber != 1 && surahNumber != 9
    val headerCount = if (uiState.surah != null) if (hasBismillah) 2 else 1 else 0

    LaunchedEffect(surahNumber) {
        ayahViewModel.loadSurah(surahNumber)
    }

    LaunchedEffect(surahNumber, uiState.ayahs.size, reciter) {
        if (uiState.ayahs.isNotEmpty()) {
            playerViewModel.refreshDownload(surahNumber, uiState.ayahs.size)
        }
    }

    // Remember last-read surah.
    LaunchedEffect(uiState.surah) {
        uiState.surah?.let { surah ->
            readingViewModel.saveLastRead(
                type = "surah",
                refId = surah.surahNumber,
                title = surah.surahNameBangla,
                subtitle = surah.meaningBangla
            )
        }
    }

    // Jump to a bookmarked ayah on first load.
    var initialScrolled by remember(surahNumber, startAyah) { mutableStateOf(startAyah <= 0) }
    LaunchedEffect(uiState.ayahs) {
        if (!initialScrolled && uiState.ayahs.isNotEmpty()) {
            val pos = uiState.ayahs.indexOfFirst { it.ayahNumber == startAyah }
            if (pos >= 0) listState.scrollToItem(headerCount + pos)
            initialScrolled = true
        }
    }

    // Follow the recitation.
    LaunchedEffect(playingAyah, isPlaying, playlistSurah) {
        if (isPlaying && playlistSurah == surahNumber && playingAyah > 0) {
            val pos = uiState.ayahs.indexOfFirst { it.ayahNumber == playingAyah }
            if (pos >= 0) listState.animateScrollToItem(headerCount + pos)
        }
    }

    val thisSurahPlaying = playlistSurah == surahNumber && isPlaying

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.surah?.surahNameBangla ?: "সূরা") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    if (uiState.ayahs.isNotEmpty()) {
                        IconButton(onClick = { playerViewModel.cycleRepeatMode() }) {
                            Icon(
                                imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) {
                                    Icons.Default.RepeatOne
                                } else {
                                    Icons.Default.Repeat
                                },
                                contentDescription = "রিপিট",
                                tint = if (repeatMode == Player.REPEAT_MODE_OFF) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                        IconButton(onClick = {
                            uiState.surah?.let { surah ->
                                if (thisSurahPlaying) {
                                    playerViewModel.toggle()
                                } else {
                                    playerViewModel.playAyahs(
                                        surah = surah,
                                        ayahs = uiState.ayahs,
                                        startAyahNumber = if (playlistSurah == surahNumber) {
                                            playingAyah
                                        } else {
                                            uiState.ayahs.first().ayahNumber
                                        }
                                    )
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (thisSurahPlaying) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                                contentDescription = if (thisSurahPlaying) "থামান" else "তিলাওয়াত"
                            )
                        }
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
            if (uiState.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (uiState.offline && uiState.ayahs.isNotEmpty()) {
                OfflineBanner()
            }
            when {
                uiState.isLoading || (uiState.ayahs.isEmpty() && uiState.isRefreshing) -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.ayahs.isEmpty() -> {
                    EmptyState(
                        title = if (uiState.offline) "ইন্টারনেট সংযোগ নেই" else "আয়াত পাওয়া যায়নি",
                        message = if (uiState.offline) {
                            "সংযোগ ফিরে এলে আবার চেষ্টা করুন"
                        } else {
                            "পুনরায় চেষ্টা করুন"
                        },
                        actionLabel = "আবার চেষ্টা করুন",
                        onAction = { ayahViewModel.retry(surahNumber) }
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        uiState.surah?.let { surah ->
                            item {
                                SurahHeaderCard(
                                    surah = surah,
                                    isPlaying = thisSurahPlaying,
                                    downloadState = downloadState,
                                    onPlayClick = {
                                        if (thisSurahPlaying) {
                                            playerViewModel.toggle()
                                        } else {
                                            playerViewModel.playAyahs(
                                                surah = surah,
                                                ayahs = uiState.ayahs,
                                                startAyahNumber = if (playlistSurah == surahNumber) {
                                                    playingAyah
                                                } else {
                                                    uiState.ayahs.first().ayahNumber
                                                }
                                            )
                                        }
                                    },
                                    onDownloadClick = {
                                        playerViewModel.downloadSurah(surah, uiState.ayahs)
                                    },
                                    onCancelDownload = { playerViewModel.cancelDownload() },
                                    onDeleteDownload = {
                                        playerViewModel.deleteDownload(
                                            surahNumber,
                                            uiState.ayahs.size
                                        )
                                    }
                                )
                            }
                            // Bismillah header (Surah Al-Fatihah and At-Tawbah excluded)
                            if (hasBismillah) {
                                item {
                                    Text(
                                        text = BISMILLAH,
                                        style = ScaledArabicTextStyle(),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                        items(uiState.ayahs, key = { it.id }) { ayah ->
                            val isCurrent = playlistSurah == surahNumber &&
                                playingAyah == ayah.ayahNumber
                            val ayahTitle = uiState.surah?.let {
                                "${it.surahNameBangla} • আয়াত ${ayah.ayahNumber.toBanglaDigits()}"
                            } ?: "আয়াত ${ayah.ayahNumber}"
                            AyahCard(
                                ayah = ayah,
                                isCurrent = isCurrent,
                                isPlaying = isCurrent && isPlaying,
                                isBookmarked = bookmarkedIds.contains("ayah:${ayah.id}"),
                                hasNote = noteIds.contains("ayah:${ayah.id}"),
                                onPlayClick = {
                                    uiState.surah?.let { surah ->
                                        if (isCurrent && isPlaying) {
                                            playerViewModel.toggle()
                                        } else {
                                            playerViewModel.playAyahs(
                                                surah = surah,
                                                ayahs = uiState.ayahs,
                                                startAyahNumber = ayah.ayahNumber
                                            )
                                        }
                                    }
                                },
                                onBookmarkClick = {
                                    readingViewModel.toggleBookmark(
                                        type = "ayah",
                                        refId = ayah.id,
                                        title = ayahTitle,
                                        subtitle = ayah.arabicText.take(80)
                                    )
                                },
                                onNoteClick = {
                                    noteTarget = NoteTarget(
                                        type = "ayah",
                                        refId = ayah.id,
                                        title = ayahTitle
                                    )
                                },
                                onShareClick = {
                                    ShareHelper.share(
                                        context,
                                        ShareHelper.ayahText(
                                            uiState.surah?.surahNameBangla
                                                ?: "সূরা $surahNumber",
                                            ayah
                                        )
                                    )
                                },
                                onTafsirClick = {
                                    tafsirTarget = TafsirTarget(
                                        surahNumber = surahNumber,
                                        ayahNumber = ayah.ayahNumber,
                                        title = ayahTitle
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

    tafsirTarget?.let { target ->
        TafsirSheet(
            target = target,
            onDismiss = { tafsirTarget = null }
        )
    }
}

@Composable
private fun SurahHeaderCard(
    surah: Quran,
    isPlaying: Boolean,
    downloadState: SurahDownloadState,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onCancelDownload: () -> Unit,
    onDeleteDownload: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surah.surahName,
                style = ScaledArabicTextStyle(),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${surah.meaningBangla} • ${surah.totalAyahs} আয়াত",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(onClick = onPlayClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPlaying) "থামান" else "তিলাওয়াত শুনুন")
            }
            Spacer(modifier = Modifier.height(8.dp))
            when {
                downloadState.downloaded -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "অফলাইনে শোনা যাবে",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onDeleteDownload) {
                            Text("মুছুন")
                        }
                    }
                }
                downloadState.progress >= 0 -> {
                    LinearProgressIndicator(
                        progress = downloadState.progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${downloadState.downloadedCount.toBanglaDigits()}/${downloadState.totalCount.toBanglaDigits()} ডাউনলোড হচ্ছে",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onCancelDownload) {
                            Text("বাতিল")
                        }
                    }
                }
                else -> {
                    OutlinedButton(onClick = onDownloadClick) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("অফলাইনে শুনতে ডাউনলোড")
                    }
                    if (downloadState.error != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = downloadState.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AyahCard(
    ayah: Ayah,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    hasNote: Boolean,
    onPlayClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onNoteClick: () -> Unit,
    onShareClick: () -> Unit,
    onTafsirClick: () -> Unit
) {
    Card(
        colors = if (isCurrent) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ayah.ayahNumber.toBanglaDigits(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onPlayClick) {
                    Icon(
                        imageVector = if (isPlaying) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = "এই আয়াত থেকে শুনুন",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = ayah.arabicText,
                style = ScaledArabicTextStyle(),
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            if (ayah.banglaTranslation.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = ayah.banglaTranslation,
                    style = ScaledBanglaTextStyle()
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onTafsirClick) {
                    Text("তাফসির দেখুন")
                }
            }
        }
    }
}
