package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.Ayah
import com.islamic.bangla.data.model.Quran
import com.islamic.bangla.data.repository.AyahRepository
import com.islamic.bangla.data.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AyahUiState(
    val surah: Quran? = null,
    val ayahs: List<Ayah> = emptyList(),
    val isLoading: Boolean = false,
    /** True while an API refresh is running (cache still visible). */
    val isRefreshing: Boolean = false,
    /** True when the API failed and cached data (if any) is shown. */
    val offline: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AyahViewModel @Inject constructor(
    private val ayahRepository: AyahRepository,
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AyahUiState())
    val uiState: StateFlow<AyahUiState> = _uiState.asStateFlow()

    /** Cancels the previous load (and its child collectors) on retry / surah switch. */
    private var loadJob: Job? = null

    /**
     * API-first with cache fallback: show the Room cache immediately,
     * refresh from the API in parallel, fall back to cache on failure.
     */
    fun loadSurah(surahNumber: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = AyahUiState(isLoading = true)
            try {
                val surah = quranRepository.getSurahById(surahNumber).first()
                _uiState.value = _uiState.value.copy(surah = surah)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "সূরার তথ্য পাওয়া যায়নি"
                )
                return@launch
            }
            // Observe cache (instant, works offline).
            launch {
                ayahRepository.getAyahsBySurah(surahNumber).collect { ayahs ->
                    _uiState.value = _uiState.value.copy(
                        ayahs = ayahs,
                        isLoading = false
                    )
                }
            }
            // Refresh from API (fresh data overwrites the cache).
            launch {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                try {
                    ayahRepository.refreshAyahs(surahNumber)
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        offline = false,
                        error = null
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        offline = true
                    )
                }
            }
        }
    }

    fun retry(surahNumber: Int) = loadSurah(surahNumber)

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
