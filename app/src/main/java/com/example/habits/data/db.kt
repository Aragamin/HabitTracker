package com.example.habits.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.Instant

class Converters {
    @TypeConverter fun fromInstant(i: Instant?): Long? = i?.toEpochMilli()
    @TypeConverter fun toInstant(v: Long?): Instant? = v?.let { Instant.ofEpochMilli(it) }
}

@Database(
    entities = [HabitEntity::class, ReminderEntity::class, CheckinEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitsDao(): HabitsDao
    abstract fun remindersDao(): RemindersDao
    abstract fun checkinsDao(): CheckinsDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE checkins ADD COLUMN isDone INTEGER NOT NULL DEFAULT 1")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE checkins ADD COLUMN value REAL")
                db.execSQL("ALTER TABLE checkins ADD COLUMN status INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE checkins SET status = CASE WHEN isDone=1 THEN 1 ELSE 2 END")
            }
        }
        fun build(ctx: Context): AppDatabase =
            Room.databaseBuilder(ctx, AppDatabase::class.java, "habits.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}
