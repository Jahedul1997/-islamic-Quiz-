package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_ayahs", primaryKeys = ["surahNumber", "numberInSurah"])
data class CachedAyahEntity(
    val surahNumber: Int,
    val numberInSurah: Int,
    val globalNumber: Int,
    val arabicText: String,
    val transliterationBn: String,
    val translationBn: String,
    val juz: Int
)

@Entity(tableName = "quran_bookmarks", primaryKeys = ["surahNumber", "ayahNumber"])
data class QuranBookmarkEntity(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameBn: String,
    val arabicSnippet: String,
    val translationSnippet: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dua_bookmarks")
data class DuaBookmarkEntity(
    @PrimaryKey val duaId: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasbih_daily_logs", primaryKeys = ["dateIso", "dhikrId"])
data class TasbihDailyLogEntity(
    val dateIso: String,
    val dhikrId: String,
    val dhikrTitleBn: String,
    val count: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_answered")
data class QuizAnsweredEntity(
    @PrimaryKey val questionId: Int,
    val answeredCorrectly: Boolean,
    val answeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_level_progress")
data class QuizLevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val unlocked: Boolean,
    val bestPercentage: Int,
    val bestScore: Int
)

@Entity(tableName = "cached_prayer_times")
data class CachedPrayerTimesEntity(
    @PrimaryKey val cacheKey: String, // dateIso|location|method|madhab
    val dateIso: String,
    val locationNameBn: String,
    val locationNameEn: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val source: String,
    val updatedAt: Long = System.currentTimeMillis()
)
