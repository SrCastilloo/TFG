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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.myapplication.ui.viewmodel.AssistantViewModel

@Composable
fun VoiceControlScreen(
    onBack: () -> Unit,
    scaffoldPadding: PaddingValues = PaddingValues(),
    viewModel: AssistantViewModel = viewModel()
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
                        Color(0xFF111827),
                        Color(0xFF312E81)
                    )
                )
            )
    ) {
        val isLandscape = maxWidth > maxHeight

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                ),
            contentPadding = PaddingValues(
                start = if (isLandscape) 10.dp else 16.dp,
                end = if (isLandscape) 10.dp else 16.dp,
                top = if (isLandscape) 18.dp else 16.dp,
                bottom = if (isLandscape) 24.dp else 40.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 10.dp else 16.dp)
        ) {
            item {
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
                        onClick = { viewModel.detectAndMoveByVoice() },
                        enabled = !uiState.isVoiceLoading
                    ) {
                        Text(
                            text = "Probar",
                            color = Color(0xFFA5F3FC),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
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
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "🎙️ Control por voz",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Pulsa el botón, di un número del uno al cinco y la Raspberry moverá la mano a la posición asociada.",
                            color = Color.White.copy(alpha = 0.95f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.detectAndMoveByVoice() },
                    enabled = !uiState.isVoiceLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF14B8A6),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF14B8A6).copy(alpha = 0.45f),
                        disabledContentColor = Color.White.copy(alpha = 0.65f)
                    )
                ) {
                    if (uiState.isVoiceLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.height(24.dp)
                        )

                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                        Text(
                            text = "Escuchando...",
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Escuchar y mover",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.voiceError != null) {
                            Color(0xFF4C1D24)
                        } else {
                            Color(0xFF0F3B2E)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Último resultado",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = when {
                                uiState.isVoiceLoading -> "Escuchando durante unos segundos..."
                                uiState.voiceError != null -> uiState.voiceError ?: ""
                                uiState.voiceMessage != null -> uiState.voiceMessage ?: ""
                                else -> "Todavía no se ha hecho ninguna detección por voz."
                            },
                            color = Color.White.copy(alpha = 0.92f),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        uiState.voiceCommandName?.let {
                            Text(
                                text = "Comando detectado: $it",
                                color = Color.White.copy(alpha = 0.92f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        uiState.voiceQuality?.let {
                            Text(
                                text = "Calidad: $it",
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }

                        uiState.voicePositionId?.let {
                            Text(
                                text = "Posición enviada: $it",
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF5))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Comandos disponibles",
                            color = Color(0xFF111827),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text("Uno → posición 1", color = Color(0xFF374151))
                        Text("Dos → posición 2", color = Color(0xFF374151))
                        Text("Tres → posición 3", color = Color(0xFF374151))
                        Text("Cuatro → posición 4", color = Color(0xFF374151))
                        Text("Cinco → posición 5", color = Color(0xFF374151))
                        Text("Ruido → no mueve la mano", color = Color(0xFF374151))
                    }
                }
            }
        }
    }
}