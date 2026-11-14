package com.example.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.IslamicQuizApp
import com.example.MainActivity
import com.example.R

class AdhanPlaybackService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val prayerName = intent?.getStringExtra(EXTRA_PRAYER_NAME) ?: "নামাজের ওয়াক্ত"
        val notification = buildNotification(prayerName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        playLocalAdhan()
        return START_NOT_STICKY
    }

    private fun playLocalAdhan() {
        releasePlayer()
        try {
            val mp = MediaPlayer.create(this, R.raw.adhan_call)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setOnCompletionListener {
                    stopSelf()
                }
                setOnErrorListener { _, _, _ ->
                    stopSelf()
                    true
                }
                start()
            }
            if (mp == null) stopSelf() else mediaPlayer = mp
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun buildNotification(prayerName: String): android.app.Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, AdhanPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, IslamicQuizApp.CHANNEL_PRAYER_ADHAN)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("আজান — $prayerName")
            .setContentText("হাইয়্যা আলাস সালাহ — নামাজের সময় হয়েছে")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_media_pause, "আজান বন্ধ করুন", stopIntent)
            .setOngoing(true)
            .build()
    }

    private fun releasePlayer() {
        mediaPlayer?.run {
            runCatching { stop() }
            runCatching { release() }
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.example.action.START_ADHAN"
        const val ACTION_STOP = "com.example.action.STOP_ADHAN"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        private const val NOTIFICATION_ID = 7001

        fun startAdhan(context: Context, prayerName: String) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PRAYER_NAME, prayerName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopAdhan(context: Context) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
