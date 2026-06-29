package com.example.myapplication.data.local.grip

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GripSettingsDao {

    @Query("SELECT * FROM grip_settings WHERE id = 'default' LIMIT 1")
    fun observeDefaultSettings(): Flow<GripSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GripSettingsEntity)
}