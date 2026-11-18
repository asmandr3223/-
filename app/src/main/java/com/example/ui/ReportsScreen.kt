package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.PeriodComparison
import com.example.model.ReportsSummary
import com.example.model.formatStatNumber

@Composable
fun ReportsTopBar() {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        modifier = Modifier
          .widthIn(max = 640.dp)
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Icon(
          imageVector = Icons.Default.BarChart,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(26.dp),
        )
        Column {
          Text(
            text = "الإحصائيات والتقارير الشاملة",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "مقارنة الأداء اليومي والأسبوعي والشهري وسجل التمارين",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
fun ReportsTabContent(
  summary: ReportsSummary,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 640.dp)
        .testTag("reports_list"),
      contentPadding = PaddingValues(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item(key = "cmp_today_yesterday") {
        ComparisonCard(
          title = "مقارنة اليوم بأمس",
          currentLabel = "اليوم",
          previousLabel = "أمس",
          icon = Icons.Default.Today,
          comparison = summary.todayVsYesterday,
          testTag = "comparison_today_yesterday",
        )
      }

      item(key = "cmp_week") {
        ComparisonCard(
          title = "الأسبوع الحالي بالأسبوع الماضي",
          currentLabel = "هذا الأسبوع",
          previousLabel = "الأسبوع الماضي",
          icon = Icons.Default.DateRange,
          comparison = summary.thisWeekVsLastWeek,
          testTag = "comparison_week",
        )
      }

      item(key = "cmp_month") {
        ComparisonCard(
          title = "الشهر الحالي بالشهر الماضي",
          currentLabel = "هذا الشهر",
          previousLabel = "الشهر الماضي",
          icon = Icons.Default.CalendarMonth,
          comparison = summary.thisMonthVsLastMonth,
          testTag = "comparison_month",
        )
      }

      item(key = "history_table") {
        ComprehensiveDataTableCard(summary = summary)
      }
    }
  }
}

@Composable
private fun ComparisonCard(
  title: String,
  currentLabel: String,
  previousLabel: String,
  icon: ImageVector,
  comparison: PeriodComparison,
  testTag: String,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier.size(34.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        PeriodStatColumn(
          label = currentLabel,
          minutes = comparison.currentMinutes,
          calories = comparison.currentCalories,
          isHighlighted = true,
          modifier = Modifier.weight(1f),
        )
        PeriodStatColumn(
          label = previousLabel,
          minutes = comparison.previousMinutes,
          calories = comparison.previousCalories,
          isHighlighted = false,
          modifier = Modifier.weight(1f),
        )
      }

      // Difference strip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        DeltaBadge(
          label = "فرق الدقائق",
          delta = comparison.minutesDelta,
          unit = "د",
          modifier = Modifier.weight(1f),
        )
        DeltaBadge(
          label = "فرق السعرات",
          delta = comparison.caloriesDelta,
          unit = "سعرة",
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun PeriodStatColumn(
  label: String,
  minutes: Double,
  calories: Double,
  isHighlighted: Boolean,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = if (isHighlighted) {
      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    },
    shape = MaterialTheme.shapes.small,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "${formatStatNumber(minutes)} دقيقة",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "${formatStatNumber(calories)} سعرة",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary,
      )
    }
  }
}

@Composable
private fun DeltaBadge(
  label: String,
  delta: Double,
  unit: String,
  modifier: Modifier = Modifier,
) {
  val isPositive = delta > 0.01
  val isNegative = delta < -0.01
  val color = when {
    isPositive -> MaterialTheme.colorScheme.tertiary
    isNegative -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }
  val prefix = if (isPositive) "+" else ""

  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    shape = RoundedCornerShape(8.dp),
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        if (isPositive) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp),
          )
        } else if (isNegative) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp),
          )
        }
        Text(
          text = "$prefix${formatStatNumber(delta)} $unit",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = color,
        )
      }
    }
  }
}

@Composable
private fun ComprehensiveDataTableCard(summary: ReportsSummary) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("comprehensive_data_table"),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = "جدول البيانات الشامل",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      )

      // Table Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TableHeaderCell("التاريخ", Modifier.weight(1.25f))
        TableHeaderCell("الجدول", Modifier.weight(1.3f))
        TableHeaderCell("الجولات", Modifier.weight(0.8f))
        TableHeaderCell("الدقائق", Modifier.weight(0.75f))
        TableHeaderCell("السعرات", Modifier.weight(0.8f))
        TableHeaderCell("الإنجاز", Modifier.weight(0.7f))
      }

      if (summary.historyRows.isEmpty()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "لا توجد سجلات تمارين بعد",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "أكمل أي جولة في شاشة التمارين ليتم تسجيلها وحساب المقارنات تلقائياً.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )
        }
      } else {
        summary.historyRows.forEachIndexed { index, row ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = row.dateKey,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1.25f),
            )
            Text(
              text = row.planName,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1.3f),
            )
            Text(
              text = "${row.completedSets}/${row.totalSets}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              modifier = Modifier.weight(0.8f),
            )
            Text(
              text = formatStatNumber(row.minutes),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Center,
              modifier = Modifier.weight(0.75f),
            )
            Text(
              text = formatStatNumber(row.calories),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.secondary,
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Center,
              modifier = Modifier.weight(0.8f),
            )
            Text(
              text = "${row.completionPercent}%",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.tertiary,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              modifier = Modifier.weight(0.7f),
            )
          }
          if (index < summary.historyRows.lastIndex) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
          }
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
    }
  }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurface,
    textAlign = TextAlign.Start,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = modifier,
  )
}
