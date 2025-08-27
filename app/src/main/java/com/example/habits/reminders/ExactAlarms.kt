package com.example.habits.reminders

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object ExactAlarms {
    fun hasPermission(ctx: Context): Boolean =
        if (Build.VERSION.SDK_INT >= 31)
            ctx.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        else true

    /** Откроет системный экран запроса доступа к точным будильникам. */
    fun request(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 31) {
            ctx.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
