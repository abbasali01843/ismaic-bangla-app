package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.Quran
import com.islamic.bangla.data.repository.QuranRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuranUiState(
    val surahs: List<Quran> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Quran> = emptyList(),
    val selectedSurah: Quran? = null
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    /** Each keystroke cancels the previous search collector. */
    private var searchJob: Job? = null

    init {
        loadAllSurahs()
    }

    fun loadAllSurahs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                quranRepository.getAllSurahs().collect { surahs ->
                    _uiState.value = _uiState.value.copy(
                        surahs = surahs,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "সূরার তালিকা লোড করা যায়নি"
                )
            }
        }
    }

    fun searchSurahs(query: String) {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        searchJob = viewModelScope.launch {
            try {
                quranRepository.searchSurahs(query).collect { results ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = results,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "অনুসন্ধান ব্যর্থ হয়েছে"
                )
            }
        }
    }

    fun selectSurah(surah: Quran) {
        _uiState.value = _uiState.value.copy(selectedSurah = surah)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
