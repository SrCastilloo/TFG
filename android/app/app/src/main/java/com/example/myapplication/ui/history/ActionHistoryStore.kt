package com.example.myapplication.ui.history

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.history.ActionHistoryDao
import com.example.myapplication.data.local.history.ActionHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ActionHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val timestampMillis: Long = System.currentTimeMillis(),
    val source: String,
    val title: String,
    val detail: String,
    val success: Boolean = true,
    val actionType: String = inferActionType(source, title)
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestampMillis))
}

object ActionHistoryStore {

    private const val MAX_ITEMS = 50

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var dao: ActionHistoryDao? = null
    private var observeJob: Job? = null

    private val _items = MutableStateFlow<List<ActionHistoryItem>>(emptyList())
    val items: StateFlow<List<ActionHistoryItem>> = _items.asStateFlow()

    fun init(context: Context) {
        if (dao != null) return

        val database = AppDatabase.getInstance(context)
        dao = database.actionHistoryDao()

        observeJob?.cancel()
        observeJob = scope.launch {
            database.actionHistoryDao()
                .observeRecent(MAX_ITEMS)
                .collect { entities ->
                    _items.value = entities.map { it.toUiItem() }
                }
        }
    }

    fun add(
        source: String,
        title: String,
        detail: String,
        success: Boolean = true
    ) {
        val newItem = ActionHistoryItem(
            source = source,
            title = title,
            detail = detail,
            success = success
        )

        _items.value = listOf(newItem) + _items.value.take(MAX_ITEMS - 1)

        val localDao = dao

        if (localDao != null) {
            scope.launch {
                localDao.insert(newItem.toEntity())
            }
        }
    }

    fun clear() {
        _items.value = emptyList()

        val localDao = dao

        if (localDao != null) {
            scope.launch {
                localDao.clear()
            }
        }
    }
}

private fun ActionHistoryItem.toEntity(): ActionHistoryEntity {
    return ActionHistoryEntity(
        id = id,
        timestampMillis = timestampMillis,
        source = source,
        title = title,
        detail = detail,
        success = success,
        actionType = actionType
    )
}

private fun ActionHistoryEntity.toUiItem(): ActionHistoryItem {
    return ActionHistoryItem(
        id = id,
        timestampMillis = timestampMillis,
        source = source,
        title = title,
        detail = detail,
        success = success,
        actionType = actionType
    )
}

private fun inferActionType(source: String, title: String): String {
    val text = "$source $title".lowercase(Locale.getDefault())

    return when {
        "agarre" in text -> "grip"
        "cámara" in text || "camara" in text || "camera" in text -> "camera"
        "voz" in text || "voice" in text || "comando" in text -> "voice"
        "parada" in text || "emergencia" in text || "stop" in text -> "emergency"
        "mano" in text || "posición" in text || "posicion" in text -> "hand"
        "sensor" in text || "capacit" in text -> "sensors"
        "estado" in text || "sistema" in text -> "system"
        else -> "other"
    }
}