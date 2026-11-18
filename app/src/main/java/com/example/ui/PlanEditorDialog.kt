package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CalorieEngine
import com.example.model.ExerciseItem
import com.example.model.ExerciseUnitType
import com.example.model.WorkoutPlan
import com.example.model.formatStatNumber

@Composable
fun PlanEditorDialog(
  initialPlan: WorkoutPlan?,
  onSave: (editingPlanId: String?, planName: String, exercises: List<ExerciseItem>) -> Unit,
  onDismiss: () -> Unit,
) {
  var planName by remember(initialPlan) {
    mutableStateOf(initialPlan?.name ?: "")
  }
  val exercises = remember(initialPlan) {
    mutableStateListOf<ExerciseItem>().apply {
      if (initialPlan != null) {
        addAll(initialPlan.allExercises)
      }
    }
  }

  // Exercise builder fields
  var exName by remember { mutableStateOf("") }
  var exUnit by remember { mutableStateOf(ExerciseUnitType.REPS) }
  var exSetsText by remember { mutableStateOf("3") }
  var exAmountText by remember { mutableStateOf("12") }
  var manualCaloriesText by remember { mutableStateOf("10") }
  var manualMinutesText by remember { mutableStateOf("") }
  var nameError by remember { mutableStateOf(false) }

  val parsedSets = exSetsText.toIntOrNull()?.coerceIn(1, 20) ?: 1
  val parsedAmount = exAmountText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
  val calorieMatch = remember(exName, exUnit, parsedAmount) {
    CalorieEngine.matchAndCalculate(exName, exUnit, parsedAmount)
  }

  val autoMinutesPerSet = CalorieEngine.estimateMinutesPerSet(exUnit, parsedAmount)
  val effectiveMinutesPerSet =
    manualMinutesText.toDoubleOrNull()?.takeIf { it > 0.0 }
      ?: calorieMatch?.estimatedMinutesPerSet
      ?: autoMinutesPerSet
  val effectiveCaloriesPerSet =
    calorieMatch?.caloriesPerSet
      ?: (manualCaloriesText.toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0)

  val totalDraftMinutes = exercises.sumOf { it.totalMinutes }
  val totalDraftCalories = exercises.sumOf { it.totalCalories }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
          Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = onDismiss,
                  modifier = Modifier.size(48.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق",
                  )
                }
                Text(
                  text = if (initialPlan == null) "تصميم جدول جديد" else "تعديل الجدول",
                  style = MaterialTheme.typography.titleLarge,
                )
              }

              Button(
                onClick = {
                  if (exercises.isNotEmpty()) {
                    onSave(
                      initialPlan?.id,
                      planName.ifBlank { "جدولي المخصص" },
                      exercises.toList(),
                    )
                  }
                },
                enabled = exercises.isNotEmpty(),
                modifier = Modifier
                  .height(44.dp)
                  .testTag("save_custom_plan_button"),
              ) {
                Text("حفظ الجدول")
              }
            }
          }
        },
      ) { innerPadding ->
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .navigationBarsPadding(),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .widthIn(max = 640.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            item(key = "plan_name_section") {
              OutlinedTextField(
                value = planName,
                onValueChange = { planName = it },
                label = { Text("اسم الجدول (مثلاً: تمرين الصباح)") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("plan_name_input"),
              )
            }

            item(key = "add_exercise_card") {
              Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(
                  modifier = Modifier.padding(14.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  Text(
                    text = "إضافة تمرين جديد",
                    style = MaterialTheme.typography.titleMedium,
                  )

                  Text(
                    text = "اختر تمريناً شائعاً أو اكتب اسم تمرينك:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )

                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    CalorieEngine.presets.forEach { preset ->
                      SuggestionChip(
                        onClick = {
                          exName = preset.displayName
                          exUnit = preset.defaultUnitType
                          exSetsText = preset.defaultSets.toString()
                          exAmountText = formatStatNumber(preset.defaultAmountPerSet)
                          manualMinutesText = ""
                          nameError = false
                        },
                        label = {
                          Text(
                            text = preset.displayName.substringBefore(" ("),
                            style = MaterialTheme.typography.labelMedium,
                          )
                        },
                      )
                    }
                  }

                  OutlinedTextField(
                    value = exName,
                    onValueChange = {
                      exName = it
                      nameError = false
                    },
                    label = { Text("اسم التمرين") },
                    isError = nameError,
                    supportingText = if (nameError) {
                      { Text("أدخل اسم التمرين أولاً") }
                    } else null,
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("exercise_name_input"),
                  )

                  // Unit type selector
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    ExerciseUnitType.entries.forEach { unit ->
                      FilterChip(
                        selected = exUnit == unit,
                        onClick = {
                          exUnit = unit
                          manualMinutesText = ""
                          if (unit == ExerciseUnitType.SECONDS && exAmountText == "12") {
                            exAmountText = "45"
                          } else if (unit == ExerciseUnitType.MINUTES && (exAmountText == "12" || exAmountText == "45")) {
                            exAmountText = "10"
                          } else if (unit == ExerciseUnitType.REPS && (exAmountText == "45" || exAmountText == "10")) {
                            exAmountText = "15"
                          }
                        },
                        label = { Text(unit.labelAr) },
                        modifier = Modifier.weight(1f),
                      )
                    }
                  }

                  // Sets and Amount per set
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                    OutlinedTextField(
                      value = exSetsText,
                      onValueChange = { input ->
                        exSetsText = input.filter { it.isDigit() }.take(2)
                      },
                      label = { Text("عدد الجولات") },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                      singleLine = true,
                      shape = MaterialTheme.shapes.small,
                      modifier = Modifier
                        .weight(1f)
                        .testTag("exercise_sets_input"),
                    )
                    OutlinedTextField(
                      value = exAmountText,
                      onValueChange = { input ->
                        exAmountText = input.filter { it.isDigit() || it == '.' }.take(6)
                      },
                      label = {
                        Text(
                          when (exUnit) {
                            ExerciseUnitType.REPS -> "التكرارات / جولة"
                            ExerciseUnitType.SECONDS -> "الثواني / جولة"
                            ExerciseUnitType.MINUTES -> "الدقائق / جولة"
                          }
                        )
                      },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                      singleLine = true,
                      shape = MaterialTheme.shapes.small,
                      modifier = Modifier
                        .weight(1f)
                        .testTag("exercise_amount_input"),
                    )
                  }

                  // Calorie engine status or manual entry
                  if (calorieMatch != null) {
                    Surface(
                      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                      shape = MaterialTheme.shapes.small,
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                      ) {
                        Icon(
                          imageVector = Icons.Default.AutoAwesome,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(18.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = "محسوب تلقائياً: ${formatStatNumber(effectiveCaloriesPerSet)} سعرة / جولة",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                          )
                          Text(
                            text = "إجمالي التمرين ($parsedSets جولات): ${formatStatNumber(effectiveCaloriesPerSet * parsedSets)} سعرة • ${formatStatNumber(effectiveMinutesPerSet * parsedSets)} د",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                          )
                        }
                      }
                    }
                  } else {
                    Text(
                      text = "تمرين مخصص غير مدرج في القائمة — أدخل السعرات والوقت التقريبي للجولة:",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.secondary,
                    )
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                      OutlinedTextField(
                        value = manualCaloriesText,
                        onValueChange = { input ->
                          manualCaloriesText = input.filter { it.isDigit() || it == '.' }.take(6)
                        },
                        label = { Text("السعرات / جولة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                          .weight(1f)
                          .testTag("manual_calories_input"),
                      )
                      OutlinedTextField(
                        value = manualMinutesText.ifEmpty { formatStatNumber(autoMinutesPerSet) },
                        onValueChange = { input ->
                          manualMinutesText = input.filter { it.isDigit() || it == '.' }.take(5)
                        },
                        label = { Text("الدقائق / جولة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f),
                      )
                    }
                  }

                  OutlinedButton(
                    onClick = {
                      if (exName.isBlank()) {
                        nameError = true
                        return@OutlinedButton
                      }
                      exercises.add(
                        ExerciseItem(
                          id = "custom_ex_${System.currentTimeMillis()}_${exercises.size}",
                          name = exName.trim(),
                          unitType = exUnit,
                          amountPerSet = parsedAmount.coerceAtLeast(1.0),
                          setsCount = parsedSets,
                          perSetMinutes = effectiveMinutesPerSet,
                          perSetCalories = effectiveCaloriesPerSet,
                          isAutoCalories = calorieMatch != null,
                        )
                      )
                      exName = ""
                      manualMinutesText = ""
                      nameError = false
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(48.dp)
                      .testTag("add_exercise_to_plan_button"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة التمرين للجدول")
                  }
                }
              }
            }

            if (exercises.isNotEmpty()) {
              item(key = "added_header") {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "تمارين الجدول (${exercises.size})",
                    style = MaterialTheme.typography.titleMedium,
                  )
                  Text(
                    text = "${formatStatNumber(totalDraftMinutes)} د • ${formatStatNumber(totalDraftCalories)} سعرة",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
              }

              itemsIndexed(
                items = exercises,
                key = { _, item -> item.id },
              ) { index, item ->
                Card(
                  shape = MaterialTheme.shapes.small,
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                      )
                      Text(
                        text = "${item.setsCount} جولات × ${formatStatNumber(item.amountPerSet)} ${item.unitType.labelAr} • ${formatStatNumber(item.totalMinutes)} د • ${formatStatNumber(item.totalCalories)} سعرة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    IconButton(
                      onClick = {
                        // Load back into editor for quick adjustment
                        exName = item.name
                        exUnit = item.unitType
                        exSetsText = item.setsCount.toString()
                        exAmountText = formatStatNumber(item.amountPerSet)
                        if (!item.isAutoCalories) {
                          manualCaloriesText = formatStatNumber(item.perSetCalories)
                          manualMinutesText = formatStatNumber(item.perSetMinutes)
                        }
                        exercises.removeAt(index)
                      },
                      modifier = Modifier.size(48.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل التمرين",
                        tint = MaterialTheme.colorScheme.primary,
                      )
                    }
                    IconButton(
                      onClick = { exercises.removeAt(index) },
                      modifier = Modifier.size(48.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف التمرين",
                        tint = MaterialTheme.colorScheme.error,
                      )
                    }
                  }
                }
              }
            } else {
              item(key = "empty_draft_hint") {
                Surface(
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                  ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                    Text(
                      text = "أضف تمريناً واحداً على الأقل لحفظ الجدول",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
