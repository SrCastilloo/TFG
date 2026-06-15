package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.common.getHandPositionDescription
import com.example.myapplication.ui.common.getHandPositionTitle
import com.example.myapplication.ui.viewmodel.StatusViewModel

@Composable
fun StatusScreen(
    onBack: () -> Unit,
    viewModel: StatusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BoxWithConstraints(
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
        val isLandscape = maxWidth > maxHeight
        val horizontalPadding = if (isLandscape) 10.dp else 16.dp
        val topPadding = if (isLandscape) 18.dp else 16.dp
        val bottomPadding = if (isLandscape) 24.dp else 110.dp
        val itemSpacing = if (isLandscape) 8.dp else 16.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                ),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = topPadding,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            item {
                TopActionRow(
                    onBack = onBack,
                    onRefresh = { viewModel.loadData() },
                    isLoading = uiState.isLoading || uiState.isActionLoading,
                    compact = isLandscape
                )
            }

            item {
                HeroStatusCard(
                    mode = uiState.mode,
                    simulation = uiState.simulation,
                    handAvailable = uiState.handAvailable,
                    cameraAvailable = uiState.cameraAvailable,
                    compact = isLandscape
                )
            }

            item {
                MessageCard(
                    actionMessage = uiState.actionMessage,
                    error = uiState.error,
                    compact = isLandscape
                )
            }

            item {
                SectionTitle(
                    title = "Resumen rápido",
                    subtitle = "Lo más importante de un vistazo",
                    compact = isLandscape
                )
            }

            item {
                OverviewGrid(
                    mode = uiState.mode,
                    simulation = uiState.simulation,
                    handAvailable = uiState.handAvailable,
                    cameraAvailable = uiState.cameraAvailable,
                    lastPositionMapped = uiState.lastPositionMapped,
                    compact = isLandscape
                )
            }

            item {
                SectionTitle(
                    title = "Acciones principales",
                    subtitle = "Controles básicos para empezar",
                    compact = isLandscape
                )
            }

            item {
                QuickActionsCard(
                    isActionLoading = uiState.isActionLoading,
                    onOpen = { viewModel.openHand() },
                    onStop = { viewModel.stopHand() },
                    compact = isLandscape
                )
            }

            item {
                ModesSelectorCard(
                    currentMode = uiState.mode,
                    isActionLoading = uiState.isActionLoading,
                    onHandMode = { viewModel.setModeHand() },
                    onVoiceMode = { viewModel.setModeVoice() },
                    onCameraMode = { viewModel.setModeCamera() },
                    compact = isLandscape
                )
            }

            item {
                SectionTitle(
                    title = "Posiciones de la mano",
                    subtitle = "Selecciona una posición guardada",
                    compact = isLandscape
                )
            }

            items(uiState.positions.chunked(2)) { rowPositions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 10.dp else 14.dp)
                ) {
                    rowPositions.forEach { position ->
                        PositionCard(
                            position = position,
                            description = getHandPositionDescription(position),
                            enabled = !uiState.isActionLoading,
                            onClick = { viewModel.moveToPosition(position) },
                            compact = isLandscape,
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
                    subtitle = "Información técnica útil",
                    compact = isLandscape
                )
            }

            item {
                TechnicalDetailsCard(
                    configPath = uiState.configPath,
                    lastPositionMapped = uiState.lastPositionMapped,
                    mode = uiState.mode,
                    simulation = uiState.simulation,
                    compact = isLandscape
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
    isLoading: Boolean,
    compact: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.clickable { onBack() },
            shape = RoundedCornerShape(999.dp),
            color = Color(0xFFCBD5E1).copy(alpha = 0.20f)
        ) {
            Text(
                text = "← Volver",
                modifier = Modifier.padding(
                    horizontal = if (compact) 12.dp else 16.dp,
                    vertical = if (compact) 7.dp else 10.dp
                ),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = if (compact) MaterialTheme.typography.bodySmall
                else MaterialTheme.typography.bodyMedium
            )
        }

        TextButton(
            onClick = onRefresh,
            enabled = !isLoading,
            contentPadding = PaddingValues(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 8.dp
            )
        ) {
            Text(
                text = "Recargar",
                color = Color(0xFFBAE6FD),
                fontWeight = FontWeight.Bold,
                style = if (compact) MaterialTheme.typography.bodySmall
                else MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HeroStatusCard(
    mode: String,
    simulation: Boolean?,
    handAvailable: Boolean,
    cameraAvailable: Boolean,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = if (compact) 22.dp else 30.dp,
            topEnd = 22.dp,
            bottomEnd = if (compact) 22.dp else 22.dp,
            bottomStart = if (compact) 22.dp else 22.dp
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
                .padding(
                    horizontal = if (compact) 16.dp else 22.dp,
                    vertical = if (compact) 14.dp else 22.dp
                )
        ) {
            if (compact) {
                // Landscape: layout horizontal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Izquierda: pill + título
                    Column(modifier = Modifier.weight(1f)) {
                        LightPill(text = "Estado de la mano", compact = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Panel de control",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Centro: descripción
                    Text(
                        text = heroDescription(mode, simulation),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.95f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // Derecha: chips de estado
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
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
            } else {
                // Portrait: layout vertical original
                Column {
                    LightPill(text = "Estado de la mano", compact = false)
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
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
}

@Composable
private fun MessageCard(
    actionMessage: String?,
    error: String?,
    compact: Boolean = false
) {
    when {
        error != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(if (compact) 18.dp else 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4C1D24))
            ) {
                if (compact) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Algo ha fallado",
                            color = Color(0xFFFFD5DC),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "·",
                            color = Color(0xFFFFD5DC).copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = error,
                            color = Color(0xFFFFE4E8),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
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
        }

        actionMessage != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(if (compact) 18.dp else 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3B2E))
            ) {
                if (compact) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Última acción",
                            color = Color(0xFFB7F7D8),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "·",
                            color = Color(0xFFB7F7D8).copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = actionMessage,
                            color = Color(0xFFE7FFF3),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
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
}

@Composable
private fun OverviewGrid(
    mode: String,
    simulation: Boolean?,
    handAvailable: Boolean,
    cameraAvailable: Boolean,
    lastPositionMapped: Int?,
    compact: Boolean = false
) {
    if (compact) {
        // Landscape: 4 cards en una sola fila + wide card aparte
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Modo actual",
                    value = friendlyMode(mode),
                    emoji = "🧭",
                    background = Brush.linearGradient(listOf(Color(0xFFFFE29F), Color(0xFFFFB087))),
                    compact = true
                )
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Entorno",
                    value = if (simulation == true) "Simulación" else "Real",
                    emoji = "⚙️",
                    background = Brush.linearGradient(listOf(Color(0xFFA7F3D0), Color(0xFF5EEAD4))),
                    compact = true
                )
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Mano",
                    value = if (handAvailable) "Disponible" else "No disponible",
                    emoji = "🖐️",
                    background = Brush.linearGradient(listOf(Color(0xFFBFDBFE), Color(0xFF93C5FD))),
                    compact = true
                )
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Última posición",
                    value = lastPositionMapped?.toString() ?: "Ninguna",
                    emoji = "📍",
                    background = Brush.linearGradient(listOf(Color(0xFFE9D5FF), Color(0xFFC4B5FD))),
                    compact = true
                )
            }
            OverviewWideCard(
                title = "Cámara",
                value = if (cameraAvailable) "Disponible para detección de objetos"
                else "No disponible en este momento",
                emoji = "📷",
                compact = true
            )
        }
    } else {
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
                    background = Brush.linearGradient(listOf(Color(0xFFFFE29F), Color(0xFFFFB087)))
                )
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Entorno",
                    value = if (simulation == true) "Simulación" else "Real",
                    emoji = "⚙️",
                    background = Brush.linearGradient(listOf(Color(0xFFA7F3D0), Color(0xFF5EEAD4)))
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
                    background = Brush.linearGradient(listOf(Color(0xFFBFDBFE), Color(0xFF93C5FD)))
                )
                OverviewCard(
                    modifier = Modifier.weight(1f),
                    title = "Última posición",
                    value = lastPositionMapped?.toString() ?: "Ninguna",
                    emoji = "📍",
                    background = Brush.linearGradient(listOf(Color(0xFFE9D5FF), Color(0xFFC4B5FD)))
                )
            }
            OverviewWideCard(
                title = "Cámara",
                value = if (cameraAvailable) "Disponible para detección de objetos"
                else "No disponible en este momento",
                emoji = "📷"
            )
        }
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    emoji: String,
    background: Brush,
    compact: Boolean = false
) {
    Card(
        modifier = modifier.heightIn(min = if (compact) 90.dp else 128.dp),
        shape = RoundedCornerShape(if (compact) 18.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(background)
                .fillMaxSize()
                .padding(if (compact) 10.dp else 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = emoji,
                    style = if (compact) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.headlineMedium
                )
                Column {
                    Text(
                        text = title,
                        color = Color(0xFF1F2937),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        color = Color(0xFF0F172A),
                        style = if (compact) MaterialTheme.typography.bodySmall
                        else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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
    emoji: String,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 18.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF5))
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 12.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = if (compact) MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.width(if (compact) 10.dp else 14.dp))
            Column {
                Text(
                    text = title,
                    color = Color(0xFF374151),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    color = Color(0xFF111827),
                    style = if (compact) MaterialTheme.typography.bodySmall
                    else MaterialTheme.typography.bodyLarge,
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
    onStop: () -> Unit,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = if (compact) 20.dp else 28.dp,
            topEnd = 18.dp,
            bottomEnd = if (compact) 20.dp else 28.dp,
            bottomStart = 18.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF6))
    ) {
        if (compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Control rápido",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Usa estas acciones para abrir la mano o detener cualquier movimiento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4B5563),
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    FriendlyActionButton(
                        text = "Abrir mano",
                        enabled = !isActionLoading,
                        onClick = onOpen,
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF22C55E),
                        compact = true
                    )
                    FriendlyActionButton(
                        text = "Parar",
                        enabled = !isActionLoading,
                        onClick = onStop,
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFFF97316),
                        compact = true
                    )
                }
            }
        } else {
            Column(modifier = Modifier.padding(20.dp)) {
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
}

@Composable
private fun ModesSelectorCard(
    currentMode: String,
    isActionLoading: Boolean,
    onHandMode: () -> Unit,
    onVoiceMode: () -> Unit,
    onCameraMode: () -> Unit,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111D33))
    ) {
        if (compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Modo de funcionamiento",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Activo: ${friendlyMode(currentMode)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ModeButton(
                        text = "Mano",
                        active = currentMode.equals("hand", ignoreCase = true),
                        enabled = !isActionLoading,
                        onClick = onHandMode,
                        modifier = Modifier.weight(1f),
                        compact = true
                    )
                    ModeButton(
                        text = "Voz",
                        active = currentMode.equals("voice", ignoreCase = true),
                        enabled = !isActionLoading,
                        onClick = onVoiceMode,
                        modifier = Modifier.weight(1f),
                        compact = true
                    )
                    ModeButton(
                        text = "Cámara",
                        active = currentMode.equals("camera", ignoreCase = true),
                        enabled = !isActionLoading,
                        onClick = onCameraMode,
                        modifier = Modifier.weight(1f),
                        compact = true
                    )
                }
            }
        } else {
            Column(modifier = Modifier.padding(20.dp)) {
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
}

@Composable
private fun PositionCard(
    position: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = if (compact) 20.dp else 26.dp,
            topEnd = 18.dp,
            bottomEnd = if (compact) 20.dp else 26.dp,
            bottomStart = 18.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF7C3AED)))
                )
                .fillMaxWidth()
                .padding(if (compact) 12.dp else 16.dp)
        ) {
            Column {
                Text(
                    text = "Posición $position:",
                    color = Color.White,
                    style = if (compact) MaterialTheme.typography.bodyLarge
                    else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))
                Text(
                    text = description,
                    color = Color.White,
                    style = if (compact) MaterialTheme.typography.bodyMedium
                    else MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = if (compact) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))
                Text(
                    text = "Mover la mano a esta configuración.",
                    color = Color.White.copy(alpha = 0.90f),
                    style = if (compact) MaterialTheme.typography.bodySmall
                    else MaterialTheme.typography.bodyMedium,
                    maxLines = if (compact) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))
                Button(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 40.dp else 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.12f),
                        disabledContentColor = Color.White.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = if (compact) 4.dp else 8.dp
                    )
                ) {
                    Text(
                        text = "Mover",
                        fontWeight = FontWeight.Bold,
                        style = if (compact) MaterialTheme.typography.bodySmall
                        else MaterialTheme.typography.bodyMedium
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
    simulation: Boolean?,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 18.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
    ) {
        if (compact) {
            // Landscape: 2 columnas de detalles
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Información técnica",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow(label = "Modo", value = friendlyMode(mode))
                        DetailRow(label = "Simulación", value = if (simulation == true) "Sí" else "No")
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow(label = "Última posición", value = lastPositionMapped?.toString() ?: "Ninguna")
                        DetailRow(label = "Config", value = configPath)
                    }
                }
            }
        } else {
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
}

@Composable
private fun DetailRow(label: String, value: String) {
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
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    compact: Boolean = false
) {
    Column {
        Text(
            text = title,
            style = if (compact) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = if (compact) MaterialTheme.typography.bodySmall
            else MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(top = if (compact) 2.dp else 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FriendlyActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    compact: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(if (compact) 44.dp else 54.dp),
        shape = RoundedCornerShape(if (compact) 14.dp else 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(
            horizontal = if (compact) 8.dp else 16.dp,
            vertical = if (compact) 4.dp else 8.dp
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = if (compact) MaterialTheme.typography.bodySmall
            else MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ModeButton(
    text: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val background = if (active) Color(0xFF7C3AED) else Color.White.copy(alpha = 0.08f)

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(if (compact) 42.dp else 50.dp),
        shape = RoundedCornerShape(if (compact) 12.dp else 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = Color.White,
            disabledContainerColor = background.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(
            horizontal = if (compact) 6.dp else 12.dp,
            vertical = if (compact) 4.dp else 8.dp
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = if (compact) MaterialTheme.typography.bodySmall
            else MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LightPill(text: String, compact: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 6.dp else 8.dp
            ),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = if (compact) MaterialTheme.typography.bodySmall
            else MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusMiniChip(label: String, ok: Boolean) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (ok) Color(0xFFDCFCE7).copy(alpha = 0.28f)
        else Color(0xFFFEE2E2).copy(alpha = 0.24f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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