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
import com.example.habits.ui.components.daysMask
import com.example.habits.ui.components.defaultWeekdays
import java.time.DayOfWeek
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun HabitEditScreen(onDone: () -> Unit) {
    val ctx = LocalContext.current
    val repo = Graph.repo
    val scheduler = Graph.scheduler
    val scope = rememberCoroutineScope()

    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var target by rememberSaveable { mutableStateOf(1) }
    var hour by rememberSaveable { mutableStateOf(9) }
    var minute by rememberSaveable { mutableStateOf(0) }
    var days by rememberSaveable { mutableStateOf(defaultWeekdays()) }

    val canSave = name.text.isNotBlank() && days.isNotEmpty()
    val snackbarHost = remember { SnackbarHostState() }
    val showTimePicker = { TimePickerDialog(ctx, { _, h, m -> hour = h; minute = m }, hour, minute, true).show() }

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Новая привычка",
                navText = "Назад",
                onNavClick = onDone,
                actions = {
                    TextButton(enabled = canSave, onClick = {
                        scope.launch {
                            val id = repo.newHabit(name.text.trim(), target, hour, minute, daysMask(days))
                            val rem = repo.getHabitWithReminders(id)!!.reminders.first()
                            scheduler.schedule(rem)
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
                DaysSelector(days = days, onToggle = { d: DayOfWeek ->
                    days = if (days.contains(d)) days - d else days + d
                })
                if (days.isEmpty()) {
                    Text("Выбери хотя бы один день", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text("Сохранение доступно при заполненном названии и выбранных днях.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
