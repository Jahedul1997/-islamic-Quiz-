package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext
                    val settings = SettingsDataStore(app).settingsFlow.first()
                    val db = AppDatabase.getInstance(app)
                    AlarmScheduler.scheduleAllAlarms(app, settings, db.prayerCacheDao())
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
