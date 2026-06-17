package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.remote.dto.CameraDetectionDto
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.history.ActionHistoryStore
import com.example.myapplication.ui.state.CameraUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    private val apiService = ApiClient.apiService
    private val repository = TfgRepository(apiService)

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun detectObject() {
        runAction("Detectar objeto") {
            repository.detectObject()
        }
    }

    fun detectAndMove() {
        runAction("Detectar objeto y mover mano") {
            repository.detectAndMove()
        }
    }

    private fun runAction(
        historyTitle: String,
        action: suspend () -> CameraDetectionDto
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                actionMessage = null
            )

            try {
                val response = action()

                val objectText = response.`object` ?: "ninguno"
                val qualityText = String.format("%.1f", response.detection_quality ?: 0.0)
                val positionText = response.target_position?.toString() ?: "sin posición"
                val finalMessage = response.message ?: "Acción completada"

                ActionHistoryStore.add(
                    source = "Cámara",
                    title = historyTitle,
                    detail = "Objeto: $objectText · Calidad: $qualityText% · Posición: $positionText",
                    success = response.ok
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    detectedObject = response.`object`,
                    detectionQuality = response.detection_quality,
                    targetPosition = response.target_position,
                    actionMessage = finalMessage,
                    error = null
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido"

                ActionHistoryStore.add(
                    source = "Cámara",
                    title = historyTitle,
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
}