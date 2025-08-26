package com.example.habits

import android.app.Application
import com.example.habits.data.AppDatabase
import com.example.habits.data.HabitRepository
import com.example.habits.notifications.Notifications
import com.example.habits.reminders.ReminderScheduler

/** Глобальные зависимости (простой DI). */
object Graph {
    lateinit var db: AppDatabase
    lateinit var repo: HabitRepository
    lateinit var scheduler: ReminderScheduler
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Канал уведомлений обязателен на API 26+
        Notifications.createChannels(this)

        // Room + репозиторий + планировщик
        val database = AppDatabase.build(this)
        Graph.db = database
        Graph.repo = HabitRepository(
            habits = database.habitsDao(),
            checkins = database.checkinsDao(),
            reminders = database.remindersDao()
        )
        Graph.scheduler = ReminderScheduler(this, Graph.repo)

        // Если добавлен BootReceiver — отдельный рескейдулинг не обязателен.
        // Можно (опционально) пересchedule-ить при старте приложения фоном.
        // Оставляем простую инициализацию без дополнительной логики.
    }
}
