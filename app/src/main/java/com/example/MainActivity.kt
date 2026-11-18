package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WorkoutPreferencesRepository
import com.example.ui.WorkoutScreen
import com.example.ui.WorkoutViewModel
import com.example.ui.theme.WorkoutTrackerTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
    )

    val repository = WorkoutPreferencesRepository(applicationContext)

    setContent {
      WorkoutTrackerTheme {
        val workoutViewModel: WorkoutViewModel = viewModel(
          factory = WorkoutViewModel.Factory(repository)
        )
        WorkoutScreen(viewModel = workoutViewModel)
      }
    }
  }
}
