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
import com.example.myapplication.ui.components.AccentCyan
import com.example.myapplication.ui.components.AccentGreen
import com.example.myapplication.ui.components.AccentRed
import com.example.myapplication.ui.components.PremiumActionButton
import com.example.myapplication.ui.components.PremiumGlassCard
import com.example.myapplication.ui.components.PremiumMessageCard
import com.example.myapplication.ui.components.PremiumScreenBackground
import com.example.myapplication.ui.components.PremiumText
import com.example.myapplication.ui.components.PremiumTextSoft
import com.example.myapplication.ui.components.PremiumTopPill
import com.example.myapplication.ui.viewmodel.HandViewModel

@Composable
fun HandScreen(
    onBack: () -> Unit,
    viewModel: HandViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PremiumScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
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
                        TextButton(onClick = onBack) {
                            Text("Volver")
                        }
                    }

                    item {
                        PremiumGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            accent = AccentGreen
                        ) {
                            Column {
                                PremiumTopPill(
                                    text = "Control manual",
                                    accent = AccentGreen
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Control de la mano",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = PremiumText,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Aquí puedo abrir la mano, pararla y moverla a posiciones predefinidas.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PremiumTextSoft
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.loadPositions() },
                                enabled = !uiState.isLoading
                            ) {
                                Text("Recargar")
                            }
                        }
                    }

                    if (uiState.error != null) {
                        item {
                            PremiumMessageCard(
                                text = "Error: ${uiState.error}",
                                isError = true
                            )
                        }
                    }

                    if (uiState.actionMessage != null) {
                        item {
                            PremiumMessageCard(
                                text = uiState.actionMessage ?: "",
                                isError = false
                            )
                        }
                    }

                    item {
                        PremiumGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            accent = AccentGreen
                        ) {
                            Column {
                                Text(
                                    text = "Acciones rápidas",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PremiumText,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    PremiumActionButton(
                                        text = "Abrir",
                                        onClick = { viewModel.openHand() },
                                        enabled = !uiState.isLoading,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp),
                                        color = AccentGreen
                                    )

                                    PremiumActionButton(
                                        text = "Parar",
                                        onClick = { viewModel.stopHand() },
                                        enabled = !uiState.isLoading,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(54.dp),
                                        color = AccentRed
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Posiciones",
                            style = MaterialTheme.typography.titleLarge,
                            color = PremiumText,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(uiState.positions.chunked(2)) { rowPositions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowPositions.forEach { position ->
                                PremiumGlassCard(
                                    modifier = Modifier.weight(1f),
                                    accent = AccentCyan
                                ) {
                                    Column {
                                        Text(
                                            text = "Posición $position",
                                            color = PremiumText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        PremiumActionButton(
                                            text = "Mover",
                                            onClick = { viewModel.moveToPosition(position) },
                                            enabled = !uiState.isLoading,
                                            modifier = Modifier.fillMaxWidth(),
                                            color = AccentCyan
                                        )
                                    }
                                }
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
                    CircularProgressIndicator(color = AccentCyan)
                }
            }
        }
    }
}