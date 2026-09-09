package com.islamic.bangla.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamic.bangla.data.model.PrayerDay
import com.islamic.bangla.data.model.PrayerTime
import com.islamic.bangla.presentation.components.EmptyState
import com.islamic.bangla.presentation.components.OfflineBanner
import com.islamic.bangla.presentation.viewmodel.DayLog
import com.islamic.bangla.presentation.viewmodel.PrayerTimeViewModel
import java.util.Calendar
import kotlinx.coroutines.delay

private val bangladeshCities = listOf(
    "Dhaka", "Chattogram", "Sylhet", "Khulna",
    "Rajshahi", "Barishal", "Rangpur", "Mymensingh"
)

private val cityBanglaNames = mapOf(
    "Dhaka" to "ঢাকা",
    "Chattogram" to "চট্টগ্রাম",
    "Sylhet" to "সিলেট",
    "Khulna" to "খুলনা",
    "Rajshahi" to "রাজশাহী",
    "Barishal" to "বরিশাল",
    "Rangpur" to "রংপুর",
    "Mymensingh" to "ময়মনসিংহ"
)

private val banglaDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

private fun String.toBanglaDigits(): String =
    map { if (it in '0'..'9') banglaDigits[it - '0'] else it }.joinToString("")

private fun Int.toBanglaDigits(): String = toString().toBanglaDigits()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimeScreen(
    viewModel: PrayerTimeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val todayLog by viewModel.todayLog.collectAsStateWithLifecycle()
    val weekLog by viewModel.weekLog.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("নামাজের সময়") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (uiState.offline && uiState.todayPrayerTime != null) {
                OfflineBanner()
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = "শহর নির্বাচন করুন",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bangladeshCities) { city ->
                            FilterChip(
                                selected = uiState.selectedCity == city,
                                onClick = { viewModel.loadTodayPrayerTime(city) },
                                label = { Text(cityBanglaNames[city] ?: city) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                when {
                    uiState.isLoading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    uiState.todayPrayerTime == null -> {
                        item {
                            Box(modifier = Modifier.height(360.dp)) {
                                EmptyState(
                                    title = if (uiState.offline) {
                                        "ইন্টারনেট সংযোগ নেই"
                                    } else {
                                        "নামাজের সময় পাওয়া যায়নি"
                                    },
                                    message = "সংযোগ ফিরে এলে আবার চেষ্টা করুন",
                                    actionLabel = "আবার চেষ্টা করুন",
                                    onAction = { viewModel.retry() }
                                )
                            }
                        }
                    }
                    else -> {
                        val prayerTime = uiState.todayPrayerTime!!
                        item {
                            PrayerTimeCard(prayerTime = prayerTime)
                        }
                        if (prayerTime.imsak.isNotBlank() && prayerTime.maghrib.isNotBlank()) {
                            item {
                                FastingCard(prayerTime = prayerTime)
                            }
                        }
                        item {
                            TrackerCard(
                                prayerTime = prayerTime,
                                todayLog = todayLog,
                                streak = streak,
                                week = weekLog,
                                onToggle = viewModel::togglePrayer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeCard(prayerTime: PrayerTime) {
    val rows = listOf(
        "ফজর" to prayerTime.fajr,
        "যোহর" to prayerTime.dhuhr,
        "আসর" to prayerTime.asr,
        "মাগরিব" to prayerTime.maghrib,
        "এশা" to prayerTime.isha
    )
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = "${cityBanglaNames[prayerTime.city] ?: prayerTime.city} • ${prayerTime.date}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp)
            )
            if (prayerTime.hijriDate.isNotBlank()) {
                Text(
                    text = prayerTime.hijriDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            rows.forEachIndexed { index, (label, time) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = time,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                if (index < rows.lastIndex) {
                    Divider(modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun FastingCard(prayerTime: PrayerTime) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }
    // aladhan returns "Ramaḍān" with diacritics — strip marks before comparing.
    val isRamadan = java.text.Normalizer.normalize(prayerTime.hijriDate, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .contains("Ramadan")
    val (label, remainingMs, sub) = remember(prayerTime.imsak, prayerTime.maghrib, now) {
        fastingCountdown(prayerTime.imsak, prayerTime.maghrib, now)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRamadan) "রমজান মুবারক" else "রোজা",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "সেহরি শেষ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = prayerTime.imsak,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ইফতার",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = prayerTime.maghrib,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDuration(remainingMs),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            if (sub.isNotBlank()) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun fastingCountdown(imsak: String, maghrib: String, now: Long): Triple<String, Long, String> {
    val imsakMs = todayMillis(imsak, now)
    val maghribMs = todayMillis(maghrib, now)
    if (imsakMs == null || maghribMs == null) return Triple("", 0L, "")
    return when {
        now < imsakMs -> Triple("সেহরি শেষ হতে বাকি", imsakMs - now, "")
        now < maghribMs -> Triple("ইফতার বাকি", maghribMs - now, "")
        else -> Triple(
            "সেহরি শেষ হতে বাকি",
            imsakMs + 24 * 60 * 60 * 1000 - now,
            "(আগামীকাল)"
        )
    }
}

private fun todayMillis(hhmm: String, now: Long): Long? {
    val parts = hhmm.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun formatDuration(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        .toBanglaDigits()
}

@Composable
private fun TrackerCard(
    prayerTime: PrayerTime,
    todayLog: PrayerDay?,
    streak: Int,
    week: List<DayLog>,
    onToggle: (String) -> Unit
) {
    val rows = listOf(
        Triple("fajr", "ফজর", prayerTime.fajr),
        Triple("dhuhr", "যোহর", prayerTime.dhuhr),
        Triple("asr", "আসর", prayerTime.asr),
        Triple("maghrib", "মাগরিব", prayerTime.maghrib),
        Triple("isha", "এশা", prayerTime.isha)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আজকের নামাজ",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (streak > 0) {
                    Text(
                        text = "ধারাবাহিক ${streak.toBanglaDigits()} দিন",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            rows.forEach { (key, label, time) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(key) }
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = todayLog?.isPrayed(key) == true,
                        onCheckedChange = { onToggle(key) }
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
            if (week.isNotEmpty()) {
                Divider(modifier = Modifier.padding(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = day.weekDay,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (day.complete) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                            ) {
                                Text(
                                    text = if (day.complete) {
                                        "✓"
                                    } else {
                                        day.prayedCount.toBanglaDigits()
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (day.complete) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
