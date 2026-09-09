package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.preferences.SettingsStore
import com.islamic.bangla.data.repository.TafsirRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TafsirUiState(
    val text: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TafsirViewModel @Inject constructor(
    private val tafsirRepository: TafsirRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(TafsirUiState())
    val uiState: StateFlow<TafsirUiState> = _uiState.asStateFlow()

    val selectedSlug: StateFlow<String> = settingsStore.tafsirSlug
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    private var surahNumber = 0
    private var ayahNumber = 0
    private var loadJob: Job? = null

    fun openTafsir(surah: Int, ayah: Int) {
        if (surah == surahNumber && ayah == ayahNumber) return
        surahNumber = surah
        ayahNumber = ayah
        _uiState.value = TafsirUiState()
        load()
    }

    fun setTafsir(slug: String) {
        viewModelScope.launch {
            settingsStore.setTafsirSlug(slug)
            load()
        }
    }

    fun retry() = load()

    fun clear() {
        loadJob?.cancel()
        surahNumber = 0
        ayahNumber = 0
        _uiState.value = TafsirUiState()
    }

    private fun load() {
        loadJob?.cancel()
        if (surahNumber <= 0 || ayahNumber <= 0) return
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val slug = settingsStore.tafsirSlug.first()
                val text = tafsirRepository.getTafsir(surahNumber, ayahNumber, slug)
                _uiState.value = TafsirUiState(text = text)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "তাফসির লোড হয়নি — ইন্টারনেট সংযোগ দেখুন"
                )
            }
        }
    }
}
