package com.example.domain.model

enum class GlancePriority(val label: String, val level: Int) {
    HIGH("HIGH", 3),
    MEDIUM("MEDIUM", 2),
    LOW("LOW", 1);

    companion object {
        fun fromString(value: String): GlancePriority {
            return when (value.trim().uppercase()) {
                "HIGH", "CRITICAL", "URGENT" -> HIGH
                "LOW", "TRIVIAL" -> LOW
                else -> MEDIUM
            }
        }
    }
}

enum class GlanceCategory(val label: String, val iconName: String) {
    ASSIGNMENT("Assignment", "School"),
    TASK("Task", "CheckCircle"),
    REMINDER("Reminder", "Alarm"),
    EVENT("Event", "Event"),
    MEETING("Meeting", "Groups"),
    EXAM("Exam", "Quiz"),
    SHOPPING("Shopping", "ShoppingCart"),
    NOTE("Note", "Description"),
    IMPORTANT("Important", "Warning"),
    GENERAL("General", "Label");

    companion object {
        fun fromString(value: String): GlanceCategory {
            return values().firstOrNull { it.label.equals(value.trim(), ignoreCase = true) }
                ?: when (value.trim().lowercase()) {
                    "homework", "project", "lab" -> ASSIGNMENT
                    "test", "quiz", "midterm", "final" -> EXAM
                    "call", "appointment", "discussion" -> MEETING
                    "schedule", "deadline", "alert" -> REMINDER
                    "todo", "action" -> TASK
                    else -> GENERAL
                }
        }
    }
}

enum class SourceType {
    CAMERA,
    VOICE,
    TEXT,
    IMPORT
}

data class TaskItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false
)

data class GlanceItem(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val category: GlanceCategory = GlanceCategory.GENERAL,
    val priority: GlancePriority = GlancePriority.MEDIUM,
    val deadline: String? = null,
    val deadlineEpochMs: Long? = null,
    val tasks: List<TaskItem> = emptyList(),
    val entities: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val sourceType: SourceType = SourceType.CAMERA,
    val rawText: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completed: Boolean = false,
    val archived: Boolean = false,
    val isPinned: Boolean = false
)
