package com.example.myapplication.ui.state

data class AssistantUiState(
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Hola, soy el asistente de la app. Puedes preguntarme cosas sobre el estado del sistema, los modos o las posiciones disponibles.",
            isUser = false
        )
    ),
    val currentInput: String = "",
    val error: String? = null
)