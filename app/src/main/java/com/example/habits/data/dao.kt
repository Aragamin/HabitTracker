package com.example.habits.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface HabitsDao {
    @Query("SELECT * FROM habits WHERE active = 1 ORDER BY id DESC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getWithReminders(id: Long): HabitWithReminder?

    @Insert suspend fun insert(h: HabitEntity): Long
    @Update suspend fun update(h: HabitEntity)
    @Delete suspend fun delete(h: HabitEntity)
}

@Dao
interface RemindersDao {
    @Query("SELECT * FROM reminders WHERE habitId = :habitId")
    suspend fun byHabit(habitId: Long): List<ReminderEntity>

    @Insert suspend fun insert(r: ReminderEntity): Long
    @Delete suspend fun delete(r: ReminderEntity)
}

@Dao
interface CheckinsDao {
    @Insert suspend fun insert(c: CheckinEntity): Long

    @Query("SELECT COUNT(*) FROM checkins WHERE habitId = :habitId AND ts >= :from AND ts < :to")
    suspend fun countBetween(habitId: Long, from: Instant, to: Instant): Int

    @Query("SELECT ts FROM checkins WHERE habitId = :habitId ORDER BY ts DESC LIMIT 200")
    suspend fun lastN(habitId: Long): List<Instant>
}
