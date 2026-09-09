package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.Dhikr
import com.islamic.bangla.data.preferences.SettingsStore
import com.islamic.bangla.data.repository.DhikrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TasbihViewModel @Inject constructor(
    private val dhikrRepository: DhikrRepository,
    private val settingsStore: SettingsStore
) : ViewModel() {

    val dhikrs: List<Dhikr> = dhikrRepository.getDhikrs()

    private val _selectedId = MutableStateFlow(dhikrs.firstOrNull()?.id ?: 1)
    val selectedId: StateFlow<Int> = _selectedId

    val selectedDhikr: Dhikr?
        get() = dhikrs.find { it.id == _selectedId.value }

    val count: StateFlow<Int> = _selectedId
        .flatMapLatest { id -> settingsStore.tasbihCount(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val total: StateFlow<Long> = settingsStore.tasbihTotal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    init {
        viewModelScope.launch {
            val lastId = settingsStore.lastDhikrId.first()
            if (dhikrs.any { it.id == lastId }) _selectedId.value = lastId
        }
    }

    fun tap() {
        viewModelScope.launch {
            settingsStore.incrementTasbih(_selectedId.value)
        }
    }

    fun selectDhikr(id: Int) {
        _selectedId.value = id
        viewModelScope.launch { settingsStore.setLastDhikrId(id) }
    }

    fun reset() {
        viewModelScope.launch { settingsStore.resetTasbih(_selectedId.value) }
    }
}
