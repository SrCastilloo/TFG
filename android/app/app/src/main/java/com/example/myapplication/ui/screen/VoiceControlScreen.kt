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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
            .imePadding()
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
        val compact = maxHeight < 560.dp || maxWidth < 390.dp

        if (isLandscape) {
            VoiceLandscapeContent(
                onBack = onBack,
                onListen = { viewModel.detectAndMoveByVoice() },
                isVoiceLoading = uiState.isVoiceLoading,
                voiceError = uiState.voiceError,
                voiceMessage = uiState.voiceMessage,
                voiceCommandName = uiState.voiceCommandName,
                voiceQuality = uiState.voiceQuality,
                voicePositionId = uiState.voicePositionId,
                compact = true
            )
        } else {
            VoicePortraitContent(
                onBack = onBack,
                onListen = { viewModel.detectAndMoveByVoice() },
                isVoiceLoading = uiState.isVoiceLoading,
                voiceError = uiState.voiceError,
                voiceMessage = uiState.voiceMessage,
                voiceCommandName = uiState.voiceCommandName,
                voiceQuality = uiState.voiceQuality,
                voicePositionId = uiState.voicePositionId,
                compact = compact
            )
        }
    }
}

@Composable
private fun VoicePortraitContent(
    onBack: () -> Unit,
    onListen: () -> Unit,
    isVoiceLoading: Boolean,
    voiceError: String?,
    voiceMessage: String?,
    voiceCommandName: String?,
    voiceQuality: Any?,
    voicePositionId: Any?,
    compact: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal
                )
            ),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(
            if (compact) 12.dp else 16.dp
        )
    ) {
        item {
            VoiceTopRow(
                onBack = onBack,
                onListen = onListen,
                isVoiceLoading = isVoiceLoading,
                compact = compact
            )
        }

        item {
            VoiceHeroCard(
                compact = compact
            )
        }

        voiceMainActionItems(
            onListen = onListen,
            isVoiceLoading = isVoiceLoading,
            voiceError = voiceError,
            voiceMessage = voiceMessage,
            voiceCommandName = voiceCommandName,
            voiceQuality = voiceQuality,
            voicePositionId = voicePositionId,
            compact = compact
        )

        voiceExtraInfoItems(
            compact = compact
        )
    }
}

@Composable
private fun VoiceLandscapeContent(
    onBack: () -> Unit,
    onListen: () -> Unit,
    isVoiceLoading: Boolean,
    voiceError: String?,
    voiceMessage: String?,
    voiceCommandName: String?,
    voiceQuality: Any?,
    voicePositionId: Any?,
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal
                )
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
            contentPadding = PaddingValues(
                bottom = 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                VoiceTopRow(
                    onBack = onBack,
                    onListen = onListen,
                    isVoiceLoading = isVoiceLoading,
                    compact = compact
                )
            }

            item {
                VoiceHeroCard(
                    compact = compact
                )
            }

            voiceMainActionItems(
                onListen = onListen,
                isVoiceLoading = isVoiceLoading,
                voiceError = voiceError,
                voiceMessage = voiceMessage,
                voiceCommandName = voiceCommandName,
                voiceQuality = voiceQuality,
                voicePositionId = voicePositionId,
                compact = compact
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                bottom = 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Guía de voz",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            voiceExtraInfoItems(
                compact = compact
            )
        }
    }
}

private fun LazyListScope.voiceMainActionItems(
    onListen: () -> Unit,
    isVoiceLoading: Boolean,
    voiceError: String?,
    voiceMessage: String?,
    voiceCommandName: String?,
    voiceQuality: Any?,
    voicePositionId: Any?,
    compact: Boolean
) {
    item {
        VoiceListenButton(
            onListen = onListen,
            isVoiceLoading = isVoiceLoading,
            compact = compact
        )
    }

    item {
        VoiceResultCard(
            isVoiceLoading = isVoiceLoading,
            voiceError = voiceError,
            voiceMessage = voiceMessage,
            voiceCommandName = voiceCommandName,
            voiceQuality = voiceQuality,
            voicePositionId = voicePositionId,
            compact = compact
        )
    }
}

private fun LazyListScope.voiceExtraInfoItems(
    compact: Boolean
) {
    item {
        VoiceCommandsCard(
            compact = compact
        )
    }

    item {
        VoiceTipsCard(
            compact = compact
        )
    }
}

@Composable
private fun VoiceTopRow(
    onBack: () -> Unit,
    onListen: () -> Unit,
    isVoiceLoading: Boolean,
    compact: Boolean
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
                    vertical = if (compact) 8.dp else 10.dp
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

        TextButton(
            onClick = onListen,
            enabled = !isVoiceLoading,
            contentPadding = PaddingValues(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 8.dp
            )
        ) {
            Text(
                text = if (isVoiceLoading) "Escuchando..." else "Probar",
                color = Color(0xFFA5F3FC),
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
}

@Composable
private fun VoiceHeroCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 30.dp),
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
                .padding(if (compact) 16.dp else 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "🎙️ Voz",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

            Text(
                text = "Control por voz",
                color = Color.White,
                style = if (compact) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pulsa el botón, di un número del uno al cinco y la Raspberry moverá la mano a la posición asociada.",
                color = Color.White.copy(alpha = 0.95f),
                style = if (compact) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                maxLines = if (compact) 3 else 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VoiceListenButton(
    onListen: () -> Unit,
    isVoiceLoading: Boolean,
    compact: Boolean
) {
    Button(
        onClick = onListen,
        enabled = !isVoiceLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 50.dp else 60.dp),
        shape = RoundedCornerShape(if (compact) 17.dp else 20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF14B8A6),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF14B8A6).copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.65f)
        )
    ) {
        if (isVoiceLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(if (compact) 20.dp else 24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Escuchando...",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = "Escuchar y mover",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VoiceResultCard(
    isVoiceLoading: Boolean,
    voiceError: String?,
    voiceMessage: String?,
    voiceCommandName: String?,
    voiceQuality: Any?,
    voicePositionId: Any?,
    compact: Boolean
) {
    val isError = voiceError != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                Color(0xFF4C1D24)
            } else {
                Color(0xFF0F3B2E)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isVoiceLoading -> "🎧"
                        isError -> "⚠️"
                        voiceMessage != null -> "✅"
                        else -> "ℹ️"
                    },
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Último resultado",
                    color = Color.White,
                    style = if (compact) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = when {
                    isVoiceLoading -> "Escuchando durante unos segundos..."
                    voiceError != null -> voiceError
                    voiceMessage != null -> voiceMessage
                    else -> "Todavía no se ha hecho ninguna detección por voz."
                },
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (compact) 3 else 5,
                overflow = TextOverflow.Ellipsis
            )

            if (voiceCommandName != null || voiceQuality != null || voicePositionId != null) {
                Spacer(modifier = Modifier.height(4.dp))
            }

            voiceCommandName?.let {
                VoiceResultLine(
                    label = "Comando detectado",
                    value = it.toString(),
                    compact = compact
                )
            }

            voiceQuality?.let {
                VoiceResultLine(
                    label = "Calidad",
                    value = it.toString(),
                    compact = compact
                )
            }

            voicePositionId?.let {
                VoiceResultLine(
                    label = "Posición enviada",
                    value = it.toString(),
                    compact = compact
                )
            }
        }
    }
}

@Composable
private fun VoiceResultLine(
    label: String,
    value: String,
    compact: Boolean
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.11f)
    ) {
        Text(
            text = "$label: $value",
            modifier = Modifier.padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 6.dp else 7.dp
            ),
            color = Color.White.copy(alpha = 0.94f),
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

@Composable
private fun VoiceCommandsCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF5))
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 8.dp)
        ) {
            Text(
                text = "Comandos disponibles",
                color = Color(0xFF111827),
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.ExtraBold
            )

            VoiceCommandRow("Uno", "posición 1", compact)
            VoiceCommandRow("Dos", "posición 2", compact)
            VoiceCommandRow("Tres", "posición 3", compact)
            VoiceCommandRow("Cuatro", "posición 4", compact)
            VoiceCommandRow("Cinco", "posición 5", compact)
            VoiceCommandRow("Ruido", "no mueve la mano", compact)
        }
    }
}

@Composable
private fun VoiceCommandRow(
    command: String,
    result: String,
    compact: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFEFF6FF)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 8.dp else 10.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = command,
                color = Color(0xFF111827),
                fontWeight = FontWeight.ExtraBold,
                style = if (compact) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = result,
                color = Color(0xFF374151),
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
}

@Composable
private fun VoiceTipsCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Consejos para que reconozca mejor",
                color = Color.White,
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.ExtraBold
            )

            VoiceTipText("Habla claro y di solo el número: uno, dos, tres, cuatro o cinco.")
            VoiceTipText("Evita hablar encima de ruido fuerte o música.")
            VoiceTipText("Espera a que aparezca “Escuchando...” antes de decir el comando.")
            VoiceTipText("Si detecta ruido, la mano no se moverá por seguridad.")
        }
    }
}

@Composable
private fun VoiceTipText(
    text: String
) {
    Text(
        text = "• $text",
        color = Color.White.copy(alpha = 0.88f),
        style = MaterialTheme.typography.bodyMedium
    )
}