package com.example.habits.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habits.Graph
import com.example.habits.notifications.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val remId = intent.getLongExtra("reminderId", -1)
        val habitId = intent.getLongExtra("habitId", -1)
        if (remId <= 0 || habitId <= 0) return

        // Показать уведомление
        Notifications.showReminder(
            context,
            notificationId = (remId % Int.MAX_VALUE).toInt(),
            title = "Напоминание по привычке",
            text = "Пора выполнить цель"
        )

        // Перепланировать следующее срабатывание
        CoroutineScope(Dispatchers.Default).launch {
            val hw = Graph.repo.getHabitWithReminders(habitId) ?: return@launch
            val rem = hw.reminders.firstOrNull { it.id == remId } ?: return@launch
            Graph.scheduler.schedule(rem)
        }
    }
}
