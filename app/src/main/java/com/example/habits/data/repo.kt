package com.example.habits.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.*

enum class DayMark { UNSET, DONE, MISSED, PARTIAL }

class HabitRepository(
    private val habits: HabitsDao,
    private val checkins: CheckinsDao,
    private val reminders: RemindersDao
) {
    /* ----- CRUD / чтение ----- */

    /** Реактивный список с todayCount — для списка UI. */
    fun observeHabitsWithToday(zone: ZoneId): Flow<List<HabitWithToday>> {
        val (from, to) = todayBounds(zone)
        return habits.observeWithToday(from, to)
    }

    /** Привычка с напоминаниями. */
    suspend fun getHabitWithReminders(id: Long): HabitWithReminder? =
        habits.getWithReminders(id)

    /** Создать привычку и один reminder. Возвращает id привычки. */
    suspend fun newHabit(
        name: String,
        targetPerDay: Int,
        hour: Int,
        minute: Int,
        daysMask: Int
    ): Long {
        val id = habits.insert(HabitEntity(name = name, targetPerDay = targetPerDay))
        reminders.insert(ReminderEntity(habitId = id, hour = hour, minute = minute, daysMask = daysMask))
        return id
    }

    /** Добавить отметку «сейчас». Тригерит инвалидацию observeWithToday(). */
    suspend fun addCheckinNow(habitId: Long) {
        checkins.insert(CheckinEntity(habitId = habitId))
    }

    /* ----- Метрики ----- */

    /** Стрик от сегодняшнего дня назад. День засчитан, если событий ≥ targetPerDay. */
    suspend fun calcStreak(habitId: Long, zone: ZoneId, targetPerDay: Int): Int {
        val last = checkins.lastN(habitId)
        if (last.isEmpty()) return 0
        val byDate = last.map { it.atZone(zone).toLocalDate() }.groupingBy { it }.eachCount()
        var streak = 0
        var day = ZonedDateTime.now(zone).toLocalDate()
        repeat(365) {
            val ok = (byDate[day] ?: 0) >= targetPerDay
            if (ok) { streak++; day = day.minusDays(1) } else return streak
        }
        return streak
    }

    /* ----- Вспомогательные ----- */

    private fun todayBounds(zone: ZoneId): Pair<Instant, Instant> {
        val d = ZonedDateTime.now(zone).toLocalDate()
        val from = d.atStartOfDay(zone).toInstant()
        val to = d.plusDays(1).atStartOfDay(zone).toInstant()
        return from to to
    }

    suspend fun allReminders(): List<ReminderEntity> = reminders.all()

    /* ----- Графика ----- */

    fun observeTodayMark(habitId: Long, zone: ZoneId): Flow<DayMark> {
        val (from,to)=todayBounds(zone)
        return checkins.todayRowFlow(habitId, from, to).map { row ->
            when {
                row == null -> DayMark.UNSET
                row.status == 1 -> DayMark.DONE
                row.status == 2 -> DayMark.MISSED
                row.status == 3 -> DayMark.PARTIAL
                else -> DayMark.UNSET
            }
        }
    }

    suspend fun setTodayMark(habitId: Long, zone: ZoneId, mark: DayMark) {
        val (from,to)=todayBounds(zone)
        checkins.deleteBetween(habitId, from, to)
        when (mark) {
            DayMark.UNSET -> Unit
            DayMark.DONE  -> checkins.insert(CheckinEntity(habitId=habitId, status=1, isDone=true))
            DayMark.MISSED-> checkins.insert(CheckinEntity(habitId=habitId, status=2, isDone=false))
            DayMark.PARTIAL-> error("Use setTodayPartial(...)")
        }
    }

    suspend fun setTodayPartial(habitId: Long, zone: ZoneId, value: Double) {
        val (from,to)=todayBounds(zone)
        checkins.deleteBetween(habitId, from, to)
        checkins.insert(CheckinEntity(habitId=habitId, status=3, value=value, isDone=false))
    }

    /* ----- удаление и обновление ----- */

    suspend fun updateHabitBasic(habitId: Long, name: String, targetPerDay: Int) {
        val hw = habits.getWithReminders(habitId) ?: return
        habits.update(hw.habit.copy(name = name, targetPerDay = targetPerDay))
    }

    suspend fun updateReminder(remId: Long, hour: Int, minute: Int, daysMask: Int): ReminderEntity? {
        val cur = reminders.byId(remId) ?: return null
        if (cur.hour == hour && cur.minute == minute && cur.daysMask == daysMask) return cur
        reminders.updateFields(remId, hour, minute, daysMask)
        return cur.copy(hour = hour, minute = minute, daysMask = daysMask)
    }


    suspend fun deleteHabit(habitId: Long) {
        val hw = habits.getWithReminders(habitId) ?: return
        habits.delete(hw.habit) // каскад удалит reminders и checkins
    }


}
