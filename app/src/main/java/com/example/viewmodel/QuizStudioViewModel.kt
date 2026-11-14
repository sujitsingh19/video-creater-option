package com.example.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.export.SlideBitmapRenderer
import com.example.model.InspectorTab
import com.example.model.QuizQuestion
import com.example.model.SlideExportState
import com.example.model.SlideStyleConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QuizStudioUiState(
    val questions: List<QuizQuestion> = listOf(
        QuizQuestion(
            question = "What is the capital of India?",
            options = listOf("Delhi", "Mumbai", "Kolkata", "Chennai"),
            answerLetter = "A"
        )
    ),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val slideRenderKey: Int = 0,
    val isTimerRunning: Boolean = false,
    val builtInCountdownSeconds: Int? = null,
    val timerPlayToken: Int = 0,
    val style: SlideStyleConfig = SlideStyleConfig(),
    val activeTab: InspectorTab = InspectorTab.SLIDE,
    val isBulkModalOpen: Boolean = false,
    val bulkDraftText: String = "",
    val showNoTimerVideoPrompt: Boolean = false,
    val exportState: SlideExportState = SlideExportState.Idle,
) {
    val currentQuestion: QuizQuestion
        get() = questions.getOrElse(currentIndex) {
            QuizQuestion(
                question = "Add a question to start",
                options = listOf("Option A", "Option B", "Option C", "Option D"),
                answerLetter = "A"
            )
        }
}

class QuizStudioViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuizStudioUiState())
    val uiState: StateFlow<QuizStudioUiState> = _uiState.asStateFlow()

    private var autoNextJob: Job? = null
    private var builtInTimerJob: Job? = null

    fun selectTab(tab: InspectorTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectOption(index: Int) {
        _uiState.update {
            it.copy(
                selectedOptionIndex = index,
                isAnswerRevealed = true
            )
        }
    }

    fun revealAnswer() {
        _uiState.update {
            it.copy(
                selectedOptionIndex = null,
                isAnswerRevealed = true
            )
        }
    }

    fun nextQuestion() {
        cancelTimers()
        _uiState.update { state ->
            if (state.currentIndex < state.questions.lastIndex) {
                state.copy(
                    currentIndex = state.currentIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerRevealed = false,
                    slideRenderKey = state.slideRenderKey + 1,
                    isTimerRunning = false,
                    builtInCountdownSeconds = null
                )
            } else state
        }
    }

    fun prevQuestion() {
        cancelTimers()
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                state.copy(
                    currentIndex = state.currentIndex - 1,
                    selectedOptionIndex = null,
                    isAnswerRevealed = false,
                    slideRenderKey = state.slideRenderKey + 1,
                    isTimerRunning = false,
                    builtInCountdownSeconds = null
                )
            } else state
        }
    }

    fun updateCurrentQuestion(
        questionText: String? = null,
        optionA: String? = null,
        optionB: String? = null,
        optionC: String? = null,
        optionD: String? = null,
        answerLetter: String? = null,
    ) {
        _uiState.update { state ->
            val updated = state.questions.toMutableList()
            val cur = updated.getOrNull(state.currentIndex) ?: return@update state
            val opts = cur.options.toMutableList().apply {
                while (size < 4) add("")
                optionA?.let { this[0] = it }
                optionB?.let { this[1] = it }
                optionC?.let { this[2] = it }
                optionD?.let { this[3] = it }
            }
            updated[state.currentIndex] = cur.copy(
                question = questionText ?: cur.question,
                options = opts,
                answerLetter = answerLetter ?: cur.answerLetter
            )
            state.copy(questions = updated)
        }
    }

    fun addNewSlide() {
        cancelTimers()
        _uiState.update { state ->
            val newQ = QuizQuestion(
                question = "New Quiz Question ${state.questions.size + 1}",
                options = listOf("Option A", "Option B", "Option C", "Option D"),
                answerLetter = "A"
            )
            val updated = state.questions + newQ
            state.copy(
                questions = updated,
                currentIndex = updated.lastIndex,
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                slideRenderKey = state.slideRenderKey + 1
            )
        }
    }

    fun deleteCurrentSlide() {
        cancelTimers()
        _uiState.update { state ->
            if (state.questions.size <= 1) return@update state
            val updated = state.questions.toMutableList().apply { removeAt(state.currentIndex) }
            val nextIdx = state.currentIndex.coerceAtMost(updated.lastIndex)
            state.copy(
                questions = updated,
                currentIndex = nextIdx,
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                slideRenderKey = state.slideRenderKey + 1
            )
        }
    }

    fun openBulkModal() {
        _uiState.update { state ->
            val serialized = state.questions.joinToString("\n") { q ->
                val opts = (0 until 4).map { q.options.getOrElse(it) { "" } }
                "${q.question} | ${opts[0]} | ${opts[1]} | ${opts[2]} | ${opts[3]} | ${q.answerLetter}"
            }
            state.copy(isBulkModalOpen = true, bulkDraftText = serialized)
        }
    }

    fun closeBulkModal() {
        _uiState.update { it.copy(isBulkModalOpen = false) }
    }

    fun updateBulkDraft(text: String) {
        _uiState.update { it.copy(bulkDraftText = text) }
    }

    fun loadSampleBulkQuestions() {
        val samples = """
            What is the capital of India? | New Delhi | Mumbai | Kolkata | Chennai | A
            भारत की खोज किसने की थी और यह किस वर्ष में समुद्री मार्ग से भारत पहुँचे थे? | वास्को डी गामा (1498) | क्रिस्टोफर कोलंबस | सिकंदर महान | फर्डिनेंड मैगलन | A
            Which planet is known as the Red Planet in our Solar System? | Venus | Mars | Jupiter | Saturn | B
            संविधान सभा की प्रथम बैठक कब हुई थी और इसके अस्थायी अध्यक्ष कौन थे? | 9 दिसंबर 1946 (सच्चिदानंद सिन्हा) | 15 अगस्त 1947 (राजेंद्र प्रसाद) | 26 जनवरी 1950 (बी.आर. अंबेडकर) | 26 नवंबर 1949 (जवाहरलाल नेहरू) | A
            Who wrote the Indian National Anthem 'Jana Gana Mana'? | Bankim Chandra Chatterjee | Rabindranath Tagore | Sarojini Naidu | Subramania Bharati | B
        """.trimIndent()
        _uiState.update { it.copy(bulkDraftText = samples) }
    }

    fun saveBulkQuestions(): Int {
        val parsed = parseBulkText(_uiState.value.bulkDraftText)
        if (parsed.isEmpty()) return 0
        cancelTimers()
        _uiState.update {
            it.copy(
                questions = parsed,
                currentIndex = 0,
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                slideRenderKey = it.slideRenderKey + 1,
                isBulkModalOpen = false
            )
        }
        return parsed.size
    }

    fun parseBulkText(rawText: String): List<QuizQuestion> {
        return rawText
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { line ->
                val parts = line.split("|").map { it.trim() }
                if (parts.size >= 5 && parts[0].isNotEmpty()) {
                    val ans = parts.getOrNull(5)?.trim()?.uppercase()?.firstOrNull()
                    val validAns = if (ans in listOf('A', 'B', 'C', 'D')) ans.toString() else "A"
                    QuizQuestion(
                        question = parts[0],
                        options = listOf(parts[1], parts[2], parts[3], parts[4]),
                        answerLetter = validAns
                    )
                } else null
            }
            .toList()
    }

    fun updateStyle(transform: (SlideStyleConfig) -> SlideStyleConfig) {
        _uiState.update { it.copy(style = transform(it.style)) }
    }

    fun dragLogoBy(dxFraction: Float, dyFraction: Float) {
        _uiState.update { state ->
            val nextLeft = (state.style.logoLeftFraction + dxFraction).coerceIn(0f, 0.90f)
            val nextTop = (state.style.logoTopFraction + dyFraction).coerceIn(0f, 0.85f)
            state.copy(
                style = state.style.copy(
                    logoLeftFraction = nextLeft,
                    logoTopFraction = nextTop
                )
            )
        }
    }

    fun dragTimerBy(dxFraction: Float, dyFraction: Float) {
        _uiState.update { state ->
            val nextLeft = (state.style.timerLeftFraction + dxFraction).coerceIn(0f, 0.85f)
            val nextTop = (state.style.timerTopFraction + dyFraction).coerceIn(0f, 0.85f)
            state.copy(
                style = state.style.copy(
                    timerLeftFraction = nextLeft,
                    timerTopFraction = nextTop
                )
            )
        }
    }

    fun resetTimerPosition() {
        _uiState.update {
            it.copy(style = it.style.copy(timerLeftFraction = 0.65f, timerTopFraction = 0.35f))
        }
    }

    fun resetLogoPosition() {
        _uiState.update {
            it.copy(
                style = it.style.copy(
                    logoLeftFraction = 12f / 1280f,
                    logoTopFraction = 12f / 720f
                )
            )
        }
    }

    fun startTimer() {
        val hasVideo = _uiState.value.style.timerVideoUri != null
        if (!hasVideo) {
            _uiState.update { it.copy(showNoTimerVideoPrompt = true) }
            return
        }
        cancelTimers()
        _uiState.update {
            it.copy(
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                isTimerRunning = true,
                builtInCountdownSeconds = null,
                timerPlayToken = it.timerPlayToken + 1
            )
        }
    }

    fun dismissNoTimerVideoPrompt() {
        _uiState.update { it.copy(showNoTimerVideoPrompt = false) }
    }

    fun startBuiltInCountdownTimer(seconds: Int = 5) {
        cancelTimers()
        _uiState.update {
            it.copy(
                showNoTimerVideoPrompt = false,
                selectedOptionIndex = null,
                isAnswerRevealed = false,
                isTimerRunning = true,
                builtInCountdownSeconds = seconds
            )
        }
        builtInTimerJob = viewModelScope.launch {
            for (rem in seconds downTo 1) {
                _uiState.update { it.copy(builtInCountdownSeconds = rem) }
                delay(1000L)
            }
            onTimerCompleted()
        }
    }

    fun onTimerCompleted() {
        _uiState.update {
            it.copy(
                isTimerRunning = false,
                builtInCountdownSeconds = null,
                selectedOptionIndex = null,
                isAnswerRevealed = true
            )
        }
        // Auto-advance to next slide after 5 seconds (matching HTML tool)
        autoNextJob?.cancel()
        autoNextJob = viewModelScope.launch {
            delay(5000L)
            val state = _uiState.value
            if (state.currentIndex < state.questions.lastIndex) {
                nextQuestion()
            }
        }
    }

    fun stopTimer() {
        cancelTimers()
        _uiState.update {
            it.copy(isTimerRunning = false, builtInCountdownSeconds = null)
        }
    }

    private fun cancelTimers() {
        autoNextJob?.cancel()
        autoNextJob = null
        builtInTimerJob?.cancel()
        builtInTimerJob = null
    }

    fun exportCurrentSlidePng(context: Context) {
        val state = _uiState.value
        val appContext = context.applicationContext
        viewModelScope.launch {
            _uiState.update {
                it.copy(exportState = SlideExportState.Exporting(1, 1, "Rendering HD PNG (2560×1440)…"))
            }
            try {
                val uri = withContext(Dispatchers.Default) {
                    val bmp = SlideBitmapRenderer.renderSlideBitmap(
                        context = appContext,
                        question = state.currentQuestion,
                        style = state.style,
                        revealAnswer = state.isAnswerRevealed,
                        showLogo = true
                    )
                    val savedUri = SlideBitmapRenderer.savePngToGalleryAndCache(
                        context = appContext,
                        bitmap = bmp,
                        fileName = "Slide_${state.currentIndex + 1}.png"
                    )
                    bmp.recycle()
                    savedUri
                }
                _uiState.update {
                    it.copy(
                        exportState = SlideExportState.Success(
                            message = "Saved Slide_${state.currentIndex + 1}.png to Pictures/QuizSlideStudio",
                            shareUri = uri,
                            mimeType = "image/png"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(exportState = SlideExportState.Error("Export failed: ${e.message ?: "Unknown error"}"))
                }
            }
        }
    }

    fun exportAllSlidesZip(context: Context) {
        val state = _uiState.value
        val appContext = context.applicationContext
        val questionsCopy = state.questions.toList()
        val styleCopy = state.style
        viewModelScope.launch {
            _uiState.update {
                it.copy(exportState = SlideExportState.Exporting(0, questionsCopy.size, "Generating ZIP…"))
            }
            try {
                val uri = withContext(Dispatchers.Default) {
                    SlideBitmapRenderer.exportAllSlidesZip(
                        context = appContext,
                        questions = questionsCopy,
                        style = styleCopy
                    ) { cur, total ->
                        _uiState.update {
                            it.copy(
                                exportState = SlideExportState.Exporting(
                                    current = cur,
                                    total = total,
                                    label = "Rendering slide $cur of $total…"
                                )
                            )
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        exportState = SlideExportState.Success(
                            message = "Saved Quiz_Slides.zip (${questionsCopy.size} HD slides) to Downloads/QuizSlideStudio",
                            shareUri = uri,
                            mimeType = "application/zip"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(exportState = SlideExportState.Error("ZIP export failed: ${e.message ?: "Unknown error"}"))
                }
            }
        }
    }

    fun clearExportState() {
        _uiState.update { it.copy(exportState = SlideExportState.Idle) }
    }

    fun setBackgroundUri(uri: Uri?) {
        _uiState.update { it.copy(style = it.style.copy(backgroundImageUri = uri)) }
    }

    fun setLogoUri(uri: Uri?) {
        _uiState.update { it.copy(style = it.style.copy(logoImageUri = uri)) }
    }

    fun setTimerVideoUri(uri: Uri?) {
        _uiState.update { it.copy(style = it.style.copy(timerVideoUri = uri)) }
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimers()
    }
}
