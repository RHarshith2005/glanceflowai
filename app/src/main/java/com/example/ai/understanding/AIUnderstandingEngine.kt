package com.example.ai.understanding

import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlancePriority
import com.example.domain.model.SourceType

enum class AIEngineMode(val label: String, val description: String) {
    GEMMA_LOCAL("GEMMA 2B (LOCAL)", "Google Gemma 2B-IT On-Device Neural Model"),
    OFFLINE_NLP("OFFLINE NLP", "Rule-based heuristic parsing engine"),
    CLOUD_GEMINI("GEMINI 3.5-FLASH", "Cloud API with automatic offline fallback")
}

data class ParsedGlance(
    val title: String,
    val description: String = "",
    val category: GlanceCategory = GlanceCategory.GENERAL,
    val priority: GlancePriority = GlancePriority.MEDIUM,
    val deadline: String? = null,
    val deadlineEpochMs: Long? = null,
    val tasks: List<String> = emptyList(),
    val entities: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val sourceType: SourceType = SourceType.CAMERA,
    val rawText: String = "",
    val engineUsed: String = "Gemma 2B-IT (On-Device)"
)

interface AIUnderstandingEngine {
    suspend fun processText(
        rawText: String,
        sourceType: SourceType = SourceType.CAMERA,
        engineMode: AIEngineMode = AIEngineMode.GEMMA_LOCAL
    ): ParsedGlance
}
