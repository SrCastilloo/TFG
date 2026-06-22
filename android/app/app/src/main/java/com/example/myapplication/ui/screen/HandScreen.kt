package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.myapplication.ui.viewmodel.HandViewModel

@Composable
fun HandScreen(
    onBack: () -> Unit,
    onVoiceControl: () -> Unit = {},
    scaffoldPadding: PaddingValues = PaddingValues(),
    viewModel: HandViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
            .consumeWindowInsets(scaffoldPadding)
            .background(AppBackground)
    ) {
        val isLandscape = maxWidth > maxHeight
        val useTwoPane = isLandscape && maxWidth >= 680.dp
        val compact = maxHeight < 560.dp || maxWidth < 390.dp
        val columns = calculatePositionColumns(maxWidth, maxHeight)

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
                LandscapeHandDashboard(
                    positions = uiState.positions,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    actionMessage = uiState.actionMessage,
                    columns = columns,
                    compact = compact,
                    onBack = onBack,
                    onRefresh = { viewModel.loadPositions() },
                    onOpenHand = { viewModel.openHand() },
                    onStopHand = { viewModel.stopHand() },
                    onMoveToPosition = { viewModel.moveToPosition(it) },
                    onVoiceControl = onVoiceControl
                )
            } else {
                PortraitHandDashboard(
                    positions = uiState.positions,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    actionMessage = uiState.actionMessage,
                    columns = columns,
                    compact = compact,
                    onBack = onBack,
                    onRefresh = { viewModel.loadPositions() },
                    onOpenHand = { viewModel.openHand() },
                    onStopHand = { viewModel.stopHand() },
                    onMoveToPosition = { viewModel.moveToPosition(it) },
                    onVoiceControl = onVoiceControl
                )
            }

            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
private fun PortraitHandDashboard(
    positions: List<Int>,
    isLoading: Boolean,
    error: String?,
    actionMessage: String?,
    columns: Int,
    compact: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenHand: () -> Unit,
    onStopHand: () -> Unit,
    onMoveToPosition: (Int) -> Unit,
    onVoiceControl: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = if (compact) 12.dp else 16.dp,
            end = if (compact) 12.dp else 16.dp,
            top = if (compact) 12.dp else 16.dp,
            bottom = if (compact) 28.dp else 40.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TopActionRow(
                onBack = onBack,
                onRefresh = onRefresh,
                isLoading = isLoading,
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            HeroHandCard(
                positionCount = positions.size,
                compact = compact
            )
        }

        if (error != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                FeedbackCard(
                    title = "Algo ha fallado",
                    text = error,
                    isError = true,
                    compact = compact
                )
            }
        }

        if (actionMessage != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                FeedbackCard(
                    title = "Última acción",
                    text = actionMessage,
                    isError = false,
                    compact = compact
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            QuickActionsCard(
                isLoading = isLoading,
                onOpen = onOpenHand,
                onStop = onStopHand,
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            QuickGuideCard(compact = compact)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle(
                title = "Posiciones guardadas",
                subtitle = "Elige un agarre o gesto para mover la mano",
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
                HandPositionCard(
                    position = position,
                    description = getHandPositionDescription(position),
                    enabled = !isLoading,
                    onClick = { onMoveToPosition(position) },
                    compact = compact
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            VoiceControlEntryCard(
                enabled = !isLoading,
                onClick = onVoiceControl,
                compact = compact
            )
        }
    }
}

@Composable
private fun LandscapeHandDashboard(
    positions: List<Int>,
    isLoading: Boolean,
    error: String?,
    actionMessage: String?,
    columns: Int,
    compact: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenHand: () -> Unit,
    onStopHand: () -> Unit,
    onMoveToPosition: (Int) -> Unit,
    onVoiceControl: () -> Unit
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                TopActionRow(
                    onBack = onBack,
                    onRefresh = onRefresh,
                    isLoading = isLoading,
                    compact = true
                )
            }

            item {
                HeroHandCard(
                    positionCount = positions.size,
                    compact = true
                )
            }

            if (error != null) {
                item {
                    FeedbackCard(
                        title = "Error",
                        text = error,
                        isError = true,
                        compact = true
                    )
                }
            }

            if (actionMessage != null) {
                item {
                    FeedbackCard(
                        title = "Acción",
                        text = actionMessage,
                        isError = false,
                        compact = true
                    )
                }
            }

            item {
                QuickActionsCard(
                    isLoading = isLoading,
                    onOpen = onOpenHand,
                    onStop = onStopHand,
                    compact = true
                )
            }

            item {
                QuickGuideCard(compact = true)
            }

            item {
                VoiceControlEntryCard(
                    enabled = !isLoading,
                    onClick = onVoiceControl,
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
                    title = "Posiciones guardadas",
                    subtitle = "Panel rápido de agarres y gestos",
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
                    HandPositionCard(
                        position = position,
                        description = getHandPositionDescription(position),
                        enabled = !isLoading,
                        onClick = { onMoveToPosition(position) },
                        compact = true
                    )
                }
            }
        }
    }
}

private fun calculatePositionColumns(width: Dp, height: Dp): Int {
    val isLandscape = width > height

    return when {
        width < 360.dp -> 1
        isLandscape && width >= 1100.dp -> 4
        isLandscape -> 2
        width >= 840.dp -> 3
        else -> 2
    }
}

@Composable
private fun BackgroundGlow() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-90).dp, y = (-80).dp)
                .background(
                    color = Color(0xFF22D3EE).copy(alpha = 0.22f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 110.dp)
                .background(
                    color = Color(0xFF8B5CF6).copy(alpha = 0.24f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 70.dp)
                .background(
                    color = Color(0xFF10B981).copy(alpha = 0.14f),
                    shape = CircleShape
                )
        )
    }
}

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
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.13f),
            onClick = onBack,
            modifier = Modifier.border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(999.dp)
            )
        ) {
            Text(
                text = "← Volver",
                modifier = Modifier.padding(
                    horizontal = if (compact) 13.dp else 16.dp,
                    vertical = if (compact) 8.dp else 10.dp
                ),
                color = Color.White,
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
                }
            )
        }
    }
}

@Composable
private fun HeroHandCard(
    positionCount: Int,
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
                    .size(if (compact) 96.dp else 132.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 32.dp, y = (-38).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.13f),
                        shape = CircleShape
                    )
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        LightPill(
                            text = "Modo manual",
                            compact = compact
                        )

                        Spacer(modifier = Modifier.height(if (compact) 8.dp else 14.dp))

                        Text(
                            text = "Control de mano robótica",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            style = if (compact) {
                                MaterialTheme.typography.titleLarge
                            } else {
                                MaterialTheme.typography.headlineMedium
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Coloca la mano en la posición que desees.",
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
                        text = "🦾",
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
                    HeroMetric(
                        title = "$positionCount",
                        subtitle = "posiciones",
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )

                    HeroMetric(
                        title = "Control",
                        subtitle = "Absoluto",
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )

                    HeroMetric(
                        title = "Voz",
                        subtitle = "atajo",
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(
    title: String,
    subtitle: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 16.dp else 20.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 10.dp else 14.dp,
                vertical = if (compact) 8.dp else 12.dp
            )
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                style = if (compact) {
                    MaterialTheme.typography.bodyLarge
                } else {
                    MaterialTheme.typography.titleLarge
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.82f),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionsCard(
    isLoading: Boolean,
    onOpen: () -> Unit,
    onStop: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC).copy(alpha = 0.96f)
        )
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
                        text = "Acciones rápidas",
                        color = Color(0xFF0F172A),
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
                        text = "Controles esenciales para usar la mano con seguridad.",
                        color = Color(0xFF475569),
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
                    enabled = !isLoading,
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFF10B981),
                    compact = compact
                )

                FriendlyActionButton(
                    text = "Parar",
                    icon = "⛔",
                    enabled = !isLoading,
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFFF97316),
                    compact = compact
                )
            }
        }
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
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.65f)
        ),
        contentPadding = PaddingValues(horizontal = 10.dp)
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
private fun HandPositionCard(
    position: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val gradient = when (position % 5) {
        0 -> Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF2563EB)))
        1 -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
        2 -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF0EA5E9)))
        3 -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444)))
        else -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF14B8A6)))
    }

    Card(
        modifier = modifier.heightIn(
            min = if (compact) 158.dp else 188.dp
        ),
        shape = RoundedCornerShape(if (compact) 22.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(if (compact) 22.dp else 28.dp)
                )
                .padding(if (compact) 12.dp else 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 72.dp else 92.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 28.dp, y = (-28).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 5.dp
                                ),
                                color = Color.White,
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
                        text = description,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = if (compact) {
                            MaterialTheme.typography.titleSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        maxLines = if (compact) 2 else 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Enviar posición a la mano",
                        color = Color.White.copy(alpha = 0.86f),
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 42.dp else 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.10f),
                        disabledContentColor = Color.White.copy(alpha = 0.55f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Mover",
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickGuideCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBEB).copy(alpha = 0.97f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 14.dp else 20.dp,
                vertical = if (compact) 14.dp else 18.dp
            )
        ) {
            Text(
                text = "Guía rápida",
                color = Color(0xFF111827),
                fontWeight = FontWeight.ExtraBold,
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                }
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

            GuideStep(
                number = "1",
                title = "Abre la mano",
                text = "Colócala en una posición inicial segura.",
                compact = compact
            )

            Spacer(modifier = Modifier.height(8.dp))

            GuideStep(
                number = "2",
                title = "Elige una posición",
                text = "Selecciona el agarre o gesto que quieras usar.",
                compact = compact
            )

            Spacer(modifier = Modifier.height(8.dp))

            GuideStep(
                number = "3",
                title = "Detén si hace falta",
                text = "Usa Parar para cortar cualquier movimiento.",
                compact = compact
            )
        }
    }
}

@Composable
private fun GuideStep(
    number: String,
    title: String,
    text: String,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF0F766E)
        ) {
            Text(
                text = number,
                modifier = Modifier.padding(
                    horizontal = if (compact) 9.dp else 11.dp,
                    vertical = if (compact) 5.dp else 7.dp
                ),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFF111827),
                fontWeight = FontWeight.ExtraBold,
                style = if (compact) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = text,
                color = Color(0xFF4B5563),
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
}

@Composable
private fun VoiceControlEntryCard(
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(VoiceGradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(if (compact) 22.dp else 30.dp)
                )
                .padding(
                    horizontal = if (compact) 14.dp else 20.dp,
                    vertical = if (compact) 14.dp else 20.dp
                )
        ) {
            if (compact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎙️ Control por voz",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Mueve la mano usando comandos hablados.",
                            color = Color.White.copy(alpha = 0.88f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onClick,
                        enabled = enabled,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.20f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.10f),
                            disabledContentColor = Color.White.copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = "Abrir",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else {
                Column {
                    Text(
                        text = "🎙️ Control por voz",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Accede al control por voz para escuchar comandos y mover la mano automáticamente.",
                        color = Color.White.copy(alpha = 0.90f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onClick,
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.20f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.10f),
                            disabledContentColor = Color.White.copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = "Ir al control por voz",
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    title: String,
    text: String,
    isError: Boolean,
    compact: Boolean
) {
    val background = if (isError) {
        Color(0xFF7F1D1D).copy(alpha = 0.88f)
    } else {
        Color(0xFF064E3B).copy(alpha = 0.88f)
    }

    val icon = if (isError) "⚠️" else "✅"

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
                    color = Color.White,
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

@Composable
private fun EmptyPositionsCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
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
                color = Color.White,
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
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    compact: Boolean
) {
    Column {
        Text(
            text = title,
            color = Color.White,
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
            text = subtitle,
            color = Color(0xFFC7D2FE),
            fontWeight = FontWeight.Medium,
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
            color = Color.White,
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
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617).copy(alpha = 0.42f)),
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
                    color = Color(0xFF67E8F9),
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Cargando posiciones",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Conectando con la mano robótica...",
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private val AppBackground = Brush.verticalGradient(
    listOf(
        Color(0xFF020617),
        Color(0xFF071A2A),
        Color(0xFF0F2F3D),
        Color(0xFF132E44)
    )
)

private val HeroGradient = Brush.linearGradient(
    listOf(
        Color(0xFF14B8A6),
        Color(0xFF06B6D4),
        Color(0xFF2563EB),
        Color(0xFF7C3AED)
    )
)

private val VoiceGradient = Brush.linearGradient(
    listOf(
        Color(0xFF7C3AED),
        Color(0xFF2563EB),
        Color(0xFF06B6D4)
    )
)