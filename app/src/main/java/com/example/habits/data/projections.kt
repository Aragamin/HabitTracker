package com.example.habits.data

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class HabitWithToday(
    @Embedded val habit: HabitEntity,
    @ColumnInfo(name = "todayCount") val todayCount: Int
)
