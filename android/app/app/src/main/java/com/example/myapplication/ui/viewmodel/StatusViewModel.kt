package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.history.ActionHistoryStore
import com.example.myapplication.ui.state.StatusUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatusViewModel : ViewModel() {

    private val apiService = ApiClient.apiService
    private val repository = TfgRepository(apiService)

    private val _uiState = MutableStateFlow(StatusUiState(isLoading = true))
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val systemInfo = repository.getSystemInfo()
                val handPositions = repository.getHandPositions()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    simulation = systemInfo.simulation,
                    mode = systemInfo.mode,
                    configPath = systemInfo.config_path,
                    lastPositionMapped = systemInfo.last_position_mapped,
                    handAvailable = systemInfo.hand_available,
                    cameraAvailable = systemInfo.camera_available,
                    positions = handPositions.positions,
                    error = null
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido"

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    private fun runAction(
        historyTitle: String,
        action: suspend () -> String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isActionLoading = true,
                error = null,
                actionMessage = null
            )

            try {
                val message = action()
                val systemInfo = repository.getSystemInfo()
                val handPositions = repository.getHandPositions()
                val finalMessage = message ?: "Acción realizada correctamente"

                ActionHistoryStore.add(
                    source = "Estado",
                    title = historyTitle,
                    detail = finalMessage,
                    success = true
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isActionLoading = false,
                    simulation = systemInfo.simulation,
                    mode = systemInfo.mode,
                    configPath = systemInfo.config_path,
                    lastPositionMapped = systemInfo.last_position_mapped,
                    handAvailable = systemInfo.hand_available,
                    cameraAvailable = systemInfo.camera_available,
                    positions = handPositions.positions,
                    actionMessage = finalMessage,
                    error = null
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido"

                ActionHistoryStore.add(
                    source = "Estado",
                    title = historyTitle,
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    fun setModeHand() {
        runAction("Activar modo mano") {
            repository.setModeHand().message
        }
    }

    fun setModeVoice() {
        runAction("Activar modo voz") {
            repository.setModeVoice().message
        }
    }

    fun setModeCamera() {
        runAction("Activar modo cámara") {
            repository.setModeCamera().message
        }
    }

    fun openHand() {
        runAction("Abrir mano") {
            repository.openHand().message
        }
    }

    fun stopHand() {
        runAction("Parar mano") {
            repository.stopHand().message
        }
    }

    fun moveToPosition(positionId: Int) {
        runAction("Mover a posición $positionId") {
            repository.moveToPosition(positionId).message
        }
    }
}