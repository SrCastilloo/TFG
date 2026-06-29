package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.grip_history.GripHistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GripHistoryUiState(
    val total: Int = 0,
    val successful: Int = 0,
    val failed: Int = 0,
    val items: List<GripHistoryUiItem> = emptyList()
)

data class GripHistoryUiItem(
    val id: String,
    val emoji: String,
    val title: String,
    val success: Boolean,
    val resultText: String,
    val reasonText: String,
    val dateText: String,
    val message: String?,
    val elapsedText: String,
    val stepsText: String,
    val closeStepText: String,
    val targetPositionText: String,
    val contactSensorText: String,
    val contactsText: String,
    val ignoredSensorsText: String,
    val missingSensorsText: String
)

class GripHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase
        .getInstance(application)
        .gripHistoryDao()

    val uiState: StateFlow<GripHistoryUiState> =
        dao.observeRecent(200)
            .map { history ->
                buildUiState(history)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GripHistoryUiState()
            )

    fun clearHistory() {
        viewModelScope.launch {
            dao.clear()
        }
    }

    private fun buildUiState(history: List<GripHistoryEntity>): GripHistoryUiState {
        val items = history.map { it.toUiItem() }

        return GripHistoryUiState(
            total = items.size,
            successful = items.count { it.success },
            failed = items.count { !it.success },
            items = items
        )
    }

    private fun GripHistoryEntity.toUiItem(): GripHistoryUiItem {
        val success = isGripSuccessful(this)

        return GripHistoryUiItem(
            id = id,
            emoji = if (gripType == "safe") "🛡️" else "🦾",
            title = friendlyGripType(gripType),
            success = success,
            resultText = if (success) "Correcto" else "Fallido",
            reasonText = friendlyReason(reason),
            dateText = formatDate(timestampMillis),
            message = message,
            elapsedText = elapsedSeconds?.let { "${formatOneDecimal(it)} s" } ?: "—",
            stepsText = stepCount?.toString() ?: "—",
            closeStepText = closeStep?.toString() ?: "—",
            targetPositionText = targetPositionId?.let { "Posición $it" } ?: "—",
            contactSensorText = friendlySensor(contactSensor).ifBlank { "—" },
            contactsText = buildContactsText(this),
            ignoredSensorsText = friendlySensorsCsv(ignoredSensorsCsv).ifBlank { "Ninguno" },
            missingSensorsText = friendlySensorsCsv(missingSensorsCsv).ifBlank { "Ninguno" }
        )
    }

    private fun isGripSuccessful(item: GripHistoryEntity): Boolean {
        return when (item.gripType) {
            "safe" -> item.ok && item.contactDetected == true
            "full" -> item.ok && item.allContactsDetected == true
            else -> item.ok
        }
    }

    private fun buildContactsText(item: GripHistoryEntity): String {
        val contactCount = item.contactCount
        val requiredCount = item.requiredContactCount

        return when {
            contactCount != null && requiredCount != null -> "$contactCount / $requiredCount"
            contactCount != null -> "$contactCount detectados"
            else -> "—"
        }
    }

    private fun formatDate(timestampMillis: Long): String {
        return SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss",
            Locale.getDefault()
        ).format(Date(timestampMillis))
    }

    private fun formatOneDecimal(value: Double): String {
        return String.format(Locale.getDefault(), "%.1f", value)
    }

    private fun friendlyGripType(type: String): String {
        return when (type) {
            "safe" -> "Agarre seguro"
            "full" -> "Agarre completo"
            else -> type
        }
    }

    private fun friendlyReason(reason: String?): String {
        return when (reason) {
            "contact_detected" -> "Contacto detectado"
            "all_contacts_detected" -> "Todos los contactos detectados"
            "timeout" -> "Tiempo agotado"
            "initial_contact" -> "Contacto inicial"
            "initial_all_contacts" -> "Contacto inicial completo"
            "hand_not_available" -> "Mano no disponible"
            "capacitive_not_available" -> "Sensores no disponibles"
            "communication_error" -> "Error de comunicación"
            "error" -> "Error"
            null -> "Sin motivo registrado"
            else -> reason
        }
    }

    private fun friendlySensor(sensor: String?): String {
        if (sensor.isNullOrBlank()) return ""

        return when (sensor.lowercase(Locale.getDefault())) {
            "pinky" -> "Meñique"
            "ring" -> "Anular"
            "middle" -> "Medio"
            "index" -> "Índice"
            "thumb" -> "Pulgar"
            "palm" -> "Palma"
            else -> sensor
        }
    }

    private fun friendlySensorsCsv(sensorsCsv: String?): String {
        if (sensorsCsv.isNullOrBlank()) return ""

        return sensorsCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString(", ") { friendlySensor(it) }
    }
}