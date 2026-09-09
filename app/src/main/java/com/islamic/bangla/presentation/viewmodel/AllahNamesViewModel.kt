package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.AllahName
import com.islamic.bangla.data.repository.AllahNamesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AllahNamesUiState(
    val names: List<AllahName> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class AllahNamesViewModel @Inject constructor(
    private val repository: AllahNamesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllahNamesUiState())
    val uiState: StateFlow<AllahNamesUiState> = _uiState.asStateFlow()

    private var allNames: List<AllahName> = emptyList()

    init {
        viewModelScope.launch {
            allNames = repository.getNames().first()
            _uiState.value = AllahNamesUiState(names = allNames, isLoading = false)
        }
    }

    fun onQueryChange(query: String) {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) {
            allNames
        } else {
            allNames.filter {
                it.banglaName.contains(trimmed) ||
                    it.meaningBangla.contains(trimmed) ||
                    it.meaningEnglish.contains(trimmed, ignoreCase = true) ||
                    it.arabic.contains(trimmed)
            }
        }
        _uiState.value = _uiState.value.copy(names = filtered, query = query)
    }
}
