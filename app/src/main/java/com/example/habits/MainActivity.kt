package com.example.habits

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.core.content.ContextCompat
import android.widget.Toast
import android.content.pm.PackageManager
import com.example.habits.reminders.ExactAlarms
import com.example.habits.ui.HabitEditScreen
import com.example.habits.ui.HabitListScreen
import com.example.habits.ui.SettingsScreen
import com.example.habits.ui.theme.HabitTheme


class MainActivity : ComponentActivity() {

    private val askNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // POST_NOTIFICATIONS только на 33+, и только если ещё не выдано
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Для напоминаний нужны точные будильники на 31+
        if (Build.VERSION.SDK_INT >= 31 && !ExactAlarms.hasPermission(this)) {
            Toast.makeText(this, "Разрешите точные будильники для напоминаний", Toast.LENGTH_LONG).show()
            ExactAlarms.request(this) // откроет системный экран
        }

        setContent {
            HabitTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = "list") {
                        composable("list") {
                            HabitListScreen(
                                onAdd = { nav.navigate("edit") },
                                onSettings = { nav.navigate("settings") },
                                onSettingsHabit = { id -> nav.navigate("edit/$id") }
                            )
                        }
                        composable("edit") {
                            HabitEditScreen(onDone = { nav.popBackStack() })
                        }
                        composable(
                            route = "edit/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getLong("id")
                            HabitEditScreen(onDone = { nav.popBackStack() }, habitId = id)
                        }
                        composable("settings") {
                            SettingsScreen(onBack = { nav.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
