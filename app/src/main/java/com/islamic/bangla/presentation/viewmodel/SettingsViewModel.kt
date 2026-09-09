package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.notification.PrayerAlarmScheduler
import com.islamic.bangla.data.preferences.SettingsStore
import com.islamic.bangla.data.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrayerToggles(
    val fajr: Boolean = true,
    val dhuhr: Boolean = true,
    val asr: Boolean = true,
    val maghrib: Boolean = true,
    val isha: Boolean = true
) {
    fun forKey(key: String): Boolean = when (key) {
        "fajr" -> fajr
        "dhuhr" -> dhuhr
        "asr" -> asr
        "maghrib" -> maghrib
        else -> isha
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val scheduler: PrayerAlarmScheduler
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settingsStore.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM
        )

    val notificationsEnabled: StateFlow<Boolean> = settingsStore.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val azanEnabled: StateFlow<Boolean> = settingsStore.azanEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    val arabicFontScale: StateFlow<Float> = settingsStore.arabicFontScale
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 1f
        )

    val banglaFontScale: StateFlow<Float> = settingsStore.banglaFontScale
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 1f
        )

    val prayerToggles: StateFlow<PrayerToggles> = combine(
        settingsStore.prayerEnabled("fajr"),
        settingsStore.prayerEnabled("dhuhr"),
        settingsStore.prayerEnabled("asr"),
        settingsStore.prayerEnabled("maghrib"),
        settingsStore.prayerEnabled("isha")
    ) { values: Array<Boolean> ->
        PrayerToggles(
            fajr = values[0],
            dhuhr = values[1],
            asr = values[2],
            maghrib = values[3],
            isha = values[4]
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PrayerToggles()
    )

    val prayerList = scheduler.prayers

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsStore.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setNotificationsEnabled(enabled)
            if (enabled) {
                val city = settingsStore.prayerCity.first()
                scheduler.scheduleAll(city)
            } else {
                scheduler.cancelAll()
            }
        }
    }

    fun setAzanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setAzanEnabled(enabled)
        }
    }

    fun setArabicFontScale(scale: Float) {
        viewModelScope.launch {
            settingsStore.setArabicFontScale(scale)
        }
    }

    fun setBanglaFontScale(scale: Float) {
        viewModelScope.launch {
            settingsStore.setBanglaFontScale(scale)
        }
    }

    fun resetFontScales() {
        viewModelScope.launch {
            settingsStore.setArabicFontScale(1f)
            settingsStore.setBanglaFontScale(1f)
        }
    }

    fun setPrayerEnabled(prayerKey: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setPrayerEnabled(prayerKey, enabled)
            if (settingsStore.notificationsEnabled.first()) {
                val city = settingsStore.prayerCity.first()
                scheduler.scheduleAll(city)
            }
        }
    }
}
