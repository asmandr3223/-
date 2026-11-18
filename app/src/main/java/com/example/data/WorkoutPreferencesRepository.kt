package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DailyHistoryRecord
import com.example.model.DefaultWorkoutPlans
import com.example.model.ExerciseItem
import com.example.model.ExerciseUnitType
import com.example.model.ReminderSettings
import com.example.model.ReportsCalculator
import com.example.model.WorkoutPhase
import com.example.model.WorkoutPlan
import com.example.model.setKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

private val Context.workoutDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "workout_tracker_v2_prefs")

data class PersistedWorkoutData(
  val plans: List<WorkoutPlan>,
  val selectedPlanId: String,
  val completedSetsByPlan: Map<String, Set<String>>,
  val reminderSettings: ReminderSettings,
  val dailyHistory: List<DailyHistoryRecord>,
)

class WorkoutPreferencesRepository(private val context: Context) {

  private object Keys {
    val PLANS_JSON = stringPreferencesKey("plans_json")
    val SELECTED_PLAN_ID = stringPreferencesKey("selected_plan_id")
    val COMPLETED_SETS_JSON = stringPreferencesKey("completed_sets_by_plan_json")
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    val DAILY_HISTORY_JSON = stringPreferencesKey("daily_history_json")
  }

  val workoutData: Flow<PersistedWorkoutData> =
    context.workoutDataStore.data.map { prefs ->
      val plans = parsePlans(prefs[Keys.PLANS_JSON])
      val selectedIdRaw = prefs[Keys.SELECTED_PLAN_ID]
      val selectedPlanId =
        if (selectedIdRaw != null && plans.any { it.id == selectedIdRaw }) {
          selectedIdRaw
        } else {
          plans.first().id
        }
      val completedByPlan = parseCompletedMap(prefs[Keys.COMPLETED_SETS_JSON])
      val reminder = ReminderSettings(
        enabled = prefs[Keys.REMINDER_ENABLED] ?: false,
        hour = prefs[Keys.REMINDER_HOUR] ?: 17,
        minute = prefs[Keys.REMINDER_MINUTE] ?: 0,
      )
      val history = parseHistory(prefs[Keys.DAILY_HISTORY_JSON])
      PersistedWorkoutData(
        plans = plans,
        selectedPlanId = selectedPlanId,
        completedSetsByPlan = completedByPlan,
        reminderSettings = reminder,
        dailyHistory = history,
      )
    }

  suspend fun selectPlan(planId: String) {
    context.workoutDataStore.edit { prefs ->
      prefs[Keys.SELECTED_PLAN_ID] = planId
    }
  }

  suspend fun toggleSet(planId: String, key: String) {
    context.workoutDataStore.edit { prefs ->
      val plans = parsePlans(prefs[Keys.PLANS_JSON])
      val map = parseCompletedMap(prefs[Keys.COMPLETED_SETS_JSON]).toMutableMap()
      val current = map[planId]?.toMutableSet() ?: mutableSetOf()
      if (key in current) current.remove(key) else current.add(key)
      map[planId] = current
      prefs[Keys.COMPLETED_SETS_JSON] = encodeCompletedMap(map)

      val activePlan = plans.firstOrNull { it.id == planId }
      if (activePlan != null) {
        updateTodayHistory(prefs, activePlan, current)
      }
    }
  }

  suspend fun setPlanCompletedSets(planId: String, setKeys: Set<String>) {
    context.workoutDataStore.edit { prefs ->
      val plans = parsePlans(prefs[Keys.PLANS_JSON])
      val map = parseCompletedMap(prefs[Keys.COMPLETED_SETS_JSON]).toMutableMap()
      map[planId] = setKeys
      prefs[Keys.COMPLETED_SETS_JSON] = encodeCompletedMap(map)

      val activePlan = plans.firstOrNull { it.id == planId }
      if (activePlan != null) {
        updateTodayHistory(prefs, activePlan, setKeys)
      }
    }
  }

  suspend fun resetPlanDay(planId: String) {
    setPlanCompletedSets(planId, emptySet())
  }

  suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
    context.workoutDataStore.edit { prefs ->
      prefs[Keys.REMINDER_ENABLED] = enabled
      prefs[Keys.REMINDER_HOUR] = hour.coerceIn(0, 23)
      prefs[Keys.REMINDER_MINUTE] = minute.coerceIn(0, 59)
    }
  }

  suspend fun upsertPlan(plan: WorkoutPlan) {
    context.workoutDataStore.edit { prefs ->
      val plans = parsePlans(prefs[Keys.PLANS_JSON]).toMutableList()
      val existingIndex = plans.indexOfFirst { it.id == plan.id }
      if (existingIndex >= 0) {
        plans[existingIndex] = plan
      } else {
        plans.add(plan)
      }
      prefs[Keys.PLANS_JSON] = encodePlans(plans)
      prefs[Keys.SELECTED_PLAN_ID] = plan.id
    }
  }

  suspend fun deletePlan(planId: String) {
    context.workoutDataStore.edit { prefs ->
      val plans = parsePlans(prefs[Keys.PLANS_JSON]).toMutableList()
      if (plans.size <= 1) return@edit
      val target = plans.firstOrNull { it.id == planId } ?: return@edit
      if (target.isBuiltInDefault) return@edit

      plans.removeAll { it.id == planId }
      prefs[Keys.PLANS_JSON] = encodePlans(plans)

      val map = parseCompletedMap(prefs[Keys.COMPLETED_SETS_JSON]).toMutableMap()
      map.remove(planId)
      prefs[Keys.COMPLETED_SETS_JSON] = encodeCompletedMap(map)

      if (prefs[Keys.SELECTED_PLAN_ID] == planId) {
        prefs[Keys.SELECTED_PLAN_ID] = plans.first().id
      }
    }
  }

  private fun updateTodayHistory(
    prefs: androidx.datastore.preferences.core.MutablePreferences,
    plan: WorkoutPlan,
    completedKeys: Set<String>,
  ) {
    val todayKey = ReportsCalculator.todayDateKey()
    var minutes = 0.0
    var calories = 0.0
    var completedSets = 0
    plan.allExercises.forEach { ex ->
      for (i in 1..ex.setsCount) {
        if (setKey(ex.id, i) in completedKeys) {
          completedSets++
          minutes += ex.perSetMinutes
          calories += ex.perSetCalories
        }
      }
    }
    val totalMin = plan.totalMinutes
    val totalSets = plan.totalSets
    val percent = when {
      totalMin > 0.0 -> ((minutes / totalMin) * 100.0).roundToInt().coerceIn(0, 100)
      totalSets > 0 -> ((completedSets.toDouble() / totalSets) * 100.0).roundToInt().coerceIn(0, 100)
      else -> 0
    }

    val history = parseHistory(prefs[Keys.DAILY_HISTORY_JSON]).toMutableList()
    history.removeAll { it.dateKey == todayKey }
    if (completedSets > 0) {
      history.add(
        DailyHistoryRecord(
          dateKey = todayKey,
          planId = plan.id,
          planName = plan.name,
          completedSets = completedSets,
          totalSets = totalSets,
          minutes = minutes,
          calories = calories,
          completionPercent = percent,
        )
      )
    }
    prefs[Keys.DAILY_HISTORY_JSON] = encodeHistory(history)
  }

  private fun parsePlans(raw: String?): List<WorkoutPlan> {
    if (raw.isNullOrBlank()) return DefaultWorkoutPlans.initialPlans
    return try {
      val arr = JSONArray(raw)
      val list = ArrayList<WorkoutPlan>(arr.length())
      for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val phasesArr = obj.getJSONArray("phases")
        val phases = ArrayList<WorkoutPhase>(phasesArr.length())
        for (p in 0 until phasesArr.length()) {
          val pObj = phasesArr.getJSONObject(p)
          val exArr = pObj.getJSONArray("exercises")
          val exercises = ArrayList<ExerciseItem>(exArr.length())
          for (e in 0 until exArr.length()) {
            val eObj = exArr.getJSONObject(e)
            exercises.add(
              ExerciseItem(
                id = eObj.getString("id"),
                name = eObj.getString("name"),
                unitType = ExerciseUnitType.valueOf(eObj.getString("unitType")),
                amountPerSet = eObj.getDouble("amountPerSet"),
                setsCount = eObj.getInt("setsCount"),
                perSetMinutes = eObj.getDouble("perSetMinutes"),
                perSetCalories = eObj.getDouble("perSetCalories"),
                isAutoCalories = eObj.optBoolean("isAutoCalories", true),
              )
            )
          }
          phases.add(
            WorkoutPhase(
              id = pObj.getString("id"),
              title = pObj.getString("title"),
              exercises = exercises,
            )
          )
        }
        list.add(
          WorkoutPlan(
            id = obj.getString("id"),
            name = obj.getString("name"),
            isBuiltInDefault = obj.optBoolean("isBuiltInDefault", false),
            phases = phases,
          )
        )
      }
      if (list.isEmpty()) DefaultWorkoutPlans.initialPlans else list
    } catch (_: Exception) {
      DefaultWorkoutPlans.initialPlans
    }
  }

  private fun encodePlans(plans: List<WorkoutPlan>): String {
    val arr = JSONArray()
    plans.forEach { plan ->
      val pObj = JSONObject().apply {
        put("id", plan.id)
        put("name", plan.name)
        put("isBuiltInDefault", plan.isBuiltInDefault)
        val phasesArr = JSONArray()
        plan.phases.forEach { phase ->
          val phObj = JSONObject().apply {
            put("id", phase.id)
            put("title", phase.title)
            val exArr = JSONArray()
            phase.exercises.forEach { ex ->
              exArr.put(
                JSONObject().apply {
                  put("id", ex.id)
                  put("name", ex.name)
                  put("unitType", ex.unitType.name)
                  put("amountPerSet", ex.amountPerSet)
                  put("setsCount", ex.setsCount)
                  put("perSetMinutes", ex.perSetMinutes)
                  put("perSetCalories", ex.perSetCalories)
                  put("isAutoCalories", ex.isAutoCalories)
                }
              )
            }
            put("exercises", exArr)
          }
          phasesArr.put(phObj)
        }
        put("phases", phasesArr)
      }
      arr.put(pObj)
    }
    return arr.toString()
  }

  private fun parseCompletedMap(raw: String?): Map<String, Set<String>> {
    if (raw.isNullOrBlank()) return emptyMap()
    return try {
      val obj = JSONObject(raw)
      val result = HashMap<String, Set<String>>()
      val keys = obj.keys()
      while (keys.hasNext()) {
        val planId = keys.next()
        val arr = obj.getJSONArray(planId)
        val set = HashSet<String>(arr.length())
        for (i in 0 until arr.length()) {
          set.add(arr.getString(i))
        }
        result[planId] = set
      }
      result
    } catch (_: Exception) {
      emptyMap()
    }
  }

  private fun encodeCompletedMap(map: Map<String, Set<String>>): String {
    val obj = JSONObject()
    map.forEach { (planId, sets) ->
      val arr = JSONArray()
      sets.forEach { arr.put(it) }
      obj.put(planId, arr)
    }
    return obj.toString()
  }

  private fun parseHistory(raw: String?): List<DailyHistoryRecord> {
    if (raw.isNullOrBlank()) return emptyList()
    return try {
      val arr = JSONArray(raw)
      val list = ArrayList<DailyHistoryRecord>(arr.length())
      for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        list.add(
          DailyHistoryRecord(
            dateKey = o.getString("dateKey"),
            planId = o.getString("planId"),
            planName = o.getString("planName"),
            completedSets = o.getInt("completedSets"),
            totalSets = o.getInt("totalSets"),
            minutes = o.getDouble("minutes"),
            calories = o.getDouble("calories"),
            completionPercent = o.getInt("completionPercent"),
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun encodeHistory(list: List<DailyHistoryRecord>): String {
    val arr = JSONArray()
    list.forEach { r ->
      arr.put(
        JSONObject().apply {
          put("dateKey", r.dateKey)
          put("planId", r.planId)
          put("planName", r.planName)
          put("completedSets", r.completedSets)
          put("totalSets", r.totalSets)
          put("minutes", r.minutes)
          put("calories", r.calories)
          put("completionPercent", r.completionPercent)
        }
      )
    }
    return arr.toString()
  }
}
