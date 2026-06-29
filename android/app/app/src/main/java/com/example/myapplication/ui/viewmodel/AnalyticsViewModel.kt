package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.history.ActionHistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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

data class AnalyticsUiState(
    val totalActions: Int = 0,
    val successActions: Int = 0,
    val failedActions: Int = 0,
    val successRate: Float = 0f,
    val topFunctions: List<FunctionUsageUi> = emptyList(),
    val sourceUsage: List<SourceUsageUi> = emptyList(),
    val dailyUsage: List<DailyUsageUi> = emptyList()
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase
        .getInstance(application)
        .actionHistoryDao()

    val uiState: StateFlow<AnalyticsUiState> =
        dao.observeAll()
            .map { history ->
                buildAnalytics(history)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AnalyticsUiState()
            )

    private fun buildAnalytics(history: List<ActionHistoryEntity>): AnalyticsUiState {
        val total = history.size
        val success = history.count { it.success }
        val failed = total - success

        val successRate = if (total > 0) {
            success.toFloat() / total.toFloat()
        } else {
            0f
        }

        val topFunctions = history
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

        val sourceUsage = history
            .groupBy { it.source }
            .map { (source, items) ->
                SourceUsageUi(
                    source = source,
                    count = items.size
                )
            }
            .sortedByDescending { it.count }

        val dailyUsage = buildLastSevenDays(history)

        return AnalyticsUiState(
            totalActions = total,
            successActions = success,
            failedActions = failed,
            successRate = successRate,
            topFunctions = topFunctions,
            sourceUsage = sourceUsage,
            dailyUsage = dailyUsage
        )
    }

    private fun buildLastSevenDays(history: List<ActionHistoryEntity>): List<DailyUsageUi> {
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
}