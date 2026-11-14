package com.example.receiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.IslamicQuizApp
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.seed.DuaStaticData
import com.example.data.seed.QuranOfflineBundledData
import com.example.data.util.HijriCalendarConverter
import com.example.service.AdhanPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext
                val db = AppDatabase.getInstance(app)
                val dataStore = SettingsDataStore(app)
                val settings = dataStore.settingsFlow.first()

                when (intent.action) {
                    ACTION_PRAYER_ALARM -> {
                        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: "নামাজ"
                        val playAdhan = intent.getBooleanExtra(EXTRA_PLAY_ADHAN, false)

                        if (playAdhan && settings.adhanMasterEnabled) {
                            AdhanPlaybackService.startAdhan(app, prayerName)
                        } else {
                            showNotification(
                                context = app,
                                id = 5001,
                                channelId = IslamicQuizApp.CHANNEL_PRAYER_ADHAN,
                                title = "$prayerName-এর ওয়াক্ত হয়েছে",
                                body = "আসসালাতু খাইরুম মিনান নাওম / নামাজের সময় হয়েছে। দ্রুত জামাআতের প্রস্তুতি নিন।"
                            )
                        }
                    }

                    ACTION_DAILY_ISLAMIC -> {
                        val today = LocalDate.now()
                        val hijri = HijriCalendarConverter.fromGregorian(today, settings.hijriAdjustmentDays)
                        val event = hijri.islamicEventBn

                        if (settings.islamicEventRemindersEnabled && event != null) {
                            showNotification(
                                context = app,
                                id = 6001,
                                channelId = IslamicQuizApp.CHANNEL_DAILY_ISLAMIC,
                                title = "আজকের বিশেষ ইসলামিক দিন: $event",
                                body = "হিজরি ${hijri.day} ${hijri.monthNameBn} ${hijri.year} — ইবাদত, দোয়া ও নফল আমলে দিনটি কাটান।"
                            )
                        } else if (settings.islamicEventRemindersEnabled && today.dayOfWeek == DayOfWeek.FRIDAY) {
                            showNotification(
                                context = app,
                                id = 6002,
                                channelId = IslamicQuizApp.CHANNEL_DAILY_ISLAMIC,
                                title = "জুমআ মোবারক!",
                                body = "আজ পবিত্র জুমার দিন। সূরা আল-কাহফ পাঠ করুন, বেশি বেশি দুরুদ শরীফ পড়ুন ও তাড়াতাড়ি মসজিদে যান।"
                            )
                        }

                        if (settings.dailyNotificationsEnabled) {
                            val quotes = QuranOfflineBundledData.dailyQuotes
                            val q = quotes[today.dayOfYear % quotes.size]
                            val dua = DuaStaticData.allDuas[today.dayOfYear % DuaStaticData.allDuas.size]
                            showNotification(
                                context = app,
                                id = 6003,
                                channelId = IslamicQuizApp.CHANNEL_DAILY_ISLAMIC,
                                title = "${q.typeBn} • আজকের দোয়া: ${dua.titleBn}",
                                body = "${q.textBn} (${q.sourceBn})"
                            )
                        }
                    }
                }

                // Reschedule for upcoming prayers and tomorrow
                AlarmScheduler.scheduleAllAlarms(app, settings, db.prayerCacheDao())
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.example.action.PRAYER_ALARM"
        const val ACTION_DAILY_ISLAMIC = "com.example.action.DAILY_ISLAMIC"
        const val EXTRA_PRAYER_KEY = "prayer_key"
        const val EXTRA_PRAYER_NAME = "prayer_name"
        const val EXTRA_PLAY_ADHAN = "play_adhan"

        fun showNotification(
            context: Context,
            id: Int,
            channelId: String,
            title: String,
            body: String
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }

            val openIntent = PendingIntent.getActivity(
                context,
                id,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openIntent)
                .build()

            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }
}
