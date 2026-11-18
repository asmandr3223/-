package com.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

enum class ExerciseUnitType(val labelAr: String) {
  REPS("تكرار"),
  SECONDS("ثانية"),
  MINUTES("دقيقة"),
}

data class ExerciseItem(
  val id: String,
  val name: String,
  val unitType: ExerciseUnitType,
  val amountPerSet: Double,
  val setsCount: Int,
  val perSetMinutes: Double,
  val perSetCalories: Double,
  val isAutoCalories: Boolean = true,
) {
  val totalMinutes: Double get() = perSetMinutes * setsCount
  val totalCalories: Double get() = perSetCalories * setsCount
}

data class WorkoutPhase(
  val id: String,
  val title: String,
  val exercises: List<ExerciseItem>,
) {
  val totalMinutes: Double get() = exercises.sumOf { it.totalMinutes }
  val totalCalories: Double get() = exercises.sumOf { it.totalCalories }
}

data class WorkoutPlan(
  val id: String,
  val name: String,
  val isBuiltInDefault: Boolean = false,
  val phases: List<WorkoutPhase>,
) {
  val allExercises: List<ExerciseItem> get() = phases.flatMap { it.exercises }
  val totalMinutes: Double get() = allExercises.sumOf { it.totalMinutes }
  val totalCalories: Double get() = allExercises.sumOf { it.totalCalories }
  val totalSets: Int get() = allExercises.sumOf { it.setsCount }
}

data class ReminderSettings(
  val enabled: Boolean = false,
  val hour: Int = 17,
  val minute: Int = 0,
)

data class DailyHistoryRecord(
  val dateKey: String,
  val planId: String,
  val planName: String,
  val completedSets: Int,
  val totalSets: Int,
  val minutes: Double,
  val calories: Double,
  val completionPercent: Int,
)

data class PeriodComparison(
  val currentMinutes: Double,
  val previousMinutes: Double,
  val currentCalories: Double,
  val previousCalories: Double,
) {
  val minutesDelta: Double get() = currentMinutes - previousMinutes
  val caloriesDelta: Double get() = currentCalories - previousCalories
}

data class ReportsSummary(
  val todayVsYesterday: PeriodComparison,
  val thisWeekVsLastWeek: PeriodComparison,
  val thisMonthVsLastMonth: PeriodComparison,
  val historyRows: List<DailyHistoryRecord>,
)

data class CaloriePreset(
  val id: String,
  val displayName: String,
  val keywords: List<String>,
  val defaultUnitType: ExerciseUnitType,
  val defaultAmountPerSet: Double,
  val defaultSets: Int,
  val ratePerSecondOrRep: Double,
  val ratePerMinute: Double? = null,
)

data class CalorieMatchResult(
  val preset: CaloriePreset,
  val caloriesPerSet: Double,
  val estimatedMinutesPerSet: Double,
)

object CalorieEngine {
  val presets: List<CaloriePreset> = listOf(
    CaloriePreset(
      id = "pushups",
      displayName = "تمرين الضغط (Push-ups)",
      keywords = listOf("ضغط", "push"),
      defaultUnitType = ExerciseUnitType.REPS,
      defaultAmountPerSet = 12.0,
      defaultSets = 3,
      ratePerSecondOrRep = 0.45,
    ),
    CaloriePreset(
      id = "squats",
      displayName = "تمرين القرفصاء (Squats)",
      keywords = listOf("قرفصاء", "سكوات", "squat"),
      defaultUnitType = ExerciseUnitType.REPS,
      defaultAmountPerSet = 15.0,
      defaultSets = 3,
      ratePerSecondOrRep = 0.35,
    ),
    CaloriePreset(
      id = "crunches",
      displayName = "تمرين البطن (Crunches)",
      keywords = listOf("بطن", "crunch", "معدة", "abs"),
      defaultUnitType = ExerciseUnitType.REPS,
      defaultAmountPerSet = 15.0,
      defaultSets = 3,
      ratePerSecondOrRep = 0.25,
    ),
    CaloriePreset(
      id = "burpees",
      displayName = "تمرين البربيز (Burpees)",
      keywords = listOf("بربيز", "بيربي", "burpee"),
      defaultUnitType = ExerciseUnitType.REPS,
      defaultAmountPerSet = 10.0,
      defaultSets = 3,
      ratePerSecondOrRep = 1.2,
    ),
    CaloriePreset(
      id = "high_knees",
      displayName = "رفع الركبتين (High Knees)",
      keywords = listOf("ركبتين", "الركبتين", "high knees"),
      defaultUnitType = ExerciseUnitType.SECONDS,
      defaultAmountPerSet = 45.0,
      defaultSets = 4,
      ratePerSecondOrRep = 0.15,
      ratePerMinute = 9.0,
    ),
    CaloriePreset(
      id = "plank",
      displayName = "تمرين البلانك (Plank)",
      keywords = listOf("بلانك", "plank"),
      defaultUnitType = ExerciseUnitType.SECONDS,
      defaultAmountPerSet = 60.0,
      defaultSets = 4,
      ratePerSecondOrRep = 0.08,
      ratePerMinute = 5.0,
    ),
    CaloriePreset(
      id = "jump_rope",
      displayName = "نط الحبل (Jump Rope)",
      keywords = listOf("حبل", "نط الحبل", "jump rope"),
      defaultUnitType = ExerciseUnitType.SECONDS,
      defaultAmountPerSet = 60.0,
      defaultSets = 3,
      ratePerSecondOrRep = 0.2,
      ratePerMinute = 12.0,
    ),
    CaloriePreset(
      id = "walking",
      displayName = "المشي السريع (Walking)",
      keywords = listOf("مشي", "المشي", "walk"),
      defaultUnitType = ExerciseUnitType.MINUTES,
      defaultAmountPerSet = 12.5,
      defaultSets = 2,
      ratePerSecondOrRep = 5.2 / 60.0,
      ratePerMinute = 5.2,
    ),
    CaloriePreset(
      id = "warmup_stretch",
      displayName = "الإحماء أو الإطالة (Warm-up / Stretching)",
      keywords = listOf("إحماء", "احماء", "إطالة", "اطالة", "تهدئة", "استشفاء", "warm", "stretch"),
      defaultUnitType = ExerciseUnitType.MINUTES,
      defaultAmountPerSet = 5.0,
      defaultSets = 1,
      ratePerSecondOrRep = 3.5 / 60.0,
      ratePerMinute = 3.5,
    ),
  )

  fun matchAndCalculate(
    exerciseName: String,
    unitType: ExerciseUnitType,
    amountPerSet: Double,
  ): CalorieMatchResult? {
    val normalized = exerciseName.trim().lowercase()
    if (normalized.isEmpty()) return null

    val preset = presets.firstOrNull { candidate ->
      candidate.keywords.any { kw -> normalized.contains(kw) }
    } ?: return null

    val safeAmount = amountPerSet.coerceAtLeast(0.0)
    val calories = when (preset.defaultUnitType) {
      ExerciseUnitType.REPS -> {
        when (unitType) {
          ExerciseUnitType.REPS -> safeAmount * preset.ratePerSecondOrRep
          ExerciseUnitType.SECONDS -> (safeAmount / 3.0) * preset.ratePerSecondOrRep
          ExerciseUnitType.MINUTES -> (safeAmount * 20.0) * preset.ratePerSecondOrRep
        }
      }
      ExerciseUnitType.SECONDS -> {
        when (unitType) {
          ExerciseUnitType.SECONDS -> safeAmount * preset.ratePerSecondOrRep
          ExerciseUnitType.MINUTES -> safeAmount * (preset.ratePerMinute ?: (preset.ratePerSecondOrRep * 60.0))
          ExerciseUnitType.REPS -> safeAmount * 2.0 * preset.ratePerSecondOrRep
        }
      }
      ExerciseUnitType.MINUTES -> {
        val perMin = preset.ratePerMinute ?: (preset.ratePerSecondOrRep * 60.0)
        when (unitType) {
          ExerciseUnitType.MINUTES -> safeAmount * perMin
          ExerciseUnitType.SECONDS -> (safeAmount / 60.0) * perMin
          ExerciseUnitType.REPS -> (safeAmount * 0.5) * perMin
        }
      }
    }

    val estimatedMinutes = estimateMinutesPerSet(unitType, safeAmount)

    return CalorieMatchResult(
      preset = preset,
      caloriesPerSet = round2(calories),
      estimatedMinutesPerSet = round2(estimatedMinutes),
    )
  }

  fun estimateMinutesPerSet(unitType: ExerciseUnitType, amountPerSet: Double): Double {
    val safeAmount = amountPerSet.coerceAtLeast(0.0)
    return when (unitType) {
      ExerciseUnitType.MINUTES -> round2(safeAmount)
      ExerciseUnitType.SECONDS -> round2((safeAmount / 60.0).coerceAtLeast(0.25))
      ExerciseUnitType.REPS -> round2(((safeAmount * 3.0) / 60.0).coerceAtLeast(0.5))
    }
  }

  fun round2(value: Double): Double = (value * 100.0).roundToInt() / 100.0
}

object DefaultWorkoutPlans {
  const val PRIMARY_PLAN_ID = "plan_primary_default"
  const val LIGHT_PLAN_ID = "plan_light_preset"

  val primaryPlan = WorkoutPlan(
    id = PRIMARY_PLAN_ID,
    name = "جدولي الأساسي",
    isBuiltInDefault = true,
    phases = listOf(
      WorkoutPhase(
        id = "phase_1",
        title = "المرحلة 1: الإحماء",
        exercises = listOf(
          ExerciseItem(
            id = "ex_warmup_joints",
            name = "تحريك المفاصل والإطالة الخفيفة",
            unitType = ExerciseUnitType.MINUTES,
            amountPerSet = 5.0,
            setsCount = 1,
            perSetMinutes = 5.0,
            perSetCalories = 20.0,
          )
        ),
      ),
      WorkoutPhase(
        id = "phase_2",
        title = "المرحلة 2: المشي",
        exercises = listOf(
          ExerciseItem(
            id = "ex_brisk_walk",
            name = "مشي سريع ومستمر",
            unitType = ExerciseUnitType.MINUTES,
            amountPerSet = 12.5,
            setsCount = 2,
            perSetMinutes = 12.5,
            perSetCalories = 65.0,
          )
        ),
      ),
      WorkoutPhase(
        id = "phase_3",
        title = "المرحلة 3: الكارديو وحرق السعرات",
        exercises = listOf(
          ExerciseItem(
            id = "ex_high_knees",
            name = "رفع الركبتين في المكان (High Knees)",
            unitType = ExerciseUnitType.SECONDS,
            amountPerSet = 45.0,
            setsCount = 4,
            perSetMinutes = 1.75,
            perSetCalories = 17.5,
          ),
          ExerciseItem(
            id = "ex_plank",
            name = "تمرين البلانك (Plank)",
            unitType = ExerciseUnitType.SECONDS,
            amountPerSet = 60.0,
            setsCount = 4,
            perSetMinutes = 1.5,
            perSetCalories = 7.5,
          ),
          ExerciseItem(
            id = "ex_cardio_squat",
            name = "تمرين القرفصاء (Squat)",
            unitType = ExerciseUnitType.REPS,
            amountPerSet = 15.0,
            setsCount = 4,
            perSetMinutes = 1.75,
            perSetCalories = 12.5,
          ),
        ),
      ),
      WorkoutPhase(
        id = "phase_4",
        title = "المرحلة 4: المقاومة بوزن الجسم",
        exercises = listOf(
          ExerciseItem(
            id = "ex_pushups",
            name = "تمرين الضغط (Push-ups)",
            unitType = ExerciseUnitType.REPS,
            amountPerSet = 12.0,
            setsCount = 3,
            perSetMinutes = 2.6,
            perSetCalories = 13.5,
          ),
          ExerciseItem(
            id = "ex_abs",
            name = "تمرين البطن (Crunches)",
            unitType = ExerciseUnitType.REPS,
            amountPerSet = 15.0,
            setsCount = 3,
            perSetMinutes = 2.6,
            perSetCalories = 11.5,
          ),
          ExerciseItem(
            id = "ex_bodyweight_squat",
            name = "السكوات بوزن الجسم",
            unitType = ExerciseUnitType.REPS,
            amountPerSet = 15.0,
            setsCount = 3,
            perSetMinutes = 3.0,
            perSetCalories = 16.5,
          ),
        ),
      ),
      WorkoutPhase(
        id = "phase_5",
        title = "المرحلة 5: التهدئة والاستشفاء",
        exercises = listOf(
          ExerciseItem(
            id = "ex_cooldown_stretch",
            name = "تمارين إطالة هادئة لتهدئة الجسم",
            unitType = ExerciseUnitType.MINUTES,
            amountPerSet = 5.0,
            setsCount = 1,
            perSetMinutes = 5.0,
            perSetCalories = 15.0,
          )
        ),
      ),
    ),
  )

  val lightPlan = WorkoutPlan(
    id = LIGHT_PLAN_ID,
    name = "جدول خفيف",
    isBuiltInDefault = false,
    phases = listOf(
      WorkoutPhase(
        id = "light_phase_1",
        title = "النشاط الخفيف والتنشيط",
        exercises = listOf(
          ExerciseItem(
            id = "light_warmup",
            name = "إحماء وإطالة لطيفة",
            unitType = ExerciseUnitType.MINUTES,
            amountPerSet = 5.0,
            setsCount = 1,
            perSetMinutes = 5.0,
            perSetCalories = 17.5,
          ),
          ExerciseItem(
            id = "light_walk",
            name = "مشي سريع ومستمر",
            unitType = ExerciseUnitType.MINUTES,
            amountPerSet = 10.0,
            setsCount = 2,
            perSetMinutes = 10.0,
            perSetCalories = 52.0,
          ),
          ExerciseItem(
            id = "light_squats",
            name = "تمرين القرفصاء (Squats)",
            unitType = ExerciseUnitType.REPS,
            amountPerSet = 12.0,
            setsCount = 2,
            perSetMinutes = 1.0,
            perSetCalories = 4.2,
          ),
          ExerciseItem(
            id = "light_plank",
            name = "تمرين البلانك (Plank)",
            unitType = ExerciseUnitType.SECONDS,
            amountPerSet = 30.0,
            setsCount = 2,
            perSetMinutes = 0.5,
            perSetCalories = 2.4,
          ),
          ExerciseItem(
            id = "light_cooldown",
            name = "إطالة واستشفاء",
            unitType = ExerciseUnitType.MINUTES,
            amountPerSet = 4.0,
            setsCount = 1,
            perSetMinutes = 4.0,
            perSetCalories = 14.0,
          ),
        ),
      )
    ),
  )

  val initialPlans: List<WorkoutPlan> = listOf(primaryPlan, lightPlan)
}

object ReportsCalculator {
  private fun newFormat(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }

  fun todayDateKey(nowMillis: Long = System.currentTimeMillis()): String {
    val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    return newFormat().format(cal.time)
  }

  fun buildSummary(
    records: List<DailyHistoryRecord>,
    nowMillis: Long = System.currentTimeMillis(),
  ): ReportsSummary {
    val fmt = newFormat()
    val todayCal = Calendar.getInstance().apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val todayKey = fmt.format(todayCal.time)

    val yesterdayCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterdayKey = fmt.format(yesterdayCal.time)

    // Start of current Saturday-based week
    val dayOfWeek = todayCal.get(Calendar.DAY_OF_WEEK) // Sun=1..Sat=7
    val daysSinceSaturday = (dayOfWeek - Calendar.SATURDAY + 7) % 7
    val thisWeekStart = (todayCal.clone() as Calendar).apply {
      add(Calendar.DAY_OF_YEAR, -daysSinceSaturday)
    }
    val lastWeekStart = (thisWeekStart.clone() as Calendar).apply {
      add(Calendar.DAY_OF_YEAR, -7)
    }

    val currentYear = todayCal.get(Calendar.YEAR)
    val currentMonth = todayCal.get(Calendar.MONTH)
    val prevMonthCal = (todayCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
    val prevMonthYear = prevMonthCal.get(Calendar.YEAR)
    val prevMonth = prevMonthCal.get(Calendar.MONTH)

    var todayMin = 0.0
    var todayKcal = 0.0
    var yestMin = 0.0
    var yestKcal = 0.0

    var curWeekMin = 0.0
    var curWeekKcal = 0.0
    var lastWeekMin = 0.0
    var lastWeekKcal = 0.0

    var curMonthMin = 0.0
    var curMonthKcal = 0.0
    var lastMonthMin = 0.0
    var lastMonthKcal = 0.0

    val sorted = records.sortedByDescending { it.dateKey }
    for (rec in sorted) {
      if (rec.dateKey == todayKey) {
        todayMin += rec.minutes
        todayKcal += rec.calories
      } else if (rec.dateKey == yesterdayKey) {
        yestMin += rec.minutes
        yestKcal += rec.calories
      }

      val parsedDate = try {
        fmt.parse(rec.dateKey)
      } catch (_: Exception) {
        null
      } ?: continue

      val recCal = Calendar.getInstance().apply {
        time = parsedDate
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }

      if (!recCal.before(thisWeekStart)) {
        curWeekMin += rec.minutes
        curWeekKcal += rec.calories
      } else if (!recCal.before(lastWeekStart)) {
        lastWeekMin += rec.minutes
        lastWeekKcal += rec.calories
      }

      val ry = recCal.get(Calendar.YEAR)
      val rm = recCal.get(Calendar.MONTH)
      if (ry == currentYear && rm == currentMonth) {
        curMonthMin += rec.minutes
        curMonthKcal += rec.calories
      } else if (ry == prevMonthYear && rm == prevMonth) {
        lastMonthMin += rec.minutes
        lastMonthKcal += rec.calories
      }
    }

    return ReportsSummary(
      todayVsYesterday = PeriodComparison(todayMin, yestMin, todayKcal, yestKcal),
      thisWeekVsLastWeek = PeriodComparison(curWeekMin, lastWeekMin, curWeekKcal, lastWeekKcal),
      thisMonthVsLastMonth = PeriodComparison(curMonthMin, lastMonthMin, curMonthKcal, lastMonthKcal),
      historyRows = sorted,
    )
  }
}

fun formatStatNumber(value: Double): String {
  val rounded = (value * 100.0).roundToInt() / 100.0
  return if (rounded % 1.0 == 0.0) {
    rounded.toInt().toString()
  } else {
    val oneDec = (rounded * 10.0).roundToInt() / 10.0
    if (kotlin.math.abs(oneDec - rounded) < 0.001) {
      oneDec.toString()
    } else {
      String.format(Locale.US, "%.2f", rounded).trimEnd('0').trimEnd('.')
    }
  }
}

fun formatReminderTimeAr(hour24: Int, minute: Int): String {
  val period = when {
    hour24 < 12 -> "صباحاً"
    hour24 in 12..17 -> "عصراً"
    else -> "مساءً"
  }
  val hour12 = when (val h = hour24 % 12) {
    0 -> 12
    else -> h
  }
  val minStr = minute.toString().padStart(2, '0')
  return "$hour12:$minStr $period"
}

fun setKey(exerciseId: String, setIndex: Int): String = "$exerciseId#$setIndex"
