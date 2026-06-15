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
    val error: String? = null,

    // Estado del control por voz
    val isVoiceLoading: Boolean = false,
    val voiceCommand: String? = null,
    val voiceCommandName: String? = null,
    val voiceQuality: Int? = null,
    val voiceMoved: Boolean = false,
    val voicePositionId: Int? = null,
    val voiceMessage: String? = null,
    val voiceError: String? = null
)