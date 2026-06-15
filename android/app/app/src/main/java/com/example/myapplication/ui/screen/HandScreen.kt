package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
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
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF071A20),
                        Color(0xFF0D2430),
                        Color(0xFF173847)
                    )
                )
            )
    ) {
        val isLandscape = maxWidth > maxHeight

        val horizontalPadding = if (isLandscape) 10.dp else 16.dp
        val topPadding = if (isLandscape) 18.dp else 16.dp
        val bottomPadding = if (isLandscape) 24.dp else 40.dp
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
                    onRefresh = { viewModel.loadPositions() },
                    isLoading = uiState.isLoading,
                    compact = isLandscape
                )
            }

            item {
                HeroHandCard(compact = isLandscape)
            }

            if (uiState.error != null) {
                item {
                    FeedbackCard(
                        title = "Algo ha fallado",
                        text = uiState.error ?: "",
                        isError = true,
                        compact = isLandscape
                    )
                }
            }

            if (uiState.actionMessage != null) {
                item {
                    FeedbackCard(
                        title = "Última acción",
                        text = uiState.actionMessage ?: "",
                        isError = false,
                        compact = isLandscape
                    )
                }
            }

            item {
                QuickGuideCard(compact = isLandscape)
            }

            item {
                SectionTitle(
                    title = "Acciones rápidas",
                    subtitle = "Controles básicos para usar la mano",
                    compact = isLandscape
                )
            }

            item {
                QuickActionsCard(
                    isLoading = uiState.isLoading,
                    onOpen = { viewModel.openHand() },
                    onStop = { viewModel.stopHand() },
                    compact = isLandscape
                )
            }

            item {
                SectionTitle(
                    title = "Posiciones guardadas",
                    subtitle = "Selecciona el tipo de agarre o gesto que quieras usar",
                    compact = isLandscape
                )
            }

            items(uiState.positions.chunked(2)) { rowPositions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        if (isLandscape) 10.dp else 14.dp
                    )
                ) {
                    rowPositions.forEach { position ->
                        HandPositionCard(
                            position = position,
                            description = getHandPositionDescription(position),
                            enabled = !uiState.isLoading,
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
                VoiceControlEntryCard(
                    enabled = !uiState.isLoading,
                    onClick = onVoiceControl,
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
                    color = Color(0xFF071A20).copy(alpha = 0.82f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF67E8F9),
                            strokeWidth = 3.dp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = "Cargando posiciones...",
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
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.14f),
            onClick = onBack
        ) {
            Text(
                text = "← Volver",
                modifier = Modifier.padding(
                    horizontal = if (compact) 12.dp else 16.dp,
                    vertical = if (compact) 7.dp else 10.dp
                ),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = if (compact) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                }
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
                color = Color(0xFFA5F3FC),
                fontWeight = FontWeight.Bold,
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
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = if (compact) 22.dp else 30.dp,
            topEnd = 22.dp,
            bottomEnd = if (compact) 24.dp else 30.dp,
            bottomStart = 22.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF14B8A6),
                            Color(0xFF06B6D4),
                            Color(0xFF3B82F6)
                        )
                    )
                )
                .padding(
                    horizontal = if (compact) 16.dp else 22.dp,
                    vertical = if (compact) 14.dp else 22.dp
                )
        ) {
            if (compact) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        LightPill(text = "Control manual", compact = true)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Movimiento de la mano",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Desde aquí puedes abrir la mano, detenerla y elegir posiciones de agarre o gestos ya configurados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.95f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column {
                    LightPill(text = "Control manual", compact = false)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Movimiento de la mano",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Desde aquí puedes abrir la mano, detenerla y elegir posiciones de agarre o gestos ya configurados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.95f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
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
    compact: Boolean = false
) {
    val background = if (isError) Color(0xFF4C1D24) else Color(0xFF0F3B2E)
    val titleColor = if (isError) Color(0xFFFFD5DC) else Color(0xFFB7F7D8)
    val textColor = if (isError) Color(0xFFFFE4E8) else Color(0xFFE7FFF3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 18.dp else 24.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        if (compact) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    color = titleColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "·",
                    color = titleColor.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Text(
                    text = title,
                    color = titleColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickGuideCard(
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 18.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF5)
        )
    ) {
        if (compact) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Guía rápida",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GuideStep(
                        number = "1",
                        text = "Pulsa Abrir mano para colocarla en posición inicial",
                        compact = true,
                        modifier = Modifier.weight(1f)
                    )
                    GuideStep(
                        number = "2",
                        text = "Selecciona una posición según el tipo de agarre",
                        compact = true,
                        modifier = Modifier.weight(1f)
                    )
                    GuideStep(
                        number = "3",
                        text = "Usa Parar si quieres detener cualquier movimiento",
                        compact = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "Guía rápida",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                GuideStep(
                    number = "1",
                    text = "Pulsa Abrir mano para colocarla en posición inicial",
                    compact = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                GuideStep(
                    number = "2",
                    text = "Selecciona una posición según el tipo de agarre",
                    compact = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                GuideStep(
                    number = "3",
                    text = "Usa Parar si quieres detener cualquier movimiento",
                    compact = false
                )
            }
        }
    }
}

@Composable
private fun GuideStep(
    number: String,
    text: String,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFFCCFBF1),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(
                    horizontal = if (compact) 8.dp else 10.dp,
                    vertical = if (compact) 4.dp else 6.dp
                )
        ) {
            Text(
                text = number,
                color = Color(0xFF0F766E),
                fontWeight = FontWeight.Bold,
                style = if (compact) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = Color(0xFF374151),
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            fontWeight = FontWeight.Medium,
            maxLines = if (compact) 2 else 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuickActionsCard(
    isLoading: Boolean,
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
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
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
                        text = "Estas acciones son las más útiles para empezar a mover la mano.",
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
                        enabled = !isLoading,
                        onClick = onOpen,
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF10B981),
                        compact = true
                    )
                    FriendlyActionButton(
                        text = "Parar",
                        enabled = !isLoading,
                        onClick = onStop,
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFFF97316),
                        compact = true
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Control rápido",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Estas acciones son las más útiles para empezar a mover la mano.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4B5563),
                    modifier = Modifier.padding(top = 6.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FriendlyActionButton(
                        text = "Abrir mano",
                        enabled = !isLoading,
                        onClick = onOpen,
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFF10B981),
                        compact = false
                    )
                    FriendlyActionButton(
                        text = "Parar",
                        enabled = !isLoading,
                        onClick = onStop,
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFFF97316),
                        compact = false
                    )
                }
            }
        }
    }
}

@Composable
private fun HandPositionCard(
    position: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(
            min = if (compact) 170.dp else 230.dp
        ),
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
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0EA5E9),
                            Color(0xFF2563EB),
                            Color(0xFF7C3AED)
                        )
                    )
                )
                .fillMaxSize()
                .padding(if (compact) 12.dp else 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Column {
                    Text(
                        text = "🖐️",
                        style = if (compact) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.headlineLarge
                        }
                    )

                    Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))

                    Text(
                        text = "Posición $position:",
                        color = Color.White,
                        style = if (compact) {
                            MaterialTheme.typography.bodyLarge
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
                        color = Color.White,
                        style = if (compact) {
                            MaterialTheme.typography.bodyMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        fontWeight = FontWeight.SemiBold,
                        maxLines = if (compact) 1 else 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))

                    Text(
                        text = "Sirve para colocar la mano en esta configuración guardada.",
                        color = Color.White.copy(alpha = 0.92f),
                        style = if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        maxLines = if (compact) 1 else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 42.dp else 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.10f),
                        disabledContentColor = Color.White.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = if (compact) 6.dp else 8.dp
                    )
                ) {
                    Text(
                        text = "Mover",
                        fontWeight = FontWeight.Bold,
                        style = if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceControlEntryCard(
    enabled: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF7C3AED),
                            Color(0xFF2563EB),
                            Color(0xFF06B6D4)
                        )
                    )
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
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎙️ Control por voz",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Pantalla provisional para mover la mano diciendo números.",
                            color = Color.White.copy(alpha = 0.92f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = onClick,
                        enabled = enabled,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.10f),
                            disabledContentColor = Color.White.copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = "Abrir",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column {
                    Text(
                        text = "🎙️ Control por voz",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Accede a una pantalla provisional para escuchar comandos de voz y mover la mano automáticamente.",
                        color = Color.White.copy(alpha = 0.92f),
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
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.10f),
                            disabledContentColor = Color.White.copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = "Ir al control por voz",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
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
            style = if (compact) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            color = Color.White,
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
            color = Color(0xFFC7D2FE),
            modifier = Modifier.padding(top = if (compact) 2.dp else 4.dp),
            maxLines = if (compact) 1 else 2,
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
        modifier = modifier.height(if (compact) 46.dp else 54.dp),
        shape = RoundedCornerShape(if (compact) 15.dp else 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(
            horizontal = if (compact) 10.dp else 14.dp,
            vertical = if (compact) 6.dp else 8.dp
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
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
    compact: Boolean = false
) {
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