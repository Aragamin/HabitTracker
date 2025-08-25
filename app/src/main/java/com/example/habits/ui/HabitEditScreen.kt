@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)


package com.example.habits.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.example.habits.Graph
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.Locale
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement


@Composable
fun HabitEditScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val repo = Graph.repo
    val scheduler = Graph.scheduler
    val scope = rememberCoroutineScope()

    // State
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var target by rememberSaveable { mutableStateOf(1) }
    var hour by rememberSaveable { mutableStateOf(9) }
    var minute by rememberSaveable { mutableStateOf(0) }
    var days by rememberSaveable { mutableStateOf(defaultWeekdays()) }

    val canSave = name.text.isNotBlank() && days.isNotEmpty()

    // AppBar scroll behavior
    val scrollBehavior = pinnedScrollBehavior()

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    val showTimePicker = {
        TimePickerDialog(ctx, { _, h, m -> hour = h; minute = m }, hour, minute, true).show()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Новая привычка") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(enabled = canSave, onClick = {
                        scope.launch {
                            val id = repo.newHabit(name.text.trim(), target, hour, minute, daysMask(days))
                            val rem = repo.getHabitWithReminders(id)!!.reminders.first()
                            scheduler.schedule(rem)
                            snackbarHostState.showSnackbar("Сохранено")
                            onDone()
                        }
                    }) {
                        Icon(Icons.Rounded.Done, contentDescription = "Сохранить")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { p ->
        Column(
            Modifier
                .padding(p)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название привычки") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Цель в день
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Цель/день", style = MaterialTheme.typography.labelLarge)
                Stepper(
                    value = target,
                    onChange = { v -> if (v in 1..20) target = v }
                )
            }

            // Время
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Время", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(onClick = showTimePicker) {
                    Text(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
                }
            }

            // Дни недели
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Дни недели", style = MaterialTheme.typography.labelLarge)
                DaysSelector(days = days, onToggle = { d ->
                    days = if (days.contains(d)) days - d else days + d
                })
                if (days.isEmpty()) {
                    AssistiveText("Выбери хотя бы один день")
                }
            }

            // Подсказки
            AssistiveText("Сохранение доступно при заполненном названии и выбранных днях.")
        }
    }
}

/* ---------- Вспомогательные компоненты ---------- */

@Composable
private fun Stepper(value: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalButton(enabled = value > 1, onClick = { onChange(value - 1) }) { Text("–") }
        Spacer(Modifier.width(12.dp))
        Text("$value", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(12.dp))
        FilledTonalButton(onClick = { onChange(value + 1) }) { Text("+") }
    }
}

@Composable
private fun DaysSelector(days: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    val order = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        order.forEach { d ->
            FilterChip(
                selected = days.contains(d),
                onClick = { onToggle(d) },
                label = { Text(short(d)) }
            )
        }
    }
}


@Composable
private fun AssistiveText(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/* ---------- Утилиты ---------- */

private fun defaultWeekdays() = setOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
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

private fun daysMask(set: Set<DayOfWeek>): Int {
    var mask = 0
    set.forEach { d -> mask = mask or (1 shl ((d.value + 6) % 7)) } // Mon->0
    return mask
}
