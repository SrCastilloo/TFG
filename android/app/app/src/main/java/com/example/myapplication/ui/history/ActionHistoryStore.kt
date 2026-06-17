package com.example.myapplication.ui.history

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val success: Boolean = true
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestampMillis))
}

object ActionHistoryStore {

    private const val MAX_ITEMS = 50

    private val _items = MutableStateFlow<List<ActionHistoryItem>>(emptyList())
    val items: StateFlow<List<ActionHistoryItem>> = _items.asStateFlow()

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
    }

    fun clear() {
        _items.value = emptyList()
    }
}


