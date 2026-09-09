package com.islamic.bangla.data.audio

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.islamic.bangla.data.remote.audio.AudioConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-scoped ExoPlayer for verse-by-verse recitation.
 * Streams MP3s from cdn.islamic.network, preferring downloaded files when present.
 * [QuranPlaybackService] keeps playback alive in the background.
 */
@Singleton
class QuranAudioPlayer @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val player: ExoPlayer = ExoPlayer.Builder(appContext).build()

    /** Exposed for the MediaSession in [QuranPlaybackService]. */
    val exoPlayer: ExoPlayer get() = player

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Playing surah number, 0 when idle. */
    private val _playlistSurah = MutableStateFlow(0)
    val playlistSurah: StateFlow<Int> = _playlistSurah.asStateFlow()

    /** Playing ayah number (within surah), 0 when idle. */
    private val _currentAyah = MutableStateFlow(0)
    val currentAyah: StateFlow<Int> = _currentAyah.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _playlistSurahName = MutableStateFlow("")
    val playlistSurahName: StateFlow<String> = _playlistSurahName.asStateFlow()

    /** ExoPlayer repeat mode (OFF / ALL / ONE). */
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    /** Current playlist as (numberInSurah, globalNumber) pairs. */
    private var playlist: List<Pair<Int, Int>> = emptyList()
    private var reciter: String = AudioConfig.DEFAULT_RECITER
    private var surahName: String = ""

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _isLoading.value = playbackState == Player.STATE_BUFFERING
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentAyah.value = mediaItem?.mediaId?.toIntOrNull() ?: 0
            }

            override fun onPlayerError(error: PlaybackException) {
                _error.value = "অডিও চালানো যায়নি — ইন্টারনেট সংযোগ দেখুন"
                _isPlaying.value = false
            }
        })
    }

    /**
     * Play [ayahs] (pairs of numberInSurah -> globalNumber) starting at
     * [startAyahNumber]. Reuses the current playlist when possible.
     */
    fun playAyahs(
        surahNumber: Int,
        surahName: String,
        ayahs: List<Pair<Int, Int>>,
        startAyahNumber: Int,
        reciterEdition: String
    ) {
        _error.value = null
        _playlistSurahName.value = surahName
        this.surahName = surahName
        val startIndex = ayahs.indexOfFirst { it.first == startAyahNumber }.coerceAtLeast(0)

        if (surahNumber == _playlistSurah.value &&
            reciterEdition == reciter &&
            player.mediaItemCount == ayahs.size &&
            player.playbackState != Player.STATE_IDLE
        ) {
            player.seekTo(startIndex, 0)
            player.play()
            startService()
            return
        }

        reciter = reciterEdition
        playlist = ayahs
        _playlistSurah.value = surahNumber
        player.setMediaItems(
            buildItems(surahNumber, surahName, ayahs, reciterEdition),
            startIndex,
            0
        )
        player.prepare()
        player.play()
        startService()
    }

    /** Switch reciter immediately, keeping the current position. */
    fun switchReciter(reciterEdition: String) {
        reciter = reciterEdition
        if (_playlistSurah.value == 0 || playlist.isEmpty()) return
        val index = player.currentMediaItemIndex.coerceIn(0, playlist.size - 1)
        val wasPlaying = player.isPlaying
        player.setMediaItems(
            buildItems(_playlistSurah.value, surahName, playlist, reciterEdition),
            index,
            0
        )
        player.prepare()
        if (wasPlaying) player.play()
    }

    /** Cycle repeat: off -> whole surah -> single ayah -> off. */
    fun cycleRepeatMode() {
        val next = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = next
        _repeatMode.value = next
    }

    fun toggle() {
        if (_playlistSurah.value == 0) return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun nextAyah() = player.seekToNextMediaItem()

    fun prevAyah() = player.seekToPreviousMediaItem()

    fun stop() {
        player.stop()
        player.clearMediaItems()
        playlist = emptyList()
        surahName = ""
        _playlistSurah.value = 0
        _playlistSurahName.value = ""
        _currentAyah.value = 0
        _isPlaying.value = false
        _error.value = null
        appContext.stopService(Intent(appContext, QuranPlaybackService::class.java))
    }

    fun clearError() {
        _error.value = null
    }

    private fun startService() {
        appContext.startService(Intent(appContext, QuranPlaybackService::class.java))
    }

    private fun buildItems(
        surahNumber: Int,
        surahName: String,
        ayahs: List<Pair<Int, Int>>,
        reciterEdition: String
    ): List<MediaItem> = ayahs.map { (inSurah, global) ->
        val localFile = AudioConfig.localAyahFile(appContext, reciterEdition, surahNumber, inSurah)
        val uri = if (localFile.exists() && localFile.length() > 0L) {
            Uri.fromFile(localFile)
        } else {
            Uri.parse(AudioConfig.ayahAudioUrl(global, reciterEdition))
        }
        MediaItem.Builder()
            .setUri(uri)
            .setMediaId(inSurah.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("$surahName • আয়াত $inSurah")
                    .setArtist(AudioConfig.reciterName(reciterEdition))
                    .build()
            )
            .build()
    }
}
