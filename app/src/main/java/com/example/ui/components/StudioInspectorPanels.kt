package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InspectorTab
import com.example.model.QuizQuestion
import com.example.model.SlideStyleConfig
import com.example.ui.theme.SlideFontOption
import kotlin.math.roundToInt

private enum class ColorTarget(val label: String) {
    QUESTION_BG("Question Box Background"),
    QUESTION_TEXT("Question Text Color"),
    OPTION_BG("Options Background"),
    OPTION_TEXT("Options Text Color"),
    CANVAS_BG("Canvas Background Fallback"),
}

@Composable
fun StudioInspectorPanel(
    activeTab: InspectorTab,
    question: QuizQuestion,
    totalSlides: Int,
    style: SlideStyleConfig,
    onSelectTab: (InspectorTab) -> Unit,
    onUpdateQuestion: (String?, String?, String?, String?, String?, String?) -> Unit,
    onAddSlide: () -> Unit,
    onDeleteSlide: () -> Unit,
    onUpdateStyle: ((SlideStyleConfig) -> SlideStyleConfig) -> Unit,
    onResetTimerPosition: () -> Unit,
    onResetLogoPosition: () -> Unit,
    onLaunchBackgroundPicker: () -> Unit,
    onLaunchLogoPicker: () -> Unit,
    onLaunchTimerVideoPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Tab Switcher Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InspectorTab.entries.forEach { tab ->
                val selected = tab == activeTab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectTab(tab) }
                        .testTag("inspector_tab_${tab.name.lowercase()}"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (activeTab) {
                InspectorTab.SLIDE -> EditSlideTab(
                    question = question,
                    totalSlides = totalSlides,
                    onUpdateQuestion = onUpdateQuestion,
                    onAddSlide = onAddSlide,
                    onDeleteSlide = onDeleteSlide
                )

                InspectorTab.TYPOGRAPHY -> TypographyAndSizeTab(
                    style = style,
                    onUpdateStyle = onUpdateStyle
                )

                InspectorTab.COLORS -> ColorsTab(
                    style = style,
                    onUpdateStyle = onUpdateStyle
                )

                InspectorTab.OVERLAYS -> MediaAndOverlaysTab(
                    style = style,
                    onUpdateStyle = onUpdateStyle,
                    onResetTimerPosition = onResetTimerPosition,
                    onResetLogoPosition = onResetLogoPosition,
                    onLaunchBackgroundPicker = onLaunchBackgroundPicker,
                    onLaunchLogoPicker = onLaunchLogoPicker,
                    onLaunchTimerVideoPicker = onLaunchTimerVideoPicker
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EditSlideTab(
    question: QuizQuestion,
    totalSlides: Int,
    onUpdateQuestion: (String?, String?, String?, String?, String?, String?) -> Unit,
    onAddSlide: () -> Unit,
    onDeleteSlide: () -> Unit,
) {
    InspectorCard(title = "Edit Current Slide") {
        OutlinedTextField(
            value = question.question,
            onValueChange = { onUpdateQuestion(it, null, null, null, null, null) },
            label = { Text("Question Text") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_question_input")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = question.options.getOrElse(0) { "" },
                onValueChange = { onUpdateQuestion(null, it, null, null, null, null) },
                label = { Text("Option A") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("edit_opt_a")
            )
            OutlinedTextField(
                value = question.options.getOrElse(1) { "" },
                onValueChange = { onUpdateQuestion(null, null, it, null, null, null) },
                label = { Text("Option B") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("edit_opt_b")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = question.options.getOrElse(2) { "" },
                onValueChange = { onUpdateQuestion(null, null, null, it, null, null) },
                label = { Text("Option C") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("edit_opt_c")
            )
            OutlinedTextField(
                value = question.options.getOrElse(3) { "" },
                onValueChange = { onUpdateQuestion(null, null, null, null, it, null) },
                label = { Text("Option D") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("edit_opt_d")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Correct Answer:", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("A", "B", "C", "D").forEach { letter ->
                    val selected = question.answerLetter.uppercase() == letter
                    FilterChip(
                        selected = selected,
                        onClick = { onUpdateQuestion(null, null, null, null, null, letter) },
                        label = { Text(letter, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF16A34A),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("answer_chip_$letter")
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onAddSlide,
                modifier = Modifier
                    .weight(1f)
                    .testTag("add_slide_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("Add Slide")
            }
            OutlinedButton(
                onClick = onDeleteSlide,
                enabled = totalSlides > 1,
                modifier = Modifier
                    .weight(1f)
                    .testTag("delete_slide_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("Delete Slide")
            }
        }
    }
}

@Composable
private fun TypographyAndSizeTab(
    style: SlideStyleConfig,
    onUpdateStyle: ((SlideStyleConfig) -> SlideStyleConfig) -> Unit,
) {
    InspectorCard(title = "Font Family Controls") {
        FontDropdownField(
            label = "Question Text Font",
            selectedFont = style.questionFont,
            onFontSelected = { font -> onUpdateStyle { it.copy(questionFont = font) } },
            testTag = "question_font_dropdown"
        )
        FontDropdownField(
            label = "Option Text Font",
            selectedFont = style.optionFont,
            onFontSelected = { font -> onUpdateStyle { it.copy(optionFont = font) } },
            testTag = "option_font_dropdown"
        )
    }

    InspectorCard(title = "Size & Layout Controls") {
        LabeledSlider(
            label = "Question Max Size",
            value = style.questionMaxFontSize,
            valueRange = 14f..70f,
            unit = "px",
            onValueChange = { v -> onUpdateStyle { it.copy(questionMaxFontSize = v) } }
        )
        LabeledSlider(
            label = "Option Text Size",
            value = style.optionFontSize,
            valueRange = 12f..45f,
            unit = "px",
            onValueChange = { v -> onUpdateStyle { it.copy(optionFontSize = v) } }
        )
        LabeledSlider(
            label = "Question Box Height",
            value = style.questionBoxHeightPercent,
            valueRange = 15f..50f,
            unit = "%",
            onValueChange = { v -> onUpdateStyle { it.copy(questionBoxHeightPercent = v) } }
        )
        LabeledSlider(
            label = "Question Box Width",
            value = style.questionBoxWidthPercent,
            valueRange = 40f..100f,
            unit = "%",
            onValueChange = { v -> onUpdateStyle { it.copy(questionBoxWidthPercent = v) } }
        )
        LabeledSlider(
            label = "Options Block Width",
            value = style.optionsBlockWidthPercent,
            valueRange = 40f..100f,
            unit = "%",
            onValueChange = { v -> onUpdateStyle { it.copy(optionsBlockWidthPercent = v) } }
        )
        LabeledSlider(
            label = "Single Option Height",
            value = style.singleOptionHeightPercent,
            valueRange = 12f..35f,
            unit = "%",
            onValueChange = { v -> onUpdateStyle { it.copy(singleOptionHeightPercent = v) } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorsTab(
    style: SlideStyleConfig,
    onUpdateStyle: ((SlideStyleConfig) -> SlideStyleConfig) -> Unit,
) {
    var activeColorTarget by remember { mutableStateOf<ColorTarget?>(null) }

    InspectorCard(title = "Color Customizer") {
        ColorRowItem(
            label = "Question Box Bg",
            color = style.questionBgColor,
            onClick = { activeColorTarget = ColorTarget.QUESTION_BG }
        )
        ColorRowItem(
            label = "Question Text Color",
            color = style.questionTextColor,
            onClick = { activeColorTarget = ColorTarget.QUESTION_TEXT }
        )
        ColorRowItem(
            label = "Options Bg",
            color = style.optionBgColor,
            onClick = { activeColorTarget = ColorTarget.OPTION_BG }
        )
        ColorRowItem(
            label = "Options Text Color",
            color = style.optionTextColor,
            onClick = { activeColorTarget = ColorTarget.OPTION_TEXT }
        )
        ColorRowItem(
            label = "Canvas Background Fallback",
            color = style.canvasBgColor,
            onClick = { activeColorTarget = ColorTarget.CANVAS_BG }
        )
    }

    InspectorCard(title = "Quick Broadcast Themes") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = {
                onUpdateStyle {
                    it.copy(
                        canvasBgColor = Color(0xFF00AEEF),
                        questionBgColor = Color(0xFFFFFF00),
                        questionTextColor = Color(0xFF1E00FF),
                        optionBgColor = Color(0xFF0000FF),
                        optionTextColor = Color.White
                    )
                }
            }) { Text("Classic Quiz", fontSize = 12.sp) }

            OutlinedButton(onClick = {
                onUpdateStyle {
                    it.copy(
                        canvasBgColor = Color(0xFF0F172A),
                        questionBgColor = Color(0xFFFACC15),
                        questionTextColor = Color(0xFF0F172A),
                        optionBgColor = Color(0xFF1E293B),
                        optionTextColor = Color(0xFFF8FAFC)
                    )
                }
            }) { Text("Midnight Gold", fontSize = 12.sp) }

            OutlinedButton(onClick = {
                onUpdateStyle {
                    it.copy(
                        canvasBgColor = Color(0xFF311042),
                        questionBgColor = Color(0xFFFFFFFF),
                        questionTextColor = Color(0xFF311042),
                        optionBgColor = Color(0xFF6D28D9),
                        optionTextColor = Color.White
                    )
                }
            }) { Text("Royal Violet", fontSize = 12.sp) }
        }
    }

    activeColorTarget?.let { target ->
        val initial = when (target) {
            ColorTarget.QUESTION_BG -> style.questionBgColor
            ColorTarget.QUESTION_TEXT -> style.questionTextColor
            ColorTarget.OPTION_BG -> style.optionBgColor
            ColorTarget.OPTION_TEXT -> style.optionTextColor
            ColorTarget.CANVAS_BG -> style.canvasBgColor
        }
        ColorPickerDialog(
            title = target.label,
            initialColor = initial,
            onDismiss = { activeColorTarget = null },
            onColorSelected = { chosen ->
                onUpdateStyle { current ->
                    when (target) {
                        ColorTarget.QUESTION_BG -> current.copy(questionBgColor = chosen)
                        ColorTarget.QUESTION_TEXT -> current.copy(questionTextColor = chosen)
                        ColorTarget.OPTION_BG -> current.copy(optionBgColor = chosen)
                        ColorTarget.OPTION_TEXT -> current.copy(optionTextColor = chosen)
                        ColorTarget.CANVAS_BG -> current.copy(canvasBgColor = chosen)
                    }
                }
                activeColorTarget = null
            }
        )
    }
}

@Composable
private fun MediaAndOverlaysTab(
    style: SlideStyleConfig,
    onUpdateStyle: ((SlideStyleConfig) -> SlideStyleConfig) -> Unit,
    onResetTimerPosition: () -> Unit,
    onResetLogoPosition: () -> Unit,
    onLaunchBackgroundPicker: () -> Unit,
    onLaunchLogoPicker: () -> Unit,
    onLaunchTimerVideoPicker: () -> Unit,
) {
    InspectorCard(title = "Slide Background Image") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onLaunchBackgroundPicker,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED), contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .testTag("upload_bg_button")
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text(if (style.backgroundImageUri == null) "Upload Background" else "Change Background")
            }
            if (style.backgroundImageUri != null) {
                IconButton(onClick = { onUpdateStyle { it.copy(backgroundImageUri = null) } }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear background image")
                }
            }
        }
    }

    InspectorCard(title = "Logo Controls (Drag on Slide)") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onLaunchLogoPicker,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777), contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .testTag("upload_logo_button")
            ) {
                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text(if (style.logoImageUri == null) "Upload Logo" else "Change Logo")
            }
            if (style.logoImageUri != null) {
                IconButton(onClick = { onUpdateStyle { it.copy(logoImageUri = null) } }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove logo")
                }
            }
        }
        LabeledSlider(
            label = "Logo Size",
            value = style.logoSizePx,
            valueRange = 40f..300f,
            unit = "px",
            onValueChange = { v -> onUpdateStyle { it.copy(logoSizePx = v) } }
        )
        OutlinedButton(
            onClick = onResetLogoPosition,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
            Text("Reset Logo Position")
        }
        Text(
            text = "Logo timer khatam hone ke baad automatic screen par dikhega. Drag karke position set karein.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    InspectorCard(title = "Timer Video Settings (Drag on Slide)") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onLaunchTimerVideoPicker,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                modifier = Modifier
                    .weight(1f)
                    .testTag("upload_timer_video_button")
            ) {
                Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text(if (style.timerVideoUri == null) "Upload Timer Video" else "Change Timer Video")
            }
            if (style.timerVideoUri != null) {
                IconButton(onClick = { onUpdateStyle { it.copy(timerVideoUri = null) } }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove timer video")
                }
            }
        }
        LabeledSlider(
            label = "Timer Video Size",
            value = style.timerVideoSizePx,
            valueRange = 80f..600f,
            unit = "px",
            onValueChange = { v -> onUpdateStyle { it.copy(timerVideoSizePx = v) } }
        )
        OutlinedButton(
            onClick = onResetTimerPosition,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
            Text("Reset Timer Position")
        }
    }
}

@Composable
private fun InspectorCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun FontDropdownField(
    label: String,
    selectedFont: SlideFontOption,
    onFontSelected: (SlideFontOption) -> Unit,
    testTag: String,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .testTag(testTag),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedFont.displayName,
                        fontFamily = selectedFont.fontFamily,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SlideFontOption.entries.forEach { font ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = font.displayName,
                                fontFamily = font.fontFamily
                            )
                        },
                        onClick = {
                            onFontSelected(font)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${value.roundToInt()} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun ColorRowItem(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(hex, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )
            }
        }
    }
}
