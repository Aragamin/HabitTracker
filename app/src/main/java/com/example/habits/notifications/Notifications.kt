package com.example.habits.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.habits.MainActivity
import com.example.habits.R

object Notifications {
    const val CHANNEL_REMIND = "reminders"

    /** Создаём канал (minSdk 26, обязателен). Вызывается один раз в App.onCreate(). */
    fun createChannels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(
            CHANNEL_REMIND,
            "Habit reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(ch)
    }

    /** Унифицированная отправка уведомления о привычке. */
    fun showReminder(
        ctx: Context,
        notificationId: Int,
        title: String,
        text: String
    ) {
        val intent = Intent(ctx, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(ctx, CHANNEL_REMIND)
            .setSmallIcon(R.drawable.ic_notification) // добавлен ранее в drawable
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.notify(notificationId, notif)
    }
}
