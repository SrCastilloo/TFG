package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.viewmodel.StatusViewModel
import com.example.myapplication.ui.common.getHandPositionDescription
import com.example.myapplication.ui.common.getHandPositionTitle





@Composable
fun StatusScreen(
    onBack: () -> Unit,
    viewModel: StatusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF132238),
                        Color(0xFF1B3353)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TopActionRow(
                    onBack = onBack,
                    onRefresh = { viewModel.loadData() },
                    isLoading = uiState.isLoading || uiState.isActionLoading
                )
            }

            item {
                HeroStatusCard(
                    mode = uiState.mode,
                    simulation = uiState.simulation,
                    handAvailable = uiState.handAvailable,
                    cameraAvailable = uiState.cameraAvailable
                )
            }

            item {
                MessageCard(
                    actionMessage = uiState.actionMessage,
                    error = uiState.error
                )
            }

            item {
                SectionTitle(
                    title = "Resumen rápido",
                    subtitle = "Lo más importante de un vistazo"
                )
            }

            item {
                OverviewGrid(
                    mode = uiState.mode,
                    simulation = uiState.simulation,
                    handAvailable = uiState.handAvailable,
                    cameraAvailable = uiState.cameraAvailable,
                    lastPositionMapped = uiState.lastPositionMapped
                )
            }

            item {
                SectionTitle(
                    title = "Acciones principales",
                    subtitle = "Controles básicos para empezar"
                )
            }

            item {
                QuickActionsCard(
                    isActionLoading = uiState.isActionLoading,
                    onOpen = { viewModel.openHand() },
                    onStop = { viewModel.stopHand() }
                )
            }

            item {
                ModesSelectorCard(
                    currentMode = uiState.mode,
                    isActionLoading = uiState.isActionLoading,
                    onHandMode = { viewModel.setModeHand() },
                    onVoiceMode = { viewModel.setModeVoice() },
                    onCameraMode = { viewModel.setModeCamera() }
                )
            }

            item {
                SectionTitle(
                    title = "Posiciones de la mano",
                    subtitle = "Selecciona una posición guardada"
                )
            }

            items(uiState.positions.chunked(2)) { rowPositions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowPositions.forEach { position ->
                        PositionCard(
                            position = position,
                            description = getHandPositionDescription(position),
                            enabled = !uiState.isActionLoading,
                            onClick = { viewModel.moveToPosition(position) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (rowPositions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                SectionTitle(
                    title = "Detalles del sistema",
                    subtitle = "Información técnica útil"
                )
            }

            item {
                TechnicalDetailsCard(
                    configPath = uiState.configPath,
                    lastPositionMapped = uiState.lastPositionMapped,
                    mode = uiState.mode,
                    simulation = uiState.simulation
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.82f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF7DD3FC),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Actualizando estado...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopActionRow(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            modifier = Modifier.clickable { onBack() },
            shape = RoundedCornerShape(999.dp),
            color = Color(0xFFCBD5E1).copy(alpha = 0.20f)
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
                text = "Recargar",
                color = Color(0xFFBAE6FD),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HeroStatusCard(
    mode: String,
    simulation: Boolean?,
    handAvailable: Boolean,
    cameraAvailable: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 30.dp,
            topEnd = 30.dp,
            bottomEnd = 22.dp,
            bottomStart = 22.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF818CF8),
                            Color(0xFFC084FC)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                LightPill(text = "Estado de la mano")

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Panel de control",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = heroDescription(mode, simulation),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.95f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusMiniChip(
                        label = if (handAvailable) "Mano conectada" else "Mano no disponible",
                        ok = handAvailable
                    )
                    StatusMiniChip(
                        label = if (cameraAvailable) "Cámara lista" else "Cámara no disponible",
                        ok = cameraAvailable
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageCard(
    actionMessage: String?,
    error: String?
) {
    when {
        error != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4C1D24)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Algo ha fallado",
                        color = Color(0xFFFFD5DC),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = error,
                        color = Color(0xFFFFE4E8),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        actionMessage != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F3B2E)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Última acción",
                        color = Color(0xFFB7F7D8),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = actionMessage,
                        color = Color(0xFFE7FFF3),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewGrid(
    mode: String,
    simulation: Boolean?,
    handAvailable: Boolean,
    cameraAvailable: Boolean,
    lastPositionMapped: Int?
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OverviewCard(
                modifier = Modifier.weight(1f),
                title = "Modo actual",
                value = friendlyMode(mode),
                emoji = "🧭",
                background = Brush.linearGradient(
                    listOf(Color(0xFFFFE29F), Color(0xFFFFB087))
                )
            )
            OverviewCard(
                modifier = Modifier.weight(1f),
                title = "Entorno",
                value = if (simulation == true) "Simulación" else "Real",
                emoji = "⚙️",
                background = Brush.linearGradient(
                    listOf(Color(0xFFA7F3D0), Color(0xFF5EEAD4))
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OverviewCard(
                modifier = Modifier.weight(1f),
                title = "Mano",
                value = if (handAvailable) "Disponible" else "No disponible",
                emoji = "🖐️",
                background = Brush.linearGradient(
                    listOf(Color(0xFFBFDBFE), Color(0xFF93C5FD))
                )
            )
            OverviewCard(
                modifier = Modifier.weight(1f),
                title = "Última posición",
                value = lastPositionMapped?.toString() ?: "Ninguna",
                emoji = "📍",
                background = Brush.linearGradient(
                    listOf(Color(0xFFE9D5FF), Color(0xFFC4B5FD))
                )
            )
        }

        OverviewWideCard(
            title = "Cámara",
            value = if (cameraAvailable) {
                "Disponible para detección de objetos"
            } else {
                "No disponible en este momento"
            },
            emoji = "📷"
        )
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    emoji: String,
    background: Brush
) {
    Card(
        modifier = modifier.heightIn(min = 128.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(background)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineMedium
                )

                Column {
                    Text(
                        text = title,
                        color = Color(0xFF1F2937),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        color = Color(0xFF0F172A),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewWideCard(
    title: String,
    value: String,
    emoji: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF5)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = Color(0xFF374151),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    color = Color(0xFF111827),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    isActionLoading: Boolean,
    onOpen: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 18.dp,
            bottomEnd = 28.dp,
            bottomStart = 18.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF6)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Control rápido",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF111827),
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Usa estas acciones para abrir la mano o detener cualquier movimiento.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B5563),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FriendlyActionButton(
                    text = "Abrir mano",
                    enabled = !isActionLoading,
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFF22C55E)
                )

                FriendlyActionButton(
                    text = "Parar",
                    enabled = !isActionLoading,
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFFF97316)
                )
            }
        }
    }
}

@Composable
private fun ModesSelectorCard(
    currentMode: String,
    isActionLoading: Boolean,
    onHandMode: () -> Unit,
    onVoiceMode: () -> Unit,
    onCameraMode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111D33)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Modo de funcionamiento",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "El modo activo ahora es: ${friendlyMode(currentMode)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModeButton(
                    text = "Mano",
                    active = currentMode.equals("hand", ignoreCase = true),
                    enabled = !isActionLoading,
                    onClick = onHandMode,
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    text = "Voz",
                    active = currentMode.equals("voice", ignoreCase = true),
                    enabled = !isActionLoading,
                    onClick = onVoiceMode,
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    text = "Cámara",
                    active = currentMode.equals("camera", ignoreCase = true),
                    enabled = !isActionLoading,
                    onClick = onCameraMode,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PositionCard(
    position: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 18.dp,
            bottomEnd = 26.dp,
            bottomStart = 18.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF1D4ED8),
                            Color(0xFF7C3AED)
                        )
                    )
                )
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Posición $position:",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Mover la mano a esta configuración.",
                    color = Color.White.copy(alpha = 0.90f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.12f),
                        disabledContentColor = Color.White.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Mover",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
@Composable
private fun TechnicalDetailsCard(
    configPath: String,
    lastPositionMapped: Int?,
    mode: String,
    simulation: Boolean?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Información técnica",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.ExtraBold
            )

            DetailRow(label = "Modo", value = friendlyMode(mode))
            DetailRow(label = "Simulación", value = if (simulation == true) "Sí" else "No")
            DetailRow(label = "Última posición", value = lastPositionMapped?.toString() ?: "Ninguna")
            DetailRow(label = "Config", value = configPath)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            color = Color(0xFF64748B),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = Color(0xFF0F172A),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FriendlyActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModeButton(
    text: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (active) Color(0xFF7C3AED) else Color.White.copy(alpha = 0.08f)
    val content = Color.White

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = content,
            disabledContainerColor = background.copy(alpha = 0.45f),
            disabledContentColor = content.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LightPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusMiniChip(
    label: String,
    ok: Boolean
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (ok) {
            Color(0xFFDCFCE7).copy(alpha = 0.28f)
        } else {
            Color(0xFFFEE2E2).copy(alpha = 0.24f)
        }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun friendlyMode(mode: String): String {
    return when (mode.lowercase()) {
        "hand" -> "Mano"
        "voice" -> "Voz"
        "camera" -> "Cámara"
        "init" -> "Inicial"
        else -> "Desconocido"
    }
}

private fun heroDescription(mode: String, simulation: Boolean?): String {
    val modeText = friendlyMode(mode)
    val environment = if (simulation == true) "simulación" else "entorno real"

    return "Ahora mismo el sistema está en modo $modeText y trabajando en $environment."
}