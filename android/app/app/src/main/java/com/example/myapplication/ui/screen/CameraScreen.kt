package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.example.myapplication.ui.components.AccentViolet
import com.example.myapplication.ui.components.PremiumActionButton
import com.example.myapplication.ui.components.PremiumGlassCard
import com.example.myapplication.ui.components.PremiumInfoRow
import com.example.myapplication.ui.components.PremiumMessageCard
import com.example.myapplication.ui.components.PremiumScreenBackground
import com.example.myapplication.ui.components.PremiumSectionTitle
import com.example.myapplication.ui.components.PremiumText
import com.example.myapplication.ui.components.PremiumTextSoft
import com.example.myapplication.ui.components.PremiumTopPill
import com.example.myapplication.ui.viewmodel.CameraViewModel

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    viewModel: CameraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PremiumScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                containerColor = Color.Transparent
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }

                    PremiumGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accent = AccentViolet
                    ) {
                        Column {
                            PremiumTopPill(
                                text = "Visión artificial",
                                accent = AccentViolet
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Cámara y detección",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PremiumText,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Aquí puedo detectar objetos y lanzar la acción automática de detectar y mover.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = PremiumTextSoft
                            )
                        }
                    }

                    if (uiState.error != null) {
                        PremiumMessageCard(
                            text = "Error: ${uiState.error}",
                            isError = true
                        )
                    }

                    if (uiState.actionMessage != null) {
                        PremiumMessageCard(
                            text = uiState.actionMessage ?: "",
                            isError = false
                        )
                    }

                    PremiumGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accent = AccentViolet
                    ) {
                        Column {
                            Text(
                                text = "Acciones",
                                style = MaterialTheme.typography.titleLarge,
                                color = PremiumText,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            PremiumActionButton(
                                text = "Detectar objeto",
                                onClick = { viewModel.detectObject() },
                                enabled = !uiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                color = AccentCyan
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            PremiumActionButton(
                                text = "Detectar y mover",
                                onClick = { viewModel.detectAndMove() },
                                enabled = !uiState.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                color = AccentViolet
                            )
                        }
                    }

                    PremiumGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accent = AccentCyan
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PremiumSectionTitle(
                                title = "Resultado",
                                subtitle = "Respuesta actual de la detección"
                            )

                            PremiumInfoRow(
                                label = "Objeto detectado",
                                value = uiState.detectedObject ?: "sin datos"
                            )

                            PremiumInfoRow(
                                label = "Calidad",
                                value = uiState.detectionQuality?.toString() ?: "sin datos"
                            )

                            PremiumInfoRow(
                                label = "Posición objetivo",
                                value = uiState.targetPosition?.toString() ?: "sin datos"
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentViolet)
                }
            }
        }
    }
}