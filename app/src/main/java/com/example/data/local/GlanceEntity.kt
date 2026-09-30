package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.domain.model.GlanceCategory
import com.example.domain.model.GlanceItem
import com.example.domain.model.GlancePriority
import com.example.domain.model.SourceType
import com.example.domain.model.TaskItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "glance_items")
data class GlanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val category: String = GlanceCategory.GENERAL.name,
    val priority: String = GlancePriority.MEDIUM.name,
    val deadline: String? = null,
    val deadlineEpochMs: Long? = null,
    val tasksJson: String = "[]",
    val entitiesJson: String = "[]",
    val tagsJson: String = "[]",
    val notesJson: String = "[]",
    val sourceType: String = SourceType.CAMERA.name,
    val rawText: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completed: Boolean = false,
    val archived: Boolean = false,
    val isPinned: Boolean = false
)

class GlanceConverters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val taskListType = Types.newParameterizedType(List::class.java, TaskItem::class.java)

    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)
    private val taskListAdapter = moshi.adapter<List<TaskItem>>(taskListType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return stringListAdapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            stringListAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromTaskList(value: List<TaskItem>?): String {
        return taskListAdapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toTaskList(value: String?): List<TaskItem> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            taskListAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

fun GlanceEntity.toDomain(converters: GlanceConverters = GlanceConverters()): GlanceItem {
    return GlanceItem(
        id = id,
        title = title,
        description = description,
        category = try { GlanceCategory.valueOf(category) } catch (e: Exception) { GlanceCategory.GENERAL },
        priority = try { GlancePriority.valueOf(priority) } catch (e: Exception) { GlancePriority.MEDIUM },
        deadline = deadline,
        deadlineEpochMs = deadlineEpochMs,
        tasks = converters.toTaskList(tasksJson),
        entities = converters.toStringList(entitiesJson),
        tags = converters.toStringList(tagsJson),
        notes = converters.toStringList(notesJson),
        sourceType = try { SourceType.valueOf(sourceType) } catch (e: Exception) { SourceType.CAMERA },
        rawText = rawText,
        createdAt = createdAt,
        completed = completed,
        archived = archived,
        isPinned = isPinned
    )
}

fun GlanceItem.toEntity(converters: GlanceConverters = GlanceConverters()): GlanceEntity {
    return GlanceEntity(
        id = id,
        title = title,
        description = description,
        category = category.name,
        priority = priority.name,
        deadline = deadline,
        deadlineEpochMs = deadlineEpochMs,
        tasksJson = converters.fromTaskList(tasks),
        entitiesJson = converters.fromStringList(entities),
        tagsJson = converters.fromStringList(tags),
        notesJson = converters.fromStringList(notes),
        sourceType = sourceType.name,
        rawText = rawText,
        createdAt = createdAt,
        completed = completed,
        archived = archived,
        isPinned = isPinned
    )
}
