package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.TfgApiService
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.state.CameraUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CameraViewModel : ViewModel() {

    private val apiService = Retrofit.Builder()
        .baseUrl("http://192.168.1.143:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TfgApiService::class.java)

    private val repository = TfgRepository(apiService)

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun detectObject() {
        runAction { repository.detectObject() }
    }

    fun detectAndMove() {
        runAction { repository.detectAndMove() }
    }

    private fun runAction(action: suspend () -> com.example.myapplication.data.remote.dto.CameraDetectionDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                actionMessage = null
            )

            try {
                val response = action()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    detectedObject = response.`object`,
                    detectionQuality = response.detection_quality,
                    targetPosition = response.target_position,
                    actionMessage = response.message ?: "Acción completada",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }
}