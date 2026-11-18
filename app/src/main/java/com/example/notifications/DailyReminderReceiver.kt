package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.WorkoutPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action
    if (action == Intent.ACTION_BOOT_COMPLETED) {
      val pendingResult = goAsync()
      CoroutineScope(Dispatchers.IO).launch {
        try {
          val repo = WorkoutPreferencesRepository(context.applicationContext)
          val settings = repo.workoutData.first().reminderSettings
          if (settings.enabled && WorkoutNotificationHelper.hasNotificationPermission(context)) {
            WorkoutNotificationHelper.scheduleDailyReminder(
              context = context.applicationContext,
              hour = settings.hour,
              minute = settings.minute,
            )
          }
        } finally {
          pendingResult.finish()
        }
      }
    } else {
      WorkoutNotificationHelper.showMotivationalNotification(context.applicationContext)
    }
  }
}
