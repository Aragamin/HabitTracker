package com.example.habits.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habits.Graph
import com.example.habits.data.HabitEntity
import kotlinx.coroutines.launch
import java.time.ZoneId

@Composable
fun HabitListScreen(onAdd: () -> Unit) {
    val repo = Graph.repo
    val habits by repo.observeHabits().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val zone = remember { ZoneId.systemDefault() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Text("+") }
        }
    ) { p ->
        LazyColumn(Modifier.padding(p).padding(16.dp)) {
            items(habits) { h ->
                HabitItem(h = h,
                    onCheck = {
                        scope.launch { repo.addCheckinNow(h.id) }
                    },
                    progressLabel = produceState(initialValue = "…", h) {
                        val today = repo.todayCount(h.id, zone)
                        value = "$today / ${h.targetPerDay}"
                    }.value
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HabitItem(h: HabitEntity, onCheck: () -> Unit, progressLabel: String) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(h.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Сегодня: $progressLabel")
                Button(onClick = onCheck) { Text("Отметить") }
            }
        }
    }
}
