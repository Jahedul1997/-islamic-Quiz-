package com.example.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.IslamicQuizApp
import com.example.data.local.UserSettings
import com.example.data.model.*
import com.example.data.repository.PrayerRepository
import com.example.data.seed.DistrictsData
import com.example.data.seed.QuranOfflineBundledData
import com.example.data.util.*
import com.example.receiver.AlarmScheduler
import com.example.receiver.PrayerAlarmReceiver
import com.example.service.AdhanPlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.abs

data class DashboardUiState(
    val settings: UserSettings = UserSettings(),
    val englishDateStr: String = "",
    val banglaDate: BanglaDateInfo = BanglaCalendarConverter.fromGregorian(LocalDate.now()),
    val hijriDate: HijriDateInfo = HijriCalendarConverter.fromGregorian(LocalDate.now(), 0),
    val prayerTimes: DailyPrayerTimes? = null,
    val nextPrayerStatus: PrayerTimeCalculator.NextPrayerStatus? = null,
    val dailyQuote: IslamicQuoteItem = QuranOfflineBundledData.dailyQuotes.first(),
    val isSyncingPrayer: Boolean = false,
    val statusBannerMessage: String? = null
)

class AppStateViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as IslamicQuizApp
    private val settingsStore = app.settingsDataStore
    private val db = app.database
    private val prayerRepo = PrayerRepository(db.prayerCacheDao())

    val settingsFlow: StateFlow<UserSettings> = settingsStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    private val _prayerTimes = MutableStateFlow<DailyPrayerTimes?>(null)
    private val _isSyncing = MutableStateFlow(false)
    private val _statusBanner = MutableStateFlow<String?>(null)
    private val _nowTick = MutableStateFlow(ZonedDateTime.now())

    val uiState: StateFlow<DashboardUiState> = combine(
        settingsFlow,
        _prayerTimes,
        _nowTick,
        _isSyncing,
        _statusBanner
    ) { settings, times, now, syncing, banner ->
        val today = now.toLocalDate()
        val bangla = BanglaCalendarConverter.fromGregorian(today)
        val hijri = HijriCalendarConverter.fromGregorian(today, settings.hijriAdjustmentDays)
        val engStr = BengaliNumeralUtil.formatGregorianDate(today, settings.language)
        val resolvedTimes = times ?: PrayerTimeCalculator.calculate(
            date = today,
            lat = if (settings.useGpsLocation) settings.gpsLat else DistrictsData.findById(settings.selectedDistrictId).lat,
            lon = if (settings.useGpsLocation) settings.gpsLon else DistrictsData.findById(settings.selectedDistrictId).lon,
            locationNameBn = if (settings.useGpsLocation) settings.gpsLocationName else DistrictsData.findById(settings.selectedDistrictId).bnName,
            locationNameEn = if (settings.useGpsLocation) settings.gpsLocationName else DistrictsData.findById(settings.selectedDistrictId).enName,
            method = settings.calculationMethod,
            madhab = settings.asrMadhab
        )
        val nextStatus = PrayerTimeCalculator.computeNextPrayer(resolvedTimes, now)
        val quotes = QuranOfflineBundledData.dailyQuotes
        val quote = quotes[today.dayOfYear % quotes.size]

        DashboardUiState(
            settings = settings,
            englishDateStr = engStr,
            banglaDate = bangla,
            hijriDate = hijri,
            prayerTimes = resolvedTimes,
            nextPrayerStatus = nextStatus,
            dailyQuote = quote,
            isSyncingPrayer = syncing,
            statusBannerMessage = banner
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    init {
        viewModelScope.launch {
            while (true) {
                _nowTick.value = ZonedDateTime.now()
                delay(1000L)
            }
        }
        viewModelScope.launch {
            settingsFlow.collectLatest { s ->
                refreshPrayerTimes(s, tryOnline = true)
            }
        }
    }

    fun refreshPrayerTimesNow() {
        viewModelScope.launch {
            refreshPrayerTimes(settingsFlow.value, tryOnline = true)
        }
    }

    private suspend fun refreshPrayerTimes(settings: UserSettings, tryOnline: Boolean) {
        _isSyncing.value = true
        val result = runCatching {
            prayerRepo.getPrayerTimes(LocalDate.now(), settings, tryOnlineSync = tryOnline)
        }
        result.onSuccess {
            _prayerTimes.value = it
            AlarmScheduler.scheduleAllAlarms(app, settings, db.prayerCacheDao())
        }
        _isSyncing.value = false
    }

    fun setLanguage(lang: AppLanguage) = viewModelScope.launch { settingsStore.setLanguage(lang) }
    fun setTheme(theme: ThemePreference) = viewModelScope.launch { settingsStore.setTheme(theme) }
    fun setSoundEnabled(v: Boolean) = viewModelScope.launch { settingsStore.setSoundEnabled(v) }
    fun setVoiceFeedback(v: Boolean) = viewModelScope.launch { settingsStore.setVoiceFeedbackEnabled(v) }
    fun setAdhanMaster(v: Boolean) = viewModelScope.launch {
        settingsStore.setAdhanMaster(v)
        rescheduleAlarms()
    }
    fun setPrayerAlertMode(prayer: PrayerType, mode: PrayerAlertMode) = viewModelScope.launch {
        settingsStore.setPrayerAlertMode(prayer, mode)
        rescheduleAlarms()
    }
    fun selectDistrict(districtId: String) = viewModelScope.launch {
        settingsStore.setDistrict(districtId)
    }
    fun setCalculationMethod(method: CalculationMethod) = viewModelScope.launch {
        settingsStore.setCalculationMethod(method)
    }
    fun setAsrMadhab(madhab: AsrMadhab) = viewModelScope.launch {
        settingsStore.setAsrMadhab(madhab)
    }
    fun setHijriAdjustment(offset: Int) = viewModelScope.launch {
        settingsStore.setHijriAdjustment(offset)
    }
    fun setQuizTimerEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsStore.setQuizTimerEnabled(enabled)
    }
    fun setQuizTimerSeconds(sec: Int) = viewModelScope.launch {
        settingsStore.setQuizTimerSeconds(sec)
    }
    fun setDailyNotifications(enabled: Boolean) = viewModelScope.launch {
        settingsStore.setDailyNotifications(enabled)
        rescheduleAlarms()
    }
    fun setEventReminders(enabled: Boolean) = viewModelScope.launch {
        settingsStore.setEventReminders(enabled)
        rescheduleAlarms()
    }

    private fun rescheduleAlarms() {
        viewModelScope.launch {
            AlarmScheduler.scheduleAllAlarms(app, settingsFlow.value, db.prayerCacheDao())
        }
    }

    @SuppressLint("MissingPermission")
    fun detectGpsLocation() {
        viewModelScope.launch {
            _statusBanner.value = "লোকেশন নির্ণয় করা হচ্ছে..."
            val loc = withContext(Dispatchers.IO) {
                val lm = app.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)
                providers.firstNotNullOfOrNull { p ->
                    runCatching { lm?.getLastKnownLocation(p) }.getOrNull()
                }
            }
            if (loc != null) {
                val name = withContext(Dispatchers.IO) {
                    runCatching {
                        @Suppress("DEPRECATION")
                        val list = Geocoder(app, Locale.getDefault()).getFromLocation(loc.latitude, loc.longitude, 1)
                        list?.firstOrNull()?.locality ?: list?.firstOrNull()?.subAdminArea
                    }.getOrNull() ?: nearestDistrictName(loc.latitude, loc.longitude)
                }
                settingsStore.setGpsLocation(loc.latitude, loc.longitude, name)
                _statusBanner.value = "GPS লোকেশন সংরক্ষিত: $name"
            } else {
                _statusBanner.value = "GPS লোকেশন পাওয়া যায়নি; অনুগ্রহ করে জেলা নির্বাচন করুন।"
            }
        }
    }

    private fun nearestDistrictName(lat: Double, lon: Double): String {
        val nearest = DistrictsData.allDistricts.minByOrNull { d ->
            abs(d.lat - lat) + abs(d.lon - lon)
        }
        return nearest?.bnName ?: "বর্তমান অবস্থান"
    }

    fun testAdhanNow() {
        AdhanPlaybackService.startAdhan(app, "টেস্ট আজান")
        _statusBanner.value = "আজান অডিও ও নোটিফিকেশন চালু হয়েছে।"
    }

    fun stopAdhanNow() {
        AdhanPlaybackService.stopAdhan(app)
    }

    fun testDailyNotificationNow() {
        val quote = QuranOfflineBundledData.dailyQuotes.random()
        PrayerAlarmReceiver.showNotification(
            context = app,
            id = 6099,
            channelId = IslamicQuizApp.CHANNEL_DAILY_ISLAMIC,
            title = "${quote.typeBn} (প্রিভিউ)",
            body = "${quote.textBn} — ${quote.sourceBn}"
        )
        _statusBanner.value = "ইসলামিক নোটিফিকেশন পাঠানো হয়েছে।"
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val s = settingsFlow.value
        val obj = JSONObject().apply {
            put("app", "IslamicQuiz")
            put("language", s.language.code)
            put("theme", s.theme.code)
            put("districtId", s.selectedDistrictId)
            put("calcMethod", s.calculationMethod.id)
            put("asrMadhab", s.asrMadhab.id)
            put("hijriOffset", s.hijriAdjustmentDays)
            put("quizHighScore", s.quizHighScore)
            put("dailyQuizStreak", s.dailyQuizStreak)
            put("lastReadSurah", s.lastReadSurah)
            put("lastReadAyah", s.lastReadAyah)
        }
        obj.toString(2)
    }

    suspend fun importBackupJson(raw: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val obj = JSONObject(raw)
            obj.optString("language").takeIf { it.isNotBlank() }?.let { code ->
                AppLanguage.entries.find { it.code == code }?.let { settingsStore.setLanguage(it) }
            }
            obj.optString("districtId").takeIf { it.isNotBlank() }?.let { settingsStore.setDistrict(it) }
            if (obj.has("hijriOffset")) settingsStore.setHijriAdjustment(obj.getInt("hijriOffset"))
            if (obj.has("quizHighScore")) settingsStore.updateHighScoreIfBetter(obj.getInt("quizHighScore"))
            if (obj.has("lastReadSurah")) {
                settingsStore.setLastRead(obj.getInt("lastReadSurah"), obj.optInt("lastReadAyah", 1))
            }
            true
        }.getOrDefault(false)
    }
}
