package com.example.habits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.habits.data.DayMark

@Composable
fun TriStateMark(value: DayMark, onChange: (DayMark) -> Unit) {
    val next = when (value) {
        DayMark.UNSET -> DayMark.DONE
        DayMark.DONE  -> DayMark.MISSED
        DayMark.MISSED-> DayMark.UNSET
    }
    val (symbol, tint, fillAlpha) = when (value) {
        DayMark.UNSET -> Triple("✕", MaterialTheme.colorScheme.outline, 0f)
        DayMark.DONE  -> Triple("✓", Color(0xFF2E7D32), 0.15f) // тёмно-зелёный
        DayMark.MISSED-> Triple("✕", Color(0xFFD32F2F), 0.15f) // красный
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(tint.copy(alpha = fillAlpha), CircleShape)
            .border(2.dp, tint, CircleShape)
            .clickable { onChange(next) },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = tint, style = MaterialTheme.typography.titleMedium)
    }
}
