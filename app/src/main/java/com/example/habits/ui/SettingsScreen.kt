package com.example.habits.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.habits.ui.components.SimpleTopBar

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    Scaffold(
        topBar = { SimpleTopBar(title = "Настройки", showBack = true, onBack = onBack) }
    ) { p ->
        Column(Modifier.padding(p).padding(16.dp)) {
            Text("Системные настройки", style = MaterialTheme.typography.titleMedium)

            Button(onClick = {
                val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                ctx.startActivity(i)
            }) { Text("Уведомления приложения") }

            if (Build.VERSION.SDK_INT >= 31) {
                Button(onClick = {
                    ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }) { Text("Точные будильники") }
            }
        }
    }
}
