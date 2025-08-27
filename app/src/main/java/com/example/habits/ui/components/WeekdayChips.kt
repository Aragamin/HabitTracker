package com.example.habits.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek

@Composable
fun DaysSelector(days: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    val week = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    )
    val weekend = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        week.forEach { d ->
            DayChip(d = d, selected = days.contains(d)) { onToggle(d) }
        }
    }
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        weekend.forEach { d ->
            DayChip(d = d, selected = days.contains(d)) { onToggle(d) }
        }
    }
}

@Composable
private fun DayChip(d: DayOfWeek, selected: Boolean, onClick: () -> Unit) {
    val colors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,      // тёмно-«синий» из темы
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(short(d)) },
        colors = colors
    )
}

private fun short(d: DayOfWeek) = when (d) {
    DayOfWeek.MONDAY -> "Пн"
    DayOfWeek.TUESDAY -> "Вт"
    DayOfWeek.WEDNESDAY -> "Ср"
    DayOfWeek.THURSDAY -> "Чт"
    DayOfWeek.FRIDAY -> "Пт"
    DayOfWeek.SATURDAY -> "Сб"
    DayOfWeek.SUNDAY -> "Вс"
}
