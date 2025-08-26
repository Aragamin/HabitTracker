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
import com.example.habits.ui.components.StateMark
import com.example.habits.ui.components.SimpleTopBar
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

    // один диалог на экран
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
                val allowPartial = hw.habit.targetPerDay > 0

                val progressText =
                    if (!allowPartial) {
                        "Сегодня: " + when (mark) {
                            DayMark.DONE -> "✓"
                            DayMark.MISSED -> "✕"
                            DayMark.UNSET, DayMark.PARTIAL -> "—"
                        }
                    } else {
                        "Сегодня: ${hw.todayCount} / ${hw.habit.targetPerDay}"
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
                            StateMark(
                                value = mark,
                                allowPartial = allowPartial
                            ) { next ->
                                if (next == DayMark.PARTIAL && allowPartial) {
                                    dialogHabitId = hw.habit.id
                                    dialogTarget = hw.habit.targetPerDay
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

    // диалог PARTIAL
    if (dialogHabitId != null) {
        PartialValueDialog(
            target = dialogTarget,
            onConfirm = { value ->
                val id = dialogHabitId!!
                dialogHabitId = null
                val clamped = value.coerceIn(0.0, dialogTarget.toDouble())
                scope.launch {
                    Graph.repo.setTodayPartial(id, zone, clamped)
                }
            },
            onDismiss = { dialogHabitId = null }
        )
    }
}
