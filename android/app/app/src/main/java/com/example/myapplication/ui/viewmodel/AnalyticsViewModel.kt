package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.auth.AppUserEntity
import com.example.myapplication.data.local.grip_history.GripHistoryEntity
import com.example.myapplication.data.local.history.ActionHistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class AnalyticsPeriod(
    val label: String
) {
    TODAY("Hoy"),
    LAST_7_DAYS("7 días"),
    LAST_30_DAYS("30 días"),
    ALL("Todo")
}

data class AnalyticsUserFilterUi(
    val userId: String?,
    val label: String,
    val isAllUsers: Boolean = false
)

data class AdminUserSummaryUi(
    val userId: String,
    val displayName: String,
    val username: String,
    val role: String,
    val actionCount: Int,
    val gripCount: Int,
    val successCount: Int,
    val failureCount: Int,
    val lastActivityLabel: String
) {
    val isAdmin: Boolean
        get() = role == "ADMIN"

    val totalRecords: Int
        get() = actionCount + gripCount

    val successRate: Float
        get() = if (totalRecords > 0) {
            successCount.toFloat() / totalRecords.toFloat()
        } else {
            0f
        }
}

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
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.ALL,

    val isAdmin: Boolean = false,
    val selectedUserId: String? = null,
    val effectiveUserId: String? = null,
    val userFilters: List<AnalyticsUserFilterUi> = emptyList(),

    val totalUsers: Int = 0,
    val adminUsers: Int = 0,
    val standardUsers: Int = 0,
    val selectedUserLabel: String = "Todos los usuarios",
    val adminUserSummaries: List<AdminUserSummaryUi> = emptyList(),
    val totalRecords: Int = 0,

    val totalActions: Int = 0,
    val successActions: Int = 0,
    val failedActions: Int = 0,
    val successRate: Float = 0f,
    val topFunctions: List<FunctionUsageUi> = emptyList(),
    val sourceUsage: List<SourceUsageUi> = emptyList(),
    val dailyUsage: List<DailyUsageUi> = emptyList(),

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

private data class AnalyticsFilters(
    val period: AnalyticsPeriod,
    val selectedUserId: String?
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)

    private val actionHistoryDao = database.actionHistoryDao()
    private val gripHistoryDao = database.gripHistoryDao()
    private val authDao = database.authDao()

    private val selectedPeriod = MutableStateFlow(AnalyticsPeriod.ALL)
    private val selectedUserId = MutableStateFlow<String?>(null)

    private val filters =
        combine(
            selectedPeriod,
            selectedUserId
        ) { period, userId ->
            AnalyticsFilters(
                period = period,
                selectedUserId = userId
            )
        }

    fun setPeriod(period: AnalyticsPeriod) {
        selectedPeriod.value = period
    }

    fun setUserFilter(userId: String?) {
        selectedUserId.value = userId
    }

    val uiState: StateFlow<AnalyticsUiState> =
        combine(
            actionHistoryDao.observeAll(),
            gripHistoryDao.observeAll(),
            authDao.observeCurrentUser(),
            authDao.observeAllUsers(),
            filters
        ) { actionHistory, gripHistory, currentUser, allUsers, activeFilters ->

            if (currentUser == null) {
                return@combine AnalyticsUiState()
            }

            val isAdmin = currentUser.role == "ADMIN"

            val effectiveUserId = if (isAdmin) {
                activeFilters.selectedUserId
            } else {
                currentUser.id
            }

            val userFilteredActions = filterActionsByUser(
                actions = actionHistory,
                isAdmin = isAdmin,
                effectiveUserId = effectiveUserId
            )

            val userFilteredGrips = filterGripsByUser(
                grips = gripHistory,
                isAdmin = isAdmin,
                effectiveUserId = effectiveUserId
            )

            val periodFilteredActions = filterActionsByPeriod(
                actions = userFilteredActions,
                period = activeFilters.period
            )

            val periodFilteredGrips = filterGripsByPeriod(
                grips = userFilteredGrips,
                period = activeFilters.period
            )

            val userFilters = buildUserFilters(
                currentUser = currentUser,
                allUsers = allUsers,
                isAdmin = isAdmin
            )

            buildAnalytics(
                actionHistory = periodFilteredActions,
                gripHistory = periodFilteredGrips,
                allActionHistory = actionHistory,
                allGripHistory = gripHistory,
                allUsers = allUsers,
                selectedPeriod = activeFilters.period,
                isAdmin = isAdmin,
                selectedUserId = activeFilters.selectedUserId,
                effectiveUserId = effectiveUserId,
                userFilters = userFilters
            )

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsUiState()
        )

    private fun buildAnalytics(
        actionHistory: List<ActionHistoryEntity>,
        gripHistory: List<GripHistoryEntity>,
        allActionHistory: List<ActionHistoryEntity>,
        allGripHistory: List<GripHistoryEntity>,
        allUsers: List<AppUserEntity>,
        selectedPeriod: AnalyticsPeriod,
        isAdmin: Boolean,
        selectedUserId: String?,
        effectiveUserId: String?,
        userFilters: List<AnalyticsUserFilterUi>
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

        val periodAllActions = filterActionsByPeriod(
            actions = allActionHistory,
            period = selectedPeriod
        )

        val periodAllGrips = filterGripsByPeriod(
            grips = allGripHistory,
            period = selectedPeriod
        )

        val adminUserSummaries = if (isAdmin) {
            buildAdminUserSummaries(
                users = allUsers,
                actionHistory = periodAllActions,
                gripHistory = periodAllGrips
            )
        } else {
            emptyList()
        }


        val selectedUserLabel = buildSelectedUserLabel(
            userFilters = userFilters,
            selectedUserId = selectedUserId
        )

        return AnalyticsUiState(
            selectedPeriod = selectedPeriod,

            isAdmin = isAdmin,
            selectedUserId = selectedUserId,
            effectiveUserId = effectiveUserId,
            userFilters = userFilters,

            totalUsers = if (isAdmin) allUsers.size else 1,
            adminUsers = if (isAdmin) allUsers.count { it.role == "ADMIN" } else 0,
            standardUsers = if (isAdmin) allUsers.count { it.role != "ADMIN" } else 0,
            selectedUserLabel = selectedUserLabel,
            adminUserSummaries = adminUserSummaries,
            totalRecords = totalActions + gripStats.totalGrips,

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

    private fun buildUserFilters(
        currentUser: AppUserEntity,
        allUsers: List<AppUserEntity>,
        isAdmin: Boolean
    ): List<AnalyticsUserFilterUi> {
        if (!isAdmin) {
            return listOf(
                AnalyticsUserFilterUi(
                    userId = currentUser.id,
                    label = "👤 ${currentUser.displayName}",
                    isAllUsers = false
                )
            )
        }

        val sortedUsers = allUsers.sortedWith(
            compareByDescending<AppUserEntity> { it.role == "ADMIN" }
                .thenBy { it.displayName.lowercase(Locale.getDefault()) }
        )

        val allUsersFilter = AnalyticsUserFilterUi(
            userId = null,
            label = "🌍 Todos los usuarios",
            isAllUsers = true
        )

        val userFilters = sortedUsers.map { user ->
            AnalyticsUserFilterUi(
                userId = user.id,
                label = if (user.role == "ADMIN") {
                    "👑 ${user.displayName}"
                } else {
                    "👤 ${user.displayName}"
                },
                isAllUsers = false
            )
        }

        return listOf(allUsersFilter) + userFilters
    }

    private fun filterActionsByUser(
        actions: List<ActionHistoryEntity>,
        isAdmin: Boolean,
        effectiveUserId: String?
    ): List<ActionHistoryEntity> {
        if (isAdmin && effectiveUserId == null) return actions

        return actions.filter { it.userId == effectiveUserId }
    }

    private fun filterGripsByUser(
        grips: List<GripHistoryEntity>,
        isAdmin: Boolean,
        effectiveUserId: String?
    ): List<GripHistoryEntity> {
        if (isAdmin && effectiveUserId == null) return grips

        return grips.filter { it.userId == effectiveUserId }
    }

    private fun filterActionsByPeriod(
        actions: List<ActionHistoryEntity>,
        period: AnalyticsPeriod
    ): List<ActionHistoryEntity> {
        val startMillis = periodStartMillis(period) ?: return actions

        return actions.filter { action ->
            action.timestampMillis >= startMillis
        }
    }

    private fun filterGripsByPeriod(
        grips: List<GripHistoryEntity>,
        period: AnalyticsPeriod
    ): List<GripHistoryEntity> {
        val startMillis = periodStartMillis(period) ?: return grips

        return grips.filter { grip ->
            grip.timestampMillis >= startMillis
        }
    }

    private fun periodStartMillis(period: AnalyticsPeriod): Long? {
        if (period == AnalyticsPeriod.ALL) return null

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        when (period) {
            AnalyticsPeriod.TODAY -> Unit
            AnalyticsPeriod.LAST_7_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -6)
            AnalyticsPeriod.LAST_30_DAYS -> calendar.add(Calendar.DAY_OF_YEAR, -29)
            AnalyticsPeriod.ALL -> return null
        }

        return calendar.timeInMillis
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
    private fun buildSelectedUserLabel(
        userFilters: List<AnalyticsUserFilterUi>,
        selectedUserId: String?
    ): String {
        return userFilters.firstOrNull { filter ->
            if (filter.isAllUsers) {
                selectedUserId == null
            } else {
                selectedUserId == filter.userId
            }
        }?.label ?: "Todos los usuarios"
    }

    private fun buildAdminUserSummaries(
        users: List<AppUserEntity>,
        actionHistory: List<ActionHistoryEntity>,
        gripHistory: List<GripHistoryEntity>
    ): List<AdminUserSummaryUi> {
        return users.map { user ->
            val userActions = actionHistory.filter { it.userId == user.id }
            val userGrips = gripHistory.filter { it.userId == user.id }

            val actionSuccessCount = userActions.count { it.success }
            val actionFailureCount = userActions.size - actionSuccessCount

            val gripSuccessCount = userGrips.count { isGripSuccessful(it) }
            val gripFailureCount = userGrips.size - gripSuccessCount

            val lastActivityMillis = (
                    userActions.map { it.timestampMillis } +
                            userGrips.map { it.timestampMillis }
                    ).maxOrNull()

            AdminUserSummaryUi(
                userId = user.id,
                displayName = user.displayName,
                username = user.username,
                role = user.role,
                actionCount = userActions.size,
                gripCount = userGrips.size,
                successCount = actionSuccessCount + gripSuccessCount,
                failureCount = actionFailureCount + gripFailureCount,
                lastActivityLabel = formatLastActivity(lastActivityMillis)
            )
        }
            .sortedWith(
                compareByDescending<AdminUserSummaryUi> { it.totalRecords }
                    .thenBy { it.displayName.lowercase(Locale.getDefault()) }
            )
    }

    private fun formatLastActivity(timestampMillis: Long?): String {
        if (timestampMillis == null) return "Sin actividad"

        val formatter = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

        return formatter.format(timestampMillis)
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