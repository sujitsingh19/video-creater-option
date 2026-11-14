package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StudioDarkColorScheme = darkColorScheme(
    primary = StudioGold,
    onPrimary = StudioOnGold,
    primaryContainer = Color(0xFF423600),
    onPrimaryContainer = Color(0xFFFDE047),
    secondary = StudioIndigo,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF312E81),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = StudioEmerald,
    onTertiary = Color.White,
    background = StudioBg,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    error = StudioCrimson,
    onError = Color.White,
)

private val StudioShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = StudioDarkColorScheme,
        typography = Typography,
        shapes = StudioShapes,
        content = content
    )
}
