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
import java.util.Locale

enum class DiagnosticStatus {
    WAITING,
    RUNNING,
    OK,
    WARNING,
    ERROR
}

data class DiagnosticCheckUi(
    val title: String,
    val endpoint: String,
    val description: String,
    val status: DiagnosticStatus = DiagnosticStatus.WAITING,
    val detail: String = "Pendiente de ejecutar",
    val latencyMs: Long? = null
)

data class DiagnosticUiState(
    val isRunning: Boolean = false,
    val baseUrl: String = ApiClient.baseUrl,
    val lastRunText: String? = null,
    val message: String? = null,
    val error: String? = null,
    val checks: List<DiagnosticCheckUi> = emptyList()
) {
    val totalChecks: Int
        get() = checks.size

    val okChecks: Int
        get() = checks.count { it.status == DiagnosticStatus.OK }

    val warningChecks: Int
        get() = checks.count { it.status == DiagnosticStatus.WARNING }

    val errorChecks: Int
        get() = checks.count { it.status == DiagnosticStatus.ERROR }

    val finishedChecks: Int
        get() = checks.count {
            it.status == DiagnosticStatus.OK ||
                    it.status == DiagnosticStatus.WARNING ||
                    it.status == DiagnosticStatus.ERROR
        }
}

class DiagnosticViewModel : ViewModel() {

    private val apiService = ApiClient.apiService
    private val repository = TfgRepository(apiService)

    private val _uiState = MutableStateFlow(
        DiagnosticUiState(
            checks = initialChecks(includeHardware = false)
        )
    )
    val uiState: StateFlow<DiagnosticUiState> = _uiState.asStateFlow()

    fun runQuickDiagnostic() {
        runDiagnostic(includeHardware = false)
    }

    fun runFullDiagnostic() {
        runDiagnostic(includeHardware = true)
    }

    private fun runDiagnostic(includeHardware: Boolean) {
        if (_uiState.value.isRunning) return

        viewModelScope.launch {
            val checks = initialChecks(includeHardware)

            _uiState.value = DiagnosticUiState(
                isRunning = true,
                baseUrl = ApiClient.baseUrl,
                checks = checks,
                message = null,
                error = null
            )

            ActionHistoryStore.add(
                source = "Diagnóstico",
                title = if (includeHardware) "Diagnóstico completo iniciado" else "Diagnóstico rápido iniciado",
                detail = "URL: ${ApiClient.baseUrl}",
                success = true
            )

            runCheck(
                title = "Backend operativo",
                endpoint = "GET /health/",
                description = "Comprueba si la API responde correctamente."
            ) {
                val response = repository.getHealth()

                if (response.ok) {
                    CheckExecutionResult(
                        status = DiagnosticStatus.OK,
                        detail = response.message ?: "Backend operativo."
                    )
                } else {
                    CheckExecutionResult(
                        status = DiagnosticStatus.ERROR,
                        detail = response.message ?: "El backend respondió, pero no está operativo."
                    )
                }
            }

            runCheck(
                title = "Información del sistema",
                endpoint = "GET /system/info",
                description = "Comprueba modo, simulación y disponibilidad de módulos."
            ) {
                val response = repository.getSystemInfo()

                val environment = if (response.simulation) "Simulación" else "Real"
                val hand = if (response.hand_available) "mano disponible" else "mano no disponible"
                val camera = if (response.camera_available) "cámara disponible" else "cámara no disponible"

                CheckExecutionResult(
                    status = DiagnosticStatus.OK,
                    detail = "Modo: ${friendlyMode(response.mode)} · $environment · $hand · $camera"
                )
            }

            runCheck(
                title = "Posiciones de la mano",
                endpoint = "GET /hand/positions",
                description = "Comprueba si el backend devuelve las posiciones configuradas."
            ) {
                val response = repository.getHandPositions()

                if (response.ok && response.positions.isNotEmpty()) {
                    CheckExecutionResult(
                        status = DiagnosticStatus.OK,
                        detail = "Posiciones disponibles: ${response.positions.joinToString(", ")}"
                    )
                } else {
                    CheckExecutionResult(
                        status = DiagnosticStatus.WARNING,
                        detail = "La petición respondió, pero no se recibieron posiciones."
                    )
                }
            }

            if (includeHardware) {
                runCheck(
                    title = "Parada segura de la mano",
                    endpoint = "POST /hand/stop",
                    description = "Envía una parada segura. No mueve la mano a una posición nueva."
                ) {
                    val response = repository.stopHand()

                    if (response.ok) {
                        CheckExecutionResult(
                            status = DiagnosticStatus.OK,
                            detail = response.message ?: "Orden de parada enviada correctamente."
                        )
                    } else {
                        CheckExecutionResult(
                            status = DiagnosticStatus.ERROR,
                            detail = response.message ?: "No se pudo enviar la orden de parada."
                        )
                    }
                }

                runCheck(
                    title = "Cámara",
                    endpoint = "POST /camera/detect",
                    description = "Comprueba si la cámara responde a una detección."
                ) {
                    val response = repository.detectObject()

                    val objectName = response.`object` ?: "sin objeto"
                    val quality = response.detection_quality?.let {
                        String.format(Locale.getDefault(), "%.1f", it)
                    } ?: "-"

                    if (response.ok) {
                        CheckExecutionResult(
                            status = DiagnosticStatus.OK,
                            detail = "Respuesta recibida · Objeto: $objectName · Calidad: $quality%"
                        )
                    } else {
                        CheckExecutionResult(
                            status = DiagnosticStatus.WARNING,
                            detail = response.message ?: "La cámara respondió, pero no detectó correctamente."
                        )
                    }
                }

                runCheck(
                    title = "Voz",
                    endpoint = "POST /voice/detect",
                    description = "Comprueba si el módulo de voz responde. Puede tardar unos segundos."
                ) {
                    val response = repository.detectVoice()

                    val commandName = commandToName(response.command)
                    val quality = response.detection_quality?.toString() ?: "-"

                    if (response.ok) {
                        CheckExecutionResult(
                            status = DiagnosticStatus.OK,
                            detail = "Comando: $commandName · Calidad: $quality"
                        )
                    } else {
                        CheckExecutionResult(
                            status = DiagnosticStatus.WARNING,
                            detail = response.error ?: "El módulo respondió, pero no reconoció un comando válido."
                        )
                    }
                }
            }

            val finalState = _uiState.value
            val success = finalState.errorChecks == 0

            ActionHistoryStore.add(
                source = "Diagnóstico",
                title = if (includeHardware) "Diagnóstico completo finalizado" else "Diagnóstico rápido finalizado",
                detail = "OK: ${finalState.okChecks}, avisos: ${finalState.warningChecks}, errores: ${finalState.errorChecks}",
                success = success
            )

            _uiState.value = finalState.copy(
                isRunning = false,
                lastRunText = "Último diagnóstico: OK ${finalState.okChecks}, avisos ${finalState.warningChecks}, errores ${finalState.errorChecks}",
                message = if (success) {
                    "Diagnóstico finalizado. No se han detectado errores críticos."
                } else {
                    "Diagnóstico finalizado con errores. Revisa las tarjetas marcadas en rojo."
                },
                error = null
            )
        }
    }

    private suspend fun runCheck(
        title: String,
        endpoint: String,
        description: String,
        block: suspend () -> CheckExecutionResult
    ) {
        addOrUpdateCheck(
            DiagnosticCheckUi(
                title = title,
                endpoint = endpoint,
                description = description,
                status = DiagnosticStatus.RUNNING,
                detail = "Ejecutando..."
            )
        )

        val startTime = System.currentTimeMillis()

        try {
            val result = block()
            val latency = System.currentTimeMillis() - startTime

            addOrUpdateCheck(
                DiagnosticCheckUi(
                    title = title,
                    endpoint = endpoint,
                    description = description,
                    status = result.status,
                    detail = result.detail,
                    latencyMs = latency
                )
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            val errorMessage = e.message ?: "Error desconocido"

            addOrUpdateCheck(
                DiagnosticCheckUi(
                    title = title,
                    endpoint = endpoint,
                    description = description,
                    status = DiagnosticStatus.ERROR,
                    detail = errorMessage,
                    latencyMs = latency
                )
            )
        }
    }

    private fun addOrUpdateCheck(newCheck: DiagnosticCheckUi) {
        val current = _uiState.value.checks.toMutableList()
        val index = current.indexOfFirst { it.title == newCheck.title }

        if (index >= 0) {
            current[index] = newCheck
        } else {
            current.add(newCheck)
        }

        _uiState.value = _uiState.value.copy(
            checks = current
        )
    }

    private data class CheckExecutionResult(
        val status: DiagnosticStatus,
        val detail: String
    )

    private fun initialChecks(includeHardware: Boolean): List<DiagnosticCheckUi> {
        val checks = mutableListOf(
            DiagnosticCheckUi(
                title = "Backend operativo",
                endpoint = "GET /health/",
                description = "Comprueba si la API responde correctamente."
            ),
            DiagnosticCheckUi(
                title = "Información del sistema",
                endpoint = "GET /system/info",
                description = "Comprueba modo, simulación y disponibilidad de módulos."
            ),
            DiagnosticCheckUi(
                title = "Posiciones de la mano",
                endpoint = "GET /hand/positions",
                description = "Comprueba si el backend devuelve las posiciones configuradas."
            )
        )

        if (includeHardware) {
            checks.add(
                DiagnosticCheckUi(
                    title = "Parada segura de la mano",
                    endpoint = "POST /hand/stop",
                    description = "Envía una parada segura. No mueve la mano a una posición nueva."
                )
            )

            checks.add(
                DiagnosticCheckUi(
                    title = "Cámara",
                    endpoint = "POST /camera/detect",
                    description = "Comprueba si la cámara responde a una detección."
                )
            )

            checks.add(
                DiagnosticCheckUi(
                    title = "Voz",
                    endpoint = "POST /voice/detect",
                    description = "Comprueba si el módulo de voz responde."
                )
            )
        }

        return checks
    }

    private fun friendlyMode(mode: String): String {
        return when (mode.lowercase()) {
            "init" -> "Inicial"
            "hand" -> "Mano"
            "voice" -> "Voz"
            "camera" -> "Cámara"
            else -> mode
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
            null -> "Sin comando"
            else -> command
        }
    }
}