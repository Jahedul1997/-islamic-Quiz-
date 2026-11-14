package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedAyahEntity::class,
        QuranBookmarkEntity::class,
        DuaBookmarkEntity::class,
        TasbihDailyLogEntity::class,
        QuizAnsweredEntity::class,
        QuizLevelProgressEntity::class,
        CachedPrayerTimesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun duaDao(): DuaDao
    abstract fun tasbihDao(): TasbihDao
    abstract fun quizDao(): QuizDao
    abstract fun prayerCacheDao(): PrayerCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "islamic_quiz_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
