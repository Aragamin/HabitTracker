package com.example.habits.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.habits.R
import com.example.habits.notifications.Notifications
import com.example.habits.Graph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra("habitId", -1)
        val remId = intent.getLongExtra("reminderId", -1)
        if (habitId <= 0 || remId <= 0) return

        // Показать уведомление
        val nm = context.getSystemService(NotificationManager::class.java)
        val notif = NotificationCompat.Builder(context, Notifications.CHANNEL_REMIND)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Напоминание по привычке")
            .setContentText("Пора выполнить цель")
            .setAutoCancel(true)
            .build()
        nm.notify(remId.toInt(), notif)

        // Перепланировать следующее срабатывание
        CoroutineScope(Dispatchers.Default).launch {
            val habit = Graph.repo.getHabitWithReminders(habitId)
            habit?.reminders?.firstOrNull { it.id == remId }?.let { Graph.scheduler.schedule(it) }
        }
    }
}
