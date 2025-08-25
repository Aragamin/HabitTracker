package com.example.habits.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object Notifications {
    const val CHANNEL_REMIND = "reminders"

    fun createChannels(ctx: Context) {
        //if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(CHANNEL_REMIND, "Habit reminders", NotificationManager.IMPORTANCE_DEFAULT)
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        //}
    }
}

