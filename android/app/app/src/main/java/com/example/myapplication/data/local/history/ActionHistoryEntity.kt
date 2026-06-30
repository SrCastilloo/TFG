package com.example.myapplication.data.local.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_history")
data class ActionHistoryEntity(
    @PrimaryKey
    val id: String,
    val timestampMillis: Long,
    val source: String,
    val title: String,
    val detail: String,
    val success: Boolean,
    val actionType: String,
    val userId: String? = null
)