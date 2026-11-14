package com.example.data.model

enum class AppLanguage(val code: String, val displayName: String) {
    BENGALI("bn", "বাংলা"),
    ENGLISH("en", "English")
}

enum class ThemePreference(val code: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark")
}

enum class CalculationMethod(val id: Int, val bnName: String, val enName: String, val fajrAngle: Double, val ishaAngle: Double) {
    KARACHI(1, "করাচি (বাংলাদেশ স্ট্যান্ডার্ড)", "Karachi (Bangladesh)", 18.0, 18.0),
    MWL(3, "মুসলিম ওয়ার্ল্ড লীগ (MWL)", "Muslim World League", 18.0, 17.0),
    ISNA(2, "ISNA (উত্তর আমেরিকা)", "ISNA", 15.0, 15.0),
    UMM_AL_QURA(4, "উম্মুল কুরা (মক্কা)", "Umm Al-Qura", 18.5, 0.0), // 90 min after maghrib
    EGYPT(5, "মিশরীয় জরিপ কর্তৃপক্ষ", "Egyptian General Authority", 19.5, 17.5)
}

enum class AsrMadhab(val id: Int, val bnName: String, val enName: String) {
    HANAFI(1, "হানাফি (বাংলাদেশ)", "Hanafi"),
    SHAFI(0, "শাফিঈ / জমহুর", "Shafi'i / Standard")
}

enum class PrayerType(val key: String, val bnName: String, val enName: String) {
    FAJR("fajr", "ফজর", "Fajr"),
    SUNRISE("sunrise", "সূর্যোদয়", "Sunrise"),
    DHUHR("dhuhr", "যোহর", "Dhuhr"),
    ASR("asr", "আসর", "Asr"),
    MAGHRIB("maghrib", "মাগরিব", "Maghrib"),
    ISHA("isha", "এশা", "Isha")
}

enum class PrayerAlertMode(val code: Int, val bnName: String, val enName: String) {
    ADHAN(2, "আজান ও নোটিফিকেশন", "Adhan & Alert"),
    NOTIFICATION(1, "শুধু নোটিফিকেশন", "Notification Only"),
    OFF(0, "বন্ধ", "Off")
}

data class DistrictLocation(
    val id: String,
    val bnName: String,
    val enName: String,
    val divisionBn: String,
    val lat: Double,
    val lon: Double
)

data class DailyPrayerTimes(
    val dateIso: String,
    val locationNameBn: String,
    val locationNameEn: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val source: String // "online" or "offline_calc" or "cached"
)

data class SurahInfo(
    val number: Int,
    val nameArabic: String,
    val nameBn: String,
    val nameEn: String,
    val meaningBn: String,
    val ayahCount: Int,
    val revelationTypeBn: String, // মক্কী / মাদানী
    val startJuz: Int
)

data class AyahItem(
    val surahNumber: Int,
    val numberInSurah: Int,
    val globalNumber: Int,
    val arabicText: String,
    val transliterationBn: String,
    val translationBn: String,
    val juz: Int
)

data class ParaInfo(
    val juzNumber: Int,
    val nameArabic: String,
    val nameBn: String,
    val startSurah: Int,
    val startAyah: Int,
    val endSurah: Int,
    val endAyah: Int
)

data class DuaItem(
    val id: Int,
    val categoryId: String,
    val categoryBn: String,
    val titleBn: String,
    val titleEn: String,
    val arabic: String,
    val transliterationBn: String,
    val translationBn: String,
    val fazilatBn: String,
    val referenceBn: String
)

data class TasbihDhikr(
    val id: String,
    val arabic: String,
    val titleBn: String,
    val meaningBn: String,
    val defaultTarget: Int
)

enum class QuizDifficulty(val code: String, val bnName: String, val enName: String, val idRange: IntRange) {
    ALL("all", "সব স্তর (১–১০০০)", "All Levels", 1..1000),
    EASY("easy", "সহজ (১–২০০)", "Easy (1-200)", 1..200),
    MEDIUM("medium", "মাঝারি (২০১–৪০০)", "Medium (201-400)", 201..400),
    HARD("hard", "কঠিন (৪০১–৭০০)", "Hard (401-700)", 401..700),
    EXPERT("expert", "অত্যন্ত কঠিন (৭০১–১০০০)", "Very Hard (701-1000)", 701..1000)
}

enum class QuizCategory(val code: String, val bnName: String, val enName: String) {
    ALL("all", "সব বিষয়", "All Topics"),
    QURAN("quran", "আল-কুরআন", "Al-Quran"),
    HADITH("hadith", "হাদিস", "Hadith"),
    PROPHETS("prophets", "নবী-রাসূলদের জীবনী", "Prophets"),
    SAHABA("sahaba", "সাহাবিদের জীবনী", "Sahaba"),
    HISTORY("history", "ইসলামের ইতিহাস", "Islamic History"),
    SALAH("salah", "নামাজ", "Salah"),
    SAWM("sawm", "রোজা", "Fasting"),
    HAJJ_ZAKAT("hajj_zakat", "হজ ও যাকাত", "Hajj & Zakat"),
    GENERAL("general", "ইসলামিক সাধারণ জ্ঞান", "General Knowledge"),
    ADAB("adab", "ইসলামী আদব ও আখলাক", "Adab & Akhlaq")
}

data class QuizQuestion(
    val id: Int,
    val level: String,
    val cat: String,
    val q: String,
    val opts: List<String>,
    val ans: Int
)

data class QuizLevelInfo(
    val levelNumber: Int,
    val titleBn: String,
    val difficulty: QuizDifficulty,
    val questionCount: Int,
    val unlocked: Boolean,
    val bestPercentage: Int
)

data class BanglaDateInfo(
    val day: Int,
    val monthIndex: Int, // 0..11
    val monthNameBn: String,
    val year: Int,
    val seasonBn: String
)

data class HijriDateInfo(
    val day: Int,
    val monthIndex: Int, // 0..11
    val monthNameBn: String,
    val monthNameAr: String,
    val year: Int,
    val islamicEventBn: String? = null
)

data class IslamicQuoteItem(
    val typeBn: String, // হাদিস / বাণী / দোয়া
    val arabic: String,
    val textBn: String,
    val sourceBn: String
)
