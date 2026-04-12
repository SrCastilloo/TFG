package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.TfgApiService
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.state.HandUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HandViewModel : ViewModel() {

    private val apiService = Retrofit.Builder()
        .baseUrl("http://192.168.1.143:8000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TfgApiService::class.java)

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
                isLoading = true,
                error = null,
                actionMessage = null
            )

            try {
                val message = action()
                val positionsResponse = repository.getHandPositions()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    positions = positionsResponse.positions,
                    actionMessage = message ?: "Acción realizada correctamente",
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