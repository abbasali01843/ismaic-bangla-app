package com.islamic.bangla.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.bangla.data.audio.AudioDownloadManager
import com.islamic.bangla.data.audio.QuranAudioPlayer
import com.islamic.bangla.data.audio.SurahDownloadState
import com.islamic.bangla.data.model.Ayah
import com.islamic.bangla.data.model.Quran
import com.islamic.bangla.data.preferences.SettingsStore
import com.islamic.bangla.data.remote.audio.AudioConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val audioPlayer: QuranAudioPlayer,
    private val downloadManager: AudioDownloadManager,
    private val settingsStore: SettingsStore
) : ViewModel() {

    val isPlaying: StateFlow<Boolean> = audioPlayer.isPlaying
    val isLoading: StateFlow<Boolean> = audioPlayer.isLoading
    val playlistSurah: StateFlow<Int> = audioPlayer.playlistSurah
    val currentAyah: StateFlow<Int> = audioPlayer.currentAyah
    val error: StateFlow<String?> = audioPlayer.error
    val repeatMode: StateFlow<Int> = audioPlayer.repeatMode

    val reciters = AudioConfig.reciters

    val reciter: StateFlow<String> = settingsStore.reciter
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AudioConfig.DEFAULT_RECITER
        )

    // Shared via the app-scoped player so the mini-player and detail
    // screen (different ViewModel instances) always agree.
    val playlistSurahName: StateFlow<String> = audioPlayer.playlistSurahName

    val downloadState: StateFlow<SurahDownloadState> = downloadManager.state

    fun playAyahs(surah: Quran, ayahs: List<Ayah>, startAyahNumber: Int) {
        viewModelScope.launch {
            val items = ayahs
                .map { it.ayahNumber to it.globalNumber }
                .filter { it.second > 0 }
            if (items.isEmpty()) return@launch
            audioPlayer.playAyahs(
                surahNumber = surah.surahNumber,
                surahName = surah.surahNameBangla,
                ayahs = items,
                startAyahNumber = startAyahNumber,
                reciterEdition = settingsStore.reciter.first()
            )
        }
    }

    fun setReciter(edition: String) {
        viewModelScope.launch {
            settingsStore.setReciter(edition)
            audioPlayer.switchReciter(edition)
        }
    }

    fun toggle() = audioPlayer.toggle()

    fun next() = audioPlayer.nextAyah()

    fun prev() = audioPlayer.prevAyah()

    fun stop() = audioPlayer.stop()

    fun clearError() = audioPlayer.clearError()

    fun cycleRepeatMode() = audioPlayer.cycleRepeatMode()

    fun refreshDownload(surahNumber: Int, totalAyahs: Int) {
        viewModelScope.launch {
            downloadManager.refresh(surahNumber, totalAyahs, settingsStore.reciter.first())
        }
    }

    fun downloadSurah(surah: Quran, ayahs: List<Ayah>) {
        viewModelScope.launch {
            val items = ayahs
                .map { it.ayahNumber to it.globalNumber }
                .filter { it.second > 0 }
            if (items.isEmpty()) return@launch
            downloadManager.downloadSurah(
                surah.surahNumber,
                items,
                settingsStore.reciter.first()
            )
        }
    }

    fun cancelDownload() = downloadManager.cancelDownload()

    fun deleteDownload(surahNumber: Int, totalAyahs: Int) {
        viewModelScope.launch {
            downloadManager.deleteSurah(
                surahNumber,
                totalAyahs,
                settingsStore.reciter.first()
            )
        }
    }

    fun clearDownloadError() = downloadManager.clearError()
}
