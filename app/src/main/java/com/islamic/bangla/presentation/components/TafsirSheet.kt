package com.islamic.bangla.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.islamic.bangla.data.remote.Tafsirs
import com.islamic.bangla.presentation.theme.ScaledBanglaTextStyle
import com.islamic.bangla.presentation.viewmodel.TafsirViewModel

/** Identifies the ayah whose tafsir is shown. */
data class TafsirTarget(
    val surahNumber: Int,
    val ayahNumber: Int,
    val title: String
)

/** Bottom sheet showing Bangla tafsir with a tafsir selector. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafsirSheet(
    target: TafsirTarget,
    onDismiss: () -> Unit,
    viewModel: TafsirViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedSlug by viewModel.selectedSlug.collectAsStateWithLifecycle()

    LaunchedEffect(target.surahNumber, target.ayahNumber) {
        viewModel.openTafsir(target.surahNumber, target.ayahNumber)
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.clear()
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "তাফসির",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = target.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(0.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Tafsirs.all, key = { it.slug }) { tafsir ->
                    FilterChip(
                        selected = selectedSlug == tafsir.slug,
                        onClick = { viewModel.setTafsir(tafsir.slug) },
                        label = { Text(tafsir.banglaName) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(onClick = viewModel::retry) {
                            Text("আবার চেষ্টা করুন")
                        }
                    }
                }
                else -> {
                    Text(
                        text = uiState.text,
                        style = ScaledBanglaTextStyle(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}
