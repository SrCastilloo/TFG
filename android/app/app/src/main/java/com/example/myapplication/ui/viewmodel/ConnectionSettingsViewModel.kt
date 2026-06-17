package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.ConnectionSettingsStore
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.ui.history.ActionHistoryStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class ConnectionSettingsUiState(
    val ip: String = ConnectionSettingsStore.DEFAULT_IP,
    val port: String = ConnectionSettingsStore.DEFAULT_PORT,
    val baseUrl: String = "http://${ConnectionSettingsStore.DEFAULT_IP}:${ConnectionSettingsStore.DEFAULT_PORT}/",
    val isTesting: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

class ConnectionSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<ConnectionSettingsUiState> = _uiState.asStateFlow()

    private fun loadInitialState(): ConnectionSettingsUiState {
        val ip = ConnectionSettingsStore.getIp(context)
        val port = ConnectionSettingsStore.getPort(context)
        val baseUrl = ConnectionSettingsStore.buildBaseUrl(ip, port)

        return ConnectionSettingsUiState(
            ip = ip,
            port = port,
            baseUrl = baseUrl
        )
    }

    fun onIpChange(value: String) {
        val newBaseUrl = ConnectionSettingsStore.buildBaseUrl(
            ip = value,
            port = _uiState.value.port
        )

        _uiState.value = _uiState.value.copy(
            ip = value,
            baseUrl = newBaseUrl,
            message = null,
            error = null
        )
    }

    fun onPortChange(value: String) {
        val cleanPort = value.filter { it.isDigit() }

        val newBaseUrl = ConnectionSettingsStore.buildBaseUrl(
            ip = _uiState.value.ip,
            port = cleanPort
        )

        _uiState.value = _uiState.value.copy(
            port = cleanPort,
            baseUrl = newBaseUrl,
            message = null,
            error = null
        )
    }

    fun saveSettings() {
        val ip = _uiState.value.ip.trim()
        val port = _uiState.value.port.trim()

        if (ip.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "La IP no puede estar vacía.",
                message = null
            )
            return
        }

        ApiClient.updateConnection(
            context = context,
            ip = ip,
            port = port
        )

        val baseUrl = ConnectionSettingsStore.buildBaseUrl(ip, port)

        ActionHistoryStore.add(
            source = "Ajustes",
            title = "Conexión actualizada",
            detail = "Nueva dirección: $baseUrl",
            success = true
        )

        _uiState.value = _uiState.value.copy(
            baseUrl = baseUrl,
            message = "Conexión guardada correctamente.",
            error = null
        )
    }

    fun testConnection() {
        val ip = _uiState.value.ip.trim()
        val port = _uiState.value.port.trim()

        if (ip.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "La IP no puede estar vacía.",
                message = null
            )
            return
        }

        val baseUrl = ConnectionSettingsStore.buildBaseUrl(ip, port)
        val healthUrl = baseUrl.trimEnd('/') + "/health/"

        _uiState.value = _uiState.value.copy(
            isTesting = true,
            message = null,
            error = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(4, TimeUnit.SECONDS)
                    .readTimeout(4, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url(healthUrl)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        ActionHistoryStore.add(
                            source = "Ajustes",
                            title = "Prueba de conexión correcta",
                            detail = "La Raspberry responde en $healthUrl",
                            success = true
                        )

                        _uiState.value = _uiState.value.copy(
                            isTesting = false,
                            baseUrl = baseUrl,
                            message = "Conexión correcta. La Raspberry responde.",
                            error = null
                        )
                    } else {
                        val errorMessage = "La Raspberry responde, pero con código ${response.code}."

                        ActionHistoryStore.add(
                            source = "Ajustes",
                            title = "Prueba de conexión fallida",
                            detail = errorMessage,
                            success = false
                        )

                        _uiState.value = _uiState.value.copy(
                            isTesting = false,
                            baseUrl = baseUrl,
                            message = null,
                            error = errorMessage
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMessage = "No se pudo conectar con la Raspberry: ${e.message}"

                ActionHistoryStore.add(
                    source = "Ajustes",
                    title = "Error probando conexión",
                    detail = errorMessage,
                    success = false
                )

                _uiState.value = _uiState.value.copy(
                    isTesting = false,
                    baseUrl = baseUrl,
                    message = null,
                    error = errorMessage
                )
            }
        }
    }
}