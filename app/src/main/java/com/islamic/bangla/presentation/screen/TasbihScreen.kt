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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamic.bangla.presentation.components.EmptyState
import com.islamic.bangla.presentation.theme.ArabicTextStyle
import com.islamic.bangla.presentation.viewmodel.TasbihViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(
    onBack: () -> Unit,
    viewModel: TasbihViewModel = hiltViewModel()
) {
    val haptic = LocalHapticFeedback.current
    val selectedId by viewModel.selectedId.collectAsStateWithLifecycle()
    val count by viewModel.count.collectAsStateWithLifecycle()
    val total by viewModel.total.collectAsStateWithLifecycle()
    val dhikr = viewModel.selectedDhikr
    val targetReached = dhikr != null && count >= dhikr.targetCount && dhikr.targetCount > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("তাসবিহ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "রিসেট")
                    }
                }
            )
        }
    ) { padding ->
        if (dhikr == null) {
            Box(modifier = Modifier.padding(padding)) {
                EmptyState(
                    title = "যিকির পাওয়া যায়নি",
                    message = "অ্যাপ পুনরায় চালু করে দেখুন"
                )
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(viewModel.dhikrs, key = { it.id }) { item ->
                    FilterChip(
                        selected = item.id == selectedId,
                        onClick = { viewModel.selectDhikr(item.id) },
                        label = { Text(item.banglaText) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dhikr.arabicText,
                        style = ArabicTextStyle,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "লক্ষ্য: ${dhikr.targetCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = if (dhikr.targetCount > 0) {
                        (count.toFloat() / dhikr.targetCount).coerceIn(0f, 1f)
                    } else 0f,
                    modifier = Modifier.size(180.dp),
                    strokeWidth = 10.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "মোট: $total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (targetReached) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "মাশাআল্লাহ! লক্ষ্য পূর্ণ হয়েছে",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.tap()
                    haptic.performHapticFeedback(
                        if (count + 1 >= dhikr.targetCount) {
                            HapticFeedbackType.LongPress
                        } else {
                            HapticFeedbackType.TextHandleMove
                        }
                    )
                },
                shape = CircleShape,
                modifier = Modifier.size(120.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "তাপ\nকরুন",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = dhikr.virtueBangla,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}
