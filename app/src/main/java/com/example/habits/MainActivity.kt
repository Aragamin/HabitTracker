package com.example.habits

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.habits.ui.HabitEditScreen
import com.example.habits.ui.HabitListScreen
import com.example.habits.ui.theme.HabitTheme

class MainActivity : ComponentActivity() {
    private val askNotif = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) askNotif.launch(Manifest.permission.POST_NOTIFICATIONS)

        setContent {
            HabitTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = "list") {
                        composable("list") { HabitListScreen(onAdd = { nav.navigate("edit") }) }
                        composable("edit") { HabitEditScreen(onDone = { nav.popBackStack() }) }
                    }
                }
            }
        }
    }
}
