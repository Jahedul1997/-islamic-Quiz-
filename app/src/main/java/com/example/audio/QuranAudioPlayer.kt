package com.example.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.data.model.AyahItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuranAudioState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentSurah: Int? = null,
    val currentAyahInSurah: Int? = null,
    val errorMessage: String? = null
)

class QuranAudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var playlist: List<AyahItem> = emptyList()
    private var currentIndex: Int = -1

    private val _state = MutableStateFlow(QuranAudioState())
    val state: StateFlow<QuranAudioState> = _state.asStateFlow()

    fun playSurahFrom(ayahs: List<AyahItem>, startIndex: Int = 0) {
        if (ayahs.isEmpty()) return
        playlist = ayahs
        currentIndex = startIndex.coerceIn(0, ayahs.lastIndex)
        playCurrent()
    }

    fun togglePlayPause(ayahs: List<AyahItem>, ayahIndex: Int) {
        val target = ayahs.getOrNull(ayahIndex) ?: return
        val st = _state.value
        if (st.currentSurah == target.surahNumber && st.currentAyahInSurah == target.numberInSurah) {
            if (st.isPlaying) {
                mediaPlayer?.pause()
                _state.value = st.copy(isPlaying = false)
            } else {
                mediaPlayer?.start()
                _state.value = st.copy(isPlaying = true)
            }
        } else {
            playSurahFrom(ayahs, ayahIndex)
        }
    }

    fun playNext() {
        if (currentIndex + 1 < playlist.size) {
            currentIndex++
            playCurrent()
        } else {
            stop()
        }
    }

    fun playPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            playCurrent()
        }
    }

    private fun playCurrent() {
        val ayah = playlist.getOrNull(currentIndex) ?: run {
            stop()
            return
        }
        releasePlayer()
        _state.value = QuranAudioState(
            isPlaying = false,
            isLoading = true,
            currentSurah = ayah.surahNumber,
            currentAyahInSurah = ayah.numberInSurah,
            errorMessage = null
        )

        val url = "https://cdn.islamic.network/quran/audio/128/ar.alafasy/${ayah.globalNumber}.mp3"
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener {
                    it.start()
                    _state.value = _state.value.copy(isPlaying = true, isLoading = false)
                }
                setOnCompletionListener {
                    playNext()
                }
                setOnErrorListener { _, _, _ ->
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        isLoading = false,
                        errorMessage = "অডিও লোড করতে ইন্টারনেট সংযোগ প্রয়োজন।"
                    )
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isPlaying = false,
                isLoading = false,
                errorMessage = "অডিও চালানো সম্ভব হয়নি।"
            )
        }
    }

    fun stop() {
        releasePlayer()
        _state.value = QuranAudioState()
    }

    private fun releasePlayer() {
        mediaPlayer?.run {
            runCatching { stop() }
            runCatching { release() }
        }
        mediaPlayer = null
    }
}
