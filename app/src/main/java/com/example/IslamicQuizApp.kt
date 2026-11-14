package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.ads.AdMobManager
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsDataStore
import com.example.receiver.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IslamicQuizApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set
    lateinit var settingsDataStore: SettingsDataStore
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        settingsDataStore = SettingsDataStore(this)
        createNotificationChannels()
        AdMobManager.registerAndInitialize(this)

        appScope.launch {
            val settings = settingsDataStore.settingsFlow.first()
            AlarmScheduler.scheduleAllAlarms(this@IslamicQuizApp, settings, database.prayerCacheDao())
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java) ?: return

            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER_ADHAN,
                "নামাজের ওয়াক্ত ও আজান",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "পাঁচ ওয়াক্ত নামাজের সময় নোটিফিকেশন ও আজান"
                enableVibration(true)
            }

            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_ISLAMIC,
                "দৈনিক হাদিস, দোয়া ও ইসলামিক বাণী",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "প্রতিদিন সকালে হাদিস, দোয়া ও ইসলামিক দিবসের স্মরণ"
            }

            manager.createNotificationChannel(prayerChannel)
            manager.createNotificationChannel(dailyChannel)
        }
    }

    companion object {
        const val CHANNEL_PRAYER_ADHAN = "channel_prayer_adhan"
        const val CHANNEL_DAILY_ISLAMIC = "channel_daily_islamic"
    }
}
