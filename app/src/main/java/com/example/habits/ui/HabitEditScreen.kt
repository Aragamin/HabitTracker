package com.example.habits.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.habits.Graph
import com.example.habits.ui.components.SimpleTopBar
import com.example.habits.ui.components.Stepper
import com.example.habits.ui.components.DaysSelector
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.Locale

@Composable
fun HabitEditScreen(onDone: () -> Unit, habitId: Long? = null) {
    val ctx = LocalContext.current
    val repo = Graph.repo
    val scheduler = Graph.scheduler
    val scope = rememberCoroutineScope()

    val isEdit = habitId != null

    // Форма
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var target by rememberSaveable { mutableStateOf(1) }
    var hour by rememberSaveable { mutableStateOf(9) }
    var minute by rememberSaveable { mutableStateOf(0) }
    var days by rememberSaveable { mutableStateOf(defaultWeekdays()) }

    // Загруженные сущности для редактирования
    var reminderId by rememberSaveable { mutableStateOf<Long?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) {
        if (isEdit) {
            val hw = repo.getHabitWithReminders(habitId!!)
            if (hw != null) {
                name = TextFieldValue(hw.habit.name)
                target = hw.habit.targetPerDay
                val r = hw.reminders.firstOrNull()
                if (r != null) {
                    reminderId = r.id
                    hour = r.hour
                    minute = r.minute
                    // восстановим days из маски
                    val set = mutableSetOf<DayOfWeek>()
                    for (i in 0..6) if ((r.daysMask and (1 shl i)) != 0) {
                        set += DayOfWeek.of(((i + 1) % 7).let { if (it == 0) 7 else it })
                    }
                    days = set
                }
            }
        }
        loaded = true
    }

    val canSave = name.text.isNotBlank() && days.isNotEmpty()
    val snackbarHost = remember { SnackbarHostState() }
    val showTimePicker = { TimePickerDialog(ctx, { _, h, m -> hour = h; minute = m }, hour, minute, true).show() }

    // Диалог удаления
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = if (isEdit) "Редактировать привычку" else "Новая привычка",
                showBack = true,
                onBack = onDone,
                actions = {
                    if (isEdit) {
                        TextButton(onClick = { confirmDelete = true }) { Text("Удалить") }
                    }
                    TextButton(enabled = canSave && loaded, onClick = {
                        scope.launch {
                            if (!isEdit) {
                                val id = repo.newHabit(name.text.trim(), target, hour, minute, daysMask(days))
                                val rem = repo.getHabitWithReminders(id)!!.reminders.first()
                                scheduler.schedule(rem)
                            } else {
                                val id = habitId!!
                                // обновим хабит
                                val hw = repo.getHabitWithReminders(id) ?: return@launch
                                repo.run {
                                    // обновление полей привычки
                                    Graph.db.habitsDao().update(hw.habit.copy(name = name.text.trim(), targetPerDay = target))
                                    // обновление напоминания
                                    val rid = reminderId ?: hw.reminders.firstOrNull()?.id
                                    if (rid != null) {
                                        val upd = Graph.repo.updateReminder(rid, hour, minute, daysMask(days))
                                        if (upd != null) {
                                            // можно без cancel: тот же requestCode перезапишет будильник
                                            Graph.scheduler.schedule(upd)
                                        }
                                    }
                                }
                                // re-schedule всех напоминаний этой привычки
                                scheduler.scheduleForHabit(id)
                            }
                            snackbarHost.showSnackbar("Сохранено")
                            onDone()
                        }
                    }) { Text("Сохранить") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
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
                value = name, onValueChange = { name = it },
                label = { Text("Название привычки") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Цель/день", style = MaterialTheme.typography.labelLarge)
                // допускаем 0
                Stepper(value = target, onChange = { target = it }, min = 0)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Время", style = MaterialTheme.typography.labelLarge)
                OutlinedButton(onClick = showTimePicker) {
                    Text(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Дни недели", style = MaterialTheme.typography.labelLarge)
                DaysSelector(days = days, onToggle = { d ->
                    days = if (days.contains(d)) days - d else days + d
                })
                if (days.isEmpty()) {
                    Text("Выбери хотя бы один день", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (confirmDelete && isEdit) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    scope.launch {
                        val id = habitId!!
                        // отменим будильники для всех напоминаний этой привычки
                        val hw = repo.getHabitWithReminders(id)
                        hw?.reminders?.forEach { r -> Graph.scheduler.cancel(r.id, id) }
                        // удалим привычку (каскад очистит checkins/reminders)
                        repo.deleteHabit(id)
                        snackbarHost.showSnackbar("Удалено")
                        onDone()
                    }
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Отмена") } },
            title = { Text("Удалить привычку?") },
            text = { Text("Это действие нельзя отменить.") }
        )
    }
}

/* --- Локальные утилиты: mask и дефолтные будни --- */

// map: Mon→0 … Sun→6
private fun daysMask(set: Set<DayOfWeek>): Int {
    var mask = 0
    set.forEach { d -> mask = mask or (1 shl ((d.value + 6) % 7)) }
    return mask
}

private fun defaultWeekdays(): Set<DayOfWeek> = setOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
)
