package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.ExerciseItem
import com.example.model.ExerciseUnitType
import com.example.model.ReminderSettings
import com.example.model.WorkoutPhase
import com.example.model.WorkoutPlan
import com.example.model.formatReminderTimeAr
import com.example.model.formatStatNumber
import com.example.model.setKey
import com.example.notifications.WorkoutNotificationHelper
import com.example.ui.theme.AthleticOnSurfaceVariant
import com.example.ui.theme.CaloriesGlowShadow
import com.example.ui.theme.CaloriesGradientEnd
import com.example.ui.theme.CaloriesGradientStart
import com.example.ui.theme.CaloriesNeonNumber
import com.example.ui.theme.CompletedCardDark
import com.example.ui.theme.DarkButtonBorder
import com.example.ui.theme.DarkButtonContainer
import com.example.ui.theme.MinutesGradientEnd
import com.example.ui.theme.MinutesGradientStart
import com.example.ui.theme.MinutesNeonNumber
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.ProgressGradientEnd
import com.example.ui.theme.ProgressGradientStart
import com.example.ui.theme.ProgressNeonNumber
import com.example.ui.theme.SoftCyanText
import com.example.ui.theme.SoftOrangeText
import kotlinx.coroutines.launch

@Composable
fun WorkoutScreen(
  viewModel: WorkoutViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  var selectedTabIndex by remember { mutableIntStateOf(0) }
  var isEditorOpen by remember { mutableStateOf(false) }
  var editingPlan by remember { mutableStateOf<WorkoutPlan?>(null) }
  var planToDelete by remember { mutableStateOf<WorkoutPlan?>(null) }
  var isReminderDialogOpen by remember { mutableStateOf(false) }
  var pendingReminderTime by remember { mutableStateOf<Pair<Int, Int>?>(null) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { granted ->
      val (h, m) = pendingReminderTime
        ?: (uiState.reminderSettings.hour to uiState.reminderSettings.minute)
      pendingReminderTime = null
      if (granted) {
        viewModel.setDailyReminder(context, enabled = true, hour = h, minute = m)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("تم تفعيل إشعارات التمارين اليومية بنجاح")
        }
      } else {
        viewModel.setDailyReminder(context, enabled = false, hour = h, minute = m)
        coroutineScope.launch {
          snackbarHostState.showSnackbar("يرجى السماح بالإشعارات من إعدادات الجهاز لتفعيل التنبيه اليومي")
        }
      }
    },
  )

  val enableReminderWithPermission: (Int, Int) -> Unit = { hour, minute ->
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      !WorkoutNotificationHelper.hasNotificationPermission(context)
    ) {
      pendingReminderTime = hour to minute
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    } else {
      viewModel.setDailyReminder(context, enabled = true, hour = hour, minute = minute)
    }
  }

  val resetMessage = stringResource(R.string.reset_snackbar_message)
  val undoLabel = stringResource(R.string.undo_action)

  val onResetClick: () -> Unit = {
    viewModel.resetActivePlanDay { hadItems ->
      if (hadItems) {
        coroutineScope.launch {
          snackbarHostState.currentSnackbarData?.dismiss()
          val result = snackbarHostState.showSnackbar(
            message = resetMessage,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short,
          )
          if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoReset()
          }
        }
      }
    }
  }

  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      containerColor = MaterialTheme.colorScheme.background,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
      },
      topBar = {
        if (selectedTabIndex == 0) {
          LiveDashboardTopBar(
            uiState = uiState,
            onSelectPlan = viewModel::selectPlan,
            onCreateNewPlan = {
              editingPlan = null
              isEditorOpen = true
            },
            onEditActivePlan = {
              editingPlan = uiState.activePlan
              isEditorOpen = true
            },
            onRequestDeleteActivePlan = {
              planToDelete = uiState.activePlan
            },
            onOpenReminderSettings = { isReminderDialogOpen = true },
            onResetClick = onResetClick,
          )
        } else {
          ReportsTopBar()
        }
      },
      bottomBar = {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          tonalElevation = 2.dp,
        ) {
          NavigationBarItem(
            selected = selectedTabIndex == 0,
            onClick = { selectedTabIndex = 0 },
            icon = {
              Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "التمارين",
              )
            },
            label = { Text("التمارين") },
            modifier = Modifier.testTag("nav_tab_workout"),
          )
          NavigationBarItem(
            selected = selectedTabIndex == 1,
            onClick = { selectedTabIndex = 1 },
            icon = {
              Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = "الإحصائيات والتقارير",
              )
            },
            label = { Text("الإحصائيات والتقارير") },
            modifier = Modifier.testTag("nav_tab_reports"),
          )
        }
      },
    ) { innerPadding ->
      if (selectedTabIndex == 0) {
        BoxWithConstraints(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
          contentAlignment = Alignment.TopCenter,
        ) {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .widthIn(max = 640.dp),
            contentPadding = PaddingValues(
              start = 14.dp,
              end = 14.dp,
              top = 12.dp,
              bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            item(key = "daily_reminder_bar") {
              DailyReminderBarCard(
                settings = uiState.reminderSettings,
                onActivateClick = {
                  if (uiState.reminderSettings.enabled) {
                    isReminderDialogOpen = true
                  } else {
                    enableReminderWithPermission(
                      uiState.reminderSettings.hour,
                      uiState.reminderSettings.minute,
                    )
                  }
                },
                onPickTimeClick = { isReminderDialogOpen = true },
              )
            }

            if (uiState.isAllCompleted) {
              item(key = "completion_celebration") {
                CompletionBannerCard()
              }
            }

            items(
              items = uiState.activePlan.phases,
              key = { "${uiState.activePlan.id}_${it.id}" },
            ) { phase ->
              WorkoutPhaseCard(
                phase = phase,
                completedSetKeys = uiState.completedSetKeys,
                onToggleSet = viewModel::toggleSet,
                onOpenTimer = viewModel::openTimer,
              )
            }

            item(key = "bottom_reset_action") {
              BottomResetSection(
                hasCompletedItems = uiState.completedSetsCount > 0,
                onResetClick = onResetClick,
              )
            }
          }
        }
      } else {
        ReportsTabContent(
          summary = uiState.reportsSummary,
          modifier = Modifier.padding(innerPadding),
        )
      }

      if (isEditorOpen) {
        PlanEditorDialog(
          initialPlan = editingPlan,
          onSave = { planId, name, exercises ->
            viewModel.saveCustomPlan(planId, name, exercises)
            isEditorOpen = false
            editingPlan = null
          },
          onDismiss = {
            isEditorOpen = false
            editingPlan = null
          },
        )
      }

      if (isReminderDialogOpen) {
        ReminderSettingsDialog(
          settings = uiState.reminderSettings,
          hasPermission = WorkoutNotificationHelper.hasNotificationPermission(context),
          onRequestPermissionAndEnable = enableReminderWithPermission,
          onDisable = { h, m ->
            viewModel.setDailyReminder(context, enabled = false, hour = h, minute = m)
          },
          onSendTestNotification = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
              !WorkoutNotificationHelper.hasNotificationPermission(context)
            ) {
              pendingReminderTime =
                uiState.reminderSettings.hour to uiState.reminderSettings.minute
              notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
              viewModel.sendTestNotification(context)
            }
          },
          onDismiss = { isReminderDialogOpen = false },
        )
      }

      val deleteTarget = planToDelete
      if (deleteTarget != null) {
        AlertDialog(
          onDismissRequest = { planToDelete = null },
          title = { Text(stringResource(R.string.delete_plan_title)) },
          text = { Text(stringResource(R.string.delete_plan_message, deleteTarget.name)) },
          confirmButton = {
            Button(
              onClick = {
                viewModel.deletePlan(deleteTarget.id)
                planToDelete = null
              }
            ) {
              Text(stringResource(R.string.confirm_delete))
            }
          },
          dismissButton = {
            TextButton(onClick = { planToDelete = null }) {
              Text(stringResource(R.string.cancel))
            }
          },
        )
      }

      if (uiState.timerState.isVisible) {
        ExerciseTimerDialog(
          timerState = uiState.timerState,
          onToggleRunning = viewModel::toggleTimerRunning,
          onReset = viewModel::resetTimer,
          onDismiss = viewModel::dismissTimer,
        )
      }
    }
  }
}

@Composable
private fun LiveDashboardTopBar(
  uiState: WorkoutUiState,
  onSelectPlan: (String) -> Unit,
  onCreateNewPlan: () -> Unit,
  onEditActivePlan: () -> Unit,
  onRequestDeleteActivePlan: () -> Unit,
  onOpenReminderSettings: () -> Unit,
  onResetClick: () -> Unit,
) {
  val progressFraction = (uiState.completionPercent / 100f).coerceIn(0f, 1f)
  val canDeleteActive = !uiState.activePlan.isBuiltInDefault && uiState.plans.size > 1

  Surface(
    color = MaterialTheme.colorScheme.surface,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = Alignment.Center,
    ) {
      Column(
        modifier = Modifier
          .widthIn(max = 640.dp)
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Row 1: Title with 💪 + User "مهند" badge + Offline indicator + Dark Reset button
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "تماريني اليومية 💪",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              // User Avatar + Name "مهند"
              Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.55f)),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(18.dp)
                      .clip(CircleShape)
                      .background(OrangeAccent),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription = "الملف الشخصي",
                      tint = Color.White,
                      modifier = Modifier.size(12.dp),
                    )
                  }
                  Text(
                    text = "مهند",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                  )
                }
              }

              // Offline pill
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(50),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                  Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = null,
                    tint = SoftCyanText,
                    modifier = Modifier.size(12.dp),
                  )
                  Text(
                    text = stringResource(R.string.offline_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftCyanText,
                  )
                }
              }
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            IconButton(
              onClick = onOpenReminderSettings,
              modifier = Modifier
                .size(40.dp)
                .testTag("top_reminder_bell_button"),
            ) {
              Icon(
                imageVector = if (uiState.reminderSettings.enabled) {
                  Icons.Default.NotificationsActive
                } else {
                  Icons.Default.NotificationsNone
                },
                contentDescription = "إشعارات التمارين اليومية",
                tint = if (uiState.reminderSettings.enabled) {
                  OrangeAccent
                } else {
                  AthleticOnSurfaceVariant
                },
              )
            }

            // Sleek dark Reset button with 🔄
            Surface(
              color = DarkButtonContainer,
              shape = RoundedCornerShape(12.dp),
              border = BorderStroke(1.dp, DarkButtonBorder),
              modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = uiState.completedSetsCount > 0) { onResetClick() }
                .testTag("reset_day_top_button"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "إعادة ضبط اليوم 🔄",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = if (uiState.completedSetsCount > 0) {
                    Color.White
                  } else {
                    Color.White.copy(alpha = 0.45f)
                  },
                )
              }
            }
          }
        }

        // Row 2: Plan Switcher + Prominent Orange "+ تصميم جدول جديد" + Edit/Delete
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          uiState.plans.forEach { plan ->
            val selected = plan.id == uiState.activePlan.id
            FilterChip(
              selected = selected,
              onClick = { onSelectPlan(plan.id) },
              label = {
                Text(
                  text = plan.name,
                  style = MaterialTheme.typography.labelLarge,
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
              ),
              modifier = Modifier.testTag("plan_chip_${plan.id}"),
            )
          }

          Button(
            onClick = onCreateNewPlan,
            colors = ButtonDefaults.buttonColors(
              containerColor = OrangeAccent,
              contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .height(40.dp)
              .testTag("new_plan_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "+ تصميم جدول جديد".removePrefix("+ "),
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
            )
          }

          if (!uiState.activePlan.isBuiltInDefault) {
            IconButton(
              onClick = onEditActivePlan,
              modifier = Modifier
                .size(40.dp)
                .testTag("edit_plan_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit_plan),
                tint = SoftCyanText,
                modifier = Modifier.size(20.dp),
              )
            }
          }

          if (canDeleteActive) {
            IconButton(
              onClick = onRequestDeleteActivePlan,
              modifier = Modifier
                .size(40.dp)
                .testTag("delete_plan_button"),
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = stringResource(R.string.delete_plan),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }

        // Row 3: 3 Gradient Glassmorphic KPI Cards
        // In RTL LayoutDirection: 1st child = Right, 2nd child = Center, 3rd child = Left
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Right Card: Completion % (Green Gradient + Neon Green)
          val percentText = "${uiState.completionPercent}%"
          GradientKpiCard(
            title = "نسبة الإنجاز",
            valueText = percentText,
            subtitleText = "${uiState.completedSetsCount} / ${uiState.totalSetsCount} جولة",
            icon = Icons.Default.Shield,
            gradientStart = ProgressGradientStart,
            gradientEnd = ProgressGradientEnd,
            valueColor = ProgressNeonNumber,
            valueFontSize = if (percentText.length >= 4) 30.sp else 36.sp,
            hasGlow = false,
            valueTestTag = "summary_percent_value",
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(),
          )

          // Center Card: Burned Calories (Orange->Red Gradient + Glow + Giant Gold-Orange)
          val calText = formatStatNumber(uiState.burnedCalories)
          GradientKpiCard(
            title = "السعرات المحروقة",
            valueText = calText,
            subtitleText = "من أصل ${formatStatNumber(uiState.targetCalories)} سعرة",
            icon = Icons.Default.LocalFireDepartment,
            gradientStart = CaloriesGradientStart,
            gradientEnd = CaloriesGradientEnd,
            valueColor = CaloriesNeonNumber,
            valueFontSize = when {
              calText.length >= 5 -> 30.sp
              calText.length == 4 -> 34.sp
              else -> 40.sp
            },
            hasGlow = true,
            valueTestTag = "summary_calories_value",
            modifier = Modifier
              .weight(1.08f)
              .fillMaxHeight(),
          )

          // Left Card: Completed Minutes (Cyan->Dark Teal Gradient + Neon Cyan)
          val minText = formatStatNumber(uiState.completedMinutes)
          GradientKpiCard(
            title = "الدقائق المنجزة",
            valueText = minText,
            subtitleText = "من أصل ${formatStatNumber(uiState.targetMinutes)} دقيقة",
            icon = Icons.Outlined.Schedule,
            gradientStart = MinutesGradientStart,
            gradientEnd = MinutesGradientEnd,
            valueColor = MinutesNeonNumber,
            valueFontSize = when {
              minText.length >= 5 -> 28.sp
              minText.length == 4 -> 32.sp
              else -> 36.sp
            },
            hasGlow = false,
            valueTestTag = "summary_minutes_value",
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight(),
          )
        }

        LinearProgressIndicator(
          progress = { progressFraction },
          modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(50)),
          color = ProgressNeonNumber,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
          strokeCap = StrokeCap.Round,
        )
      }
    }
  }
}

@Composable
private fun GradientKpiCard(
  title: String,
  valueText: String,
  subtitleText: String,
  icon: ImageVector,
  gradientStart: Color,
  gradientEnd: Color,
  valueColor: Color,
  valueFontSize: TextUnit,
  hasGlow: Boolean,
  valueTestTag: String,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(16.dp)
  val borderColor = if (hasGlow) {
    CaloriesNeonNumber.copy(alpha = 0.55f)
  } else {
    Color.White.copy(alpha = 0.22f)
  }

  val cardModifier = if (hasGlow) {
    modifier
      .shadow(
        elevation = 14.dp,
        shape = shape,
        ambientColor = CaloriesGlowShadow,
        spotColor = CaloriesGlowShadow,
      )
      .clip(shape)
  } else {
    modifier.clip(shape)
  }

  Box(
    modifier = cardModifier
      .background(
        brush = Brush.linearGradient(
          colors = listOf(gradientStart, gradientEnd),
          start = Offset(0f, 0f),
          end = Offset(400f, 400f),
        )
      )
      // Subtle glassmorphic top highlight sheen
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color.White.copy(alpha = 0.16f),
            Color.Transparent,
          )
        )
      )
      .border(width = 1.dp, color = borderColor, shape = shape)
      .padding(horizontal = 8.dp, vertical = 10.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Title + Icon
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = Color.White.copy(alpha = 0.95f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      // Huge Neon Primary Metric
      Text(
        text = valueText,
        fontSize = valueFontSize,
        lineHeight = valueFontSize,
        fontWeight = FontWeight.ExtraBold,
        color = valueColor,
        maxLines = 1,
        softWrap = false,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(vertical = 4.dp)
          .testTag(valueTestTag),
      )

      // Crisp White Subtitle
      Text(
        text = subtitleText,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = Color.White.copy(alpha = 0.92f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun DailyReminderBarCard(
  settings: ReminderSettings,
  onActivateClick: () -> Unit,
  onPickTimeClick: () -> Unit,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = if (settings.enabled) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
      } else {
        MaterialTheme.colorScheme.surface
      },
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (settings.enabled) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
      } else {
        MaterialTheme.colorScheme.outline
      },
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = if (settings.enabled) {
          Icons.Default.NotificationsActive
        } else {
          Icons.Default.NotificationsNone
        },
        contentDescription = null,
        tint = if (settings.enabled) OrangeAccent else MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(22.dp),
      )

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (settings.enabled) {
            "التنبيه اليومي مفعّل"
          } else {
            "تفعيل إشعارات التمارين اليومية"
          },
          style = MaterialTheme.typography.titleSmall,
          color = Color.White,
        )
        Text(
          text = "موعد التنبيه: ${formatReminderTimeAr(settings.hour, settings.minute)}",
          style = MaterialTheme.typography.bodySmall,
          color = AthleticOnSurfaceVariant,
        )
      }

      OutlinedButton(
        onClick = onPickTimeClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier
          .height(40.dp)
          .testTag("pick_reminder_time_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Schedule,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "الوقت",
          style = MaterialTheme.typography.labelMedium,
        )
      }

      if (!settings.enabled) {
        Button(
          onClick = onActivateClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = OrangeAccent,
            contentColor = Color.White,
          ),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier
            .height(40.dp)
            .testTag("enable_notifications_button"),
        ) {
          Text(
            text = "تفعيل",
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }
  }
}

@Composable
private fun CompletionBannerCard() {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("completion_banner"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.size(40.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(22.dp),
          )
        }
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.all_done_title),
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
        )
        Text(
          text = stringResource(R.string.all_done_subtitle),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
      }
    }
  }
}

@Composable
private fun WorkoutPhaseCard(
  phase: WorkoutPhase,
  completedSetKeys: Set<String>,
  onToggleSet: (exerciseId: String, setIndex: Int) -> Unit,
  onOpenTimer: (String, Int) -> Unit,
) {
  val totalSetsInPhase = phase.exercises.sumOf { it.setsCount }
  val completedSetsInPhase = phase.exercises.sumOf { ex ->
    (1..ex.setsCount).count { setKey(ex.id, it) in completedSetKeys }
  }
  val isPhaseComplete = totalSetsInPhase > 0 && completedSetsInPhase == totalSetsInPhase

  val cardBg = if (isPhaseComplete) CompletedCardDark else MaterialTheme.colorScheme.surface

  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = cardBg),
    border = BorderStroke(
      width = 1.dp,
      color = if (isPhaseComplete) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
      } else {
        MaterialTheme.colorScheme.outline
      },
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("phase_card_${phase.id}"),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = phase.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "${formatStatNumber(phase.totalMinutes)} دقيقة",
              style = MaterialTheme.typography.bodySmall,
              color = SoftCyanText,
            )
            Text(
              text = "•",
              style = MaterialTheme.typography.bodySmall,
              color = AthleticOnSurfaceVariant,
            )
            Text(
              text = "${formatStatNumber(phase.totalCalories)} سعرة",
              style = MaterialTheme.typography.bodySmall,
              color = SoftOrangeText,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
        Surface(
          shape = RoundedCornerShape(50),
          color = if (isPhaseComplete) {
            MaterialTheme.colorScheme.tertiaryContainer
          } else {
            MaterialTheme.colorScheme.surfaceVariant
          },
        ) {
          Text(
            text = "$completedSetsInPhase/$totalSetsInPhase جولة",
            style = MaterialTheme.typography.labelMedium,
            color = if (isPhaseComplete) {
              ProgressNeonNumber
            } else {
              AthleticOnSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))

      phase.exercises.forEachIndexed { index, exercise ->
        ExerciseSetTrackingItem(
          exercise = exercise,
          completedSetKeys = completedSetKeys,
          onToggleSet = onToggleSet,
          onOpenTimer = onOpenTimer,
        )
        if (index < phase.exercises.lastIndex) {
          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseSetTrackingItem(
  exercise: ExerciseItem,
  completedSetKeys: Set<String>,
  onToggleSet: (exerciseId: String, setIndex: Int) -> Unit,
  onOpenTimer: (String, Int) -> Unit,
) {
  val completedInExercise = (1..exercise.setsCount).count {
    setKey(exercise.id, it) in completedSetKeys
  }
  val isExerciseComplete = completedInExercise == exercise.setsCount

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag("exercise_item_${exercise.id}"),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        // Larger, bright crisp white exercise title
        Text(
          text = exercise.name,
          fontSize = 18.sp,
          lineHeight = 25.sp,
          fontWeight = FontWeight.Bold,
          color = if (isExerciseComplete) ProgressNeonNumber else Color.White,
        )

        // Spacious, color-coded readable breakdown
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "${exercise.setsCount} ${if (exercise.setsCount == 1) "جولة" else "جولات"} × ${formatStatNumber(exercise.amountPerSet)} ${exercise.unitType.labelAr}",
            style = MaterialTheme.typography.bodyMedium,
            color = AthleticOnSurfaceVariant,
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = AthleticOnSurfaceVariant,
          )
          Text(
            text = "لكل جولة: ${formatStatNumber(exercise.perSetMinutes)} د",
            style = MaterialTheme.typography.bodyMedium,
            color = SoftCyanText,
          )
          Text(
            text = "|",
            style = MaterialTheme.typography.bodyMedium,
            color = AthleticOnSurfaceVariant,
          )
          Text(
            text = "${formatStatNumber(exercise.perSetCalories)} سعرة",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = SoftOrangeText,
          )
        }
      }

      if (exercise.unitType == ExerciseUnitType.SECONDS) {
        val secs = exercise.amountPerSet.toInt().coerceAtLeast(5)
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onOpenTimer(exercise.name, secs) }
            .testTag("timer_chip_${exercise.id}"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = "مؤقت $secs ثانية",
              tint = SoftCyanText,
              modifier = Modifier.size(15.dp),
            )
            Text(
              text = "$secs ث",
              style = MaterialTheme.typography.labelMedium,
              color = Color.White,
            )
          }
        }
      }
    }

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      for (setIndex in 1..exercise.setsCount) {
        val isDone = setKey(exercise.id, setIndex) in completedSetKeys
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isDone) {
            ProgressGradientStart
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
          },
          border = BorderStroke(
            width = 1.dp,
            color = if (isDone) ProgressNeonNumber.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline,
          ),
          modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Checkbox) {
              onToggleSet(exercise.id, setIndex)
            }
            .testTag("set_button_${exercise.id}_$setIndex"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            if (isDone) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "مكتمل",
                tint = Color.White,
                modifier = Modifier.size(17.dp),
              )
            }
            Text(
              text = "جولة $setIndex",
              style = MaterialTheme.typography.labelLarge,
              color = Color.White,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BottomResetSection(
  hasCompletedItems: Boolean,
  onResetClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp, bottom = 8.dp),
  ) {
    Surface(
      color = DarkButtonContainer,
      shape = MaterialTheme.shapes.medium,
      border = BorderStroke(
        width = 1.dp,
        color = if (hasCompletedItems) DarkButtonBorder else MaterialTheme.colorScheme.outline,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .clip(MaterialTheme.shapes.medium)
        .clickable(enabled = hasCompletedItems) { onResetClick() }
        .testTag("reset_day_button"),
    ) {
      Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Text(
          text = "إعادة ضبط اليوم 🔄",
          style = MaterialTheme.typography.labelLarge,
          color = if (hasCompletedItems) Color.White else Color.White.copy(alpha = 0.45f),
        )
      }
    }
  }
}

@Composable
private fun ExerciseTimerDialog(
  timerState: TimerState,
  onToggleRunning: () -> Unit,
  onReset: () -> Unit,
  onDismiss: () -> Unit,
) {
  val isDone = timerState.remainingSeconds == 0
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = stringResource(R.string.timer_title),
        style = MaterialTheme.typography.titleLarge,
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = timerState.exerciseName,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
          shape = CircleShape,
          color = if (isDone) {
            MaterialTheme.colorScheme.tertiaryContainer
          } else {
            MaterialTheme.colorScheme.primaryContainer
          },
          modifier = Modifier.size(104.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = if (isDone) {
                stringResource(R.string.timer_done)
              } else {
                "${timerState.remainingSeconds} ث"
              },
              style = if (isDone) {
                MaterialTheme.typography.titleMedium
              } else {
                MaterialTheme.typography.headlineMedium
              },
              color = if (isDone) {
                ProgressNeonNumber
              } else {
                MinutesNeonNumber
              },
            )
          }
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Button(
            onClick = onToggleRunning,
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
          ) {
            Icon(
              imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = null,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (timerState.isRunning) {
                stringResource(R.string.timer_pause)
              } else {
                stringResource(R.string.timer_start)
              }
            )
          }
          FilledTonalButton(
            onClick = onReset,
            modifier = Modifier.height(48.dp),
          ) {
            Text(text = stringResource(R.string.timer_reset))
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(R.string.timer_close))
      }
    },
  )
}
