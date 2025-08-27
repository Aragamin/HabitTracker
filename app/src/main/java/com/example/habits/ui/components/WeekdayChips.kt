package com.example.habits.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek

@Composable
fun DaysSelector(days: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    val order = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )
    val firstRow = order.take(4)
    val secondRow = order.drop(4)

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        firstRow.forEach { d ->
            FilterChip(selected = days.contains(d), onClick = { onToggle(d) }, label = { Text(short(d)) })
        }
    }
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        secondRow.forEach { d ->
            FilterChip(selected = days.contains(d), onClick = { onToggle(d) }, label = { Text(short(d)) })
        }
    }
}

fun daysMask(set: Set<DayOfWeek>): Int {
    var mask = 0
    set.forEach { d -> mask = mask or (1 shl ((d.value + 6) % 7)) } // Mon->0 .. Sun->6
    return mask
}

fun defaultWeekdays() = setOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
)

private fun short(d: DayOfWeek) = when (d) {
    DayOfWeek.MONDAY -> "Пн"
    DayOfWeek.TUESDAY -> "Вт"
    DayOfWeek.WEDNESDAY -> "Ср"
    DayOfWeek.THURSDAY -> "Чт"
    DayOfWeek.FRIDAY -> "Пт"
    DayOfWeek.SATURDAY -> "Сб"
    DayOfWeek.SUNDAY -> "Вс"
}
