package com.islamic.bangla.data.audio

import android.content.Context
import com.islamic.bangla.data.remote.audio.AudioConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/** progress: 0..100 while downloading, -1 when idle. */
data class SurahDownloadState(
    val surahNumber: Int = 0,
    val downloaded: Boolean = false,
    val progress: Int = -1,
    val downloadedCount: Int = 0,
    val totalCount: Int = 0,
    val error: String? = null
)

/**
 * Downloads a surah's recitation (current reciter) for offline listening.
 * Files already on disk are skipped, so interrupted downloads resume.
 */
@Singleton
class AudioDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(SurahDownloadState())
    val state: StateFlow<SurahDownloadState> = _state.asStateFlow()
    private var downloadJob: Job? = null

    fun refresh(surahNumber: Int, totalAyahs: Int, edition: String) {
        if (_state.value.surahNumber == surahNumber && _state.value.progress >= 0) return
        scope.launch {
            val count = AudioConfig.downloadedAyahCount(context, edition, surahNumber, totalAyahs)
            _state.value = SurahDownloadState(
                surahNumber = surahNumber,
                downloaded = totalAyahs > 0 && count >= totalAyahs,
                progress = -1,
                downloadedCount = count,
                totalCount = totalAyahs
            )
        }
    }

    fun downloadSurah(surahNumber: Int, ayahs: List<Pair<Int, Int>>, edition: String) {
        if (_state.value.progress >= 0) return
        downloadJob?.cancel()
        downloadJob = scope.launch {
            val total = ayahs.size
            _state.value = SurahDownloadState(
                surahNumber = surahNumber,
                downloaded = false,
                progress = 0,
                downloadedCount = 0,
                totalCount = total
            )
            var done = 0
            try {
                for ((ayahNumber, global) in ayahs) {
                    ensureActive()
                    val file = AudioConfig.localAyahFile(context, edition, surahNumber, ayahNumber)
                    if (!file.exists() || file.length() == 0L) {
                        file.parentFile?.mkdirs()
                        downloadFile(AudioConfig.ayahAudioUrl(global, edition), file)
                    }
                    done++
                    _state.value = _state.value.copy(
                        progress = done * 100 / total,
                        downloadedCount = done
                    )
                }
                _state.value = SurahDownloadState(
                    surahNumber = surahNumber,
                    downloaded = true,
                    progress = -1,
                    downloadedCount = total,
                    totalCount = total
                )
            } catch (_: CancellationException) {
                _state.value = _state.value.copy(progress = -1, error = null)
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    progress = -1,
                    error = "ডাউনলোড ব্যর্থ — ইন্টারনেট দেখুন"
                )
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        _state.value = _state.value.copy(progress = -1)
    }

    fun deleteSurah(surahNumber: Int, totalAyahs: Int, edition: String) {
        if (_state.value.progress >= 0) return
        scope.launch {
            AudioConfig.deleteSurahAudio(context, edition, surahNumber)
            _state.value = SurahDownloadState(
                surahNumber = surahNumber,
                downloaded = false,
                progress = -1,
                downloadedCount = 0,
                totalCount = totalAyahs
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun downloadFile(url: String, dest: java.io.File) {
        val connection = URL(url).openConnection()
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        connection.getInputStream().use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
    }
}
