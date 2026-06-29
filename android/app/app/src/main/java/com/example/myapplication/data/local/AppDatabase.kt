package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.grip.GripSettingsDao
import com.example.myapplication.data.local.grip.GripSettingsEntity
import com.example.myapplication.data.local.history.ActionHistoryDao
import com.example.myapplication.data.local.history.ActionHistoryEntity

@Database(
    entities = [
        ActionHistoryEntity::class,
        GripSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun actionHistoryDao(): ActionHistoryDao

    abstract fun gripSettingsDao(): GripSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS grip_settings (
                        id TEXT NOT NULL PRIMARY KEY,
                        ignoredSensorsCsv TEXT NOT NULL,
                        gripSpeedName TEXT NOT NULL,
                        targetPositionId INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tfg_local_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}