package com.example.myapplication.ui.state

data class ChatMessage(
    val text: String,
    val isUser: Boolean,

    val provider: String? = null,
    val model: String? = null,
    val usedAi: Boolean = false,
    val totalTokens: Int? = null
)