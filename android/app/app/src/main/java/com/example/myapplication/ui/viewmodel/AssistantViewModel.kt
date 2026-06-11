package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.TfgApiService
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.state.AssistantUiState
import com.example.myapplication.ui.state.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AssistantViewModel : ViewModel() {

    private val apiService = Retrofit.Builder()
        .baseUrl("http://192.168.100.203:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TfgApiService::class.java)

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
}