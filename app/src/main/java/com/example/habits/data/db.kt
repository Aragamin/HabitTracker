// app/src/main/java/com/example/habits/data/db.kt
package com.example.habits.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.Instant

class Converters {
    @TypeConverter fun fromInstant(i: Instant?): Long? = i?.toEpochMilli()
    @TypeConverter fun toInstant(v: Long?): Instant? = v?.let { Instant.ofEpochMilli(it) }
}

@Database(
    entities = [HabitEntity::class, ReminderEntity::class, CheckinEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitsDao(): HabitsDao
    abstract fun remindersDao(): RemindersDao
    abstract fun checkinsDao(): CheckinsDao

    companion object {
        // Миграция 1→2: добавляем isDone (0/1) в checkins, по умолчанию = 1 (сделано)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE checkins ADD COLUMN isDone INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        fun build(ctx: Context): AppDatabase =
            Room.databaseBuilder(ctx, AppDatabase::class.java, "habits.db")
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
