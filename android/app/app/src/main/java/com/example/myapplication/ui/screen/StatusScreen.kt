package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.common.getHandPositionDescription
import com.example.myapplication.ui.common.getHandPositionTitle
import com.example.myapplication.ui.viewmodel.StatusViewModel
import com.example.myapplication.ui.history.ActionHistorySection

// ─────────────────────────────────────────────────────────────────────────────
// Color tokens
// ─────────────────────────────────────────────────────────────────────────────

private val BgTop = Color(0xFF020617)
private val BgMiddle = Color(0xFF071A2A)
private val BgBottom = Color(0xFF132E44)

private val TextPrimary = Color.White
private val TextMuted = Color(0xFFCBD5E1)
private val TextDim = Color(0xFF94A3B8)

private val DarkText = Color(0xFF0F172A)
private val BodyText = Color(0xFF475569)

private val AccentCyan = Color(0xFF06B6D4)
private val AccentSky = Color(0xFF38BDF8)
private val AccentBlue = Color(0xFF2563EB)
private val AccentViolet = Color(0xFF7C3AED)
private val AccentPurple = Color(0xFFA855F7)
private val AccentPink = Color(0xFFEC4899)
private val AccentOrange = Color(0xFFF97316)
private val AccentGreen = Color(0xFF10B981)
private val AccentTeal = Color(0xFF14B8A6)
private val AccentRed = Color(0xFFEF4444)

private val AppBackground = Brush.verticalGradient(
    listOf(
        BgTop,
        BgMiddle,
        BgBottom
    )
)

private val HeroGradient = Brush.linearGradient(
    listOf(
        AccentSky,
        AccentBlue,
        AccentViolet,
        AccentPurple
    )
)

private val ModeGradient = Brush.linearGradient(
    listOf(
        Color(0xFF111827),
        Color(0xFF1E1B4B),
        Color(0xFF164E63)
    )
)

private val DetailsGradient = Brush.linearGradient(
    listOf(
        Color(0xFFF8FAFC),
        Color(0xFFE0F2FE)
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatusScreen(
    onBack: () -> Unit,
    viewModel: StatusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        val isLandscape = maxWidth > maxHeight
        val useTwoPane = isLandscape && maxWidth >= 720.dp
        val compact = maxHeight < 560.dp || maxWidth < 390.dp
        val columns = statusColumns(maxWidth, maxHeight)

        BackgroundGlow()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
        ) {
            if (useTwoPane) {
                StatusLandscapeContent(
                    mode = uiState.mode,
                    simulation = uiState.simulation,
                    handAvailable = uiState.handAvailable,
                    cameraAvailable = uiState.cameraAvailable,
                    lastPositionMapped = uiState.lastPositionMapped,
                    configPath = uiState.configPath,
                    positions = uiState.positions,
                    actionMessage = uiState.actionMessage,
                    error = uiState.error,
                    isLoading = uiState.isLoading,
                    isActionLoading = uiState.isActionLoading,
                    columns = columns,
                    onBack = onBack,
                    onRefresh = { viewModel.loadData() },
                    onOpen = { viewModel.openHand() },
                    onStop = { viewModel.stopHand() },
                    onHandMode = { viewModel.setModeHand() },
                    onVoiceMode = { viewModel.setModeVoice() },
                    onCameraMode = { viewModel.setModeCamera() },
                    onMoveToPosition = { viewModel.moveToPosition(it) }
                )
            } else {
                StatusPortraitContent(
                    mode = uiState.mode,
                    simulation = uiState.simulation,
                    handAvailable = uiState.handAvailable,
                    cameraAvailable = uiState.cameraAvailable,
                    lastPositionMapped = uiState.lastPositionMapped,
                    configPath = uiState.configPath,
                    positions = uiState.positions,
                    actionMessage = uiState.actionMessage,
                    error = uiState.error,
                    isLoading = uiState.isLoading,
                    isActionLoading = uiState.isActionLoading,
                    compact = compact,
                    columns = columns,
                    onBack = onBack,
                    onRefresh = { viewModel.loadData() },
                    onOpen = { viewModel.openHand() },
                    onStop = { viewModel.stopHand() },
                    onHandMode = { viewModel.setModeHand() },
                    onVoiceMode = { viewModel.setModeVoice() },
                    onCameraMode = { viewModel.setModeCamera() },
                    onMoveToPosition = { viewModel.moveToPosition(it) }
                )
            }

            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Responsive layouts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusPortraitContent(
    mode: String,
    simulation: Boolean?,
    handAvailable: Boolean,
    cameraAvailable: Boolean,
    lastPositionMapped: Int?,
    configPath: String,
    positions: List<Int>,
    actionMessage: String?,
    error: String?,
    isLoading: Boolean,
    isActionLoading: Boolean,
    compact: Boolean,
    columns: Int,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpen: () -> Unit,
    onStop: () -> Unit,
    onHandMode: () -> Unit,
    onVoiceMode: () -> Unit,
    onCameraMode: () -> Unit,
    onMoveToPosition: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = if (compact) 12.dp else 16.dp,
            end = if (compact) 12.dp else 16.dp,
            top = if (compact) 12.dp else 16.dp,
            bottom = if (compact) 36.dp else 110.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TopActionRow(
                onBack = onBack,
                onRefresh = onRefresh,
                isLoading = isLoading || isActionLoading,
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            HeroStatusCard(
                mode = mode,
                simulation = simulation,
                handAvailable = handAvailable,
                cameraAvailable = cameraAvailable,
                compact = compact
            )
        }

        if (error != null || actionMessage != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                MessageCard(
                    actionMessage = actionMessage,
                    error = error,
                    compact = compact
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle(
                title = "Resumen rápido",
                subtitle = "Lo más importante de un vistazo",
                compact = compact
            )
        }

        item {
            OverviewCard(
                title = "Modo actual",
                value = friendlyMode(mode),
                emoji = "🧭",
                background = Brush.linearGradient(listOf(Color(0xFFFFE29F), Color(0xFFFFB087))),
                compact = compact
            )
        }

        item {
            OverviewCard(
                title = "Entorno",
                value = if (simulation == true) "Simulación" else "Real",
                emoji = "⚙️",
                background = Brush.linearGradient(listOf(Color(0xFFA7F3D0), Color(0xFF5EEAD4))),
                compact = compact
            )
        }

        item {
            OverviewCard(
                title = "Mano",
                value = if (handAvailable) "Disponible" else "No disponible",
                emoji = "🖐️",
                background = Brush.linearGradient(listOf(Color(0xFFBFDBFE), Color(0xFF93C5FD))),
                compact = compact
            )
        }

        item {
            OverviewCard(
                title = "Última posición",
                value = lastPositionMapped?.toString() ?: "Ninguna",
                emoji = "📍",
                background = Brush.linearGradient(listOf(Color(0xFFE9D5FF), Color(0xFFC4B5FD))),
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            CameraWideCard(
                cameraAvailable = cameraAvailable,
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle(
                title = "Acciones principales",
                subtitle = "Controles básicos para empezar",
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            QuickActionsCard(
                isActionLoading = isActionLoading,
                onOpen = onOpen,
                onStop = onStop,
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            ModesSelectorCard(
                currentMode = mode,
                isActionLoading = isActionLoading,
                onHandMode = onHandMode,
                onVoiceMode = onVoiceMode,
                onCameraMode = onCameraMode,
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle(
                title = "Posiciones de la mano",
                subtitle = "Selecciona una posición guardada",
                compact = compact
            )
        }

        if (positions.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyPositionsCard(compact = compact)
            }
        } else {
            items(
                items = positions,
                key = { it }
            ) { position ->
                PositionCard(
                    position = position,
                    title = getHandPositionTitle(position),
                    description = getHandPositionDescription(position),
                    enabled = !isActionLoading,
                    onClick = { onMoveToPosition(position) },
                    compact = compact
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            ActionHistorySection(
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle(
                title = "Detalles del sistema",
                subtitle = "Información técnica útil",
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            TechnicalDetailsCard(
                configPath = configPath,
                lastPositionMapped = lastPositionMapped,
                mode = mode,
                simulation = simulation,
                compact = compact
            )
        }
    }
}

@Composable
private fun StatusLandscapeContent(
    mode: String,
    simulation: Boolean?,
    handAvailable: Boolean,
    cameraAvailable: Boolean,
    lastPositionMapped: Int?,
    configPath: String,
    positions: List<Int>,
    actionMessage: String?,
    error: String?,
    isLoading: Boolean,
    isActionLoading: Boolean,
    columns: Int,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpen: () -> Unit,
    onStop: () -> Unit,
    onHandMode: () -> Unit,
    onVoiceMode: () -> Unit,
    onCameraMode: () -> Unit,
    onMoveToPosition: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 12.dp,
                bottom = 18.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(0.43f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TopActionRow(
                    onBack = onBack,
                    onRefresh = onRefresh,
                    isLoading = isLoading || isActionLoading,
                    compact = true
                )
            }

            item {
                HeroStatusCard(
                    mode = mode,
                    simulation = simulation,
                    handAvailable = handAvailable,
                    cameraAvailable = cameraAvailable,
                    compact = true
                )
            }

            if (error != null || actionMessage != null) {
                item {
                    MessageCard(
                        actionMessage = actionMessage,
                        error = error,
                        compact = true
                    )
                }
            }

            item {
                QuickActionsCard(
                    isActionLoading = isActionLoading,
                    onOpen = onOpen,
                    onStop = onStop,
                    compact = true
                )
            }

            item {
                ModesSelectorCard(
                    currentMode = mode,
                    isActionLoading = isActionLoading,
                    onHandMode = onHandMode,
                    onVoiceMode = onVoiceMode,
                    onCameraMode = onCameraMode,
                    compact = true
                )
            }


            item {
                ActionHistorySection(
                    compact = true
                )
            }

            item {
                TechnicalDetailsCard(
                    configPath = configPath,
                    lastPositionMapped = lastPositionMapped,
                    mode = mode,
                    simulation = simulation,
                    compact = true
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns.coerceAtLeast(2)),
            modifier = Modifier
                .weight(0.57f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionTitle(
                    title = "Resumen del sistema",
                    subtitle = "Estado operativo y posiciones disponibles",
                    compact = true
                )
            }

            item {
                OverviewCard(
                    title = "Modo actual",
                    value = friendlyMode(mode),
                    emoji = "🧭",
                    background = Brush.linearGradient(listOf(Color(0xFFFFE29F), Color(0xFFFFB087))),
                    compact = true
                )
            }

            item {
                OverviewCard(
                    title = "Entorno",
                    value = if (simulation == true) "Simulación" else "Real",
                    emoji = "⚙️",
                    background = Brush.linearGradient(listOf(Color(0xFFA7F3D0), Color(0xFF5EEAD4))),
                    compact = true
                )
            }

            item {
                OverviewCard(
                    title = "Mano",
                    value = if (handAvailable) "Disponible" else "No disponible",
                    emoji = "🖐️",
                    background = Brush.linearGradient(
                        listOf(
                            Color(0xFFBFDBFE),
                            Color(0xFF93C5FD)
                        )
                    ),
                    compact = true
                )
            }
            item {
                OverviewCard(
                    title = "Última posición",
                    value = lastPositionMapped?.toString() ?: "Ninguna",
                    emoji = "📍",
                    background = Brush.linearGradient(listOf(Color(0xFFE9D5FF), Color(0xFFC4B5FD))),
                    compact = true
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                CameraWideCard(
                    cameraAvailable = cameraAvailable,
                    compact = true
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionTitle(
                    title = "Posiciones de la mano",
                    subtitle = "Panel rápido de configuraciones guardadas",
                    compact = true
                )
            }

            if (positions.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyPositionsCard(compact = true)
                }
            } else {
                items(
                    items = positions,
                    key = { it }
                ) { position ->
                    PositionCard(
                        position = position,
                        title = getHandPositionTitle(position),
                        description = getHandPositionDescription(position),
                        enabled = !isActionLoading,
                        onClick = { onMoveToPosition(position) },
                        compact = true
                    )
                }
            }
        }
    }
}

private fun statusColumns(
    maxWidth: Dp,
    maxHeight: Dp
): Int {
    val isLandscape = maxWidth > maxHeight

    return when {
        maxWidth < 370.dp -> 1
        isLandscape && maxWidth >= 1100.dp -> 3
        isLandscape -> 2
        maxWidth >= 900.dp -> 3
        maxWidth >= 600.dp -> 2
        else -> 2
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Background
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BackgroundGlow() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-120).dp, y = (-100).dp)
                .background(
                    color = AccentCyan.copy(alpha = 0.18f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 110.dp)
                .background(
                    color = AccentViolet.copy(alpha = 0.22f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 70.dp)
                .background(
                    color = AccentGreen.copy(alpha = 0.13f),
                    shape = CircleShape
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopActionRow(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .clickable { onBack() }
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp)
                ),
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.13f)
        ) {
            Text(
                text = "← Volver",
                modifier = Modifier.padding(
                    horizontal = if (compact) 13.dp else 16.dp,
                    vertical = if (compact) 8.dp else 10.dp
                ),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = if (compact) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                maxLines = 1
            )
        }

        TextButton(
            onClick = onRefresh,
            enabled = !isLoading,
            contentPadding = PaddingValues(
                horizontal = if (compact) 10.dp else 14.dp,
                vertical = if (compact) 6.dp else 8.dp
            )
        ) {
            Text(
                text = "Recargar",
                color = Color(0xFFA5F3FC),
                fontWeight = FontWeight.ExtraBold,
                style = if (compact) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                maxLines = 1
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
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(HeroGradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(if (compact) 24.dp else 34.dp)
                )
                .padding(
                    horizontal = if (compact) 16.dp else 22.dp,
                    vertical = if (compact) 16.dp else 24.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 110.dp else 150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 42.dp, y = (-48).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
            )

            Column {
                LightPill(
                    text = "Estado del sistema",
                    compact = compact
                )

                Spacer(modifier = Modifier.height(if (compact) 10.dp else 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Panel de estado",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            style = if (compact) {
                                MaterialTheme.typography.titleLarge
                            } else {
                                MaterialTheme.typography.headlineMedium
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = heroDescription(mode, simulation),
                            color = Color.White.copy(alpha = 0.92f),
                            style = if (compact) {
                                MaterialTheme.typography.bodySmall
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            maxLines = if (compact) 2 else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "📊",
                        style = if (compact) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.displaySmall
                        },
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(if (compact) 14.dp else 20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
                ) {
                    StatusMiniChip(
                        label = if (handAvailable) "Mano conectada" else "Mano no disponible",
                        ok = handAvailable,
                        modifier = Modifier.weight(1f)
                    )

                    StatusMiniChip(
                        label = if (cameraAvailable) "Cámara lista" else "Sin cámara",
                        ok = cameraAvailable,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Messages
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MessageCard(
    actionMessage: String?,
    error: String?,
    compact: Boolean
) {
    val isError = error != null
    val title = if (isError) "Algo ha fallado" else "Última acción"
    val text = error ?: actionMessage.orEmpty()
    val icon = if (isError) "⚠️" else "✅"
    val background = if (isError) {
        Color(0xFF7F1D1D).copy(alpha = 0.92f)
    } else {
        Color(0xFF065F46).copy(alpha = 0.92f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 14.dp else 18.dp,
                vertical = if (compact) 12.dp else 16.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = text,
                    color = Color.White.copy(alpha = 0.90f),
                    style = if (compact) {
                        MaterialTheme.typography.bodySmall
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    maxLines = if (compact) 2 else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overview
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverviewCard(
    title: String,
    value: String,
    emoji: String,
    background: Brush,
    compact: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 132.dp else 150.dp),
        shape = RoundedCornerShape(if (compact) 22.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(background)
                .fillMaxWidth()
                .padding(if (compact) 14.dp else 18.dp)
        ) {
            Column {
                Text(
                    text = emoji,
                    style = if (compact) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.headlineMedium
                    }
                )

                Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

                Text(
                    text = title,
                    color = Color(0xFF1F2937),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = value,
                    color = DarkText,
                    style = if (compact) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CameraWideCard(
    cameraAvailable: Boolean,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFFFBF5),
                            Color(0xFFE0F2FE)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(if (compact) 22.dp else 28.dp)
                )
                .padding(if (compact) 14.dp else 18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📷",
                    style = if (compact) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.headlineLarge
                    }
                )

                Spacer(modifier = Modifier.width(if (compact) 12.dp else 16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cámara",
                        color = Color(0xFF374151),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = if (cameraAvailable) {
                            "Disponible para detección de objetos"
                        } else {
                            "No disponible en este momento"
                        },
                        color = DarkText,
                        style = if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Actions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsCard(
    isActionLoading: Boolean,
    onOpen: () -> Unit,
    onStop: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC).copy(alpha = 0.97f))
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 14.dp else 20.dp,
                vertical = if (compact) 14.dp else 20.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡",
                    style = if (compact) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.headlineSmall
                    }
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Control rápido",
                        color = DarkText,
                        fontWeight = FontWeight.ExtraBold,
                        style = if (compact) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Abre la mano o detén cualquier movimiento.",
                        color = BodyText,
                        style = if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
            ) {
                FriendlyActionButton(
                    text = "Abrir",
                    icon = "🖐️",
                    enabled = !isActionLoading,
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    containerColor = AccentGreen,
                    compact = compact
                )

                FriendlyActionButton(
                    text = "Parar",
                    icon = "⛔",
                    enabled = !isActionLoading,
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    containerColor = AccentOrange,
                    compact = compact
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
    onCameraMode: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(ModeGradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(if (compact) 24.dp else 30.dp)
                )
                .padding(if (compact) 14.dp else 20.dp)
        ) {
            Column {
                Text(
                    text = "Modo de funcionamiento",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    style = if (compact) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Activo: ${friendlyMode(currentMode)}",
                    color = TextMuted,
                    style = if (compact) {
                        MaterialTheme.typography.bodySmall
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
                ) {
                    ModeButton(
                        text = "Mano",
                        icon = "🖐️",
                        active = currentMode.equals("hand", ignoreCase = true),
                        enabled = !isActionLoading,
                        onClick = onHandMode,
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )

                    ModeButton(
                        text = "Voz",
                        icon = "🎙️",
                        active = currentMode.equals("voice", ignoreCase = true),
                        enabled = !isActionLoading,
                        onClick = onVoiceMode,
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )

                    ModeButton(
                        text = "Cámara",
                        icon = "📷",
                        active = currentMode.equals("camera", ignoreCase = true),
                        enabled = !isActionLoading,
                        onClick = onCameraMode,
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Positions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PositionCard(
    position: Int,
    title: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean
) {
    val gradient = when (position % 5) {
        0 -> Brush.linearGradient(listOf(AccentCyan, AccentBlue))
        1 -> Brush.linearGradient(listOf(AccentPurple, AccentPink))
        2 -> Brush.linearGradient(listOf(AccentGreen, AccentCyan))
        3 -> Brush.linearGradient(listOf(Color(0xFFF59E0B), AccentRed))
        else -> Brush.linearGradient(listOf(AccentViolet, AccentTeal))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 168.dp else 196.dp),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(if (compact) 24.dp else 30.dp)
                )
                .padding(if (compact) 14.dp else 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 80.dp else 100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 34.dp, y = (-34).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
            )

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White.copy(alpha = 0.20f)
                    ) {
                        Text(
                            text = "#$position",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "🖐️",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

                Text(
                    text = title,
                    color = TextPrimary,
                    style = if (compact) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.90f),
                    style = if (compact) {
                        MaterialTheme.typography.bodySmall
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    maxLines = if (compact) 2 else 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

                Button(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 42.dp else 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.20f),
                        contentColor = TextPrimary,
                        disabledContainerColor = Color.White.copy(alpha = 0.10f),
                        disabledContentColor = Color.White.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Mover",
                        fontWeight = FontWeight.ExtraBold,
                        style = if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPositionsCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📭",
                style = if (compact) {
                    MaterialTheme.typography.headlineMedium
                } else {
                    MaterialTheme.typography.displaySmall
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No hay posiciones cargadas",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                }
            )

            Text(
                text = "Pulsa Recargar para pedirlas de nuevo al backend.",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Details
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TechnicalDetailsCard(
    configPath: String,
    lastPositionMapped: Int?,
    mode: String,
    simulation: Boolean?,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(DetailsGradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(if (compact) 24.dp else 30.dp)
                )
                .padding(if (compact) 16.dp else 20.dp)
        ) {
            Column {
                Text(
                    text = "Información técnica",
                    color = DarkText,
                    fontWeight = FontWeight.ExtraBold,
                    style = if (compact) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow(
                            label = "Modo",
                            value = friendlyMode(mode)
                        )

                        DetailRow(
                            label = "Simulación",
                            value = if (simulation == true) "Sí" else "No"
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DetailRow(
                            label = "Última posición",
                            value = lastPositionMapped?.toString() ?: "Ninguna"
                        )

                        DetailRow(
                            label = "Config",
                            value = configPath
                        )
                    }
                }
            }
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
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            color = DarkText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Common components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    compact: Boolean
) {
    Column {
        Text(
            text = title,
            style = if (compact) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = TextMuted,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FriendlyActionButton(
    text: String,
    icon: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    compact: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(if (compact) 48.dp else 56.dp),
        shape = RoundedCornerShape(if (compact) 16.dp else 20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = TextPrimary,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.62f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(
            text = "$icon $text",
            fontWeight = FontWeight.ExtraBold,
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ModeButton(
    text: String,
    icon: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    val background = if (active) {
        AccentViolet
    } else {
        Color.White.copy(alpha = 0.10f)
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(if (compact) 44.dp else 50.dp),
        shape = RoundedCornerShape(if (compact) 14.dp else 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = TextPrimary,
            disabledContainerColor = background.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.62f)
        ),
        contentPadding = PaddingValues(horizontal = 6.dp)
    ) {
        Text(
            text = if (compact) text else "$icon $text",
            fontWeight = FontWeight.ExtraBold,
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LightPill(
    text: String,
    compact: Boolean
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.18f),
        modifier = Modifier.border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.16f),
            shape = RoundedCornerShape(999.dp)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 6.dp else 8.dp
            ),
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusMiniChip(
    label: String,
    ok: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = if (ok) {
            Color(0xFFDCFCE7).copy(alpha = 0.26f)
        } else {
            Color(0xFFFEE2E2).copy(alpha = 0.24f)
        }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617).copy(alpha = 0.46f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.94f),
            modifier = Modifier.border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(28.dp)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF7DD3FC),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Actualizando estado",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Consultando información del sistema...",
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun friendlyMode(mode: String): String {
    return when (mode.lowercase()) {
        "hand" -> "Mano"
        "voice" -> "Voz"
        "camera" -> "Cámara"
        "init" -> "Inicial"
        else -> "Desconocido"
    }
}

private fun heroDescription(
    mode: String,
    simulation: Boolean?
): String {
    val modeText = friendlyMode(mode)
    val environment = if (simulation == true) "simulación" else "entorno real"
    return "Sistema en modo $modeText trabajando en $environment."
}