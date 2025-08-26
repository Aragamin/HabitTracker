package com.example.habits.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habits.Graph
import com.example.habits.data.DayMark
import com.example.habits.ui.components.SimpleTopBar
import com.example.habits.ui.components.TriStateMark
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

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Привычки",
                actions = { TextButton(onClick = onSettings) { Text("Настройки") } }
            )
        },
        floatingActionButton = { FloatingActionButton(onClick = onAdd) { Text("+") } }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(items, key = { it.habit.id }) { hw ->
                HabitItem(
                    name = hw.habit.name,
                    progressLabel = "${hw.todayCount} / ${hw.habit.targetPerDay}",
                    mark = repo.observeTodayMark(hw.habit.id, zone)
                        .collectAsState(initial = DayMark.UNSET).value,
                    onToggle = { new ->
                        scope.launch { repo.setTodayMark(hw.habit.id, zone, new) }
                    }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HabitItem(
    name: String,
    progressLabel: String,
    mark: DayMark,
    onToggle: (DayMark) -> Unit
) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Сегодня: $progressLabel")
                TriStateMark(value = mark, onChange = onToggle)
            }
        }
    }
}
