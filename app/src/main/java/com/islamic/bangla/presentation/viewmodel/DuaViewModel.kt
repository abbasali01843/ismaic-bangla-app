package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.Dua
import com.islamic.bangla.data.local.seed.DuaCategories
import com.islamic.bangla.data.repository.DuaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuaUiState(
    val duas: List<Dua> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<Dua> = emptyList(),
    val selectedDua: Dua? = null,
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null
)

@HiltViewModel
class DuaViewModel @Inject constructor(
    private val duaRepository: DuaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuaUiState())
    val uiState: StateFlow<DuaUiState> = _uiState.asStateFlow()

    /** Guards the `duas` list: switching filters cancels the previous collector. */
    private var listJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadAllDuas()
        loadCategories()
    }

    fun loadAllDuas() {
        listJob?.cancel()
        listJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedCategory = null)
            try {
                duaRepository.getAllDuas().collect { duas ->
                    _uiState.value = _uiState.value.copy(
                        duas = duas,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "দোয়ার তালিকা লোড করা যায়নি"
                )
            }
        }
    }

    fun loadDuasByCategory(category: String) {
        listJob?.cancel()
        listJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedCategory = category)
            try {
                duaRepository.getDuasByCategory(category).collect { duas ->
                    _uiState.value = _uiState.value.copy(
                        duas = duas,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "দোয়া লোড করা যায়নি"
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                duaRepository.getCategories().collect { categories ->
                    _uiState.value = _uiState.value.copy(categories = DuaCategories.ordered(categories))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "ক্যাটাগরি লোড করা যায়নি"
                )
            }
        }
    }

    fun searchDuas(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                duaRepository.searchDuas(query).collect { results ->
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

    fun selectDua(dua: Dua) {
        _uiState.value = _uiState.value.copy(selectedDua = dua)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchResults = emptyList())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
