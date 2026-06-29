package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.grip_history.GripHistoryEntity
import com.example.myapplication.data.local.history.ActionHistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class FunctionUsageUi(
    val label: String,
    val count: Int,
    val successCount: Int,
    val failureCount: Int
)

data class SourceUsageUi(
    val source: String,
    val count: Int
)

data class DailyUsageUi(
    val dayLabel: String,
    val count: Int
)

data class GripTypeUsageUi(
    val label: String,
    val count: Int
)

data class GripSensorUsageUi(
    val sensor: String,
    val count: Int
)

data class GripTargetUsageUi(
    val targetPositionId: Int,
    val count: Int
)

data class GripReasonUsageUi(
    val reason: String,
    val count: Int
)

data class AnalyticsUiState(
    val totalActions: Int = 0,
    val successActions: Int = 0,
    val failedActions: Int = 0,
    val successRate: Float = 0f,
    val topFunctions: List<FunctionUsageUi> = emptyList(),
    val sourceUsage: List<SourceUsageUi> = emptyList(),
    val dailyUsage: List<DailyUsageUi> = emptyList(),

    // Estadísticas específicas de agarres
    val totalGrips: Int = 0,
    val successfulGrips: Int = 0,
    val failedGrips: Int = 0,
    val gripSuccessRate: Float = 0f,
    val safeGripCount: Int = 0,
    val fullGripCount: Int = 0,
    val averageGripSeconds: Double = 0.0,
    val averageGripSteps: Double = 0.0,
    val gripTypeUsage: List<GripTypeUsageUi> = emptyList(),
    val topContactSensors: List<GripSensorUsageUi> = emptyList(),
    val targetPositionUsage: List<GripTargetUsageUi> = emptyList(),
    val gripReasonUsage: List<GripReasonUsageUi> = emptyList()
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)

    private val actionHistoryDao = database.actionHistoryDao()
    private val gripHistoryDao = database.gripHistoryDao()

    val uiState: StateFlow<AnalyticsUiState> =
        combine(
            actionHistoryDao.observeAll(),
            gripHistoryDao.observeAll()
        ) { actionHistory, gripHistory ->
            buildAnalytics(
                actionHistory = actionHistory,
                gripHistory = gripHistory
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsUiState()
        )

    private fun buildAnalytics(
        actionHistory: List<ActionHistoryEntity>,
        gripHistory: List<GripHistoryEntity>
    ): AnalyticsUiState {
        val totalActions = actionHistory.size
        val successActions = actionHistory.count { it.success }
        val failedActions = totalActions - successActions

        val successRate = if (totalActions > 0) {
            successActions.toFloat() / totalActions.toFloat()
        } else {
            0f
        }

        val topFunctions = actionHistory
            .groupBy { "${it.source} · ${it.title}" }
            .map { (label, items) ->
                FunctionUsageUi(
                    label = label,
                    count = items.size,
                    successCount = items.count { it.success },
                    failureCount = items.count { !it.success }
                )
            }
            .sortedByDescending { it.count }
            .take(6)

        val sourceUsage = actionHistory
            .groupBy { it.source }
            .map { (source, items) ->
                SourceUsageUi(
                    source = source,
                    count = items.size
                )
            }
            .sortedByDescending { it.count }

        val dailyUsage = buildLastSevenDays(actionHistory)

        val gripStats = buildGripAnalytics(gripHistory)

        return AnalyticsUiState(
            totalActions = totalActions,
            successActions = successActions,
            failedActions = failedActions,
            successRate = successRate,
            topFunctions = topFunctions,
            sourceUsage = sourceUsage,
            dailyUsage = dailyUsage,

            totalGrips = gripStats.totalGrips,
            successfulGrips = gripStats.successfulGrips,
            failedGrips = gripStats.failedGrips,
            gripSuccessRate = gripStats.gripSuccessRate,
            safeGripCount = gripStats.safeGripCount,
            fullGripCount = gripStats.fullGripCount,
            averageGripSeconds = gripStats.averageGripSeconds,
            averageGripSteps = gripStats.averageGripSteps,
            gripTypeUsage = gripStats.gripTypeUsage,
            topContactSensors = gripStats.topContactSensors,
            targetPositionUsage = gripStats.targetPositionUsage,
            gripReasonUsage = gripStats.gripReasonUsage
        )
    }

    private fun buildLastSevenDays(
        history: List<ActionHistoryEntity>
    ): List<DailyUsageUi> {
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())

        val days = mutableListOf<Pair<Long, String>>()
        val calendar = Calendar.getInstance()

        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startOfDay = calendar.timeInMillis
            val label = formatter.format(calendar.time)

            days.add(startOfDay to label)
        }

        return days.mapIndexed { index, pair ->
            val start = pair.first
            val label = pair.second

            val end = if (index < days.lastIndex) {
                days[index + 1].first
            } else {
                start + 24 * 60 * 60 * 1000L
            }

            val count = history.count {
                it.timestampMillis in start until end
            }

            DailyUsageUi(
                dayLabel = label,
                count = count
            )
        }
    }

    private fun buildGripAnalytics(
        gripHistory: List<GripHistoryEntity>
    ): GripAnalyticsResult {
        val totalGrips = gripHistory.size
        val successfulGrips = gripHistory.count { isGripSuccessful(it) }
        val failedGrips = totalGrips - successfulGrips

        val gripSuccessRate = if (totalGrips > 0) {
            successfulGrips.toFloat() / totalGrips.toFloat()
        } else {
            0f
        }

        val safeGripCount = gripHistory.count { it.gripType == "safe" }
        val fullGripCount = gripHistory.count { it.gripType == "full" }

        val elapsedValues = gripHistory
            .mapNotNull { it.elapsedSeconds }
            .filter { it > 0.0 }

        val averageGripSeconds = if (elapsedValues.isNotEmpty()) {
            elapsedValues.average()
        } else {
            0.0
        }

        val stepValues = gripHistory
            .mapNotNull { it.stepCount }
            .filter { it > 0 }

        val averageGripSteps = if (stepValues.isNotEmpty()) {
            stepValues.average()
        } else {
            0.0
        }

        val gripTypeUsage = gripHistory
            .groupBy { it.gripType }
            .map { (type, items) ->
                GripTypeUsageUi(
                    label = friendlyGripType(type),
                    count = items.size
                )
            }
            .sortedByDescending { it.count }

        val topContactSensors = gripHistory
            .mapNotNull { it.contactSensor }
            .filter { it.isNotBlank() }
            .groupingBy { friendlySensorName(it) }
            .eachCount()
            .map { (sensor, count) ->
                GripSensorUsageUi(
                    sensor = sensor,
                    count = count
                )
            }
            .sortedByDescending { it.count }
            .take(6)

        val targetPositionUsage = gripHistory
            .mapNotNull { it.targetPositionId }
            .groupingBy { it }
            .eachCount()
            .map { (targetPositionId, count) ->
                GripTargetUsageUi(
                    targetPositionId = targetPositionId,
                    count = count
                )
            }
            .sortedByDescending { it.count }
            .take(6)

        val gripReasonUsage = gripHistory
            .mapNotNull { it.reason }
            .filter { it.isNotBlank() }
            .groupingBy { friendlyGripReason(it) }
            .eachCount()
            .map { (reason, count) ->
                GripReasonUsageUi(
                    reason = reason,
                    count = count
                )
            }
            .sortedByDescending { it.count }
            .take(6)

        return GripAnalyticsResult(
            totalGrips = totalGrips,
            successfulGrips = successfulGrips,
            failedGrips = failedGrips,
            gripSuccessRate = gripSuccessRate,
            safeGripCount = safeGripCount,
            fullGripCount = fullGripCount,
            averageGripSeconds = averageGripSeconds,
            averageGripSteps = averageGripSteps,
            gripTypeUsage = gripTypeUsage,
            topContactSensors = topContactSensors,
            targetPositionUsage = targetPositionUsage,
            gripReasonUsage = gripReasonUsage
        )
    }

    private fun isGripSuccessful(item: GripHistoryEntity): Boolean {
        return when (item.gripType) {
            "safe" -> item.ok && item.contactDetected == true
            "full" -> item.ok && item.allContactsDetected == true
            else -> item.ok
        }
    }

    private fun friendlyGripType(type: String): String {
        return when (type) {
            "safe" -> "Agarre seguro"
            "full" -> "Agarre completo"
            else -> type
        }
    }

    private fun friendlySensorName(sensor: String): String {
        return when (sensor.lowercase(Locale.getDefault())) {
            "pinky" -> "Meñique"
            "ring" -> "Anular"
            "middle" -> "Medio"
            "index" -> "Índice"
            "thumb" -> "Pulgar"
            "palm" -> "Palma"
            else -> sensor
        }
    }

    private fun friendlyGripReason(reason: String): String {
        return when (reason) {
            "contact_detected" -> "Contacto detectado"
            "all_contacts_detected" -> "Todos en contacto"
            "timeout" -> "Tiempo agotado"
            "initial_contact" -> "Contacto inicial"
            "initial_all_contacts" -> "Contacto inicial completo"
            "hand_not_available" -> "Mano no disponible"
            "capacitive_not_available" -> "Sensores no disponibles"
            "communication_error" -> "Error de comunicación"
            "error" -> "Error"
            else -> reason
        }
    }
}

private data class GripAnalyticsResult(
    val totalGrips: Int,
    val successfulGrips: Int,
    val failedGrips: Int,
    val gripSuccessRate: Float,
    val safeGripCount: Int,
    val fullGripCount: Int,
    val averageGripSeconds: Double,
    val averageGripSteps: Double,
    val gripTypeUsage: List<GripTypeUsageUi>,
    val topContactSensors: List<GripSensorUsageUi>,
    val targetPositionUsage: List<GripTargetUsageUi>,
    val gripReasonUsage: List<GripReasonUsageUi>
)