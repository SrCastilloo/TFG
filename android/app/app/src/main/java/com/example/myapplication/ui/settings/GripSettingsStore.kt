package com.example.myapplication.ui.settings

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.grip.GripSettingsDao
import com.example.myapplication.data.local.grip.GripSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GripSettingsState(
    val ignoredSensors: Set<String> = setOf("ring", "palm", "middle"),
    val gripSpeedName: String = "MEDIUM",
    val targetPositionId: Int = 2
)

object GripSettingsStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var dao: GripSettingsDao? = null
    private var observeJob: Job? = null

    private val defaultSettings = GripSettingsState()

    private val _settings = MutableStateFlow(defaultSettings)
    val settings: StateFlow<GripSettingsState> = _settings.asStateFlow()

    fun init(context: Context) {
        if (dao != null) return

        val database = AppDatabase.getInstance(context)
        dao = database.gripSettingsDao()

        observeJob?.cancel()
        observeJob = scope.launch {
            database.gripSettingsDao()
                .observeDefaultSettings()
                .collect { entity ->
                    if (entity == null) {
                        val defaultEntity = defaultSettings.toEntity()
                        database.gripSettingsDao().saveSettings(defaultEntity)
                        _settings.value = defaultSettings
                    } else {
                        _settings.value = entity.toState()
                    }
                }
        }
    }

    fun save(
        ignoredSensors: Set<String>,
        gripSpeedName: String,
        targetPositionId: Int
    ) {
        val newSettings = GripSettingsState(
            ignoredSensors = ignoredSensors,
            gripSpeedName = gripSpeedName,
            targetPositionId = targetPositionId
        )

        _settings.value = newSettings

        val localDao = dao

        if (localDao != null) {
            scope.launch {
                localDao.saveSettings(newSettings.toEntity())
            }
        }
    }
}

private fun GripSettingsState.toEntity(): GripSettingsEntity {
    return GripSettingsEntity(
        id = "default",
        ignoredSensorsCsv = ignoredSensors.toList().sorted().joinToString(","),
        gripSpeedName = gripSpeedName,
        targetPositionId = targetPositionId,
        updatedAtMillis = System.currentTimeMillis()
    )
}

private fun GripSettingsEntity.toState(): GripSettingsState {
    val sensors = ignoredSensorsCsv
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toSet()

    return GripSettingsState(
        ignoredSensors = sensors,
        gripSpeedName = gripSpeedName,
        targetPositionId = targetPositionId
    )
}