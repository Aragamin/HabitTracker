package com.example.habits.data

import androidx.room.*
import java.time.Instant

@Entity(tableName = "habits")
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
    // legacy поле оставляем для миграции, логику считаем по status/value
    val isDone: Boolean = true,
    // частичный прогресс
    val value: Double? = null,
    // 1=DONE, 2=MISSED, 3=PARTIAL
    val status: Int = 1
)

data class HabitWithReminder(
    @Embedded val habit: HabitEntity,
    @Relation(parentColumn = "id", entityColumn = "habitId")
    val reminders: List<ReminderEntity>
)
