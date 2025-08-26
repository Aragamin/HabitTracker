package com.example.habits.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun PartialValueDialog(
    target: Int,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val v = text.replace(',', '.').toDoubleOrNull()
    val ok = target > 0 && v != null && v >= 0.0 && v <= target.toDouble()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(enabled = ok, onClick = { onConfirm(v!!) }) { Text("Ок") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        title = { Text("Частичное выполнение") },
        text = {
            Column {
                Text("Введите прогресс за сегодня (0..$target).")
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("например 0.5") }
                )
            }
        }
    )
}
