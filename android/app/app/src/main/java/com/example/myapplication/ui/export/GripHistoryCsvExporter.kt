package com.example.myapplication.ui.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.grip_history.GripHistoryEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GripHistoryCsvExporter {

    suspend fun exportGripHistory(context: Context): Uri {
        val database = AppDatabase.getInstance(context)
        val grips = database.gripHistoryDao().getAllOnce()

        val csvContent = buildCsv(grips)

        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }

        val fileName = "historial_agarres_${fileDate()}.csv"
        val csvFile = File(exportsDir, fileName)

        csvFile.writeText(csvContent, Charsets.UTF_8)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            csvFile
        )
    }

    private fun buildCsv(grips: List<GripHistoryEntity>): String {
        return buildString {
            appendLine("sep=;")

            appendLine(
                listOf(
                    "fecha_hora",
                    "tipo_agarre",
                    "resultado",
                    "motivo",
                    "mensaje",
                    "tiempo_segundos",
                    "pasos",
                    "close_step",
                    "posicion_objetivo",
                    "contacto_detectado",
                    "sensor_contacto",
                    "todos_contactos_detectados",
                    "sensores_activos",
                    "sensores_contacto",
                    "sensores_faltantes",
                    "contactos_detectados",
                    "contactos_requeridos",
                    "sensores_ignorados"
                ).joinToString(";")
            )

            grips.forEach { grip ->
                appendLine(
                    listOf(
                        csv(formatDate(grip.timestampMillis)),
                        csv(friendlyGripType(grip.gripType)),
                        csv(if (grip.ok) "Correcto" else "Fallido"),
                        csv(friendlyReason(grip.reason)),
                        csv(grip.message),
                        csv(grip.elapsedSeconds),
                        csv(grip.stepCount),
                        csv(grip.closeStep),
                        csv(grip.targetPositionId),
                        csv(grip.contactDetected),
                        csv(friendlySensor(grip.contactSensor)),
                        csv(grip.allContactsDetected),
                        csv(friendlySensorsCsv(grip.activeSensorsCsv)),
                        csv(friendlySensorsCsv(grip.contactSensorsCsv)),
                        csv(friendlySensorsCsv(grip.missingSensorsCsv)),
                        csv(grip.contactCount),
                        csv(grip.requiredContactCount),
                        csv(friendlySensorsCsv(grip.ignoredSensorsCsv))
                    ).joinToString(";")
                )
            }
        }
    }

    private fun csv(value: Any?): String {
        val text = value?.toString().orEmpty()
        val escaped = text.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun formatDate(timestampMillis: Long): String {
        return SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date(timestampMillis))
    }

    private fun fileDate(): String {
        return SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
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
            null -> ""
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