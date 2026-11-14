package com.example.ui.components

import android.media.MediaPlayer
import android.widget.VideoView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.R
import com.example.model.QuizQuestion
import com.example.model.SlideStyleConfig
import com.example.ui.theme.CorrectAnswerBorder
import com.example.ui.theme.CorrectAnswerBrightGreen
import com.example.ui.theme.CorrectAnswerDarkGreen
import com.example.ui.theme.CorrectAnswerTextYellow
import com.example.ui.theme.WrongAnswerBorder
import com.example.ui.theme.WrongAnswerRed
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun QuizSlideCanvas(
    question: QuizQuestion,
    style: SlideStyleConfig,
    slideRenderKey: Int,
    selectedOptionIndex: Int?,
    isAnswerRevealed: Boolean,
    isTimerRunning: Boolean,
    builtInCountdownSeconds: Int?,
    timerPlayToken: Int,
    onSelectOption: (Int) -> Unit,
    onLogoDrag: (dxFraction: Float, dyFraction: Float) -> Unit,
    onTimerDrag: (dxFraction: Float, dyFraction: Float) -> Unit,
    onTimerCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(style.canvasBgColor)
            .border(1.5.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
            .testTag("quiz_slide_canvas")
    ) {
        val density = LocalDensity.current
        val canvasWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val canvasHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
        // Scale factor relative to 1280x720 reference canvas
        val scale = (maxWidth.value / 1280f).coerceAtLeast(0.15f)

        // Optional Background Image
        if (style.backgroundImageUri != null) {
            AsyncImage(
                model = style.backgroundImageUri,
                contentDescription = stringResource(R.string.cd_slide_background),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        val outerPadDp = (12f * scale).dp
        val gapDp = (8f * scale).dp

        // Main Column: Question Box + Options Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadDp),
            horizontalAlignment = Alignment.Start
        ) {
            // Question Box popDown Animation
            val qAnimOffsetY = remember(slideRenderKey) { Animatable(-80f * scale) }
            val qAnimScale = remember(slideRenderKey) { Animatable(0.84f) }
            val qAnimAlpha = remember(slideRenderKey) { Animatable(0f) }

            LaunchedEffect(slideRenderKey) {
                qAnimAlpha.snapTo(0f)
                qAnimOffsetY.snapTo(-80f * scale)
                qAnimScale.snapTo(0.84f)
            }
            LaunchedEffect(slideRenderKey) {
                qAnimAlpha.animateTo(1f, tween(280))
            }
            LaunchedEffect(slideRenderKey) {
                qAnimOffsetY.animateTo(
                    0f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            }
            LaunchedEffect(slideRenderKey) {
                qAnimScale.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                )
            }

            val qHeightFraction = (style.questionBoxHeightPercent / 100f).coerceIn(0.15f, 0.50f)
            val qWidthFraction = (style.questionBoxWidthPercent / 100f).coerceIn(0.40f, 1.0f)

            Surface(
                modifier = Modifier
                    .fillMaxWidth(qWidthFraction)
                    .fillMaxHeight(qHeightFraction)
                    .graphicsLayer {
                        translationY = qAnimOffsetY.value
                        scaleX = qAnimScale.value
                        scaleY = qAnimScale.value
                        alpha = qAnimAlpha.value
                    }
                    .shadow((4f * scale).dp, RoundedCornerShape((12f * scale).dp))
                    .testTag("question_box"),
                shape = RoundedCornerShape((12f * scale).dp),
                color = style.questionBgColor,
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
            ) {
                AutoFitQuestionText(
                    text = question.question.ifBlank { "Enter your question…" },
                    maxFontSizeVirtual = style.questionMaxFontSize,
                    scale = scale,
                    textColor = style.questionTextColor,
                    fontFamily = style.questionFont.fontFamily,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = (16f * scale).dp, vertical = (10f * scale).dp)
                )
            }

            Spacer(modifier = Modifier.height(gapDp))

            // Options Container
            val optsWidthFraction = (style.optionsBlockWidthPercent / 100f).coerceIn(0.40f, 1.0f)
            val singleOptHeightFraction = (style.singleOptionHeightPercent / 100f).coerceIn(0.12f, 0.245f)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(optsWidthFraction)
                    .padding(vertical = (4f * scale).dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val letters = listOf("A", "B", "C", "D")
                val weightValue = (singleOptHeightFraction * 4f).coerceIn(0.45f, 1f)
                for (i in 0 until 4) {
                    val optText = question.options.getOrElse(i) { "" }
                    val isCorrect = isAnswerRevealed && i == question.correctIndex
                    val isWrong = isAnswerRevealed && selectedOptionIndex == i && i != question.correctIndex

                    OptionSlideBox(
                        index = i,
                        letter = letters[i],
                        text = optText,
                        style = style,
                        scale = scale,
                        slideRenderKey = slideRenderKey,
                        isCorrectRevealed = isCorrect,
                        isWrongSelected = isWrong,
                        onClick = { onSelectOption(i) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(weightValue)
                    )
                }
            }
        }

        // Draggable Logo Overlay (hidden while timer runs, visible after timer finishes)
        if (style.logoImageUri != null && !isTimerRunning) {
            val logoDp = (style.logoSizePx * scale).dp
            val logoLeftPx = (style.logoLeftFraction * canvasWidthPx).roundToInt()
            val logoTopPx = (style.logoTopFraction * canvasHeightPx).roundToInt()

            AsyncImage(
                model = style.logoImageUri,
                contentDescription = stringResource(R.string.cd_slide_logo),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .offset { IntOffset(logoLeftPx, logoTopPx) }
                    .size(logoDp)
                    .zIndex(35f)
                    .pointerInput(canvasWidthPx, canvasHeightPx) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onLogoDrag(dragAmount.x / canvasWidthPx, dragAmount.y / canvasHeightPx)
                        }
                    }
                    .testTag("logo_overlay")
            )
        }

        // Draggable Timer Video Overlay or Built-In Countdown Overlay
        if (isTimerRunning) {
            val timerWidthDp = (style.timerVideoSizePx * scale).dp
            val timerLeftPx = (style.timerLeftFraction * canvasWidthPx).roundToInt()
            val timerTopPx = (style.timerTopFraction * canvasHeightPx).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(timerLeftPx, timerTopPx) }
                    .width(timerWidthDp)
                    .zIndex(60f)
                    .pointerInput(canvasWidthPx, canvasHeightPx) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onTimerDrag(dragAmount.x / canvasWidthPx, dragAmount.y / canvasHeightPx)
                        }
                    }
                    .testTag("timer_overlay")
            ) {
                if (builtInCountdownSeconds != null) {
                    // Sleek Built-In Countdown Pill when user tests timer without an uploaded video
                    Surface(
                        shape = RoundedCornerShape((16f * scale).dp),
                        color = Color(0xE60F172A),
                        border = BorderStroke(2.dp, Color(0xFFEF4444)),
                        modifier = Modifier.padding((4f * scale).dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = (20f * scale).dp,
                                vertical = (12f * scale).dp
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size((32f * scale).coerceAtLeast(18f).dp)
                            )
                            Text(
                                text = "${builtInCountdownSeconds}s",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = (36f * scale).coerceAtLeast(16f).sp
                            )
                        }
                    }
                } else if (style.timerVideoUri != null) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setZOrderMediaOverlay(true)
                            }
                        },
                        update = { videoView ->
                            val targetUri = style.timerVideoUri
                            if (videoView.tag != timerPlayToken) {
                                videoView.tag = timerPlayToken
                                videoView.setVideoURI(targetUri)
                                videoView.setOnPreparedListener { mp ->
                                    mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                                    videoView.start()
                                }
                                videoView.setOnCompletionListener {
                                    onTimerCompleted()
                                }
                                videoView.setOnErrorListener { _, _, _ ->
                                    onTimerCompleted()
                                    true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape((8f * scale).dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoFitQuestionText(
    text: String,
    maxFontSizeVirtual: Float,
    scale: Float,
    textColor: Color,
    fontFamily: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val availWidthPx = with(density) { maxWidth.roundToPx() }.coerceAtLeast(10)
        val availHeightPx = with(density) { maxHeight.roundToPx() }.coerceAtLeast(10)

        val (fittedSp, lineHeightMult) = remember(
            text,
            maxFontSizeVirtual,
            scale,
            availWidthPx,
            availHeightPx,
            fontFamily
        ) {
            var virtualSize = maxFontSizeVirtual
            var mult = 1.25f

            while (virtualSize > 14f) {
                val candidateSp = (virtualSize * scale).sp
                val result = textMeasurer.measure(
                    text = text,
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = candidateSp,
                        lineHeight = candidateSp * mult,
                        textAlign = TextAlign.Center
                    ),
                    constraints = Constraints(maxWidth = availWidthPx)
                )
                if (result.size.height <= availHeightPx && !result.hasVisualOverflow) {
                    break
                }
                virtualSize -= 1f
                mult = when {
                    virtualSize < 40f -> 1.12f
                    virtualSize < 46f -> 1.18f
                    else -> 1.22f
                }
            }
            (virtualSize * scale).sp to mult
        }

        Text(
            text = text,
            color = textColor,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fittedSp,
            lineHeight = fittedSp * lineHeightMult,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OptionSlideBox(
    index: Int,
    letter: String,
    text: String,
    style: SlideStyleConfig,
    scale: Float,
    slideRenderKey: Int,
    isCorrectRevealed: Boolean,
    isWrongSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Staggered entrance from top
    val entranceOffsetY = remember(slideRenderKey) { Animatable(-40f * scale) }
    val entranceAlpha = remember(slideRenderKey) { Animatable(0f) }

    LaunchedEffect(slideRenderKey) {
        entranceOffsetY.snapTo(-40f * scale)
        entranceAlpha.snapTo(0f)
        delay((index + 1) * 70L)
        entranceAlpha.animateTo(1f, tween(280))
    }
    LaunchedEffect(slideRenderKey) {
        delay((index + 1) * 70L)
        entranceOffsetY.animateTo(0f, tween(350, easing = FastOutSlowInEasing))
    }

    // Answer pop-up pulse animation for correct option
    val popScale = remember { Animatable(1f) }
    LaunchedEffect(isCorrectRevealed) {
        if (isCorrectRevealed) {
            popScale.snapTo(1f)
            popScale.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = 700
                    1.06f at 320
                    0.98f at 520
                    1.00f at 700
                }
            )
        } else {
            popScale.snapTo(1f)
        }
    }

    val targetBgColor = when {
        isCorrectRevealed -> CorrectAnswerDarkGreen
        isWrongSelected -> WrongAnswerRed
        else -> style.optionBgColor
    }
    val animatedBgColor by animateColorAsState(
        targetValue = if (isCorrectRevealed && popScale.value > 1.01f) CorrectAnswerBrightGreen else targetBgColor,
        animationSpec = tween(220),
        label = "optionBgColor"
    )

    val targetTextColor = when {
        isCorrectRevealed -> CorrectAnswerTextYellow
        isWrongSelected -> Color.White
        else -> style.optionTextColor
    }
    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(200),
        label = "optionTextColor"
    )

    val borderColor = when {
        isCorrectRevealed -> CorrectAnswerBorder
        isWrongSelected -> WrongAnswerBorder
        else -> Color.White.copy(alpha = 0.14f)
    }

    val optFontSp = (style.optionFontSize * scale).sp
    val badgeSizeDp = (32f * scale).coerceAtLeast(14f).dp
    val badgeFontSp = (16f * scale).coerceAtLeast(8f).sp

    Surface(
        modifier = modifier
            .padding(vertical = (2f * scale).dp)
            .zIndex(if (isCorrectRevealed) 10f else 1f)
            .graphicsLayer {
                translationY = entranceOffsetY.value
                alpha = entranceAlpha.value
                scaleX = popScale.value
                scaleY = popScale.value
            }
            .shadow((3f * scale).dp, RoundedCornerShape((8f * scale).dp))
            .clip(RoundedCornerShape((8f * scale).dp))
            .clickable(onClick = onClick)
            .testTag("option_box_$index"),
        shape = RoundedCornerShape((8f * scale).dp),
        color = animatedBgColor,
        border = BorderStroke(if (isCorrectRevealed) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = (16f * scale).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((12f * scale).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(badgeSizeDp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = animatedTextColor,
                    fontFamily = style.optionFont.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = badgeFontSp
                )
            }

            Text(
                text = text.ifBlank { "Option $letter" },
                color = animatedTextColor,
                fontFamily = style.optionFont.fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = optFontSp,
                lineHeight = optFontSp * 1.15f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
