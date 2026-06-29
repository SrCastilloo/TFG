package com.example.myapplication.data.local.grip_history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grip_history")
data class GripHistoryEntity(
    @PrimaryKey
    val id: String,

    val timestampMillis: Long,

    // "safe" o "full"
    val gripType: String,

    val ok: Boolean,
    val reason: String?,
    val message: String?,

    val elapsedSeconds: Double?,
    val stepCount: Int?,
    val closeStep: Int?,
    val targetPositionId: Int?,

    // Para agarre seguro
    val contactDetected: Boolean?,
    val contactSensor: String?,

    // Para agarre completo
    val allContactsDetected: Boolean?,
    val activeSensorsCsv: String?,
    val contactSensorsCsv: String?,
    val missingSensorsCsv: String?,

    val contactCount: Int?,
    val requiredContactCount: Int?,

    val ignoredSensorsCsv: String?
)