package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.PrayerCacheDao
import com.example.data.local.UserSettings
import com.example.data.model.PrayerAlertMode
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerRepository
import com.example.data.util.PrayerTimeCalculator
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object AlarmScheduler {

    private const val RC_FAJR = 101
    private const val RC_DHUHR = 102
    private const val RC_ASR = 103
    private const val RC_MAGHRIB = 104
    private const val RC_ISHA = 105
    private const val RC_DAILY_ISLAMIC = 201

    suspend fun scheduleAllAlarms(
        context: Context,
        settings: UserSettings,
        prayerCacheDao: PrayerCacheDao
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val repo = PrayerRepository(prayerCacheDao)
        val today = LocalDate.now()
        val now = ZonedDateTime.now()

        val todayTimes = repo.getPrayerTimes(today, settings, tryOnlineSync = false)
        val tomorrowTimes = repo.getPrayerTimes(today.plusDays(1), settings, tryOnlineSync = false)

        val prayers = listOf(
            Triple(PrayerType.FAJR, RC_FAJR, todayTimes.fajr to tomorrowTimes.fajr),
            Triple(PrayerType.DHUHR, RC_DHUHR, todayTimes.dhuhr to tomorrowTimes.dhuhr),
            Triple(PrayerType.ASR, RC_ASR, todayTimes.asr to tomorrowTimes.asr),
            Triple(PrayerType.MAGHRIB, RC_MAGHRIB, todayTimes.maghrib to tomorrowTimes.maghrib),
            Triple(PrayerType.ISHA, RC_ISHA, todayTimes.isha to tomorrowTimes.isha)
        )

        for ((prayer, rc, timesPair) in prayers) {
            val mode = settings.alertModeFor(prayer)
            if (mode == PrayerAlertMode.OFF) {
                cancelAlarm(context, alarmManager, rc)
                continue
            }

            val todayTarget = ZonedDateTime.of(today, PrayerTimeCalculator.parseLocalTime(timesPair.first), ZoneId.systemDefault())
            val triggerZdt = if (todayTarget.isAfter(now.plusSeconds(10))) {
                todayTarget
            } else {
                ZonedDateTime.of(today.plusDays(1), PrayerTimeCalculator.parseLocalTime(timesPair.second), ZoneId.systemDefault())
            }

            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = PrayerAlarmReceiver.ACTION_PRAYER_ALARM
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_KEY, prayer.key)
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayer.bnName)
                putExtra(PrayerAlarmReceiver.EXTRA_PLAY_ADHAN, mode == PrayerAlertMode.ADHAN)
            }
            scheduleExactOrFallback(context, alarmManager, rc, triggerZdt.toInstant().toEpochMilli(), intent)
        }

        // Daily morning Islamic Quote / Hadith / Dua / Event notification at 07:00 AM
        if (settings.dailyNotificationsEnabled || settings.islamicEventRemindersEnabled) {
            val morningToday = ZonedDateTime.of(today, LocalTime.of(7, 0), ZoneId.systemDefault())
            val triggerMorning = if (morningToday.isAfter(now.plusSeconds(10))) morningToday else morningToday.plusDays(1)
            val dailyIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = PrayerAlarmReceiver.ACTION_DAILY_ISLAMIC
            }
            scheduleExactOrFallback(context, alarmManager, RC_DAILY_ISLAMIC, triggerMorning.toInstant().toEpochMilli(), dailyIntent)
        } else {
            cancelAlarm(context, alarmManager, RC_DAILY_ISLAMIC)
        }
    }

    private fun scheduleExactOrFallback(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        triggerAtMillis: Long,
        intent: Intent
    ) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        try {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
