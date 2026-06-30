package com.example.myapplication.ui.history

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.grip_history.GripHistoryDao
import com.example.myapplication.data.local.grip_history.GripHistoryEntity
import com.example.myapplication.data.remote.dto.FullGripDto
import com.example.myapplication.data.remote.dto.FullGripRequest
import com.example.myapplication.data.remote.dto.SafeGripDto
import com.example.myapplication.data.remote.dto.SafeGripRequest
import com.example.myapplication.ui.auth.AuthSessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

object GripHistoryStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var dao: GripHistoryDao? = null

    fun init(context: Context) {
        if (dao != null) return

        val database = AppDatabase.getInstance(context)
        dao = database.gripHistoryDao()
    }

    fun addSafeGrip(
        response: SafeGripDto,
        request: SafeGripRequest
    ) {
        val localDao = dao ?: return

        val entity = GripHistoryEntity(
            id = UUID.randomUUID().toString(),
            timestampMillis = System.currentTimeMillis(),

            gripType = "safe",

            ok = response.ok,
            reason = response.reason,
            message = response.message,

            elapsedSeconds = response.elapsed_seconds,
            stepCount = response.step_count,
            closeStep = response.close_step ?: request.close_step,
            targetPositionId = response.target_position_id ?: request.target_position_id,

            contactDetected = response.contact_detected,
            contactSensor = response.contact_sensor,

            allContactsDetected = null,
            activeSensorsCsv = null,
            contactSensorsCsv = null,
            missingSensorsCsv = null,

            contactCount = response.contact_count,
            requiredContactCount = null,

            ignoredSensorsCsv = (response.ignored_sensors ?: request.ignored_sensors).toCsv(),

            userId = AuthSessionStore.currentUserId()
        )

        scope.launch {
            localDao.insert(entity)
        }
    }

    fun addFullGrip(
        response: FullGripDto,
        request: FullGripRequest
    ) {
        val localDao = dao ?: return

        val entity = GripHistoryEntity(
            id = UUID.randomUUID().toString(),
            timestampMillis = System.currentTimeMillis(),

            gripType = "full",

            ok = response.ok,
            reason = response.reason,
            message = response.message,

            elapsedSeconds = response.elapsed_seconds,
            stepCount = response.step_count,
            closeStep = response.close_step ?: request.close_step,
            targetPositionId = null,

            contactDetected = null,
            contactSensor = null,

            allContactsDetected = response.all_contacts_detected,
            activeSensorsCsv = response.active_sensors.toCsvOrNull(),
            contactSensorsCsv = response.contact_sensors.toCsvOrNull(),
            missingSensorsCsv = response.missing_sensors.toCsvOrNull(),

            contactCount = response.contact_count,
            requiredContactCount = response.required_contact_count,

            ignoredSensorsCsv = (response.ignored_sensors ?: request.ignored_sensors).toCsv(),

            userId = AuthSessionStore.currentUserId()
        )

        scope.launch {
            localDao.insert(entity)
        }
    }

    fun addSafeGripFailure(
        request: SafeGripRequest,
        errorMessage: String,
        elapsedSeconds: Double? = null
    ) {
        val localDao = dao ?: return

        val entity = GripHistoryEntity(
            id = UUID.randomUUID().toString(),
            timestampMillis = System.currentTimeMillis(),

            gripType = "safe",

            ok = false,
            reason = "communication_error",
            message = errorMessage,

            elapsedSeconds = elapsedSeconds,
            stepCount = null,
            closeStep = request.close_step,
            targetPositionId = request.target_position_id,

            contactDetected = false,
            contactSensor = null,

            allContactsDetected = null,
            activeSensorsCsv = null,
            contactSensorsCsv = null,
            missingSensorsCsv = null,

            contactCount = 0,
            requiredContactCount = null,

            ignoredSensorsCsv = request.ignored_sensors.toCsv(),

            userId = AuthSessionStore.currentUserId()
        )

        scope.launch {
            localDao.insert(entity)
        }
    }

    fun addFullGripFailure(
        request: FullGripRequest,
        errorMessage: String,
        elapsedSeconds: Double? = null
    ) {
        val localDao = dao ?: return

        val entity = GripHistoryEntity(
            id = UUID.randomUUID().toString(),
            timestampMillis = System.currentTimeMillis(),

            gripType = "full",

            ok = false,
            reason = "communication_error",
            message = errorMessage,

            elapsedSeconds = elapsedSeconds,
            stepCount = null,
            closeStep = request.close_step,
            targetPositionId = null,

            contactDetected = null,
            contactSensor = null,

            allContactsDetected = false,
            activeSensorsCsv = null,
            contactSensorsCsv = null,
            missingSensorsCsv = null,

            contactCount = 0,
            requiredContactCount = null,

            ignoredSensorsCsv = request.ignored_sensors.toCsv(),

            userId = AuthSessionStore.currentUserId()
        )

        scope.launch {
            localDao.insert(entity)
        }
    }
}

private fun List<String>.toCsv(): String {
    return this
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .sorted()
        .joinToString(",")
}

private fun List<String>?.toCsvOrNull(): String? {
    if (this == null) return null

    return this
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .sorted()
        .joinToString(",")
}