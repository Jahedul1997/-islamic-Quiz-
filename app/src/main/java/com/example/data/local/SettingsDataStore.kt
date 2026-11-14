package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "islamic_quiz_settings")

data class UserSettings(
    val language: AppLanguage = AppLanguage.BENGALI,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val soundEnabled: Boolean = true,
    val voiceFeedbackEnabled: Boolean = true,
    val tasbihVibrationEnabled: Boolean = true,
    val tasbihSoundEnabled: Boolean = true,
    val adhanMasterEnabled: Boolean = true,
    val fajrAlertMode: PrayerAlertMode = PrayerAlertMode.ADHAN,
    val dhuhrAlertMode: PrayerAlertMode = PrayerAlertMode.ADHAN,
    val asrAlertMode: PrayerAlertMode = PrayerAlertMode.ADHAN,
    val maghribAlertMode: PrayerAlertMode = PrayerAlertMode.ADHAN,
    val ishaAlertMode: PrayerAlertMode = PrayerAlertMode.ADHAN,
    val useGpsLocation: Boolean = false,
    val selectedDistrictId: String = "dhaka",
    val gpsLat: Double = 23.8103,
    val gpsLon: Double = 90.4125,
    val gpsLocationName: String = "ঢাকা",
    val calculationMethod: CalculationMethod = CalculationMethod.KARACHI,
    val asrMadhab: AsrMadhab = AsrMadhab.HANAFI,
    val hijriAdjustmentDays: Int = 0,
    val quizTimerEnabled: Boolean = true,
    val quizTimerSeconds: Int = 30, // 15, 30, 60, or 0 for Unlimited
    val dailyNotificationsEnabled: Boolean = true,
    val islamicEventRemindersEnabled: Boolean = true,
    val lastReadSurah: Int = 1,
    val lastReadAyah: Int = 1,
    val quranArabicFontSize: Int = 26,
    val quizHighScore: Int = 0,
    val dailyQuizLastDate: String = "",
    val dailyQuizStreak: Int = 0
) {
    fun alertModeFor(prayer: PrayerType): PrayerAlertMode {
        if (!adhanMasterEnabled && prayer != PrayerType.SUNRISE) {
            return when (prayer) {
                PrayerType.FAJR -> if (fajrAlertMode == PrayerAlertMode.OFF) PrayerAlertMode.OFF else PrayerAlertMode.NOTIFICATION
                PrayerType.DHUHR -> if (dhuhrAlertMode == PrayerAlertMode.OFF) PrayerAlertMode.OFF else PrayerAlertMode.NOTIFICATION
                PrayerType.ASR -> if (asrAlertMode == PrayerAlertMode.OFF) PrayerAlertMode.OFF else PrayerAlertMode.NOTIFICATION
                PrayerType.MAGHRIB -> if (maghribAlertMode == PrayerAlertMode.OFF) PrayerAlertMode.OFF else PrayerAlertMode.NOTIFICATION
                PrayerType.ISHA -> if (ishaAlertMode == PrayerAlertMode.OFF) PrayerAlertMode.OFF else PrayerAlertMode.NOTIFICATION
                PrayerType.SUNRISE -> PrayerAlertMode.OFF
            }
        }
        return when (prayer) {
            PrayerType.FAJR -> fajrAlertMode
            PrayerType.DHUHR -> dhuhrAlertMode
            PrayerType.ASR -> asrAlertMode
            PrayerType.MAGHRIB -> maghribAlertMode
            PrayerType.ISHA -> ishaAlertMode
            PrayerType.SUNRISE -> PrayerAlertMode.OFF
        }
    }
}

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VOICE_FEEDBACK = booleanPreferencesKey("voice_feedback")
        val TASBIH_VIBRATION = booleanPreferencesKey("tasbih_vibration")
        val TASBIH_SOUND = booleanPreferencesKey("tasbih_sound")
        val ADHAN_MASTER = booleanPreferencesKey("adhan_master")
        val FAJR_ALERT = intPreferencesKey("fajr_alert")
        val DHUHR_ALERT = intPreferencesKey("dhuhr_alert")
        val ASR_ALERT = intPreferencesKey("asr_alert")
        val MAGHRIB_ALERT = intPreferencesKey("maghrib_alert")
        val ISHA_ALERT = intPreferencesKey("isha_alert")
        val USE_GPS = booleanPreferencesKey("use_gps")
        val DISTRICT_ID = stringPreferencesKey("district_id")
        val GPS_LAT = doublePreferencesKey("gps_lat")
        val GPS_LON = doublePreferencesKey("gps_lon")
        val GPS_NAME = stringPreferencesKey("gps_name")
        val CALC_METHOD = intPreferencesKey("calc_method")
        val ASR_MADHAB = intPreferencesKey("asr_madhab")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset")
        val QUIZ_TIMER_ENABLED = booleanPreferencesKey("quiz_timer_enabled")
        val QUIZ_TIMER_SECONDS = intPreferencesKey("quiz_timer_seconds")
        val DAILY_NOTIFS = booleanPreferencesKey("daily_notifs")
        val EVENT_REMINDERS = booleanPreferencesKey("event_reminders")
        val LAST_READ_SURAH = intPreferencesKey("last_read_surah")
        val LAST_READ_AYAH = intPreferencesKey("last_read_ayah")
        val QURAN_FONT_SIZE = intPreferencesKey("quran_font_size")
        val QUIZ_HIGH_SCORE = intPreferencesKey("quiz_high_score")
        val DAILY_QUIZ_DATE = stringPreferencesKey("daily_quiz_date")
        val DAILY_QUIZ_STREAK = intPreferencesKey("daily_quiz_streak")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            language = AppLanguage.entries.find { it.code == prefs[Keys.LANGUAGE] } ?: AppLanguage.BENGALI,
            theme = ThemePreference.entries.find { it.code == prefs[Keys.THEME] } ?: ThemePreference.SYSTEM,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            voiceFeedbackEnabled = prefs[Keys.VOICE_FEEDBACK] ?: true,
            tasbihVibrationEnabled = prefs[Keys.TASBIH_VIBRATION] ?: true,
            tasbihSoundEnabled = prefs[Keys.TASBIH_SOUND] ?: true,
            adhanMasterEnabled = prefs[Keys.ADHAN_MASTER] ?: true,
            fajrAlertMode = PrayerAlertMode.entries.find { it.code == prefs[Keys.FAJR_ALERT] } ?: PrayerAlertMode.ADHAN,
            dhuhrAlertMode = PrayerAlertMode.entries.find { it.code == prefs[Keys.DHUHR_ALERT] } ?: PrayerAlertMode.ADHAN,
            asrAlertMode = PrayerAlertMode.entries.find { it.code == prefs[Keys.ASR_ALERT] } ?: PrayerAlertMode.ADHAN,
            maghribAlertMode = PrayerAlertMode.entries.find { it.code == prefs[Keys.MAGHRIB_ALERT] } ?: PrayerAlertMode.ADHAN,
            ishaAlertMode = PrayerAlertMode.entries.find { it.code == prefs[Keys.ISHA_ALERT] } ?: PrayerAlertMode.ADHAN,
            useGpsLocation = prefs[Keys.USE_GPS] ?: false,
            selectedDistrictId = prefs[Keys.DISTRICT_ID] ?: "dhaka",
            gpsLat = prefs[Keys.GPS_LAT] ?: 23.8103,
            gpsLon = prefs[Keys.GPS_LON] ?: 90.4125,
            gpsLocationName = prefs[Keys.GPS_NAME] ?: "ঢাকা",
            calculationMethod = CalculationMethod.entries.find { it.id == prefs[Keys.CALC_METHOD] } ?: CalculationMethod.KARACHI,
            asrMadhab = AsrMadhab.entries.find { it.id == prefs[Keys.ASR_MADHAB] } ?: AsrMadhab.HANAFI,
            hijriAdjustmentDays = (prefs[Keys.HIJRI_OFFSET] ?: 0).coerceIn(-2, 2),
            quizTimerEnabled = prefs[Keys.QUIZ_TIMER_ENABLED] ?: true,
            quizTimerSeconds = prefs[Keys.QUIZ_TIMER_SECONDS] ?: 30,
            dailyNotificationsEnabled = prefs[Keys.DAILY_NOTIFS] ?: true,
            islamicEventRemindersEnabled = prefs[Keys.EVENT_REMINDERS] ?: true,
            lastReadSurah = prefs[Keys.LAST_READ_SURAH] ?: 1,
            lastReadAyah = prefs[Keys.LAST_READ_AYAH] ?: 1,
            quranArabicFontSize = prefs[Keys.QURAN_FONT_SIZE] ?: 26,
            quizHighScore = prefs[Keys.QUIZ_HIGH_SCORE] ?: 0,
            dailyQuizLastDate = prefs[Keys.DAILY_QUIZ_DATE] ?: "",
            dailyQuizStreak = prefs[Keys.DAILY_QUIZ_STREAK] ?: 0
        )
    }

    suspend fun update(transform: (MutablePreferences) -> Unit) {
        context.dataStore.edit(transform)
    }

    suspend fun setLanguage(lang: AppLanguage) = update { it[Keys.LANGUAGE] = lang.code }
    suspend fun setTheme(theme: ThemePreference) = update { it[Keys.THEME] = theme.code }
    suspend fun setSoundEnabled(v: Boolean) = update { it[Keys.SOUND_ENABLED] = v }
    suspend fun setVoiceFeedbackEnabled(v: Boolean) = update { it[Keys.VOICE_FEEDBACK] = v }
    suspend fun setTasbihVibration(v: Boolean) = update { it[Keys.TASBIH_VIBRATION] = v }
    suspend fun setTasbihSound(v: Boolean) = update { it[Keys.TASBIH_SOUND] = v }
    suspend fun setAdhanMaster(v: Boolean) = update { it[Keys.ADHAN_MASTER] = v }

    suspend fun setPrayerAlertMode(prayer: PrayerType, mode: PrayerAlertMode) = update {
        when (prayer) {
            PrayerType.FAJR -> it[Keys.FAJR_ALERT] = mode.code
            PrayerType.DHUHR -> it[Keys.DHUHR_ALERT] = mode.code
            PrayerType.ASR -> it[Keys.ASR_ALERT] = mode.code
            PrayerType.MAGHRIB -> it[Keys.MAGHRIB_ALERT] = mode.code
            PrayerType.ISHA -> it[Keys.ISHA_ALERT] = mode.code
            PrayerType.SUNRISE -> Unit
        }
    }

    suspend fun setDistrict(districtId: String) = update {
        it[Keys.USE_GPS] = false
        it[Keys.DISTRICT_ID] = districtId
    }

    suspend fun setGpsLocation(lat: Double, lon: Double, name: String) = update {
        it[Keys.USE_GPS] = true
        it[Keys.GPS_LAT] = lat
        it[Keys.GPS_LON] = lon
        it[Keys.GPS_NAME] = name
    }

    suspend fun setCalculationMethod(method: CalculationMethod) = update { it[Keys.CALC_METHOD] = method.id }
    suspend fun setAsrMadhab(madhab: AsrMadhab) = update { it[Keys.ASR_MADHAB] = madhab.id }
    suspend fun setHijriAdjustment(offset: Int) = update { it[Keys.HIJRI_OFFSET] = offset.coerceIn(-2, 2) }
    suspend fun setQuizTimerEnabled(enabled: Boolean) = update { it[Keys.QUIZ_TIMER_ENABLED] = enabled }
    suspend fun setQuizTimerSeconds(seconds: Int) = update { it[Keys.QUIZ_TIMER_SECONDS] = seconds }
    suspend fun setDailyNotifications(enabled: Boolean) = update { it[Keys.DAILY_NOTIFS] = enabled }
    suspend fun setEventReminders(enabled: Boolean) = update { it[Keys.EVENT_REMINDERS] = enabled }

    suspend fun setLastRead(surah: Int, ayah: Int) = update {
        it[Keys.LAST_READ_SURAH] = surah
        it[Keys.LAST_READ_AYAH] = ayah
    }

    suspend fun setQuranFontSize(size: Int) = update { it[Keys.QURAN_FONT_SIZE] = size.coerceIn(18, 40) }

    suspend fun updateHighScoreIfBetter(score: Int) = update {
        val current = it[Keys.QUIZ_HIGH_SCORE] ?: 0
        if (score > current) it[Keys.QUIZ_HIGH_SCORE] = score
    }

    suspend fun recordDailyQuizPlayed(dateIso: String, yesterdayIso: String) = update {
        val prevDate = it[Keys.DAILY_QUIZ_DATE] ?: ""
        if (prevDate != dateIso) {
            val prevStreak = it[Keys.DAILY_QUIZ_STREAK] ?: 0
            it[Keys.DAILY_QUIZ_STREAK] = if (prevDate == yesterdayIso) prevStreak + 1 else 1
            it[Keys.DAILY_QUIZ_DATE] = dateIso
        }
    }
}
