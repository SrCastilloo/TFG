package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.history.ActionHistoryStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmergencyStopUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class EmergencyStopViewModel : ViewModel() {

    private val apiService = ApiClient.apiService
    private val repository = TfgRepository(apiService)

    private val _uiState = MutableStateFlow(EmergencyStopUiState())
    val uiState: StateFlow<EmergencyStopUiState> = _uiState.asStateFlow()

    fun emergencyStop() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = EmergencyStopUiState(
                isLoading = true
            )

            try {
                val response = repository.stopHand()
                val finalMessage = response.message ?: "Movimiento detenido correctamente."

                ActionHistoryStore.add(
                    source = "Emergencia",
                    title = "Parada de emergencia",
                    detail = finalMessage,
                    success = true
                )

                _uiState.value = EmergencyStopUiState(
                    isLoading = false,
                    message = "Mano detenida"
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido"

                ActionHistoryStore.add(
                    source = "Emergencia",
                    title = "Error en parada de emergencia",
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = EmergencyStopUiState(
                    isLoading = false,
                    error = "No se pudo detener la mano"
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(
            message = null,
            error = null
        )
    }
}