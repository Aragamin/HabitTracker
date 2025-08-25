package com.example.habits.data

import kotlinx.coroutines.flow.Flow
import java.time.*

class HabitRepository(
    private val habits: HabitsDao,
    private val checkins: CheckinsDao,
    private val reminders: RemindersDao
) {
    fun observeHabits(): Flow<List<HabitEntity>> = habits.observeAll()

    suspend fun newHabit(name: String, targetPerDay: Int, hour: Int, minute: Int, daysMask: Int): Long {
        val id = habits.insert(HabitEntity(name = name, targetPerDay = targetPerDay))
        reminders.insert(ReminderEntity(habitId = id, hour = hour, minute = minute, daysMask = daysMask))
        return id
    }

    suspend fun addCheckinNow(habitId: Long) { checkins.insert(CheckinEntity(habitId = habitId)) }

    suspend fun getHabitWithReminders(id: Long) = habits.getWithReminders(id)

    suspend fun todayCount(habitId: Long, zone: ZoneId): Int {
        val now = ZonedDateTime.now(zone).toLocalDate()
        val from = now.atStartOfDay(zone).toInstant()
        val to = now.plusDays(1).atStartOfDay(zone).toInstant()
        return checkins.countBetween(habitId, from, to)
    }

    suspend fun calcStreak(habitId: Long, zone: ZoneId, targetPerDay: Int): Int {
        val last = checkins.lastN(habitId).map { it.atZone(zone).toLocalDate() }
        if (last.isEmpty()) return 0
        val dates = last.groupingBy { it }.eachCount()
        var streak = 0
        var day = ZonedDateTime.now(zone).toLocalDate()
        while (true) {
            val done = (dates[day] ?: 0) >= targetPerDay
            if (done) { streak++; day = day.minusDays(1) } else break
        }
        return streak
    }
}
