package com.example.habits.data

import android.content.Context
import androidx.room.*
import java.time.Instant

class Converters {
    @TypeConverter fun fromInstant(i: Instant?): Long? = i?.toEpochMilli()
    @TypeConverter fun toInstant(v: Long?): Instant? = v?.let { Instant.ofEpochMilli(it) }
}

@Database(
    entities = [HabitEntity::class, ReminderEntity::class, CheckinEntity::class],
    version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitsDao(): HabitsDao
    abstract fun checkinsDao(): CheckinsDao
    abstract fun remindersDao(): RemindersDao

    companion object {
        fun build(ctx: Context) = Room.databaseBuilder(ctx, AppDatabase::class.java, "habits.db").build()
    }
}
