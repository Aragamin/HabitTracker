package com.example.habits.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Stepper(value: Int, onChange: (Int) -> Unit, min: Int = 0, max: Int = 20) {
    val h = 40.dp
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalButton(
            enabled = value > min,
            onClick = { if (value > min) onChange(value - 1) },
            modifier = Modifier.height(h).width(48.dp),
            contentPadding = PaddingValues(0.dp)
        ) { Text("–") }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier.height(h).widthIn(min = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("$value", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.width(12.dp))

        FilledTonalButton(
            enabled = value < max,
            onClick = { if (value < max) onChange(value + 1) },
            modifier = Modifier.height(h).width(48.dp),
            contentPadding = PaddingValues(0.dp)
        ) { Text("+") }
    }
}
