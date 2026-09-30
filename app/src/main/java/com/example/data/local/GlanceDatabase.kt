package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [GlanceEntity::class], version = 1, exportSchema = false)
@TypeConverters(GlanceConverters::class)
abstract class GlanceDatabase : RoomDatabase() {
    abstract fun glanceDao(): GlanceDao

    companion object {
        @Volatile
        private var INSTANCE: GlanceDatabase? = null

        fun getDatabase(context: Context): GlanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GlanceDatabase::class.java,
                    "glanceflow_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
