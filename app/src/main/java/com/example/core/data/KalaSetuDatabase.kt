package com.example.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, OfflineQueueEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KalaSetuDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun offlineQueueDao(): OfflineQueueDao

    companion object {
        @Volatile
        private var INSTANCE: KalaSetuDatabase? = null

        fun getDatabase(context: Context): KalaSetuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KalaSetuDatabase::class.java,
                    "kalasetu_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
