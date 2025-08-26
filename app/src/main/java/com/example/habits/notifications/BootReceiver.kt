package com.example.habits.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habits.Graph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.Default).launch {
            // Требует маленького метода в DAO (см. ниже): reminders.all()
            val all = try { Graph.repo.allReminders() } catch (_: Throwable) { emptyList() }
            all.forEach { Graph.scheduler.schedule(it) }
        }
    }
}
