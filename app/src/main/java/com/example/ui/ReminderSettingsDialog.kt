package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ReminderSettings
import com.example.model.formatReminderTimeAr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsDialog(
  settings: ReminderSettings,
  hasPermission: Boolean,
  onRequestPermissionAndEnable: (hour: Int, minute: Int) -> Unit,
  onDisable: (hour: Int, minute: Int) -> Unit,
  onSendTestNotification: () -> Unit,
  onDismiss: () -> Unit,
) {
  var isEnabled by remember(settings) { mutableStateOf(settings.enabled) }
  val timePickerState = rememberTimePickerState(
    initialHour = settings.hour,
    initialMinute = settings.minute,
    is24Hour = false,
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Default.NotificationsActive,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "إشعارات التمارين اليومية",
          style = MaterialTheme.typography.titleLarge,
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "تفعيل إشعارات التمارين اليومية",
              style = MaterialTheme.typography.titleSmall,
            )
            Text(
              text = "تنبيه يومي الساعة ${formatReminderTimeAr(timePickerState.hour, timePickerState.minute)}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Switch(
            checked = isEnabled,
            onCheckedChange = { checked ->
              isEnabled = checked
              if (checked) {
                onRequestPermissionAndEnable(timePickerState.hour, timePickerState.minute)
              } else {
                onDisable(timePickerState.hour, timePickerState.minute)
              }
            },
            modifier = Modifier.testTag("reminder_dialog_switch"),
          )
        }

        if (!hasPermission && isEnabled) {
          Text(
            text = "يرجى السماح بإذن الإشعارات ليصلك التنبيه اليومي في موعده.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }

        Text(
          text = "اختر وقت التنبيه اليومي:",
          style = MaterialTheme.typography.labelLarge,
        )

        TimeInput(
          state = timePickerState,
          modifier = Modifier.fillMaxWidth(),
        )

        FilledTonalButton(
          onClick = onSendTestNotification,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("test_notification_button"),
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("إرسال إشعار تجريبي الآن")
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (isEnabled) {
            onRequestPermissionAndEnable(timePickerState.hour, timePickerState.minute)
          } else {
            onDisable(timePickerState.hour, timePickerState.minute)
          }
          onDismiss()
        },
        modifier = Modifier.testTag("save_reminder_button"),
      ) {
        Text("حفظ الوقت")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("إلغاء")
      }
    },
  )
}
