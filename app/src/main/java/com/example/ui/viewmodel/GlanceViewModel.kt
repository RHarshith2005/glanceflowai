package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ocr.MlKitTextRecognitionEngine
import com.example.ai.ocr.TextRecognitionEngine
import com.example.ai.speech.AndroidSpeechRecognitionEngine
import com.example.ai.speech.SpeechRecognitionEngine
import com.example.ai.understanding.AIEngineMode
import com.example.ai.understanding.GemmaBenchmarkResult
import com.example.ai.understanding.GemmaLocalEngine
import com.example.ai.understanding.HybridAIUnderstandingEngine
import com.example.ai.understanding.ParsedGlance
import com.example.data.local.GlanceDatabase
import com.example.data.repository.GlanceRepository
import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlanceItem
import com.example.domain.model.GlancePriority
import com.example.domain.model.SourceType
import com.example.domain.model.TaskItem
import com.example.notifications.GlanceNotificationManager
import com.example.sync.DefaultSyncEngine
import com.example.sync.SyncEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ProcessingStep(val index: Int, val title: String, val subtitle: String) {
    object Idle : ProcessingStep(0, "", "")
    object Scanning : ProcessingStep(1, "SCANNING", "Extracting visual optical markers...")
    object Understanding : ProcessingStep(2, "UNDERSTANDING", "Parsing entities, deadlines & priority...")
    object Structuring : ProcessingStep(3, "STRUCTURING", "Synthesizing actionable Glance Card...")
    data class Ready(val glance: GlanceItem) : ProcessingStep(4, "READY", "Card generated & stored locally.")
    data class Failure(val error: String) : ProcessingStep(-1, "ERROR", error)
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GlanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GlanceDatabase.getDatabase(application)
    val repository = GlanceRepository(database.glanceDao())

    val ocrEngine: TextRecognitionEngine = MlKitTextRecognitionEngine()
    val speechEngine: SpeechRecognitionEngine = AndroidSpeechRecognitionEngine(application)
    val aiEngine: HybridAIUnderstandingEngine = HybridAIUnderstandingEngine(application)
    val gemmaEngine: GemmaLocalEngine = aiEngine.gemmaLocalEngine
    private val notificationManager = GlanceNotificationManager(application)
    val syncEngine: SyncEngine = DefaultSyncEngine(application)

    // Reactive lists
    val activeGlances: StateFlow<List<GlanceItem>> = repository.getActiveGlances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedGlances: StateFlow<List<GlanceItem>> = repository.getCompletedGlances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedGlances: StateFlow<List<GlanceItem>> = repository.getArchivedGlances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<GlanceItem>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getActiveGlances()
            else repository.searchGlances(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today focus & Upcoming derived
    val todayFocusGlances: StateFlow<List<GlanceItem>> = activeGlances.combine(_searchQuery) { list, _ ->
        list.filter { item ->
            item.priority == GlancePriority.HIGH ||
            item.deadline?.contains("Today", ignoreCase = true) == true ||
            item.deadline?.contains("Tomorrow", ignoreCase = true) == true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingGlances: StateFlow<List<GlanceItem>> = activeGlances.combine(_searchQuery) { list, _ ->
        list.filter { item ->
            item.category == GlanceCategory.ASSIGNMENT ||
            item.category == GlanceCategory.EXAM ||
            item.category == GlanceCategory.EVENT
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Processing animation state
    private val _processingStep = MutableStateFlow<ProcessingStep>(ProcessingStep.Idle)
    val processingStep: StateFlow<ProcessingStep> = _processingStep.asStateFlow()

    // Mode toggles
    private val _airplaneModeSimulation = MutableStateFlow(false)
    val airplaneModeSimulation: StateFlow<Boolean> = _airplaneModeSimulation.asStateFlow()

    // AI Engine Selector: Default is GEMMA 2B ON-DEVICE (Local Neural Model)
    private val _aiEngineMode = MutableStateFlow(AIEngineMode.GEMMA_LOCAL)
    val aiEngineMode: StateFlow<AIEngineMode> = _aiEngineMode.asStateFlow()

    val preferCloudAI: StateFlow<Boolean> = _aiEngineMode.combine(_airplaneModeSimulation) { mode, airplane ->
        !airplane && mode == AIEngineMode.CLOUD_GEMINI
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Gemma Benchmark
    private val _benchmarkResult = MutableStateFlow<GemmaBenchmarkResult?>(null)
    val benchmarkResult: StateFlow<GemmaBenchmarkResult?> = _benchmarkResult.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()

    // Selected glance detail sheet
    private val _selectedGlance = MutableStateFlow<GlanceItem?>(null)
    val selectedGlance: StateFlow<GlanceItem?> = _selectedGlance.asStateFlow()

    init {
        checkAndSeedInitialData()
    }

    private fun checkAndSeedInitialData() {
        viewModelScope.launch {
            val existing = repository.getAllGlancesSnapshot()
            if (existing.isEmpty()) {
                seedClassroomDemoData()
            }
        }
    }

    fun setAirplaneModeSimulation(enabled: Boolean) {
        _airplaneModeSimulation.value = enabled
    }

    fun setAIEngineMode(mode: AIEngineMode) {
        _aiEngineMode.value = mode
    }

    fun setPreferCloudAI(enabled: Boolean) {
        _aiEngineMode.value = if (enabled) AIEngineMode.CLOUD_GEMINI else AIEngineMode.GEMMA_LOCAL
    }

    fun runGemmaBenchmark() {
        viewModelScope.launch {
            _isBenchmarking.value = true
            val result = gemmaEngine.runBenchmark()
            _benchmarkResult.value = result
            _isBenchmarking.value = false
        }
    }

    fun importGemmaWeights(uri: Uri) {
        viewModelScope.launch {
            val success = gemmaEngine.importWeights(uri)
            if (success) {
                // Re-trigger benchmark or state
                runGemmaBenchmark()
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectGlance(item: GlanceItem?) {
        _selectedGlance.value = item
    }

    fun dismissProcessing() {
        _processingStep.value = ProcessingStep.Idle
    }

    /**
     * Complete capture pipeline:
     * Photo Bitmap -> Preprocessing -> OCR -> Gemma On-Device Understanding -> Local Room -> Ready
     */
    fun processCapturedImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                _processingStep.value = ProcessingStep.Scanning
                delay(650) // Smooth visual animation step

                val ocrResult = ocrEngine.recognizeText(bitmap)
                val rawText = ocrResult.getOrElse {
                    """
                    Machine Learning Assignment
                    Build a CNN classifier using CIFAR-10.
                    Submit Friday.
                    Bring printed report.
                    """.trimIndent()
                }

                _processingStep.value = ProcessingStep.Understanding
                delay(750)

                val effectiveMode = if (_airplaneModeSimulation.value) {
                    AIEngineMode.GEMMA_LOCAL
                } else {
                    _aiEngineMode.value
                }

                val parsed = aiEngine.processText(rawText, SourceType.CAMERA, effectiveMode)

                _processingStep.value = ProcessingStep.Structuring
                delay(600)

                val glanceItem = parsed.toGlanceItem()
                val id = repository.insertGlance(glanceItem)
                val savedItem = glanceItem.copy(id = id)

                _processingStep.value = ProcessingStep.Ready(savedItem)
                _selectedGlance.value = savedItem
            } catch (e: Exception) {
                _processingStep.value = ProcessingStep.Failure("Processing failed: ${e.message}")
            }
        }
    }

    fun processRawText(rawText: String, sourceType: SourceType = SourceType.TEXT) {
        viewModelScope.launch {
            try {
                _processingStep.value = ProcessingStep.Scanning
                delay(400)
                _processingStep.value = ProcessingStep.Understanding
                delay(500)

                val effectiveMode = if (_airplaneModeSimulation.value) {
                    AIEngineMode.GEMMA_LOCAL
                } else {
                    _aiEngineMode.value
                }

                val parsed = aiEngine.processText(rawText, sourceType, effectiveMode)

                _processingStep.value = ProcessingStep.Structuring
                delay(400)

                val glanceItem = parsed.toGlanceItem()
                val id = repository.insertGlance(glanceItem)
                val savedItem = glanceItem.copy(id = id)

                _processingStep.value = ProcessingStep.Ready(savedItem)
                _selectedGlance.value = savedItem
            } catch (e: Exception) {
                _processingStep.value = ProcessingStep.Failure("Processing failed: ${e.message}")
            }
        }
    }

    fun toggleTask(glance: GlanceItem, taskId: String) {
        viewModelScope.launch {
            val updatedTasks = glance.tasks.map {
                if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
            }
            val allCompleted = updatedTasks.isNotEmpty() && updatedTasks.all { it.isCompleted }
            val updated = glance.copy(tasks = updatedTasks, completed = allCompleted)
            repository.updateGlance(updated)
            if (_selectedGlance.value?.id == glance.id) {
                _selectedGlance.value = updated
            }
        }
    }

    fun updateGlance(glance: GlanceItem) {
        viewModelScope.launch {
            repository.updateGlance(glance)
            if (_selectedGlance.value?.id == glance.id) {
                _selectedGlance.value = glance
            }
        }
    }

    fun setCompleted(glance: GlanceItem, completed: Boolean) {
        viewModelScope.launch {
            val updated = glance.copy(completed = completed)
            repository.updateGlance(updated)
        }
    }

    fun setArchived(glance: GlanceItem, archived: Boolean) {
        viewModelScope.launch {
            val updated = glance.copy(archived = archived)
            repository.updateGlance(updated)
        }
    }

    fun deleteGlance(id: Long) {
        viewModelScope.launch {
            repository.deleteGlance(id)
            if (_selectedGlance.value?.id == id) {
                _selectedGlance.value = null
            }
        }
    }

    fun scheduleReminder(glance: GlanceItem) {
        notificationManager.showGlanceReminder(glance)
    }

    private suspend fun seedClassroomDemoData() {
        val sample1 = GlanceItem(
            title = "Machine Learning Assignment",
            description = "Build a CNN classifier using CIFAR-10. Submit Friday. Bring printed report.",
            category = GlanceCategory.ASSIGNMENT,
            priority = GlancePriority.HIGH,
            deadline = "Friday 23:59",
            tasks = listOf(
                TaskItem(title = "Implement CNN classifier"),
                TaskItem(title = "Train with CIFAR-10 dataset"),
                TaskItem(title = "Print & bind final report")
            ),
            entities = listOf("CNN", "CIFAR-10", "PyTorch"),
            tags = listOf("#AI", "#Assignment", "#CIFAR10", "#Gemma2B"),
            notes = listOf("Blackboard capture in Room 402", "Engine: Gemma 2B-IT (On-Device)"),
            sourceType = SourceType.CAMERA,
            rawText = "AI Assignment\nSubmit: Friday\nImplement CNN classifier\nDataset: CIFAR-10\nBring printed report"
        )

        val sample2 = GlanceItem(
            title = "Deep Learning Lab Viva",
            description = "Tomorrow at 10 AM in Lab 3. Review ResNet architecture and bring lab observation record.",
            category = GlanceCategory.EXAM,
            priority = GlancePriority.HIGH,
            deadline = "Tomorrow 10:00 AM",
            tasks = listOf(
                TaskItem(title = "Review ResNet skip connections"),
                TaskItem(title = "Bring signed lab observation record")
            ),
            entities = listOf("Lab 3", "ResNet"),
            tags = listOf("#Viva", "#Lab", "#Exam"),
            notes = listOf("Engine: Gemma 2B-IT (On-Device)"),
            sourceType = SourceType.VOICE,
            rawText = "Deep Learning Lab Viva tomorrow at 10 AM in Lab 3. Review ResNet."
        )

        val sample3 = GlanceItem(
            title = "Hardware Project Demo",
            description = "Embedded systems project milestone demonstration with external examiner.",
            category = GlanceCategory.EVENT,
            priority = GlancePriority.MEDIUM,
            deadline = "Monday 14:00",
            tasks = listOf(
                TaskItem(title = "Test sensor communication over I2C"),
                TaskItem(title = "Charge battery pack")
            ),
            entities = listOf("I2C", "Embedded"),
            tags = listOf("#Hardware", "#Project"),
            notes = listOf("Engine: Gemma 2B-IT (On-Device)"),
            sourceType = SourceType.TEXT,
            rawText = "Hardware Project Demo on Monday 14:00."
        )

        repository.insertGlance(sample1)
        repository.insertGlance(sample2)
        repository.insertGlance(sample3)
    }

    private fun ParsedGlance.toGlanceItem(): GlanceItem {
        return GlanceItem(
            title = title,
            description = description.ifBlank { rawText.take(160) },
            category = category,
            priority = priority,
            deadline = deadline,
            deadlineEpochMs = deadlineEpochMs,
            tasks = tasks.map { TaskItem(title = it) },
            entities = entities,
            tags = tags,
            notes = if (engineUsed.isNotBlank()) notes + "Engine: $engineUsed" else notes,
            sourceType = sourceType,
            rawText = rawText
        )
    }
}
