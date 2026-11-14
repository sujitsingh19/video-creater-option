package com.example.model

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DefaultCanvasBlue
import com.example.ui.theme.DefaultOptionBlue
import com.example.ui.theme.DefaultOptionTextWhite
import com.example.ui.theme.DefaultQuestionTextBlue
import com.example.ui.theme.DefaultQuestionYellow
import com.example.ui.theme.SlideFontOption
import java.util.UUID

data class QuizQuestion(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val options: List<String>,
    val answerLetter: String,
) {
    val correctIndex: Int
        get() {
            val ch = answerLetter.trim().uppercase().firstOrNull() ?: 'A'
            return (ch - 'A').coerceIn(0, 3)
        }
}

data class SlideStyleConfig(
    val questionFont: SlideFontOption = SlideFontOption.TIRO_DEVANAGARI_HINDI,
    val optionFont: SlideFontOption = SlideFontOption.NOTO_SERIF_DEVANAGARI,
    val questionMaxFontSize: Float = 51f,
    val optionFontSize: Float = 32f,
    val questionBoxHeightPercent: Float = 30f,
    val questionBoxWidthPercent: Float = 100f,
    val optionsBlockWidthPercent: Float = 65f,
    val singleOptionHeightPercent: Float = 22f,
    val questionBgColor: Color = DefaultQuestionYellow,
    val questionTextColor: Color = DefaultQuestionTextBlue,
    val optionBgColor: Color = DefaultOptionBlue,
    val optionTextColor: Color = DefaultOptionTextWhite,
    val canvasBgColor: Color = DefaultCanvasBlue,
    val backgroundImageUri: Uri? = null,
    val logoImageUri: Uri? = null,
    val logoSizePx: Float = 131f,
    val logoLeftFraction: Float = 12f / 1280f,
    val logoTopFraction: Float = 12f / 720f,
    val timerVideoUri: Uri? = null,
    val timerVideoSizePx: Float = 359f,
    val timerLeftFraction: Float = 0.65f,
    val timerTopFraction: Float = 0.35f,
)

enum class InspectorTab(val title: String) {
    SLIDE("Edit Slide"),
    TYPOGRAPHY("Fonts & Size"),
    COLORS("Colors"),
    OVERLAYS("Media & Timer"),
}

sealed interface SlideExportState {
    data object Idle : SlideExportState
    data class Exporting(val current: Int, val total: Int, val label: String) : SlideExportState
    data class Success(
        val message: String,
        val shareUri: Uri,
        val mimeType: String,
    ) : SlideExportState
    data class Error(val message: String) : SlideExportState
}
