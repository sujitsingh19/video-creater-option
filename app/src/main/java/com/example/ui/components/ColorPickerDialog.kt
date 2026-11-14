package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val PresetColors = listOf(
    Color(0xFFFFFF00), // Quiz Yellow
    Color(0xFF1E00FF), // Royal Text Blue
    Color(0xFF0000FF), // Deep Option Blue
    Color(0xFF00AEEF), // Sky Cyan
    Color(0xFFFFFFFF), // White
    Color(0xFF0F172A), // Midnight Slate
    Color(0xFFDC2626), // Crimson Red
    Color(0xFF16A34A), // Emerald Green
    Color(0xFF7C3AED), // Violet
    Color(0xFFEA580C), // Bright Orange
    Color(0xFFEC4899), // Magenta
    Color(0xFF000000), // Pure Black
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    title: String,
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit,
) {
    var r by remember { mutableFloatStateOf(initialColor.red) }
    var g by remember { mutableFloatStateOf(initialColor.green) }
    var b by remember { mutableFloatStateOf(initialColor.blue) }
    val currentColor = Color(r, g, b, 1f)
    var hexInput by remember(currentColor) {
        mutableStateOf(String.format("#%06X", 0xFFFFFF and currentColor.toArgb()))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(currentColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PresetColors.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(preset)
                                .border(
                                    width = if (preset.toArgb() == currentColor.toArgb()) 2.5.dp else 1.dp,
                                    color = if (preset.toArgb() == currentColor.toArgb()) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    r = preset.red
                                    g = preset.green
                                    b = preset.blue
                                }
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    RgbSliderRow("R", r, Color(0xFFEF4444)) { r = it }
                    RgbSliderRow("G", g, Color(0xFF22C55E)) { g = it }
                    RgbSliderRow("B", b, Color(0xFF3B82F6)) { b = it }
                }

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input
                        val cleaned = input.trim().removePrefix("#")
                        if (cleaned.length == 6) {
                            cleaned.toLongOrNull(16)?.let { rgb ->
                                val parsed = Color((0xFF000000L or rgb).toInt())
                                r = parsed.red
                                g = parsed.green
                                b = parsed.blue
                            }
                        }
                    },
                    label = { Text("Hex Color (#RRGGBB)") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("color_hex_input")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onColorSelected(currentColor) },
                modifier = Modifier.testTag("color_apply_button")
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RgbSliderRow(
    label: String,
    value: Float,
    tint: Color,
    onValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label ${(value * 255).toInt()}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.size(width = 44.dp, height = 16.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(thumbColor = tint, activeTrackColor = tint),
            modifier = Modifier.weight(1f)
        )
    }
}
