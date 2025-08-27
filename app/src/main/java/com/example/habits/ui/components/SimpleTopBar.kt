package com.example.habits.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.habits.R

@Composable
fun SimpleTopBar(
    title: String,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack && onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_back), contentDescription = "Назад")
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Row(content = actions)
        }
    }
}
