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
fun StateMark(
    value: DayMark,
    allowPartial: Boolean,
    onChange: (DayMark) -> Unit
) {
    val next = nextState(value, allowPartial)
    val (symbol, tint, fillAlpha) = when (value) {
        DayMark.UNSET   -> Triple("✕", MaterialTheme.colorScheme.outline, 0f)            // серый
        DayMark.DONE    -> Triple("✓", Color(0xFF2E7D32), 0.15f)                 // зелёный
        DayMark.MISSED  -> Triple("✕", Color(0xFFD32F2F), 0.15f)                 // красный
        DayMark.PARTIAL -> Triple("~", Color(0xFFF9A825), 0.15f)                 // жёлтый
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

private fun nextState(cur: DayMark, allowPartial: Boolean): DayMark =
    if (!allowPartial) {
        when (cur) {
            DayMark.UNSET -> DayMark.DONE
            DayMark.DONE  -> DayMark.MISSED
            else          -> DayMark.UNSET
        }
    } else {
        when (cur) {
            DayMark.UNSET -> DayMark.DONE
            DayMark.DONE  -> DayMark.MISSED
            DayMark.MISSED-> DayMark.PARTIAL
            DayMark.PARTIAL-> DayMark.UNSET
        }
    }
