package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    fun getAyahsBySurah(surahNumber: Int): Flow<List<CachedAyahEntity>>

    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah ASC")
    suspend fun getAyahsBySurahOnce(surahNumber: Int): List<CachedAyahEntity>

    @Query("SELECT COUNT(DISTINCT surahNumber) FROM cached_ayahs")
    fun getCachedSurahCount(): Flow<Int>

    @Query("SELECT DISTINCT surahNumber FROM cached_ayahs")
    fun getCachedSurahNumbers(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<CachedAyahEntity>)

    @Query("SELECT * FROM quran_bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<QuranBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: QuranBookmarkEntity)

    @Query("DELETE FROM quran_bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun deleteBookmark(surahNumber: Int, ayahNumber: Int)
}

@Dao
interface DuaDao {
    @Query("SELECT duaId FROM dua_bookmarks")
    fun getBookmarkedDuaIds(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(item: DuaBookmarkEntity)

    @Query("DELETE FROM dua_bookmarks WHERE duaId = :duaId")
    suspend fun removeBookmark(duaId: Int)
}

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_daily_logs WHERE dateIso = :dateIso ORDER BY updatedAt DESC")
    fun getLogsForDate(dateIso: String): Flow<List<TasbihDailyLogEntity>>

    @Query("SELECT * FROM tasbih_daily_logs ORDER BY dateIso DESC, updatedAt DESC LIMIT 60")
    fun getRecentLogs(): Flow<List<TasbihDailyLogEntity>>

    @Query("SELECT COALESCE(SUM(count), 0) FROM tasbih_daily_logs")
    fun getLifetimeTotalCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(count), 0) FROM tasbih_daily_logs WHERE dateIso = :dateIso")
    fun getTodayTotalCount(dateIso: String): Flow<Int>

    @Query("SELECT * FROM tasbih_daily_logs WHERE dateIso = :dateIso AND dhikrId = :dhikrId LIMIT 1")
    suspend fun getLogOnce(dateIso: String, dhikrId: String): TasbihDailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: TasbihDailyLogEntity)
}

@Dao
interface QuizDao {
    @Query("SELECT questionId FROM quiz_answered")
    fun getAnsweredQuestionIds(): Flow<List<Int>>

    @Query("SELECT questionId FROM quiz_answered")
    suspend fun getAnsweredQuestionIdsOnce(): List<Int>

    @Query("SELECT COUNT(*) FROM quiz_answered WHERE answeredCorrectly = 1")
    fun getTotalCorrectCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAnswered(items: List<QuizAnsweredEntity>)

    @Query("DELETE FROM quiz_answered")
    suspend fun clearAnsweredQuestions()

    @Query("SELECT * FROM quiz_level_progress ORDER BY levelNumber ASC")
    fun getLevelProgress(): Flow<List<QuizLevelProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevelProgress(progress: QuizLevelProgressEntity)

    @Query("DELETE FROM quiz_level_progress")
    suspend fun clearLevelProgress()
}

@Dao
interface PrayerCacheDao {
    @Query("SELECT * FROM cached_prayer_times WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getByKey(cacheKey: String): CachedPrayerTimesEntity?

    @Query("SELECT * FROM cached_prayer_times ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getMostRecent(): CachedPrayerTimesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedPrayerTimesEntity)
}
