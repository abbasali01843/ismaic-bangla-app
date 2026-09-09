package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.model.PrayerDay
import com.islamic.bangla.data.model.PrayerTime
import com.islamic.bangla.data.notification.PrayerAlarmScheduler
import com.islamic.bangla.data.preferences.SettingsStore
import com.islamic.bangla.data.repository.PrayerLogRepository
import com.islamic.bangla.data.repository.PrayerTimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PrayerTimeUiState(
    val todayPrayerTime: PrayerTime? = null,
    val selectedCity: String = "Dhaka",
    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
    val isLoading: Boolean = false,
    /** True while an API refresh is running (cache still visible). */
    val isRefreshing: Boolean = false,
    /** True when the API failed and cached data (if any) is shown. */
    val offline: Boolean = false,
    val error: String? = null
)

/** One day in the 7-day tracker strip. */
data class DayLog(
    val date: String,
    val weekDay: String,
    val prayedCount: Int,
    val complete: Boolean
)

@HiltViewModel
class PrayerTimeViewModel @Inject constructor(
    private val prayerTimeRepository: PrayerTimeRepository,
    private val prayerLogRepository: PrayerLogRepository,
    private val settingsStore: SettingsStore,
    private val scheduler: PrayerAlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerTimeUiState())
    val uiState: StateFlow<PrayerTimeUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    /** Today's prayer checklist. */
    val todayLog: StateFlow<PrayerDay?> = prayerLogRepository
        .getDay(_uiState.value.todayDate)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /** Last 7 days (oldest first) for the tracker strip. */
    val weekLog: StateFlow<List<DayLog>> = prayerLogRepository.getRecentDays(30)
        .map { days -> buildWeek(days) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Consecutive complete (5/5) days ending today or yesterday. */
    val streak: StateFlow<Int> = prayerLogRepository.getRecentDays(400)
        .map { days -> computeStreak(days) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    init {
        viewModelScope.launch {
            val city = settingsStore.prayerCity.first()
            loadTodayPrayerTime(city)
        }
    }

    /**
     * API-first with cache fallback: show the Room cache immediately,
     * refresh from the API in parallel, fall back to cache on failure.
     */
    fun loadTodayPrayerTime(city: String) {
        observeJob?.cancel()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedCity = city,
                offline = false,
                error = null
            )
            settingsStore.setPrayerCity(city)
            // Observe cache (instant, works offline).
            observeJob = launch {
                prayerTimeRepository.getTodayPrayerTime(city, _uiState.value.todayDate)
                    .collect { prayerTime ->
                        _uiState.value = _uiState.value.copy(
                            todayPrayerTime = prayerTime,
                            isLoading = false
                        )
                    }
            }
            // Refresh from API (fresh data overwrites the cache).
            launch {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                try {
                    prayerTimeRepository.refreshToday(city)
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        offline = false
                    )
                    // Keep alarms in sync with the freshly fetched times.
                    if (settingsStore.notificationsEnabled.first()) {
                        scheduler.scheduleAll(city)
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        offline = true
                    )
                }
            }
        }
    }

    fun togglePrayer(prayerKey: String) {
        viewModelScope.launch {
            prayerLogRepository.togglePrayer(_uiState.value.todayDate, prayerKey)
        }
    }

    fun retry() = loadTodayPrayerTime(_uiState.value.selectedCity)

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun buildWeek(days: List<PrayerDay>): List<DayLog> {
        val byDate = days.associateBy { it.date }
        val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val weekday = SimpleDateFormat("EEE", Locale("bn"))
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        return (0..6).map {
            val dateStr = iso.format(cal.time)
            val dayName = weekday.format(cal.time)
            val log = byDate[dateStr]
            val entry = DayLog(
                date = dateStr,
                weekDay = dayName,
                prayedCount = log?.prayedCount() ?: 0,
                complete = log?.isComplete() == true
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
            entry
        }
    }

    private fun computeStreak(days: List<PrayerDay>): Int {
        val byDate = days.associateBy { it.date }
        val iso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        if (byDate[iso.format(cal.time)]?.isComplete() != true) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        var count = 0
        while (byDate[iso.format(cal.time)]?.isComplete() == true) {
            count++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return count
    }
}
