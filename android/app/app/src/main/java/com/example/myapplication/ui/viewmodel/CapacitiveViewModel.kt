package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.remote.dto.CapacitiveDto
import com.example.myapplication.data.remote.dto.FullGripDto
import com.example.myapplication.data.remote.dto.FullGripRequest
import com.example.myapplication.data.remote.dto.SafeGripDto
import com.example.myapplication.data.remote.dto.SafeGripRequest
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.history.ActionHistoryStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GripSpeed(
    val label: String,
    val closeStep: Int,
    val stepSettleSeconds: Double
) {
    SLOW(
        label = "Lenta",
        closeStep = 15,
        stepSettleSeconds = 0.25
    ),
    MEDIUM(
        label = "Media",
        closeStep = 30,
        stepSettleSeconds = 0.20
    ),
    FAST(
        label = "Rápida",
        closeStep = 50,
        stepSettleSeconds = 0.15
    )
}

data class CapacitiveUiState(
    val isLoading: Boolean = false,
    val isSafeGripLoading: Boolean = false,
    val isFullGripLoading: Boolean = false,

    val data: CapacitiveDto? = null,
    val safeGripResult: SafeGripDto? = null,
    val fullGripResult: FullGripDto? = null,

    val message: String? = null,
    val error: String? = null,

    // Configuración común para agarre seguro y completo
    val ignoredSensors: Set<String> = setOf("ring", "palm", "middle"),
    val gripSpeed: GripSpeed = GripSpeed.MEDIUM,
    val targetPositionId: Int = 2
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

    fun toggleIgnoredSensor(sensor: String) {
        val current = _uiState.value.ignoredSensors

        val updated = if (sensor in current) {
            current - sensor
        } else {
            current + sensor
        }

        _uiState.value = _uiState.value.copy(
            ignoredSensors = updated
        )
    }

    fun setGripSpeed(speed: GripSpeed) {
        _uiState.value = _uiState.value.copy(
            gripSpeed = speed
        )
    }

    fun increaseTargetPosition() {
        val current = _uiState.value.targetPositionId

        _uiState.value = _uiState.value.copy(
            targetPositionId = (current + 1).coerceAtMost(8)
        )
    }

    fun decreaseTargetPosition() {
        val current = _uiState.value.targetPositionId

        _uiState.value = _uiState.value.copy(
            targetPositionId = (current - 1).coerceAtLeast(0)
        )
    }

    private fun buildSafeGripRequest(): SafeGripRequest {
        val state = _uiState.value

        return SafeGripRequest(
            max_seconds = 15.0,
            poll_interval = 0.08,
            consecutive_reads = 2,
            ignored_sensors = state.ignoredSensors.toList().sorted(),
            start_from_open = true,
            open_wait_seconds = 3.0,
            target_position_id = state.targetPositionId,
            close_step = state.gripSpeed.closeStep,
            step_settle_seconds = state.gripSpeed.stepSettleSeconds,
            pause_between_steps = 0.0
        )
    }

    private fun buildFullGripRequest(): FullGripRequest {
        val state = _uiState.value

        return FullGripRequest(
            max_seconds = 15.0,
            poll_interval = 0.08,
            consecutive_reads = 2,
            ignored_sensors = state.ignoredSensors.toList().sorted(),
            required_sensors = null,
            start_from_open = true,
            open_wait_seconds = 3.0,
            close_step = state.gripSpeed.closeStep,
            step_settle_seconds = state.gripSpeed.stepSettleSeconds,
            pause_between_steps = 0.20
        )
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
                val request = buildSafeGripRequest()
                val response = repository.safeGrip(request)

                ActionHistoryStore.add(
                    source = "Capacitivos",
                    title = "Agarre seguro",
                    detail = "${response.message ?: "Agarre seguro ejecutado."} · Ignorados: ${request.ignored_sensors.joinToString(", ")}",
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

    fun startFullGrip() {
        if (_uiState.value.isFullGripLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFullGripLoading = true,
                error = null,
                message = null,
                fullGripResult = null
            )

            try {
                val request = buildFullGripRequest()
                val response = repository.fullGrip(request)

                ActionHistoryStore.add(
                    source = "Capacitivos",
                    title = "Agarre completo",
                    detail = "${response.message ?: "Agarre completo ejecutado."} · Ignorados: ${request.ignored_sensors.joinToString(", ")}",
                    success = response.ok
                )

                _uiState.value = _uiState.value.copy(
                    isFullGripLoading = false,
                    data = response.capacitive ?: _uiState.value.data,
                    fullGripResult = response,
                    message = response.message,
                    error = null
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido ejecutando agarre completo."

                ActionHistoryStore.add(
                    source = "Capacitivos",
                    title = "Error en agarre completo",
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isFullGripLoading = false,
                    error = errorMessage
                )
            }
        }
    }
}