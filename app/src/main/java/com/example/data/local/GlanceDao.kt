package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GlanceDao {
    @Query("SELECT * FROM glance_items WHERE archived = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun getActiveGlances(): Flow<List<GlanceEntity>>

    @Query("SELECT * FROM glance_items WHERE completed = 1 AND archived = 0 ORDER BY createdAt DESC")
    fun getCompletedGlances(): Flow<List<GlanceEntity>>

    @Query("SELECT * FROM glance_items WHERE archived = 1 ORDER BY createdAt DESC")
    fun getArchivedGlances(): Flow<List<GlanceEntity>>

    @Query("SELECT * FROM glance_items ORDER BY createdAt DESC")
    fun getAllGlances(): Flow<List<GlanceEntity>>

    @Query("SELECT * FROM glance_items ORDER BY createdAt DESC")
    suspend fun getAllGlancesSnapshot(): List<GlanceEntity>

    @Query("SELECT * FROM glance_items WHERE archived = 0 AND completed = 0 ORDER BY createdAt DESC")
    suspend fun getActiveGlancesSnapshot(): List<GlanceEntity>

    @Query("SELECT * FROM glance_items WHERE id = :id LIMIT 1")
    suspend fun getGlanceById(id: Long): GlanceEntity?

    @Query("""
        SELECT * FROM glance_items 
        WHERE (title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR rawText LIKE '%' || :query || '%' 
           OR entitiesJson LIKE '%' || :query || '%' 
           OR tasksJson LIKE '%' || :query || '%')
          AND archived = 0
        ORDER BY createdAt DESC
    """)
    fun searchGlances(query: String): Flow<List<GlanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGlance(glance: GlanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(glances: List<GlanceEntity>)

    @Update
    suspend fun updateGlance(glance: GlanceEntity)

    @Query("UPDATE glance_items SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Query("UPDATE glance_items SET archived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Query("UPDATE glance_items SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("DELETE FROM glance_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM glance_items")
    suspend fun clearAll()
}
