package com.example.myapplication.data.local.grip

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grip_settings")
data class GripSettingsEntity(
    @PrimaryKey
    val id: String = "default",
    val ignoredSensorsCsv: String,
    val gripSpeedName: String,
    val targetPositionId: Int,
    val updatedAtMillis: Long
)