package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.data.preferences.SettingsStore
import com.islamic.bangla.data.remote.HadithCollectionInfo
import com.islamic.bangla.data.remote.HadithCollections
import com.islamic.bangla.data.repository.HadithLoadError
import com.islamic.bangla.data.repository.HadithRefreshResult
import com.islamic.bangla.data.repository.HadithRepository
import com.islamic.bangla.data.repository.toHadithLoadError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HadithUiState(
    val hadiths: List<Hadith> = emptyList(),
    val isLoading: Boolean = false,
    /** True while a full collection is downloading from the API. */
    val isSyncing: Boolean = false,
    /** True only when the device genuinely cannot reach the CDN. */
    val offline: Boolean = false,
    /**
     * Precise refresh failure (server / data / unknown / empty / …), or null
     * when the last refresh succeeded. Cached hadiths stay visible alongside.
     */
    val syncError: HadithLoadError? = null,
    /** True when Bangla loaded but the Arabic edition failed (best-effort). */
    val arabicMissing: Boolean = false,
    val error: String? = null,
    val searchResults: List<Hadith> = emptyList(),
    val selectedHadith: Hadith? = null,
    val collections: List<HadithCollectionInfo> = HadithCollections.all,
    val selectedCollection: String? = null
)

@HiltViewModel
class HadithViewModel @Inject constructor(
    private val hadithRepository: HadithRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HadithUiState())
    val uiState: StateFlow<HadithUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        selectCollection(HadithCollections.all.first().key)
    }

    /**
     * API-first with cache fallback. Collections are large, so a full
     * re-download happens only when the cache is empty or older than 7 days;
     * otherwise the cache is shown instantly.
     */
    fun selectCollection(collection: String) {
        observeJob?.cancel()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedCollection = collection,
                searchResults = emptyList(),
                offline = false,
                syncError = null,
                arabicMissing = false,
                error = null
            )
            observeJob = launch {
                hadithRepository.getHadithsByCollection(collection).collect { hadiths ->
                    _uiState.value = _uiState.value.copy(
                        hadiths = hadiths,
                        isLoading = false
                    )
                }
            }
            launch {
                try {
                    val cached = hadithRepository.getHadithsByCollection(collection).first()
                    val lastSync = settingsStore.getLastSync(collection)
                    val stale = lastSync == null ||
                        System.currentTimeMillis() - lastSync > SYNC_STALE_MS
                    if (cached.isEmpty() || stale) {
                        _uiState.value = _uiState.value.copy(isSyncing = true)
                        when (val result = hadithRepository.refreshCollection(collection)) {
                            is HadithRefreshResult.Success -> {
                                settingsStore.setLastSync(collection)
                                _uiState.value = _uiState.value.copy(
                                    isSyncing = false,
                                    offline = false,
                                    syncError = null,
                                    arabicMissing = !result.arabicComplete,
                                    error = null
                                )
                            }
                            is HadithRefreshResult.Failure -> {
                                // Cache fallback: the collector above keeps
                                // showing cached hadiths; only flags change.
                                _uiState.value = _uiState.value.copy(
                                    isSyncing = false,
                                    offline = result.error is HadithLoadError.NoInternet,
                                    syncError = result.error,
                                    arabicMissing = false
                                )
                            }
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            offline = false,
                            syncError = null,
                            error = null
                        )
                    }
                } catch (e: Exception) {
                    // Safety net for cache/DataStore failures (never the API:
                    // refreshCollection returns results instead of throwing).
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        offline = false,
                        syncError = e.toHadithLoadError()
                    )
                }
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
                    error = e.message ?: "অনুসন্ধান ব্যর্থ হয়েছে"
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

    companion object {
        private const val SYNC_STALE_MS = 7L * 24 * 60 * 60 * 1000 // 7 days
    }
}
