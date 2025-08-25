package com.example.habits

import android.app.Application
import com.example.habits.data.AppDatabase
import com.example.habits.data.HabitRepository
import com.example.habits.reminders.ReminderScheduler
import com.example.habits.notifications.Notifications

object Graph {
    lateinit var db: AppDatabase
    lateinit var repo: HabitRepository
    lateinit var scheduler: ReminderScheduler
}

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Notifications.createChannels(this)
        val database = AppDatabase.build(this)
        Graph.db = database
        Graph.repo = HabitRepository(database.habitsDao(), database.checkinsDao(), database.remindersDao())
        Graph.scheduler = ReminderScheduler(this, Graph.repo)
    }
}
