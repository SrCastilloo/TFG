package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.data.repository.TfgRepository
import com.example.myapplication.ui.demo.DemoControlCenter
import com.example.myapplication.ui.history.ActionHistoryStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DemoStepUi(
    val title: String,
    val detail: String,
    val success: Boolean? = null
)

data class DemoUiState(
    val isRunning: Boolean = false,
    val activeDemoTitle: String? = null,
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val currentStepText: String? = null,
    val message: String? = null,
    val error: String? = null,
    val steps: List<DemoStepUi> = emptyList()
)

private data class DemoStep(
    val title: String,
    val action: suspend () -> StepResult
)

private data class StepResult(
    val success: Boolean,
    val detail: String
)

class DemoViewModel : ViewModel() {

    private val apiService = ApiClient.apiService
    private val repository = TfgRepository(apiService)

    private var demoJob: Job? = null

    private val _uiState = MutableStateFlow(DemoUiState())
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            DemoControlCenter.cancelRequests.collect {
                cancelDemoFromEmergency()
            }
        }
    }

    fun startPositionsDemo() {
        val steps = listOf(
            DemoStep("Abrir mano") {
                val response = repository.openHand()
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Orden enviada para abrir la mano."
                )
            },
            DemoStep("Mover a posición 1") {
                val response = repository.moveToPosition(1)
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Orden enviada para mover a posición 1."
                )
            },
            DemoStep("Mover a posición 2") {
                val response = repository.moveToPosition(2)
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Orden enviada para mover a posición 2."
                )
            },
            DemoStep("Mover a posición 3") {
                val response = repository.moveToPosition(3)
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Orden enviada para mover a posición 3."
                )
            },
            DemoStep("Parar mano") {
                val response = repository.stopHand()
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Orden enviada para detener la mano."
                )
            }
        )

        runDemo(
            title = "Demo de posiciones",
            steps = steps
        )
    }

    fun startCameraDemo() {
        val steps = listOf(
            DemoStep("Activar modo cámara") {
                val response = repository.setModeCamera()
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Modo cámara activado."
                )
            },
            DemoStep("Detectar objeto y mover") {
                val response = repository.detectAndMove()

                val detectedObject = response.`object` ?: "ninguno"
                val quality = response.detection_quality ?: 0.0
                val position = response.target_position?.toString() ?: "sin posición"

                StepResult(
                    success = response.ok,
                    detail = "Objeto: $detectedObject · Calidad: ${"%.1f".format(quality)}% · Posición: $position"
                )
            }
        )

        runDemo(
            title = "Demo por cámara",
            steps = steps
        )
    }

    fun startVoiceDemo() {
        val steps = listOf(
            DemoStep("Activar modo voz") {
                val response = repository.setModeVoice()
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Modo voz activado."
                )
            },
            DemoStep("Escuchar comando de voz") {
                val response = repository.detectVoiceAndMove()

                val commandName = commandToName(response.voice?.command)
                val quality = response.voice?.detection_quality?.toString() ?: "-"
                val position = response.position_id?.toString() ?: "sin movimiento"

                StepResult(
                    success = response.ok,
                    detail = "Comando: $commandName · Calidad: $quality · Posición: $position"
                )
            }
        )

        runDemo(
            title = "Demo por voz",
            steps = steps
        )
    }

    fun startFullDemo() {
        val steps = listOf(
            DemoStep("Abrir mano") {
                val response = repository.openHand()
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Orden enviada para abrir la mano."
                )
            },
            DemoStep("Mover a posición 1") {
                val response = repository.moveToPosition(1)
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Orden enviada para mover a posición 1."
                )
            },
            DemoStep("Detectar objeto con cámara") {
                val modeResponse = repository.setModeCamera()

                if (!modeResponse.ok) {
                    return@DemoStep StepResult(
                        success = false,
                        detail = modeResponse.message ?: "No se pudo activar el modo cámara."
                    )
                }

                val response = repository.detectAndMove()

                val detectedObject = response.`object` ?: "ninguno"
                val quality = response.detection_quality ?: 0.0
                val position = response.target_position?.toString() ?: "sin posición"

                StepResult(
                    success = response.ok,
                    detail = "Objeto: $detectedObject · Calidad: ${"%.1f".format(quality)}% · Posición: $position"
                )
            },
            DemoStep("Escuchar comando de voz") {
                val modeResponse = repository.setModeVoice()

                if (!modeResponse.ok) {
                    return@DemoStep StepResult(
                        success = false,
                        detail = modeResponse.message ?: "No se pudo activar el modo voz."
                    )
                }

                val response = repository.detectVoiceAndMove()

                val commandName = commandToName(response.voice?.command)
                val quality = response.voice?.detection_quality?.toString() ?: "-"
                val position = response.position_id?.toString() ?: "sin movimiento"

                StepResult(
                    success = response.ok,
                    detail = "Comando: $commandName · Calidad: $quality · Posición: $position"
                )
            },
            DemoStep("Parar mano") {
                val response = repository.stopHand()
                StepResult(
                    success = response.ok,
                    detail = response.message ?: "Orden enviada para detener la mano."
                )
            }
        )

        runDemo(
            title = "Demo completa",
            steps = steps
        )
    }

    fun cancelDemo() {
        if (!_uiState.value.isRunning) return

        demoJob?.cancel()

        viewModelScope.launch {
            try {
                val response = repository.stopHand()
                val detail = response.message ?: "Demo cancelada y mano detenida."

                ActionHistoryStore.add(
                    source = "Demo",
                    title = "Demo cancelada",
                    detail = detail,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    currentStepText = null,
                    message = "Demo cancelada. Se ha enviado la orden de parada.",
                    error = null
                )
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido"

                ActionHistoryStore.add(
                    source = "Demo",
                    title = "Error al cancelar demo",
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    currentStepText = null,
                    message = null,
                    error = "No se pudo detener la mano al cancelar la demo: $errorMessage"
                )
            }
        }
    }

    private fun cancelDemoFromEmergency() {
        if (!_uiState.value.isRunning) return

        demoJob?.cancel()

        ActionHistoryStore.add(
            source = "Demo",
            title = "Demo cancelada por emergencia",
            detail = "Se ha solicitado una parada de emergencia global.",
            success = false
        )

        _uiState.value = _uiState.value.copy(
            isRunning = false,
            currentStepText = null,
            message = null,
            error = "Demo cancelada por parada de emergencia."
        )
    }

    private fun runDemo(
        title: String,
        steps: List<DemoStep>
    ) {
        if (_uiState.value.isRunning) return

        demoJob = viewModelScope.launch {
            _uiState.value = DemoUiState(
                isRunning = true,
                activeDemoTitle = title,
                currentStep = 0,
                totalSteps = steps.size,
                currentStepText = "Preparando demo...",
                steps = emptyList()
            )

            ActionHistoryStore.add(
                source = "Demo",
                title = "Inicio de demo",
                detail = title,
                success = true
            )

            try {
                steps.forEachIndexed { index, step ->
                    _uiState.value = _uiState.value.copy(
                        currentStep = index + 1,
                        totalSteps = steps.size,
                        currentStepText = step.title
                    )

                    val result = step.action()

                    ActionHistoryStore.add(
                        source = "Demo",
                        title = step.title,
                        detail = result.detail,
                        success = result.success
                    )

                    _uiState.value = _uiState.value.copy(
                        steps = _uiState.value.steps + DemoStepUi(
                            title = step.title,
                            detail = result.detail,
                            success = result.success
                        )
                    )

                    if (!result.success) {
                        throw IllegalStateException(result.detail)
                    }

                    delay(900)
                }

                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    currentStepText = null,
                    message = "$title finalizada correctamente.",
                    error = null
                )

                ActionHistoryStore.add(
                    source = "Demo",
                    title = "Demo finalizada",
                    detail = title,
                    success = true
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Error desconocido"

                try {
                    repository.stopHand()
                } catch (_: Exception) {
                }

                ActionHistoryStore.add(
                    source = "Demo",
                    title = "Demo interrumpida",
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    currentStepText = null,
                    message = null,
                    error = "Demo interrumpida: $errorMessage"
                )
            }
        }
    }

    private fun commandToName(command: String?): String {
        return when (command) {
            "0x11" -> "Uno"
            "0x19" -> "Dos"
            "0x1d" -> "Tres"
            "0x1f" -> "Cuatro"
            "0x1" -> "Cinco"
            "0x0" -> "Ruido"
            else -> "Desconocido"
        }
    }
}