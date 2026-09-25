package com.example.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, OfflineQueueEntity::class],
    version = 2,
    exportSchema = false
)
abstract class KalaSetuDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun offlineQueueDao(): OfflineQueueDao

    companion object {
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN metadata TEXT NOT NULL DEFAULT '{}'")
                // Historical SAVED flags did not prove cloud persistence. Retain all rows for review.
                db.execSQL("UPDATE products SET status = 'DRAFT' WHERE status = 'SAVED'")
                db.execSQL("UPDATE offline_queue SET status = 'queued' WHERE status = 'uploading'")
            }
        }
        @Volatile
        private var INSTANCE: KalaSetuDatabase? = null

        fun getDatabase(context: Context): KalaSetuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KalaSetuDatabase::class.java,
                    "kalasetu_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
