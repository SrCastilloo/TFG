package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.repository.TfgRepository
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

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = _uiState.value.messages + ChatMessage(
                        text = response.reply,
                        isUser = false
                    ),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = _uiState.value.messages + ChatMessage(
                        text = "Ha ocurrido un error al contactar con el asistente.",
                        isUser = false
                    ),
                    error = e.message
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

                _uiState.value = _uiState.value.copy(
                    isVoiceLoading = false,
                    voiceCommand = command,
                    voiceCommandName = commandName,
                    voiceQuality = quality,
                    voiceMoved = moved,
                    voicePositionId = positionId,
                    voiceMessage = buildVoiceMessage(
                        commandName = commandName,
                        moved = moved,
                        positionId = positionId
                    ),
                    voiceError = if (!response.ok) response.error else null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVoiceLoading = false,
                    voiceMoved = false,
                    voiceMessage = null,
                    voiceError = "No se pudo contactar con la Raspberry: ${e.message}"
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
}