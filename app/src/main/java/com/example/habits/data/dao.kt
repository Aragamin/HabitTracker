package com.example.habits.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/* ---------- Проекции ---------- */

/** Habit + вычисленный todayCount для списка. */
/*data class HabitWithToday(
    @Embedded val habit: HabitEntity,
    @ColumnInfo(name = "todayCount") val todayCount: Int
)*/

/* ---------- DAO ---------- */

@Dao
interface HabitsDao {
    @Query("SELECT * FROM habits WHERE active = 1 ORDER BY id DESC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Transaction
    @Query("""
        SELECT h.*,
        CASE
          WHEN h.targetPerDay = 0 THEN         -- бинарная привычка
            CASE WHEN EXISTS(
              SELECT 1 FROM checkins c
              WHERE c.habitId=h.id AND c.ts>=:from AND c.ts<:to AND c.status=1
            ) THEN 1 ELSE 0 END
          ELSE                                  -- количественная
            CASE
              WHEN EXISTS(
                SELECT 1 FROM checkins c
                WHERE c.habitId=h.id AND c.ts>=:from AND c.ts<:to AND c.status=1
              ) THEN h.targetPerDay             -- DONE закрывает весь объём
              ELSE (
                SELECT CAST(
                  MIN( h.targetPerDay,
                       COALESCE(SUM(CASE WHEN c.status=3 THEN COALESCE(c.value,0) ELSE 0 END),0)
                  ) AS INTEGER
                )
                FROM checkins c
                WHERE c.habitId=h.id AND c.ts>=:from AND c.ts<:to
              )
            END
        END AS todayCount
        FROM habits h
        WHERE h.active=1
        ORDER BY h.id DESC
    """)
    fun observeWithToday(from: Instant, to: Instant): Flow<List<HabitWithToday>>


    @Transaction
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getWithReminders(id: Long): HabitWithReminder?

    @Insert suspend fun insert(h: HabitEntity): Long
    @Update suspend fun update(h: HabitEntity)
    @Delete suspend fun delete(h: HabitEntity)
}

@Dao
interface RemindersDao {
    @Query("SELECT * FROM reminders WHERE habitId = :habitId ORDER BY id ASC")
    suspend fun byHabit(habitId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun byId(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders")
    suspend fun all(): List<ReminderEntity>

    @Insert suspend fun insert(r: ReminderEntity): Long
    @Update suspend fun update(r: ReminderEntity)

    @Query("UPDATE reminders SET hour = :hour, minute = :minute, daysMask = :daysMask WHERE id = :id")
    suspend fun updateFields(id: Long, hour: Int, minute: Int, daysMask: Int): Int

    @Delete suspend fun delete(r: ReminderEntity)
}


@Dao
interface CheckinsDao {
    @Insert suspend fun insert(c: CheckinEntity): Long

    // удалить все записи за сегодня
    @Query("DELETE FROM checkins WHERE habitId=:habitId AND ts>=:from AND ts<:to")
    suspend fun deleteBetween(habitId: Long, from: Instant, to: Instant): Int

    // поток: первая запись за сегодня (или null)
    @Query("SELECT * FROM checkins WHERE habitId=:habitId AND ts>=:from AND ts<:to LIMIT 1")
    fun todayRowFlow(habitId: Long, from: Instant, to: Instant): Flow<CheckinEntity?>

    @Query("SELECT ts FROM checkins WHERE habitId = :habitId ORDER BY ts DESC LIMIT 200")
    suspend fun lastN(habitId: Long): List<Instant>

    @Query("SELECT COUNT(*) FROM checkins WHERE habitId = :habitId AND ts >= :from AND ts < :to")
    suspend fun countBetween(habitId: Long, from: Instant, to: Instant): Int
}
