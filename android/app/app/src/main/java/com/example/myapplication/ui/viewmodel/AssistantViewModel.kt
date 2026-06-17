package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.history.ActionHistoryStore
import com.example.myapplication.ui.state.AssistantUiState
import com.example.myapplication.ui.state.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssistantViewModel : ViewModel() {

    private val apiService = ApiClient.apiService
    private val repository = TfgRepository(apiService)

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        _uiState.value = _uiState.value.copy(currentInput = value)
    }

    fun sendMessage() {
        val message = _uiState.value.currentInput.trim()

        if (message.isBlank()) return

        val updatedMessages = _uiState.value.messages + ChatMessage(
            text = message,
            isUser = true
        )

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            currentInput = "",
            messages = updatedMessages,
            error = null
        )

        viewModelScope.launch {
            try {
                val response = repository.chatWithAssistant(message)

                val historyTitle = if (looksLikeActionResponse(response.reply)) {
                    "Acción desde asistente"
                } else {
                    "Consulta al asistente"
                }

                ActionHistoryStore.add(
                    source = "Asistente",
                    title = historyTitle,
                    detail = "Usuario: ${message.take(80)}",
                    success = response.ok
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = _uiState.value.messages + ChatMessage(
                        text = response.reply,
                        isUser = false
                    ),
                    error = null
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido"

                ActionHistoryStore.add(
                    source = "Asistente",
                    title = "Error en asistente",
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = _uiState.value.messages + ChatMessage(
                        text = "Ha ocurrido un error al contactar con el asistente.",
                        isUser = false
                    ),
                    error = errorMessage
                )
            }
        }
    }

    fun detectAndMoveByVoice() {
        _uiState.value = _uiState.value.copy(
            isVoiceLoading = true,
            voiceCommand = null,
            voiceCommandName = null,
            voiceQuality = null,
            voiceMoved = false,
            voicePositionId = null,
            voiceMessage = "Escuchando comando de voz...",
            voiceError = null
        )

        viewModelScope.launch {
            try {
                val response = repository.detectVoiceAndMove()

                val command = response.voice?.command
                val quality = response.voice?.detection_quality
                val moved = response.moved == true
                val positionId = response.position_id
                val commandName = commandToName(command)

                val finalMessage = buildVoiceMessage(
                    commandName = commandName,
                    moved = moved,
                    positionId = positionId
                )

                ActionHistoryStore.add(
                    source = "Voz",
                    title = if (moved) "Comando de voz reconocido" else "Comando de voz sin movimiento",
                    detail = if (moved && positionId != null) {
                        "$commandName · Posición $positionId · Calidad ${quality ?: "-"}"
                    } else {
                        "$commandName · No se mueve la mano · Calidad ${quality ?: "-"}"
                    },
                    success = response.ok
                )

                _uiState.value = _uiState.value.copy(
                    isVoiceLoading = false,
                    voiceCommand = command,
                    voiceCommandName = commandName,
                    voiceQuality = quality,
                    voiceMoved = moved,
                    voicePositionId = positionId,
                    voiceMessage = finalMessage,
                    voiceError = if (!response.ok) response.error else null
                )

            } catch (e: Exception) {
                val errorMessage = "No se pudo contactar con la Raspberry: ${e.message}"

                ActionHistoryStore.add(
                    source = "Voz",
                    title = "Error en control por voz",
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isVoiceLoading = false,
                    voiceMoved = false,
                    voiceMessage = null,
                    voiceError = errorMessage
                )
            }
        }
    }

    private fun commandToName(command: String?): String {
        return when (command) {
            "0x11" -> "Uno"
            "0x19" -> "Dos"
            "0x1d" -> "Tres"
            "0x1f" -> "Cuatro"
            "0x1" -> "Cinco"
            "0x0" -> "Ruido"
            else -> "Desconocido"
        }
    }

    private fun buildVoiceMessage(
        commandName: String,
        moved: Boolean,
        positionId: Int?
    ): String {
        return if (moved && positionId != null) {
            "Comando reconocido: $commandName. Mano movida a la posición $positionId."
        } else {
            "No se ha reconocido un comando válido. Detectado: $commandName."
        }
    }

    private fun looksLikeActionResponse(reply: String): Boolean {
        val normalized = reply.lowercase()

        return normalized.contains("mano movida") ||
                normalized.contains("movimiento enviado") ||
                normalized.contains("modo") && normalized.contains("activado") ||
                normalized.contains("mano abierta") ||
                normalized.contains("movimiento detenido") ||
                normalized.contains("objeto detectado") ||
                normalized.contains("acción ejecutada") ||
                normalized.contains("accion ejecutada")
    }
}