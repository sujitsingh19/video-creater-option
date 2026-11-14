package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.SlideExportState
import com.example.viewmodel.QuizStudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizStudioScreen(
    viewModel: QuizStudioViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Zero-permission Photo Picker Launchers
    val bgPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.setBackgroundUri(it) } }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.setLogoUri(it) } }

    val timerVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.setTimerVideoUri(it) } }

    // Handle Export Snackbars with Share Sheet action
    LaunchedEffect(state.exportState) {
        when (val export = state.exportState) {
            is SlideExportState.Success -> {
                val res = snackbarHostState.showSnackbar(
                    message = export.message,
                    actionLabel = "Share",
                    duration = SnackbarDuration.Long
                )
                if (res == SnackbarResult.ActionPerformed) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = export.mimeType
                        putExtra(Intent.EXTRA_STREAM, export.shareUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Quiz Export"))
                }
                viewModel.clearExportState()
            }

            is SlideExportState.Error -> {
                snackbarHostState.showSnackbar(export.message)
                viewModel.clearExportState()
            }

            else -> Unit
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.subtitle_auto_fit),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { viewModel.openBulkModal() },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("bulk_upload_button")
                    ) {
                        Icon(Icons.Default.PlaylistAdd, contentDescription = stringResource(R.string.cd_bulk_questions), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bulk", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.exportCurrentSlidePng(context) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF16A34A),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("export_png_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = stringResource(R.string.cd_export_png), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PNG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { viewModel.exportAllSlidesZip(context) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF4F46E5),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("export_zip_button")
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = stringResource(R.string.cd_export_zip), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ZIP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val isExpanded = maxWidth >= 840.dp

            if (isExpanded) {
                // Canonical Tablet / Landscape Two-Pane Studio Layout
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.45f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuizSlideCanvas(
                            question = state.currentQuestion,
                            style = state.style,
                            slideRenderKey = state.slideRenderKey,
                            selectedOptionIndex = state.selectedOptionIndex,
                            isAnswerRevealed = state.isAnswerRevealed,
                            isTimerRunning = state.isTimerRunning,
                            builtInCountdownSeconds = state.builtInCountdownSeconds,
                            timerPlayToken = state.timerPlayToken,
                            onSelectOption = viewModel::selectOption,
                            onLogoDrag = viewModel::dragLogoBy,
                            onTimerDrag = viewModel::dragTimerBy,
                            onTimerCompleted = viewModel::onTimerCompleted
                        )
                        SlideTransportBar(
                            currentIndex = state.currentIndex,
                            totalCount = state.questions.size,
                            isTimerRunning = state.isTimerRunning,
                            onPrev = viewModel::prevQuestion,
                            onNext = viewModel::nextQuestion,
                            onRevealAnswer = viewModel::revealAnswer,
                            onToggleTimer = {
                                if (state.isTimerRunning) viewModel.stopTimer() else viewModel.startTimer()
                            }
                        )
                    }

                    StudioInspectorPanel(
                        activeTab = state.activeTab,
                        question = state.currentQuestion,
                        totalSlides = state.questions.size,
                        style = state.style,
                        onSelectTab = viewModel::selectTab,
                        onUpdateQuestion = viewModel::updateCurrentQuestion,
                        onAddSlide = viewModel::addNewSlide,
                        onDeleteSlide = viewModel::deleteCurrentSlide,
                        onUpdateStyle = viewModel::updateStyle,
                        onResetTimerPosition = viewModel::resetTimerPosition,
                        onResetLogoPosition = viewModel::resetLogoPosition,
                        onLaunchBackgroundPicker = {
                            bgPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onLaunchLogoPicker = {
                            logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onLaunchTimerVideoPicker = {
                            timerVideoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            } else {
                // Compact Handheld Portrait Layout: Pinned 16:9 Slide + Transport + Tabbed Inspector
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuizSlideCanvas(
                        question = state.currentQuestion,
                        style = state.style,
                        slideRenderKey = state.slideRenderKey,
                        selectedOptionIndex = state.selectedOptionIndex,
                        isAnswerRevealed = state.isAnswerRevealed,
                        isTimerRunning = state.isTimerRunning,
                        builtInCountdownSeconds = state.builtInCountdownSeconds,
                        timerPlayToken = state.timerPlayToken,
                        onSelectOption = viewModel::selectOption,
                        onLogoDrag = viewModel::dragLogoBy,
                        onTimerDrag = viewModel::dragTimerBy,
                        onTimerCompleted = viewModel::onTimerCompleted
                    )

                    SlideTransportBar(
                        currentIndex = state.currentIndex,
                        totalCount = state.questions.size,
                        isTimerRunning = state.isTimerRunning,
                        onPrev = viewModel::prevQuestion,
                        onNext = viewModel::nextQuestion,
                        onRevealAnswer = viewModel::revealAnswer,
                        onToggleTimer = {
                            if (state.isTimerRunning) viewModel.stopTimer() else viewModel.startTimer()
                        }
                    )

                    StudioInspectorPanel(
                        activeTab = state.activeTab,
                        question = state.currentQuestion,
                        totalSlides = state.questions.size,
                        style = state.style,
                        onSelectTab = viewModel::selectTab,
                        onUpdateQuestion = viewModel::updateCurrentQuestion,
                        onAddSlide = viewModel::addNewSlide,
                        onDeleteSlide = viewModel::deleteCurrentSlide,
                        onUpdateStyle = viewModel::updateStyle,
                        onResetTimerPosition = viewModel::resetTimerPosition,
                        onResetLogoPosition = viewModel::resetLogoPosition,
                        onLaunchBackgroundPicker = {
                            bgPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onLaunchLogoPicker = {
                            logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onLaunchTimerVideoPicker = {
                            timerVideoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }

            // Export Progress Overlay
            val exportState = state.exportState
            if (exportState is SlideExportState.Exporting) {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    tonalElevation = 12.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        Text(
                            text = exportState.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Bulk Upload Modal
    if (state.isBulkModalOpen) {
        val parsedCount = remember(state.bulkDraftText) {
            viewModel.parseBulkText(state.bulkDraftText).size
        }
        BulkUploadDialog(
            draftText = state.bulkDraftText,
            parsedCount = parsedCount,
            onDraftChange = viewModel::updateBulkDraft,
            onLoadSample = viewModel::loadSampleBulkQuestions,
            onDismiss = viewModel::closeBulkModal,
            onConfirmLoad = { viewModel.saveBulkQuestions() }
        )
    }

    // Prompt if Timer Video hasn't been uploaded yet
    if (state.showNoTimerVideoPrompt) {
        AlertDialog(
            onDismissRequest = viewModel::dismissNoTimerVideoPrompt,
            title = { Text("Timer Video Not Uploaded") },
            text = {
                Text("Aap apna MP4 timer video upload kar sakte hain, ya abhi 5-second built-in visual countdown timer chala sakte hain.")
            },
            confirmButton = {
                Button(onClick = { viewModel.startBuiltInCountdownTimer(5) }) {
                    Text("Run 5s Built-In Timer")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.dismissNoTimerVideoPrompt()
                    timerVideoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                }) {
                    Text("Upload Timer Video")
                }
            }
        )
    }
}

@Composable
private fun SlideTransportBar(
    currentIndex: Int,
    totalCount: Int,
    isTimerRunning: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onRevealAnswer: () -> Unit,
    onToggleTimer: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPrev,
                    enabled = currentIndex > 0,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("prev_slide_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.cd_previous_slide))
                    Text("Prev", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Slide ${currentIndex + 1} / $totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("slide_page_indicator")
                )

                OutlinedButton(
                    onClick = onNext,
                    enabled = currentIndex < totalCount - 1,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("next_slide_button")
                ) {
                    Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.cd_next_slide))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRevealAnswer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("show_answer_button")
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = stringResource(R.string.cd_show_answer), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Show Answer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onToggleTimer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) Color(0xFF334155) else Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("start_timer_button")
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Stop else Icons.Default.Timer,
                        contentDescription = stringResource(R.string.cd_start_timer),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTimerRunning) "Stop Timer" else "Start Timer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
