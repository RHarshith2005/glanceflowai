package com.example.ai.understanding

import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlancePriority
import com.example.domain.model.SourceType
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object OfflineUnderstandingEngine {

    fun parse(rawText: String, sourceType: SourceType = SourceType.CAMERA): ParsedGlance {
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (lines.isEmpty()) {
            return ParsedGlance(
                title = "Untitled Glance",
                category = GlanceCategory.NOTE,
                priority = GlancePriority.LOW,
                sourceType = sourceType,
                rawText = rawText,
                engineUsed = "Offline Local NLP"
            )
        }

        // 1. Extract Deadline & Normalized Epoch
        val deadlineResult = extractDeadline(rawText)

        // 2. Extract Category
        val category = detectCategory(rawText, lines)

        // 3. Extract Priority
        val priority = detectPriority(rawText, deadlineResult.text)

        // 4. Extract Entities
        val entities = extractEntities(lines)

        // 5. Extract Tasks
        val tasks = extractTasks(lines, deadlineResult.text)

        // 6. Extract Title
        val title = extractTitle(lines, category)

        // 7. Extract Tags & Notes
        val tags = mutableListOf<String>()
        tags.add("#${category.label}")
        if (priority == GlancePriority.HIGH) tags.add("#Urgent")
        if (sourceType == SourceType.VOICE) tags.add("#VoiceCapture")
        if (sourceType == SourceType.CAMERA) tags.add("#VisionScan")
        entities.take(2).forEach { tags.add("#${it.replace(" ", "")}") }

        val notes = mutableListOf<String>()
        lines.forEach { line ->
            if (line.contains("note:", ignoreCase = true) ||
                line.contains("dataset:", ignoreCase = true) ||
                line.contains("ref:", ignoreCase = true) ||
                line.contains("url:", ignoreCase = true)
            ) {
                notes.add(line)
            }
        }

        return ParsedGlance(
            title = title,
            description = if (lines.size > 1) lines.drop(1).take(3).joinToString(" • ") else "",
            category = category,
            priority = priority,
            deadline = deadlineResult.text,
            deadlineEpochMs = deadlineResult.epochMs,
            tasks = if (tasks.isNotEmpty()) tasks else listOf("Review: $title"),
            entities = entities,
            tags = tags.distinct(),
            notes = notes,
            sourceType = sourceType,
            rawText = rawText,
            engineUsed = "Offline Local NLP (iQOO Ambient)"
        )
    }

    private data class DeadlineInfo(val text: String?, val epochMs: Long?)

    private fun extractDeadline(text: String): DeadlineInfo {
        val lower = text.lowercase(Locale.ROOT)
        val now = Calendar.getInstance()

        // Match "tomorrow at 10 AM", "tomorrow at 10:30", etc.
        val timeRegex = Pattern.compile("(\\d{1,2})(:\\d{2})?\\s*(am|pm)", Pattern.CASE_INSENSITIVE)
        val timeMatcher = timeRegex.matcher(lower)
        var parsedHour: Int? = null
        var parsedMin: Int? = null
        if (timeMatcher.find()) {
            val h = timeMatcher.group(1)?.toIntOrNull() ?: 9
            val m = timeMatcher.group(2)?.removePrefix(":")?.toIntOrNull() ?: 0
            val ampm = timeMatcher.group(3)?.lowercase()
            parsedHour = if (ampm == "pm" && h < 12) h + 12 else if (ampm == "am" && h == 12) 0 else h
            parsedMin = m
        }

        // Relative days
        if (lower.contains("today") || lower.contains("tonight")) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, parsedHour ?: 21)
                set(Calendar.MINUTE, parsedMin ?: 0)
                set(Calendar.SECOND, 0)
            }
            val timeStr = if (parsedHour != null) String.format(Locale.US, "Today at %02d:%02d", parsedHour, parsedMin ?: 0) else "Today"
            return DeadlineInfo(timeStr, cal.timeInMillis)
        }

        if (lower.contains("tomorrow")) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, parsedHour ?: 17)
                set(Calendar.MINUTE, parsedMin ?: 0)
                set(Calendar.SECOND, 0)
            }
            val timeStr = if (parsedHour != null) String.format(Locale.US, "Tomorrow at %02d:%02d", parsedHour, parsedMin ?: 0) else "Tomorrow"
            return DeadlineInfo(timeStr, cal.timeInMillis)
        }

        // Days of week
        val days = listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
        for ((idx, day) in days.withIndex()) {
            if (lower.contains(day)) {
                val targetDayOfWeek = idx + 1
                val cal = Calendar.getInstance()
                var diff = targetDayOfWeek - cal.get(Calendar.DAY_OF_WEEK)
                if (diff <= 0) diff += 7
                cal.add(Calendar.DAY_OF_YEAR, diff)
                cal.set(Calendar.HOUR_OF_DAY, parsedHour ?: 17)
                cal.set(Calendar.MINUTE, parsedMin ?: 0)
                cal.set(Calendar.SECOND, 0)
                val dayCapitalized = day.replaceFirstChar { it.uppercase() }
                val timeStr = if (parsedHour != null) "$dayCapitalized at ${timeMatcher.group(0)}" else dayCapitalized
                return DeadlineInfo(timeStr, cal.timeInMillis)
            }
        }

        // Check explicit "Submit: Friday" or "Deadline: Oct 5"
        val submitPattern = Pattern.compile("(submit|deadline|due|by)\\s*[:=-]?\\s*([a-zA-Z0-9 ]{3,15})", Pattern.CASE_INSENSITIVE)
        val submitMatcher = submitPattern.matcher(text)
        if (submitMatcher.find()) {
            val extracted = submitMatcher.group(2)?.trim()
            if (!extracted.isNullOrBlank()) {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }
                return DeadlineInfo(extracted, cal.timeInMillis)
            }
        }

        return DeadlineInfo(null, null)
    }

    private fun detectCategory(rawText: String, lines: List<String>): GlanceCategory {
        val lower = rawText.lowercase(Locale.ROOT)
        return when {
            lower.contains("assignment") || lower.contains("homework") || lower.contains("lab") || lower.contains("cifar") || lower.contains("dataset") -> GlanceCategory.ASSIGNMENT
            lower.contains("exam") || lower.contains("quiz") || lower.contains("midterm") || lower.contains("test") -> GlanceCategory.EXAM
            lower.contains("meeting") || lower.contains("standup") || lower.contains("sync") || lower.contains("discussion") || lower.contains("interview") -> GlanceCategory.MEETING
            lower.contains("remind me") || lower.contains("reminder") || lower.contains("alarm") || lower.contains("deadline") -> GlanceCategory.REMINDER
            lower.contains("buy") || lower.contains("purchase") || lower.contains("shopping") || lower.contains("store") || lower.contains("order") -> GlanceCategory.SHOPPING
            lower.contains("event") || lower.contains("hackathon") || lower.contains("workshop") || lower.contains("webinar") || lower.contains("conference") -> GlanceCategory.EVENT
            lower.contains("todo") || lower.contains("task") || lower.contains("implement") || lower.contains("build") -> GlanceCategory.TASK
            lower.contains("urgent") || lower.contains("important") || lower.contains("critical") -> GlanceCategory.IMPORTANT
            else -> GlanceCategory.NOTE
        }
    }

    private fun detectPriority(rawText: String, deadline: String?): GlancePriority {
        val lower = rawText.lowercase(Locale.ROOT)
        return when {
            lower.contains("urgent") || lower.contains("asap") || lower.contains("critical") ||
            lower.contains("high priority") || lower.contains("important exam") ||
            lower.contains("tomorrow") || lower.contains("today") ||
            (deadline != null && (deadline.contains("Today", ignoreCase = true) || deadline.contains("Tomorrow", ignoreCase = true) || deadline.contains("Friday", ignoreCase = true))) -> GlancePriority.HIGH

            lower.contains("low") || lower.contains("someday") || lower.contains("buy notebook") || lower.contains("optional") -> GlancePriority.LOW
            else -> GlancePriority.MEDIUM
        }
    }

    private fun extractTitle(lines: List<String>, category: GlanceCategory): String {
        // Clean leading words like "AI Assignment", "Machine Learning Lab", etc.
        val firstLine = lines.firstOrNull()?.trim() ?: "Quick Glance"
        val cleaned = firstLine
            .removePrefix("#")
            .removePrefix("-")
            .removePrefix("*")
            .trim()

        // If first line starts with "Remind me to", format nicely
        if (cleaned.startsWith("remind me to ", ignoreCase = true)) {
            val task = cleaned.substring(13).trim()
            return task.replaceFirstChar { it.uppercase() }
        }

        if (cleaned.length in 3..40) {
            return cleaned
        }

        return "${category.label} - ${cleaned.take(24)}..."
    }

    private fun extractTasks(lines: List<String>, deadlineText: String?): List<String> {
        val tasks = mutableListOf<String>()
        val actionVerbs = listOf(
            "implement", "build", "submit", "bring", "prepare", "read",
            "review", "buy", "write", "finish", "test", "deploy", "design", "call", "send", "create"
        )

        for (line in lines) {
            val trimmed = line.trim()
            val lower = trimmed.lowercase(Locale.ROOT)

            // Skip title-like or metadata-only lines
            if (trimmed.startsWith("Dataset:", ignoreCase = true) ||
                trimmed.startsWith("Submit:", ignoreCase = true) ||
                trimmed.startsWith("Deadline:", ignoreCase = true) ||
                trimmed.equals(lines.firstOrNull(), ignoreCase = true)
            ) {
                continue
            }

            // Bullet or dash line
            val isBullet = trimmed.startsWith("-") || trimmed.startsWith("*") ||
                    trimmed.startsWith("•") || trimmed.startsWith("✓") ||
                    trimmed.matches(Regex("^\\d+[.)].*"))

            val cleanText = trimmed
                .replace(Regex("^[-*•✓\\d.)]+\\s*"), "")
                .trim()

            val startsWithVerb = actionVerbs.any { lower.startsWith(it) || cleanText.lowercase(Locale.ROOT).startsWith(it) }

            if (isBullet || startsWithVerb) {
                if (cleanText.isNotBlank() && cleanText.length > 2) {
                    tasks.add(cleanText.replaceFirstChar { it.uppercase() })
                }
            }
        }

        return tasks.distinct()
    }

    private fun extractEntities(lines: List<String>): List<String> {
        val entities = mutableListOf<String>()
        val commonTechOrEntities = listOf(
            "CIFAR-10", "CNN", "RNN", "LSTM", "PyTorch", "TensorFlow", "React", "Keras",
            "Room 402", "Room 101", "Lab 3", "CS401", "CS101", "Prof. Sharma", "Dr. Rao",
            "Docker", "AWS", "Figma", "Jetpack Compose"
        )

        val fullText = lines.joinToString(" ")
        for (item in commonTechOrEntities) {
            if (fullText.contains(item, ignoreCase = true)) {
                entities.add(item)
            }
        }

        // Check for "Dataset: <value>"
        val datasetRegex = Pattern.compile("Dataset:\\s*([A-Za-z0-9_-]+)", Pattern.CASE_INSENSITIVE)
        val matcher = datasetRegex.matcher(fullText)
        if (matcher.find()) {
            matcher.group(1)?.let { entities.add(it) }
        }

        return entities.distinct()
    }
}
