package com.example.habits.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Stepper(value: Int, onChange: (Int) -> Unit, min: Int = 1, max: Int = 20) {
    Row {
        FilledTonalButton(enabled = value > min, onClick = { onChange((value - 1).coerceAtLeast(min)) }) { Text("–") }
        Spacer(Modifier.width(12.dp))
        Text("$value", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(12.dp))
        FilledTonalButton(enabled = value < max, onClick = { onChange((value + 1).coerceAtMost(max)) }) { Text("+") }
    }
}
