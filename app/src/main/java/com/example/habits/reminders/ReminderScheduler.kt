package com.example.habits.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.habits.data.HabitRepository
import com.example.habits.data.ReminderEntity
import java.time.*

class ReminderScheduler(
    private val ctx: Context,
    private val repo: HabitRepository
) {
    private val am: AlarmManager = ctx.getSystemService(AlarmManager::class.java)

    /** Поставить будильник для одного напоминания. */
    suspend fun schedule(rem: ReminderEntity) {
        val whenMs = computeNext(Instant.now(), rem, ZoneId.systemDefault()).toEpochMilli()
        val pi = pending(rem.id, rem.habitId)

        // Если нет разрешения на точные — используем неточный set()
        if (Build.VERSION.SDK_INT >= 31 && !ExactAlarms.hasPermission(ctx)) {
            am.set(AlarmManager.RTC_WAKEUP, whenMs, pi)
            return
        }

        // Пытаемся точный, при SecurityException откатываемся на set()
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMs, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, whenMs, pi)
            }
        } catch (_: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, whenMs, pi)
        }
    }

    /** Поставить будильники для всех напоминаний привычки. */
    suspend fun scheduleForHabit(habitId: Long) {
        val hw = repo.getHabitWithReminders(habitId) ?: return
        hw.reminders.forEach { schedule(it) }
    }

    /** Отменить будильник конкретного напоминания. */
    fun cancel(remId: Long, habitId: Long) {
        val pi = pending(remId, habitId)
        am.cancel(pi)
        pi.cancel()
    }

    // --- Вспомогательные ---

    private fun pending(remId: Long, habitId: Long): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra("reminderId", remId)
            putExtra("habitId", habitId)
        }
        val req = (remId % Int.MAX_VALUE).toInt()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(ctx, req, i, flags)
    }

    /** Рассчитывает ближайшее будущее срабатывание по daysMask, hour:minute и локальной зоне. */
    fun computeNext(nowUtc: Instant, r: ReminderEntity, zone: ZoneId): Instant {
        val nowZ = ZonedDateTime.ofInstant(nowUtc, zone)
        repeat(14) { delta ->
            val day = nowZ.toLocalDate().plusDays(delta.toLong())
            val dowIndex = ((day.dayOfWeek.value + 6) % 7) // Mon=0..Sun=6
            val match = (r.daysMask and (1 shl dowIndex)) != 0
            val candidate = ZonedDateTime.of(day, LocalTime.of(r.hour, r.minute), zone).toInstant()
            if (match && candidate.isAfter(nowUtc)) return candidate
        }
        return nowUtc.plus(Duration.ofDays(1)) // fallback
    }
}
