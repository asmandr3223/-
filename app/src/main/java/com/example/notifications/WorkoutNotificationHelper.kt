package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

object WorkoutNotificationHelper {
  const val CHANNEL_ID = "daily_workout_reminder_channel"
  const val NOTIFICATION_ID = 1001
  private const val REQUEST_CODE_ALARM = 2001
  const val ACTION_DAILY_REMINDER = "com.example.ACTION_DAILY_WORKOUT_REMINDER"

  const val REMINDER_TITLE = "وقت النشاط يا مهند!"
  const val REMINDER_BODY =
    "وقت النشاط يا مهند! حافظ على جدولك اليومي وحقق أهدافك الرياضية اليوم"

  fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "تذكيرات التمارين اليومية",
        NotificationManager.IMPORTANCE_HIGH,
      ).apply {
        description = "تنبيه يومي للمحافظة على جدول التمارين الرياضية"
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun showMotivationalNotification(context: Context) {
    if (!hasNotificationPermission(context)) return
    ensureChannel(context)

    val openAppIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      0,
      openAppIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_notification_fitness)
      .setContentTitle(REMINDER_TITLE)
      .setContentText(REMINDER_BODY)
      .setStyle(NotificationCompat.BigTextStyle().bigText(REMINDER_BODY))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(contentPendingIntent)
      .build()

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
  }

  fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
    ensureChannel(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent = buildReminderPendingIntent(context)

    val calendar = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
      if (timeInMillis <= System.currentTimeMillis()) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    alarmManager.setInexactRepeating(
      AlarmManager.RTC_WAKEUP,
      calendar.timeInMillis,
      AlarmManager.INTERVAL_DAY,
      pendingIntent,
    )
  }

  fun cancelDailyReminder(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(buildReminderPendingIntent(context))
  }

  private fun buildReminderPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, DailyReminderReceiver::class.java).apply {
      action = ACTION_DAILY_REMINDER
    }
    return PendingIntent.getBroadcast(
      context,
      REQUEST_CODE_ALARM,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }
}
