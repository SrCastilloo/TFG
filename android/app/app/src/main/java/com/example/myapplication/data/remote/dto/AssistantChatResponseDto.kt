package com.example.myapplication.data.remote.dto

data class AssistantChatResponseDto(
    val ok: Boolean,
    val reply: String,

    val provider: String? = null,
    val model: String? = null,

    val used_ai: Boolean = false,

    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,

    val error: String? = null
)