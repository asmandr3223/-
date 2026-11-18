package com.example

import com.example.model.CalorieEngine
import com.example.model.DailyHistoryRecord
import com.example.model.DefaultWorkoutPlans
import com.example.model.ExerciseUnitType
import com.example.model.ReportsCalculator
import com.example.model.formatReminderTimeAr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class WorkoutEngineTest {

  @Test
  fun calorieEngine_calculatesExactRatesForAllKnownExercises() {
    val pushups = CalorieEngine.matchAndCalculate("تمرين الضغط", ExerciseUnitType.REPS, 10.0)
    assertNotNull(pushups)
    assertEquals(4.5, pushups!!.caloriesPerSet, 0.01)

    val squats = CalorieEngine.matchAndCalculate("Squats", ExerciseUnitType.REPS, 20.0)
    assertNotNull(squats)
    assertEquals(7.0, squats!!.caloriesPerSet, 0.01)

    val crunches = CalorieEngine.matchAndCalculate("تمرين البطن", ExerciseUnitType.REPS, 20.0)
    assertNotNull(crunches)
    assertEquals(5.0, crunches!!.caloriesPerSet, 0.01)

    val burpees = CalorieEngine.matchAndCalculate("بربيز", ExerciseUnitType.REPS, 10.0)
    assertNotNull(burpees)
    assertEquals(12.0, burpees!!.caloriesPerSet, 0.01)

    val highKnees = CalorieEngine.matchAndCalculate("رفع الركبتين", ExerciseUnitType.SECONDS, 45.0)
    assertNotNull(highKnees)
    assertEquals(6.75, highKnees!!.caloriesPerSet, 0.01)

    val plankMin = CalorieEngine.matchAndCalculate("البلانك", ExerciseUnitType.MINUTES, 1.0)
    assertNotNull(plankMin)
    assertEquals(5.0, plankMin!!.caloriesPerSet, 0.01)

    val plankSec = CalorieEngine.matchAndCalculate("Plank", ExerciseUnitType.SECONDS, 60.0)
    assertNotNull(plankSec)
    assertEquals(4.8, plankSec!!.caloriesPerSet, 0.01)

    val jumpRope = CalorieEngine.matchAndCalculate("نط الحبل", ExerciseUnitType.SECONDS, 60.0)
    assertNotNull(jumpRope)
    assertEquals(12.0, jumpRope!!.caloriesPerSet, 0.01)

    val walking = CalorieEngine.matchAndCalculate("مشي سريع", ExerciseUnitType.MINUTES, 10.0)
    assertNotNull(walking)
    assertEquals(52.0, walking!!.caloriesPerSet, 0.01)

    val stretch = CalorieEngine.matchAndCalculate("إطالة", ExerciseUnitType.MINUTES, 5.0)
    assertNotNull(stretch)
    assertEquals(17.5, stretch!!.caloriesPerSet, 0.01)
  }

  @Test
  fun calorieEngine_returnsNullForUnknownExercise() {
    val unknown = CalorieEngine.matchAndCalculate("تمرين العقلة", ExerciseUnitType.REPS, 8.0)
    assertNull(unknown)
  }

  @Test
  fun defaultPrimaryPlan_hasExactPerSetBreakdown() {
    val plan = DefaultWorkoutPlans.primaryPlan
    assertEquals(5, plan.phases.size)
    assertEquals(9, plan.allExercises.size)
    assertEquals(25, plan.totalSets)
    assertEquals(79.6, plan.totalMinutes, 0.01)
    assertEquals(439.5, plan.totalCalories, 0.01)
  }

  @Test
  fun reportsCalculator_computesDayWeekMonthComparisons() {
    val refCal = Calendar.getInstance().apply {
      set(2026, Calendar.NOVEMBER, 17, 10, 0, 0) // Tuesday
      set(Calendar.MILLISECOND, 0)
    }
    val records = listOf(
      DailyHistoryRecord("2026-11-17", "p1", "جدولي الأساسي", 10, 25, 40.0, 220.0, 50),
      DailyHistoryRecord("2026-11-16", "p1", "جدولي الأساسي", 8, 25, 30.0, 150.0, 38),
      DailyHistoryRecord("2026-11-10", "p1", "جدولي الأساسي", 25, 25, 80.0, 440.0, 100),
      DailyHistoryRecord("2026-10-20", "p1", "جدولي الأساسي", 20, 25, 60.0, 300.0, 75),
    )

    val summary = ReportsCalculator.buildSummary(records, refCal.timeInMillis)
    assertEquals(40.0, summary.todayVsYesterday.currentMinutes, 0.01)
    assertEquals(30.0, summary.todayVsYesterday.previousMinutes, 0.01)
    assertEquals(70.0, summary.todayVsYesterday.caloriesDelta, 0.01)

    assertEquals(70.0, summary.thisWeekVsLastWeek.currentMinutes, 0.01)
    assertEquals(80.0, summary.thisWeekVsLastWeek.previousMinutes, 0.01)

    assertEquals(150.0, summary.thisMonthVsLastMonth.currentMinutes, 0.01)
    assertEquals(60.0, summary.thisMonthVsLastMonth.previousMinutes, 0.01)
    assertEquals(4, summary.historyRows.size)
  }

  @Test
  fun formatReminderTimeAr_formatsCorrectly() {
    assertEquals("5:00 عصراً", formatReminderTimeAr(17, 0))
    assertEquals("8:30 صباحاً", formatReminderTimeAr(8, 30))
    assertEquals("9:15 مساءً", formatReminderTimeAr(21, 15))
  }
}
