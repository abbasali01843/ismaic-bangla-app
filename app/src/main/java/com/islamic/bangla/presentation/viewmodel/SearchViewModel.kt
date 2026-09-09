package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.Ayah
import com.islamic.bangla.data.model.Dua
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.data.preferences.SettingsStore
import com.islamic.bangla.data.repository.AyahRepository
import com.islamic.bangla.data.repository.DuaRepository
import com.islamic.bangla.data.repository.HadithRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val filter: String = "all",
    val ayahResults: List<Ayah> = emptyList(),
    val hadithResults: List<Hadith> = emptyList(),
    val duaResults: List<Dua> = emptyList(),
    val isSearching: Boolean = false
)

/**
 * Global search across cached Quran ayahs, hadiths and duas.
 * Searches the offline cache (API-first sync happens on each content screen).
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val ayahRepository: AyahRepository,
    private val hadithRepository: HadithRepository,
    private val duaRepository: DuaRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val recentSearches: StateFlow<List<String>> = settingsStore.recentSearches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    ayahResults = emptyList(),
                    hadithResults = emptyList(),
                    duaResults = emptyList(),
                    isSearching = false
                )
            }
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(isSearching = true) }
            val trimmed = query.trim()
            val ayahs = ayahRepository.searchAyahs(trimmed).first().take(MAX_RESULTS)
            val hadiths = hadithRepository.searchHadiths(trimmed).first().take(MAX_RESULTS)
            val duas = duaRepository.searchDuas(trimmed).first().take(MAX_RESULTS)
            _uiState.update {
                it.copy(
                    ayahResults = ayahs,
                    hadithResults = hadiths,
                    duaResults = duas,
                    isSearching = false
                )
            }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(filter = filter) }
    }

    /** Persist the submitted query in recent searches. */
    fun submitSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            settingsStore.addRecentSearch(trimmed)
        }
    }

    fun clearRecent() {
        viewModelScope.launch {
            settingsStore.clearRecentSearches()
        }
    }

    companion object {
        private const val MAX_RESULTS = 30
    }
}
