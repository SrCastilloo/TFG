package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AccentBlue
import com.example.myapplication.ui.components.PremiumGlassCard
import com.example.myapplication.ui.components.PremiumInfoRow
import com.example.myapplication.ui.components.PremiumScreenBackground
import com.example.myapplication.ui.components.PremiumSectionTitle
import com.example.myapplication.ui.components.PremiumText
import com.example.myapplication.ui.components.PremiumTextSoft
import com.example.myapplication.ui.components.PremiumTopPill
import com.example.myapplication.ui.viewmodel.StatusViewModel

@Composable
fun StatusScreen(
    onBack: () -> Unit,
    viewModel: StatusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PremiumScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Button(onClick = onBack) {
                            Text("Volver")
                        }
                    }

                    item {
                        HeroHeader(
                            mode = uiState.mode,
                            simulation = uiState.simulation
                        )
                    }

                    item {
                        ActionMessageCard(
                            actionMessage = uiState.actionMessage,
                            error = uiState.error
                        )
                    }

                    item {
                        RefreshSection(
                            isLoading = uiState.isLoading || uiState.isActionLoading,
                            onRefresh = { viewModel.loadData() }
                        )
                    }

                    item {
                        SystemInfoCard(
                            simulation = uiState.simulation,
                            mode = uiState.mode,
                            configPath = uiState.configPath,
                            lastPositionMapped = uiState.lastPositionMapped,
                            handAvailable = uiState.handAvailable,
                            cameraAvailable = uiState.cameraAvailable
                        )
                    }

                    item {
                        ModesCard(
                            isActionLoading = uiState.isActionLoading,
                            onHandMode = { viewModel.setModeHand() },
                            onVoiceMode = { viewModel.setModeVoice() },
                            onCameraMode = { viewModel.setModeCamera() }
                        )
                    }

                    item {
                        HandControlCard(
                            isActionLoading = uiState.isActionLoading,
                            onOpen = { viewModel.openHand() },
                            onStop = { viewModel.stopHand() }
                        )
                    }

                    item {
                        Text(
                            text = "Posiciones disponibles",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(uiState.positions.chunked(2)) { rowPositions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowPositions.forEach { position ->
                                PositionCard(
                                    position = position,
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
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF8FB2FF))
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(
    mode: String,
    simulation: Boolean?
) {
    PremiumGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accent = AccentBlue
    ) {
        Column {
            PremiumTopPill(
                text = "Estado del sistema",
                accent = AccentBlue
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Panel de estado",
                style = MaterialTheme.typography.headlineSmall,
                color = PremiumText,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Modo actual: ${if (mode.isBlank()) "desconocido" else mode}",
                color = PremiumText,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Simulación: ${simulation ?: "sin datos"}",
                color = PremiumTextSoft,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ActionMessageCard(
    actionMessage: String?,
    error: String?
) {
    when {
        error != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF3A1820)
                )
            ) {
                Text(
                    text = "Error: $error",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFFFB4C1),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        actionMessage != null -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF163126)
                )
            ) {
                Text(
                    text = actionMessage,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFA7F3D0),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun RefreshSection(
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = onRefresh,
            enabled = !isLoading
        ) {
            Text("Recargar")
        }
    }
}

@Composable
private fun SystemInfoCard(
    simulation: Boolean?,
    mode: String,
    configPath: String,
    lastPositionMapped: Int?,
    handAvailable: Boolean,
    cameraAvailable: Boolean
) {
    PremiumGlassCard(
        modifier = Modifier.fillMaxWidth(),
        accent = AccentBlue
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PremiumSectionTitle(
                title = "Información general",
                subtitle = "Datos del backend y estado actual"
            )

            PremiumInfoRow("Simulación", simulation?.toString() ?: "sin datos")
            PremiumInfoRow("Modo actual", if (mode.isBlank()) "sin datos" else mode)
            PremiumInfoRow("Config", configPath)
            PremiumInfoRow("Última posición", lastPositionMapped?.toString() ?: "null")
            PremiumInfoRow("Mano disponible", handAvailable.toString())
            PremiumInfoRow("Cámara disponible", cameraAvailable.toString())
        }
    }
}

@Composable
private fun ModesCard(
    isActionLoading: Boolean,
    onHandMode: () -> Unit,
    onVoiceMode: () -> Unit,
    onCameraMode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2744)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Cambiar modo",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionButton(
                    text = "Mano",
                    enabled = !isActionLoading,
                    onClick = onHandMode,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF5B8CFF)
                )
                ActionButton(
                    text = "Voz",
                    enabled = !isActionLoading,
                    onClick = onVoiceMode,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF7B61FF)
                )
                ActionButton(
                    text = "Cámara",
                    enabled = !isActionLoading,
                    onClick = onCameraMode,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF00A6C7)
                )
            }
        }
    }
}

@Composable
private fun HandControlCard(
    isActionLoading: Boolean,
    onOpen: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2430)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Control manual",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    text = "Abrir",
                    enabled = !isActionLoading,
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF16A34A)
                )
                ActionButton(
                    text = "Parar",
                    enabled = !isActionLoading,
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFDC2626)
                )
            }
        }
    }
}

@Composable
private fun PositionCard(
    position: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A3042)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Posición $position",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Mover")
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        )
    ) {
        Text(text)
    }
}