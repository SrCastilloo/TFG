package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.remote.dto.CapacitiveDto
import com.example.myapplication.data.remote.dto.SafeGripDto
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.history.ActionHistoryStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CapacitiveUiState(
    val isLoading: Boolean = false,
    val isSafeGripLoading: Boolean = false,
    val data: CapacitiveDto? = null,
    val safeGripResult: SafeGripDto? = null,
    val message: String? = null,
    val error: String? = null
)

class CapacitiveViewModel : ViewModel() {

    private val apiService = ApiClient.apiService
    private val repository = TfgRepository(apiService)

    private val _uiState = MutableStateFlow(CapacitiveUiState())
    val uiState: StateFlow<CapacitiveUiState> = _uiState.asStateFlow()

    init {
        loadStatus()
    }

    fun loadStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                message = null
            )

            try {
                val response = repository.getCapacitiveStatus()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    data = response,
                    message = response.message,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido leyendo sensores capacitivos."
                )
            }
        }
    }

    fun refreshStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                message = null
            )

            try {
                val response = repository.refreshCapacitiveStatus()

                ActionHistoryStore.add(
                    source = "Capacitivos",
                    title = "Lectura de sensores",
                    detail = "Contactos detectados: ${response.contact_count}",
                    success = response.ok
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    data = response,
                    message = response.message,
                    error = null
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido"

                ActionHistoryStore.add(
                    source = "Capacitivos",
                    title = "Error leyendo sensores",
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    fun startSafeGrip() {
        if (_uiState.value.isSafeGripLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSafeGripLoading = true,
                error = null,
                message = null,
                safeGripResult = null
            )

            try {
                val response = repository.safeGrip()

                ActionHistoryStore.add(
                    source = "Capacitivos",
                    title = "Agarre seguro",
                    detail = response.message ?: "Agarre seguro ejecutado.",
                    success = response.ok
                )

                _uiState.value = _uiState.value.copy(
                    isSafeGripLoading = false,
                    data = response.capacitive ?: _uiState.value.data,
                    safeGripResult = response,
                    message = response.message,
                    error = null
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido ejecutando agarre seguro."

                ActionHistoryStore.add(
                    source = "Capacitivos",
                    title = "Error en agarre seguro",
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isSafeGripLoading = false,
                    error = errorMessage
                )
            }
        }
    }
}