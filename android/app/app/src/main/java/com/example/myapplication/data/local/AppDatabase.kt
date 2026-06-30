package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.auth.AppUserEntity
import com.example.myapplication.data.local.auth.AuthDao
import com.example.myapplication.data.local.auth.AuthSessionEntity
import com.example.myapplication.data.local.grip.GripSettingsDao
import com.example.myapplication.data.local.grip.GripSettingsEntity
import com.example.myapplication.data.local.grip_history.GripHistoryDao
import com.example.myapplication.data.local.grip_history.GripHistoryEntity
import com.example.myapplication.data.local.history.ActionHistoryDao
import com.example.myapplication.data.local.history.ActionHistoryEntity
import com.example.myapplication.data.local.assistant.AssistantAccountDao
import com.example.myapplication.data.local.assistant.AssistantAccountEntity

@Database(
    entities = [
        ActionHistoryEntity::class,
        GripSettingsEntity::class,
        GripHistoryEntity::class,
        AppUserEntity::class,
        AuthSessionEntity::class,
        AssistantAccountEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun actionHistoryDao(): ActionHistoryDao

    abstract fun assistantAccountDao(): AssistantAccountDao

    abstract fun gripSettingsDao(): GripSettingsDao

    abstract fun gripHistoryDao(): GripHistoryDao

    abstract fun authDao(): AuthDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS grip_history (
                        id TEXT NOT NULL PRIMARY KEY,
                        timestampMillis INTEGER NOT NULL,
                        gripType TEXT NOT NULL,
                        ok INTEGER NOT NULL,
                        reason TEXT,
                        message TEXT,
                        elapsedSeconds REAL,
                        stepCount INTEGER,
                        closeStep INTEGER,
                        targetPositionId INTEGER,
                        contactDetected INTEGER,
                        contactSensor TEXT,
                        allContactsDetected INTEGER,
                        activeSensorsCsv TEXT,
                        contactSensorsCsv TEXT,
                        missingSensorsCsv TEXT,
                        contactCount INTEGER,
                        requiredContactCount INTEGER,
                        ignoredSensorsCsv TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS app_users (
                        id TEXT NOT NULL PRIMARY KEY,
                        username TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        passwordHash TEXT NOT NULL,
                        passwordSalt TEXT NOT NULL,
                        role TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_app_users_username
                    ON app_users(username)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS auth_session (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        loggedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            ALTER TABLE action_history 
            ADD COLUMN userId TEXT
            """.trimIndent()
                )

                database.execSQL(
                    """
            ALTER TABLE grip_history 
            ADD COLUMN userId TEXT
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS assistant_accounts (
                userId TEXT NOT NULL PRIMARY KEY,
                selectedProvider TEXT NOT NULL,
                openAiApiKeyEncrypted TEXT,
                geminiApiKeyEncrypted TEXT,
                openAiModel TEXT NOT NULL,
                geminiModel TEXT NOT NULL,
                updatedAtMillis INTEGER NOT NULL
            )
            """.trimIndent()
                )
            }
        }
    }
}