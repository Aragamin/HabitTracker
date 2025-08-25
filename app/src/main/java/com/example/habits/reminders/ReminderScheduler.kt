package com.example.habits.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.habits.data.HabitRepository
import com.example.habits.data.ReminderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.*

class ReminderScheduler(private val ctx: Context, private val repo: HabitRepository) {
    private val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(rem: ReminderEntity) {
        val next = computeNext(Instant.now(), rem, ZoneId.systemDefault())
        val pi = pending(rem.id)
        val intent = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra("reminderId", rem.id)
            putExtra("habitId", rem.habitId)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pending = PendingIntent.getBroadcast(ctx, rem.id.toInt(), intent, flags)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toEpochMilli(), pending)
    }

    fun cancel(remId: Long) { pending(remId).cancel() }

    private fun pending(remId: Long) =
        PendingIntent.getBroadcast(ctx, remId.toInt(), Intent(ctx, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun computeNext(nowUtc: Instant, r: ReminderEntity, zone: ZoneId): Instant {
        val nowZ = ZonedDateTime.ofInstant(nowUtc, zone)
        for (i in 0..14) {
            val day = nowZ.toLocalDate().plusDays(i.toLong())
            val dowIndex = ((day.dayOfWeek.value + 6) % 7) // Mon=1 -> 0
            val match = (r.daysMask and (1 shl dowIndex)) != 0
            val candidate = ZonedDateTime.of(day, LocalTime.of(r.hour, r.minute), zone).toInstant()
            if (match && candidate.isAfter(nowUtc)) return candidate
        }
        return nowUtc.plus(Duration.ofDays(1))
    }
}
