package com.example.myapplication.data.remote.dto

data class AssistantChatRequest(
    val message: String,

    // "OPENAI" o "GEMINI"
    val provider: String,

    // La API key propia del usuario.
    // El backend la recibe, la usa y no la guarda.
    val api_key: String,

    // Modelo elegido por el usuario.
    val model: String?
)

data class AssistantChatResponse(
    val ok: Boolean,
    val reply: String,

    val provider: String?,
    val model: String?,

    val used_ai: Boolean,

    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?,

    val error: String?
)