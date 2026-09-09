package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.data.repository.HadithRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HadithUiState(
    val hadiths: List<Hadith> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<Hadith> = emptyList(),
    val selectedHadith: Hadith? = null,
    val collections: List<String> = emptyList(),
    val selectedCollection: String? = null
)

@HiltViewModel
class HadithViewModel @Inject constructor(
    private val hadithRepository: HadithRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HadithUiState())
    val uiState: StateFlow<HadithUiState> = _uiState.asStateFlow()

    init {
        loadAllHadiths()
        loadCollections()
    }

    fun loadAllHadiths() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                hadithRepository.getAllHadiths().collect { hadiths ->
                    _uiState.value = _uiState.value.copy(
                        hadiths = hadiths,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load hadiths"
                )
            }
        }
    }

    fun loadHadithsByCollection(collection: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedCollection = collection)
            try {
                hadithRepository.getHadithsByCollection(collection).collect { hadiths ->
                    _uiState.value = _uiState.value.copy(
                        hadiths = hadiths,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load hadiths"
                )
            }
        }
    }

    private fun loadCollections() {
        viewModelScope.launch {
            try {
                hadithRepository.getHadithCollections().collect { collections ->
                    _uiState.value = _uiState.value.copy(collections = collections)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load collections"
                )
            }
        }
    }

    fun searchHadiths(query: String) {
        viewModelScope.launch {
            try {
                hadithRepository.searchHadiths(query).collect { results ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = results,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun selectHadith(hadith: Hadith) {
        _uiState.value = _uiState.value.copy(selectedHadith = hadith)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
