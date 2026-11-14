package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.IslamicQuizApp
import com.example.audio.SoundEffectPlayer
import com.example.audio.TtsFeedbackManager
import com.example.data.local.DuaBookmarkEntity
import com.example.data.local.TasbihDailyLogEntity
import com.example.data.model.DuaItem
import com.example.data.model.TasbihDhikr
import com.example.data.seed.QuranOfflineBundledData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

// -------------------- TASBIH VIEWMODEL --------------------

data class TasbihUiState(
    val selectedDhikr: TasbihDhikr = QuranOfflineBundledData.tasbihList.first(),
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val todayTotal: Int = 0,
    val lifetimeTotal: Int = 0,
    val todayLogs: List<TasbihDailyLogEntity> = emptyList(),
    val recentLogs: List<TasbihDailyLogEntity> = emptyList(),
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val milestoneReached: Boolean = false
)

class TasbihViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as IslamicQuizApp
    private val tasbihDao = app.database.tasbihDao()
    private val settingsStore = app.settingsDataStore
    private val soundPlayer = SoundEffectPlayer(app)

    private val _selectedDhikr = MutableStateFlow(QuranOfflineBundledData.tasbihList.first())
    private val _count = MutableStateFlow(0)
    private val _target = MutableStateFlow(33)
    private val _milestone = MutableStateFlow(false)

    private val todayIso = LocalDate.now().toString()

    val uiState: StateFlow<TasbihUiState> = combine(
        _selectedDhikr,
        _count,
        _target,
        tasbihDao.getTodayTotalCount(todayIso),
        tasbihDao.getLifetimeTotalCount(),
        tasbihDao.getLogsForDate(todayIso),
        tasbihDao.getRecentLogs(),
        settingsStore.settingsFlow,
        _milestone
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        TasbihUiState(
            selectedDhikr = arr[0] as TasbihDhikr,
            currentCount = arr[1] as Int,
            targetCount = arr[2] as Int,
            todayTotal = arr[3] as Int,
            lifetimeTotal = arr[4] as Int,
            todayLogs = arr[5] as List<TasbihDailyLogEntity>,
            recentLogs = arr[6] as List<TasbihDailyLogEntity>,
            vibrationEnabled = (arr[7] as com.example.data.local.UserSettings).tasbihVibrationEnabled,
            soundEnabled = (arr[7] as com.example.data.local.UserSettings).tasbihSoundEnabled,
            milestoneReached = arr[8] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TasbihUiState())

    fun selectDhikr(dhikr: TasbihDhikr) {
        _selectedDhikr.value = dhikr
        _count.value = 0
        _target.value = dhikr.defaultTarget
        _milestone.value = false
    }

    fun setTarget(target: Int) {
        _target.value = target.coerceIn(1, 100000)
        _milestone.value = _count.value >= _target.value
    }

    fun increment() {
        val next = _count.value + 1
        val target = _target.value
        val hitTarget = next == target
        _count.value = next
        _milestone.value = next >= target

        val st = uiState.value
        if (st.soundEnabled) {
            if (hitTarget) soundPlayer.playTasbihMilestone() else soundPlayer.playTasbihTick()
        }
        if (st.vibrationEnabled) {
            vibrate(if (hitTarget) 90L else 22L)
        }

        val dhikr = _selectedDhikr.value
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existing = tasbihDao.getLogOnce(today, dhikr.id)
            tasbihDao.upsertLog(
                TasbihDailyLogEntity(
                    dateIso = today,
                    dhikrId = dhikr.id,
                    dhikrTitleBn = dhikr.titleBn,
                    count = (existing?.count ?: 0) + 1
                )
            )
        }
    }

    fun resetCounter() {
        _count.value = 0
        _milestone.value = false
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setTasbihVibration(enabled) }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setTasbihSound(enabled) }
    }

    private fun vibrate(ms: Long) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(ms)
                }
            }
        }
    }

    override fun onCleared() {
        soundPlayer.release()
        super.onCleared()
    }
}

// -------------------- DUA VIEWMODEL --------------------

class DuaViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as IslamicQuizApp
    private val duaDao = app.database.duaDao()
    private val tts = TtsFeedbackManager(app)

    val bookmarkedIds: StateFlow<Set<Int>> = duaDao.getBookmarkedDuaIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _playingDuaId = MutableStateFlow<Int?>(null)
    val playingDuaId: StateFlow<Int?> = _playingDuaId.asStateFlow()

    fun toggleBookmark(duaId: Int, currentlyBookmarked: Boolean) {
        viewModelScope.launch {
            if (currentlyBookmarked) duaDao.removeBookmark(duaId)
            else duaDao.addBookmark(DuaBookmarkEntity(duaId))
        }
    }

    fun playDuaAudio(dua: DuaItem) {
        if (_playingDuaId.value == dua.id) {
            tts.stop()
            _playingDuaId.value = null
        } else {
            _playingDuaId.value = dua.id
            tts.speakDuaAudio(dua.arabic, "${dua.transliterationBn}। অর্থ: ${dua.translationBn}")
        }
    }

    fun stopAudio() {
        tts.stop()
        _playingDuaId.value = null
    }

    override fun onCleared() {
        tts.shutdown()
        super.onCleared()
    }
}
