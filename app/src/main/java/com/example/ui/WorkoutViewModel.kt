package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.WorkoutPreferencesRepository
import com.example.model.DefaultWorkoutPlans
import com.example.model.ExerciseItem
import com.example.model.ReminderSettings
import com.example.model.ReportsCalculator
import com.example.model.ReportsSummary
import com.example.model.WorkoutPhase
import com.example.model.WorkoutPlan
import com.example.model.setKey
import com.example.notifications.WorkoutNotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class TimerState(
  val isVisible: Boolean = false,
  val exerciseName: String = "",
  val initialSeconds: Int = 45,
  val remainingSeconds: Int = 45,
  val isRunning: Boolean = false,
)

data class WorkoutUiState(
  val plans: List<WorkoutPlan> = DefaultWorkoutPlans.initialPlans,
  val activePlan: WorkoutPlan = DefaultWorkoutPlans.primaryPlan,
  val completedSetKeys: Set<String> = emptySet(),
  val completedMinutes: Double = 0.0,
  val targetMinutes: Double = DefaultWorkoutPlans.primaryPlan.totalMinutes,
  val burnedCalories: Double = 0.0,
  val targetCalories: Double = DefaultWorkoutPlans.primaryPlan.totalCalories,
  val completionPercent: Int = 0,
  val completedSetsCount: Int = 0,
  val totalSetsCount: Int = DefaultWorkoutPlans.primaryPlan.totalSets,
  val isAllCompleted: Boolean = false,
  val reminderSettings: ReminderSettings = ReminderSettings(),
  val reportsSummary: ReportsSummary = ReportsCalculator.buildSummary(emptyList()),
  val timerState: TimerState = TimerState(),
)

class WorkoutViewModel(
  private val repository: WorkoutPreferencesRepository
) : ViewModel() {

  private val _timerState = MutableStateFlow(TimerState())
  private var timerJob: Job? = null
  private var lastClearedPlanId: String? = null
  private var lastClearedKeys: Set<String> = emptySet()

  val uiState: StateFlow<WorkoutUiState> =
    combine(repository.workoutData, _timerState) { data, timer ->
      val activePlan =
        data.plans.firstOrNull { it.id == data.selectedPlanId } ?: data.plans.first()
      val completedKeys = data.completedSetsByPlan[activePlan.id] ?: emptySet()

      var completedMinutes = 0.0
      var burnedCalories = 0.0
      var completedSetsCount = 0

      activePlan.allExercises.forEach { exercise ->
        for (setIdx in 1..exercise.setsCount) {
          if (setKey(exercise.id, setIdx) in completedKeys) {
            completedSetsCount++
            completedMinutes += exercise.perSetMinutes
            burnedCalories += exercise.perSetCalories
          }
        }
      }

      val targetMinutes = activePlan.totalMinutes
      val targetCalories = activePlan.totalCalories
      val totalSets = activePlan.totalSets
      val percent =
        if (targetMinutes > 0.0) {
          ((completedMinutes / targetMinutes) * 100.0).roundToInt().coerceIn(0, 100)
        } else if (totalSets > 0) {
          ((completedSetsCount.toDouble() / totalSets) * 100.0).roundToInt().coerceIn(0, 100)
        } else {
          0
        }

      WorkoutUiState(
        plans = data.plans,
        activePlan = activePlan,
        completedSetKeys = completedKeys,
        completedMinutes = completedMinutes,
        targetMinutes = targetMinutes,
        burnedCalories = burnedCalories,
        targetCalories = targetCalories,
        completionPercent = percent,
        completedSetsCount = completedSetsCount,
        totalSetsCount = totalSets,
        isAllCompleted = totalSets > 0 && completedSetsCount == totalSets,
        reminderSettings = data.reminderSettings,
        reportsSummary = ReportsCalculator.buildSummary(data.dailyHistory),
        timerState = timer,
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = WorkoutUiState(),
    )

  fun selectPlan(planId: String) {
    viewModelScope.launch { repository.selectPlan(planId) }
  }

  fun toggleSet(exerciseId: String, setIndex: Int) {
    val activePlanId = uiState.value.activePlan.id
    val key = setKey(exerciseId, setIndex)
    viewModelScope.launch { repository.toggleSet(activePlanId, key) }
  }

  fun resetActivePlanDay(onCleared: (hadItems: Boolean) -> Unit) {
    val activePlanId = uiState.value.activePlan.id
    val currentKeys = uiState.value.completedSetKeys
    if (currentKeys.isEmpty()) {
      onCleared(false)
      return
    }
    lastClearedPlanId = activePlanId
    lastClearedKeys = currentKeys
    viewModelScope.launch {
      repository.resetPlanDay(activePlanId)
      onCleared(true)
    }
  }

  fun undoReset() {
    val planId = lastClearedPlanId ?: return
    val keys = lastClearedKeys
    if (keys.isEmpty()) return
    lastClearedPlanId = null
    lastClearedKeys = emptySet()
    viewModelScope.launch { repository.setPlanCompletedSets(planId, keys) }
  }

  fun saveCustomPlan(
    editingPlanId: String?,
    planName: String,
    exercises: List<ExerciseItem>,
  ) {
    val trimmedName = planName.trim().ifEmpty { "جدول مخصص" }
    val planId = editingPlanId ?: "plan_custom_${System.currentTimeMillis()}"
    val existing = uiState.value.plans.firstOrNull { it.id == planId }

    val plan = WorkoutPlan(
      id = planId,
      name = trimmedName,
      isBuiltInDefault = existing?.isBuiltInDefault ?: false,
      phases = listOf(
        WorkoutPhase(
          id = "${planId}_phase_1",
          title = "تمارين $trimmedName",
          exercises = exercises,
        )
      ),
    )

    viewModelScope.launch { repository.upsertPlan(plan) }
  }

  fun deletePlan(planId: String) {
    viewModelScope.launch { repository.deletePlan(planId) }
  }

  fun setDailyReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
    val appCtx = context.applicationContext
    viewModelScope.launch {
      repository.updateReminderSettings(enabled = enabled, hour = hour, minute = minute)
      if (enabled) {
        WorkoutNotificationHelper.scheduleDailyReminder(appCtx, hour, minute)
      } else {
        WorkoutNotificationHelper.cancelDailyReminder(appCtx)
      }
    }
  }

  fun sendTestNotification(context: Context) {
    WorkoutNotificationHelper.showMotivationalNotification(context.applicationContext)
  }

  fun openTimer(exerciseName: String, seconds: Int) {
    timerJob?.cancel()
    _timerState.value = TimerState(
      isVisible = true,
      exerciseName = exerciseName,
      initialSeconds = seconds,
      remainingSeconds = seconds,
      isRunning = false,
    )
  }

  fun toggleTimerRunning() {
    val current = _timerState.value
    if (current.isRunning) {
      timerJob?.cancel()
      _timerState.value = current.copy(isRunning = false)
    } else {
      val startFrom =
        if (current.remainingSeconds <= 0) current.initialSeconds else current.remainingSeconds
      _timerState.value = current.copy(remainingSeconds = startFrom, isRunning = true)
      startTicker()
    }
  }

  fun resetTimer() {
    timerJob?.cancel()
    val current = _timerState.value
    _timerState.value = current.copy(
      remainingSeconds = current.initialSeconds,
      isRunning = false,
    )
  }

  fun dismissTimer() {
    timerJob?.cancel()
    _timerState.value = TimerState(isVisible = false)
  }

  private fun startTicker() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (true) {
        delay(1_000L)
        val current = _timerState.value
        if (!current.isRunning) break
        if (current.remainingSeconds <= 1) {
          _timerState.value = current.copy(remainingSeconds = 0, isRunning = false)
          break
        } else {
          _timerState.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }

  class Factory(
    private val repository: WorkoutPreferencesRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return WorkoutViewModel(repository) as T
    }
  }
}
