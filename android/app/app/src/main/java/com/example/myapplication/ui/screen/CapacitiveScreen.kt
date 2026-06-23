package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.remote.dto.CapacitiveDto
import com.example.myapplication.data.remote.dto.SafeGripDto
import com.example.myapplication.ui.viewmodel.CapacitiveViewModel

@Composable
fun CapacitiveScreen(
    onBack: () -> Unit,
    scaffoldPadding: PaddingValues = PaddingValues(),
    viewModel: CapacitiveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
            .consumeWindowInsets(scaffoldPadding)
            .imePadding()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020617),
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
    ) {
        val isLandscape = maxWidth > maxHeight
        val compact = maxHeight < 560.dp || maxWidth < 390.dp

        if (isLandscape) {
            CapacitiveLandscapeContent(
                data = uiState.data,
                isLoading = uiState.isLoading,
                message = uiState.message,
                error = uiState.error,
                onBack = onBack,
                onRefresh = { viewModel.refreshStatus() },
                compact = true,
                safeGripResult = uiState.safeGripResult,
                isSafeGripLoading = uiState.isSafeGripLoading,
                onSafeGrip = { viewModel.startSafeGrip() },
            )
        } else {
            CapacitivePortraitContent(
                data = uiState.data,
                isLoading = uiState.isLoading,
                message = uiState.message,
                error = uiState.error,
                onBack = onBack,
                onRefresh = { viewModel.refreshStatus() },
                compact = compact,
                safeGripResult = uiState.safeGripResult,
                isSafeGripLoading = uiState.isSafeGripLoading,
                onSafeGrip = { viewModel.startSafeGrip() },
            )
        }
    }
}

@Composable
private fun CapacitivePortraitContent(
    data: CapacitiveDto?,
    isLoading: Boolean,
    message: String?,
    error: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    compact: Boolean,
    safeGripResult: SafeGripDto?,
    isSafeGripLoading: Boolean,
    onSafeGrip: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
            ),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp)
    ) {
        item {
            CapacitiveTopRow(
                onBack = onBack,
                onRefresh = onRefresh,
                isLoading = isLoading
            )
        }

        item {
            CapacitiveHeroCard(compact = compact)
        }

        item {
            SafeGripCard(
                safeGripResult = safeGripResult,
                isLoading = isSafeGripLoading,
                onSafeGrip = onSafeGrip,
                compact = compact
            )
        }

        capacitiveSummaryItems(
            data = data,
            message = message,
            error = error,
            compact = compact
        )

        capacitiveSensorItems(
            data = data,
            compact = compact
        )

        item {
            CapacitiveHelpCard(compact = compact)
        }
    }
}

@Composable
private fun CapacitiveLandscapeContent(
    data: CapacitiveDto?,
    isLoading: Boolean,
    message: String?,
    error: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    compact: Boolean,
    safeGripResult: SafeGripDto?,
    isSafeGripLoading: Boolean,
    onSafeGrip: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
            )
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 12.dp,
                bottom = 14.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CapacitiveTopRow(
                    onBack = onBack,
                    onRefresh = onRefresh,
                    isLoading = isLoading
                )
            }

            item {
                CapacitiveHeroCard(compact = compact)
            }

            item {
                SafeGripCard(
                    safeGripResult = safeGripResult,
                    isLoading = isSafeGripLoading,
                    onSafeGrip = onSafeGrip,
                    compact = compact
                )
            }

            capacitiveSummaryItems(
                data = data,
                message = message,
                error = error,
                compact = compact
            )

            item {
                CapacitiveHelpCard(compact = compact)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Lecturas",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            capacitiveSensorItems(
                data = data,
                compact = compact
            )
        }
    }
}

private fun LazyListScope.capacitiveSummaryItems(
    data: CapacitiveDto?,
    message: String?,
    error: String?,
    compact: Boolean
) {
    item {
        CapacitiveSummaryCard(
            data = data,
            compact = compact
        )
    }

    if (message != null) {
        item {
            CapacitiveFeedbackCard(
                title = "Última lectura",
                text = message,
                isError = false,
                compact = compact
            )
        }
    }

    if (error != null) {
        item {
            CapacitiveFeedbackCard(
                title = "Error leyendo sensores",
                text = error,
                isError = true,
                compact = compact
            )
        }
    }
}

private fun LazyListScope.capacitiveSensorItems(
    data: CapacitiveDto?,
    compact: Boolean
) {
    val sensors = listOf("pinky", "ring", "middle", "index", "thumb", "palm")

    if (data == null) {
        item {
            EmptyCapacitiveCard(compact = compact)
        }
    } else {
        items(sensors) { sensor ->
            CapacitiveSensorCard(
                sensorName = sensor,
                value = data.status?.get(sensor),
                threshold = data.heights?.get(sensor),
                contact = data.contacts?.get(sensor) == true,
                compact = compact
            )
        }
    }
}

@Composable
private fun CapacitiveTopRow(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.14f),
            onClick = onBack
        ) {
            Text(
                text = "← Volver",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(
            onClick = onRefresh,
            enabled = !isLoading
        ) {
            Text(
                text = if (isLoading) "Leyendo..." else "Actualizar",
                color = Color(0xFFA5F3FC),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CapacitiveHeroCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF14B8A6),
                            Color(0xFF2563EB),
                            Color(0xFF7C3AED)
                        )
                    )
                )
                .padding(if (compact) 16.dp else 22.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "✋ Sensores capacitivos",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

            Text(
                text = "Lectura táctil de la mano",
                color = Color.White,
                style = if (compact) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Consulta los valores de cada sensor y comprueba si alguno supera su umbral de contacto.",
                color = Color.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (compact) 3 else 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CapacitiveSummaryCard(
    data: CapacitiveDto?,
    compact: Boolean
) {
    val available = data?.available == true
    val contactCount = data?.contact_count ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F1B33)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Resumen táctil",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryChip(
                    title = "Estado",
                    value = if (available) "OK" else "NO",
                    color = if (available) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )

                SummaryChip(
                    title = "Contactos",
                    value = contactCount.toString(),
                    color = if (contactCount > 0) Color(0xFFF59E0B) else Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = if (data == null) {
                    "Todavía no se ha recibido ninguna lectura."
                } else if (contactCount > 0) {
                    "Se ha detectado contacto en $contactCount sensor(es)."
                } else {
                    "No se detecta contacto en los sensores."
                },
                color = Color(0xFFCBD5E1),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SummaryChip(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.18f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = title,
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CapacitiveSensorCard(
    sensorName: String,
    value: Int?,
    threshold: Int?,
    contact: Boolean,
    compact: Boolean
) {
    val safeValue = value ?: 0
    val safeThreshold = threshold ?: 1
    val progress = (safeValue.toFloat() / safeThreshold.toFloat()).coerceIn(0f, 1f)

    val contactColor = if (contact) Color(0xFFF59E0B) else Color(0xFF38BDF8)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 14.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = friendlySensorName(sensorName),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Valor $safeValue · Umbral $safeThreshold",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = contactColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = if (contact) "Contacto" else "Libre",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = contactColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = contactColor,
                trackColor = Color.White.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun CapacitiveFeedbackCard(
    title: String,
    text: String,
    isError: Boolean,
    compact: Boolean
) {
    val background = if (isError) Color(0xFF4C1D24) else Color(0xFF0F3B2E)
    val titleColor = if (isError) Color(0xFFFFD5DC) else Color(0xFFB7F7D8)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 18.dp)
        ) {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = text,
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CapacitiveHelpCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF5)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Cómo interpretar los valores",
                color = Color(0xFF111827),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Cada sensor devuelve un valor numérico leído por I2C.",
                color = Color(0xFF374151)
            )

            Text(
                text = "Si el valor supera el umbral configurado, se considera contacto.",
                color = Color(0xFF374151)
            )

            Text(
                text = "Estos sensores permiten añadir feedback táctil a la mano robótica.",
                color = Color(0xFF374151)
            )
        }
    }
}

@Composable
private fun EmptyCapacitiveCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Text(
            text = "Pulsa actualizar para leer los sensores capacitivos.",
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun friendlySensorName(sensorName: String): String {
    return when (sensorName) {
        "pinky" -> "Meñique"
        "ring" -> "Anular"
        "middle" -> "Medio"
        "index" -> "Índice"
        "thumb" -> "Pulgar"
        "palm" -> "Palma"
        else -> sensorName.replaceFirstChar { it.uppercase() }
    }
}


@Composable
private fun SafeGripCard(
    safeGripResult: SafeGripDto?,
    isLoading: Boolean,
    onSafeGrip: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🤝 Agarre seguro",
                color = Color(0xFF0F172A),
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Cierra la mano y la detiene automáticamente cuando cualquier sensor capacitivo detecta contacto.",
                color = Color(0xFF475569),
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onSafeGrip,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 46.dp else 52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF14B8A6),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF14B8A6).copy(alpha = 0.45f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = if (isLoading) "Ejecutando..." else "Iniciar agarre seguro",
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (safeGripResult != null) {
                val resultColor = when {
                    safeGripResult.ok && safeGripResult.contact_detected == true -> Color(0xFF065F46)
                    safeGripResult.ok && safeGripResult.reason == "timeout" -> Color(0xFF92400E)
                    else -> Color(0xFF7F1D1D)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = resultColor.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = safeGripResult.message ?: "Resultado del agarre seguro.",
                            color = resultColor,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "Contacto: ${if (safeGripResult.contact_detected == true) "sí" else "no"} · Sensor: ${friendlySafeGripSensor(safeGripResult.contact_sensor)}",
                            color = Color(0xFF334155),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            text = "Tiempo: ${safeGripResult.elapsed_seconds ?: 0.0}s · Motivo: ${friendlySafeGripReason(safeGripResult.reason)}",
                            color = Color(0xFF64748B),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

private fun friendlySafeGripSensor(sensor: String?): String {
    return when (sensor) {
        "pinky" -> "Meñique"
        "ring" -> "Anular"
        "middle" -> "Medio"
        "index" -> "Índice"
        "thumb" -> "Pulgar"
        "palm" -> "Palma"
        null -> "Ninguno"
        else -> sensor
    }
}

private fun friendlySafeGripReason(reason: String?): String {
    return when (reason) {
        "contact_detected" -> "contacto detectado"
        "initial_contact" -> "contacto previo"
        "timeout" -> "tiempo máximo"
        "capacitive_not_available" -> "sensores no disponibles"
        "hand_not_available" -> "mano no disponible"
        "error" -> "error"
        null -> "desconocido"
        else -> reason
    }
}