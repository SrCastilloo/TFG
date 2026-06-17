package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.history.ActionHistoryStore
import com.example.myapplication.ui.state.HandUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HandViewModel : ViewModel() {

    private val apiService = ApiClient.apiService
    private val repository = TfgRepository(apiService)

    private val _uiState = MutableStateFlow(HandUiState(isLoading = true))
    val uiState: StateFlow<HandUiState> = _uiState.asStateFlow()

    init {
        loadPositions()
    }

    fun loadPositions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val positionsResponse = repository.getHandPositions()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    positions = positionsResponse.positions,
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
                isLoading = true,
                error = null,
                actionMessage = null
            )

            try {
                val message = action()
                val positionsResponse = repository.getHandPositions()
                val finalMessage = message ?: "Acción realizada correctamente"

                ActionHistoryStore.add(
                    source = "Mano",
                    title = historyTitle,
                    detail = finalMessage,
                    success = true
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    positions = positionsResponse.positions,
                    actionMessage = finalMessage,
                    error = null
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido"

                ActionHistoryStore.add(
                    source = "Mano",
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