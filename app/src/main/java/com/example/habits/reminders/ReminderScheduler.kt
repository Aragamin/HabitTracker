package com.example.habits.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.habits.data.HabitRepository
import com.example.habits.data.ReminderEntity
import java.time.*

class ReminderScheduler(
    private val ctx: Context,
    private val repo: HabitRepository
) {
    private val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Поставить будильник для одного напоминания. */
    suspend fun schedule(rem: ReminderEntity) {
        val next = computeNext(Instant.now(), rem, ZoneId.systemDefault())
        val pi = pending(rem.id, rem.habitId)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toEpochMilli(), pi)
    }

    /** Поставить будильники для всех напоминаний привычки. */
    suspend fun scheduleForHabit(habitId: Long) {
        val hw = repo.getHabitWithReminders(habitId) ?: return
        hw.reminders.forEach { schedule(it) }
    }

    /** Отменить будильник конкретного напоминания. */
    fun cancel(remId: Long, habitId: Long) {
        pending(remId, habitId).cancel()
        am.cancel(pending(remId, habitId))
    }

    // --- Вспомогательные ---

    private fun pending(remId: Long, habitId: Long): PendingIntent {
        val i = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra("reminderId", remId)
            putExtra("habitId", habitId)
        }
        val req = ((remId % Int.MAX_VALUE).toInt())
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(ctx, req, i, flags)
    }

    /** Рассчитывает ближайшее будущее срабатывание по daysMask, hour:minute и локальной зоне. */
    fun computeNext(nowUtc: Instant, r: ReminderEntity, zone: ZoneId): Instant {
        val nowZ = ZonedDateTime.ofInstant(nowUtc, zone)
        repeat(14) { delta ->
            val day = nowZ.toLocalDate().plusDays(delta.toLong())
            val dowIndex = ((day.dayOfWeek.value + 6) % 7) // Mon=0 .. Sun=6
            val match = (r.daysMask and (1 shl dowIndex)) != 0
            val candidate = ZonedDateTime.of(day, LocalTime.of(r.hour, r.minute), zone).toInstant()
            if (match && candidate.isAfter(nowUtc)) return candidate
        }
        // Fallback: через сутки
        return nowUtc.plus(Duration.ofDays(1))
    }
}
