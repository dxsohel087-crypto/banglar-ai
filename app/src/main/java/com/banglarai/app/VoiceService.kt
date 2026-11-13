package com.banglarai.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

/**
 * Foreground-service foundation for future background voice features.
 * Microphone capture must only be started from a visible activity after permission is granted.
 */
class VoiceService : Service() {

    companion object {
        const val ACTION_START = "com.banglarai.app.action.START_VOICE_SERVICE"
        const val ACTION_STOP = "com.banglarai.app.action.STOP_VOICE_SERVICE"
        private const val CHANNEL_ID = "banglar_ai_voice"
        private const val NOTIFICATION_ID = 2101
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "বাংলার AI ভয়েস",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "বাংলার AI ভয়েস সার্ভিসের নোটিফিকেশন"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("বাংলার AI")
                .setContentText("ভয়েস সার্ভিস প্রস্তুত")
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("বাংলার AI")
                .setContentText("ভয়েস সার্ভিস প্রস্তুত")
                .setOngoing(true)
                .build()
        }

        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
