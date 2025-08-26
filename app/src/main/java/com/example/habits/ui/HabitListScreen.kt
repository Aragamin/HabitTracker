package com.example.habits.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habits.Graph
import com.example.habits.data.DayMark
import com.example.habits.ui.components.PartialValueDialog
import com.example.habits.ui.components.SimpleTopBar
import com.example.habits.ui.components.StateMark
import kotlinx.coroutines.launch
import java.time.ZoneId

@Composable
fun HabitListScreen(
    onAdd: () -> Unit,
    onSettings: () -> Unit
) {
    val repo = Graph.repo
    val scope = rememberCoroutineScope()
    val zone = ZoneId.systemDefault()

    val items by repo.observeHabitsWithToday(zone).collectAsState(initial = emptyList())

    var dialogHabitId by remember { mutableStateOf<Long?>(null) }
    var dialogTarget by remember { mutableStateOf(0) }

    Scaffold(
        topBar = { SimpleTopBar(title = "Привычки", actions = { TextButton(onClick = onSettings) { Text("Настройки") } }) },
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Text("+") } }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(items, key = { it.habit.id }) { hw ->
                val mark by repo.observeTodayMark(hw.habit.id, zone).collectAsState(initial = DayMark.UNSET)

                val target = hw.habit.targetPerDay
                val showNumbers = target >= 2
                val progressText =
                    if (showNumbers) "Сегодня: ${hw.todayCount} / $target"
                    else "Сегодня: " + when (mark) {
                        DayMark.DONE -> "✓"
                        DayMark.MISSED -> "✕"
                        DayMark.PARTIAL -> "~"
                        DayMark.UNSET -> "—"
                    }

                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(hw.habit.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(progressText)
                            // 4 состояния всегда доступны, даже при target 0/1
                            StateMark(
                                value = mark,
                                allowPartial = true
                            ) { next ->
                                if (next == DayMark.PARTIAL) {
                                    if (target >= 2) {
                                        dialogHabitId = hw.habit.id
                                        dialogTarget = target
                                    } else {
                                        // для 0/1 — PARTIAL без числа
                                        scope.launch { repo.setTodayPartial(hw.habit.id, zone, 0.0) }
                                    }
                                } else {
                                    scope.launch { repo.setTodayMark(hw.habit.id, zone, next) }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // Диалог PARTIAL только если target >= 2
    if (dialogHabitId != null) {
        val id = dialogHabitId!!
        val target = dialogTarget
        PartialValueDialog(
            target = target,
            onConfirm = { value ->
                dialogHabitId = null
                val v = value.coerceIn(0.0, target.toDouble())
                // если частичное == цели → DONE
                if (v >= target.toDouble()) {
                    scope.launch { Graph.repo.setTodayMark(id, zone, DayMark.DONE) }
                } else {
                    scope.launch { Graph.repo.setTodayPartial(id, zone, v) }
                }
            },
            onDismiss = { dialogHabitId = null }
        )
    }
}
