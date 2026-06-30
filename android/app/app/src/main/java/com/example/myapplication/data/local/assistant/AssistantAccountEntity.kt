package com.example.myapplication.data.local.assistant

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assistant_accounts")
data class AssistantAccountEntity(
    @PrimaryKey
    val userId: String,

    // "OPENAI" o "GEMINI"
    val selectedProvider: String,

    val openAiApiKeyEncrypted: String?,
    val geminiApiKeyEncrypted: String?,

    val openAiModel: String,
    val geminiModel: String,

    val updatedAtMillis: Long
)


// un punto de partida razonable puede ser gpt-4.1-mini y gemini-2.5-flash