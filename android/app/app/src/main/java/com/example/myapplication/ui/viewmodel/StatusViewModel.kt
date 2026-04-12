package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.TfgApiService
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.state.StatusUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StatusViewModel : ViewModel() {

    private val apiService = Retrofit.Builder()
        .baseUrl("http://192.168.1.143:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TfgApiService::class.java)

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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    private fun runAction(action: suspend () -> String?) {
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
                    actionMessage = message ?: "Acción realizada correctamente",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    fun setModeHand() {
        runAction { repository.setModeHand().message }
    }

    fun setModeVoice() {
        runAction { repository.setModeVoice().message }
    }

    fun setModeCamera() {
        runAction { repository.setModeCamera().message }
    }

    fun openHand() {
        runAction { repository.openHand().message }
    }

    fun stopHand() {
        runAction { repository.stopHand().message }
    }

    fun moveToPosition(positionId: Int) {
        runAction { repository.moveToPosition(positionId).message }
    }
}