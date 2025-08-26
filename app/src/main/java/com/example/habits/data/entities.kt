package com.example.habits.data

import androidx.room.*
import java.time.Instant

@Entity(
    tableName = "habits"
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetPerDay: Int = 1,
    val active: Boolean = true,
    val createdAt: Instant = Instant.now()
)

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("habitId")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val hour: Int,
    val minute: Int,
    /** Биты Пн..Вс: 1<<0..1<<6 */
    val daysMask: Int
)

@Entity(
    tableName = "checkins",
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("habitId"), Index("ts")]
)
data class CheckinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val ts: Instant = Instant.now(),
    val isDone: Boolean = true,      // legacy, не используем в логике подсчёта
    val value: Double? = null,       // прогресс (для PARTIAL), единицы = "в день"
    val status: Int = 1              // 1=DONE, 2=MISSED, 3=PARTIAL
)

/** Habit + reminders для планировщика и деталей. */
data class HabitWithReminder(
    @Embedded val habit: HabitEntity,
    @Relation(parentColumn = "id", entityColumn = "habitId")
    val reminders: List<ReminderEntity>
)
