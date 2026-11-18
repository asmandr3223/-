package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AthleticDarkColorScheme = darkColorScheme(
  primary = CyanPrimary,
  onPrimary = CyanOnPrimary,
  primaryContainer = CyanPrimaryContainer,
  onPrimaryContainer = CyanOnPrimaryContainer,
  secondary = OrangeAccent,
  onSecondary = OrangeOnAccent,
  secondaryContainer = OrangeContainer,
  onSecondaryContainer = OrangeOnContainer,
  tertiary = EmeraldTertiary,
  tertiaryContainer = EmeraldContainer,
  onTertiaryContainer = EmeraldOnContainer,
  background = AthleticBackground,
  onBackground = AthleticOnSurface,
  surface = AthleticSurface,
  onSurface = AthleticOnSurface,
  surfaceVariant = AthleticSurfaceVariant,
  onSurfaceVariant = AthleticOnSurfaceVariant,
  outline = AthleticOutline,
  error = Color(0xFFF87171),
  onError = Color(0xFF450A0A),
)

private val AppShapes = Shapes(
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(22.dp),
)

@Composable
fun WorkoutTrackerTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = AthleticDarkColorScheme,
    typography = Typography,
    shapes = AppShapes,
    content = content,
  )
}
