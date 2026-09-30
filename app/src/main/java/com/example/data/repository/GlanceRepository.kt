package com.example.data.repository

import com.example.data.local.GlanceConverters
import com.example.data.local.GlanceDao
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.domain.model.GlanceItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GlanceRepository(private val glanceDao: GlanceDao) {
    private val converters = GlanceConverters()

    fun getActiveGlances(): Flow<List<GlanceItem>> {
        return glanceDao.getActiveGlances().map { entities ->
            entities.map { it.toDomain(converters) }
        }
    }

    fun getCompletedGlances(): Flow<List<GlanceItem>> {
        return glanceDao.getCompletedGlances().map { entities ->
            entities.map { it.toDomain(converters) }
        }
    }

    fun getArchivedGlances(): Flow<List<GlanceItem>> {
        return glanceDao.getArchivedGlances().map { entities ->
            entities.map { it.toDomain(converters) }
        }
    }

    fun getAllGlances(): Flow<List<GlanceItem>> {
        return glanceDao.getAllGlances().map { entities ->
            entities.map { it.toDomain(converters) }
        }
    }

    suspend fun getAllGlancesSnapshot(): List<GlanceItem> {
        return glanceDao.getAllGlancesSnapshot().map { it.toDomain(converters) }
    }

    fun searchGlances(query: String): Flow<List<GlanceItem>> {
        return glanceDao.searchGlances(query).map { entities ->
            entities.map { it.toDomain(converters) }
        }
    }

    suspend fun getGlanceById(id: Long): GlanceItem? {
        return glanceDao.getGlanceById(id)?.toDomain(converters)
    }

    suspend fun insertGlance(item: GlanceItem): Long {
        return glanceDao.insertGlance(item.toEntity(converters))
    }

    suspend fun insertAll(items: List<GlanceItem>) {
        glanceDao.insertAll(items.map { it.toEntity(converters) })
    }

    suspend fun updateGlance(item: GlanceItem) {
        glanceDao.updateGlance(item.toEntity(converters))
    }

    suspend fun setCompleted(id: Long, completed: Boolean) {
        glanceDao.setCompleted(id, completed)
    }

    suspend fun setArchived(id: Long, archived: Boolean) {
        glanceDao.setArchived(id, archived)
    }

    suspend fun setPinned(id: Long, isPinned: Boolean) {
        glanceDao.setPinned(id, isPinned)
    }

    suspend fun deleteGlance(id: Long) {
        glanceDao.deleteById(id)
    }

    suspend fun clearAll() {
        glanceDao.clearAll()
    }
}
