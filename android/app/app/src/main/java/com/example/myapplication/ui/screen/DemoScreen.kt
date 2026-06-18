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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import com.example.myapplication.ui.viewmodel.DemoStepUi
import com.example.myapplication.ui.viewmodel.DemoUiState
import com.example.myapplication.ui.viewmodel.DemoViewModel

@Composable
fun DemoScreen(
    onBack: () -> Unit,
    scaffoldPadding: PaddingValues = PaddingValues(),
    viewModel: DemoViewModel = viewModel()
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
                        Color(0xFF020617),
                        Color(0xFF111827),
                        Color(0xFF312E81)
                    )
                )
            )
    ) {
        val isLandscape = maxWidth > maxHeight
        val compact = maxHeight < 560.dp || maxWidth < 390.dp

        if (isLandscape) {
            DemoLandscapeContent(
                uiState = uiState,
                viewModel = viewModel,
                onBack = onBack,
                compact = true
            )
        } else {
            DemoPortraitContent(
                uiState = uiState,
                viewModel = viewModel,
                onBack = onBack,
                compact = compact
            )
        }
    }
}

@Composable
private fun DemoPortraitContent(
    uiState: DemoUiState,
    viewModel: DemoViewModel,
    onBack: () -> Unit,
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
            DemoTopRow(
                onBack = onBack,
                onCancel = { viewModel.cancelDemo() },
                isRunning = uiState.isRunning
            )
        }

        item {
            DemoHeroCard(
                compact = compact
            )
        }

        demoStatusItems(
            uiState = uiState,
            compact = compact
        )

        demoActionItems(
            isRunning = uiState.isRunning,
            compact = compact,
            viewModel = viewModel
        )

        demoStepItems(
            steps = uiState.steps,
            compact = compact
        )
    }
}

@Composable
private fun DemoLandscapeContent(
    uiState: DemoUiState,
    viewModel: DemoViewModel,
    onBack: () -> Unit,
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
                DemoTopRow(
                    onBack = onBack,
                    onCancel = { viewModel.cancelDemo() },
                    isRunning = uiState.isRunning
                )
            }

            item {
                DemoHeroCard(
                    compact = compact
                )
            }

            demoStatusItems(
                uiState = uiState,
                compact = compact
            )

            item {
                DemoInfoCard(
                    compact = compact
                )
            }
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
                    text = "Demos disponibles",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            demoActionItems(
                isRunning = uiState.isRunning,
                compact = compact,
                viewModel = viewModel
            )

            demoStepItems(
                steps = uiState.steps,
                compact = compact
            )
        }
    }
}

private fun LazyListScope.demoStatusItems(
    uiState: DemoUiState,
    compact: Boolean
) {
    if (uiState.isRunning || uiState.currentStepText != null) {
        item {
            DemoProgressCard(
                title = uiState.activeDemoTitle ?: "Demo en ejecución",
                currentStep = uiState.currentStep,
                totalSteps = uiState.totalSteps,
                currentStepText = uiState.currentStepText ?: "Ejecutando...",
                compact = compact
            )
        }
    }

    if (uiState.message != null) {
        item {
            DemoFeedbackCard(
                title = "Demo finalizada",
                text = uiState.message,
                isError = false
            )
        }
    }

    if (uiState.error != null) {
        item {
            DemoFeedbackCard(
                title = "Demo interrumpida",
                text = uiState.error,
                isError = true
            )
        }
    }
}

private fun LazyListScope.demoActionItems(
    isRunning: Boolean,
    compact: Boolean,
    viewModel: DemoViewModel
) {
    item {
        DemoActionCard(
            emoji = "🖐️",
            title = "Demo de posiciones",
            subtitle = "Abre la mano y recorre varias posiciones predefinidas.",
            buttonText = "Iniciar posiciones",
            enabled = !isRunning,
            compact = compact,
            gradient = Brush.linearGradient(
                listOf(
                    Color(0xFFFF6B6B),
                    Color(0xFFFF8E53),
                    Color(0xFFEC4899)
                )
            ),
            onClick = { viewModel.startPositionsDemo() }
        )
    }

    item {
        DemoActionCard(
            emoji = "📷",
            title = "Demo por cámara",
            subtitle = "Detecta un objeto y mueve la mano según el resultado.",
            buttonText = "Iniciar cámara",
            enabled = !isRunning,
            compact = compact,
            gradient = Brush.linearGradient(
                listOf(
                    Color(0xFF8B5CF6),
                    Color(0xFF7C3AED),
                    Color(0xFF2563EB)
                )
            ),
            onClick = { viewModel.startCameraDemo() }
        )
    }

    item {
        DemoActionCard(
            emoji = "🎙️",
            title = "Demo por voz",
            subtitle = "Escucha un comando del uno al cinco y mueve la mano.",
            buttonText = "Iniciar voz",
            enabled = !isRunning,
            compact = compact,
            gradient = Brush.linearGradient(
                listOf(
                    Color(0xFF10B981),
                    Color(0xFF14B8A6),
                    Color(0xFF06B6D4)
                )
            ),
            onClick = { viewModel.startVoiceDemo() }
        )
    }

    item {
        DemoActionCard(
            emoji = "🎬",
            title = "Demo completa",
            subtitle = "Combina apertura, posiciones, cámara, voz y parada final.",
            buttonText = "Iniciar completa",
            enabled = !isRunning,
            compact = compact,
            gradient = Brush.linearGradient(
                listOf(
                    Color(0xFFF59E0B),
                    Color(0xFFEF4444),
                    Color(0xFFA855F7)
                )
            ),
            onClick = { viewModel.startFullDemo() }
        )
    }
}

private fun LazyListScope.demoStepItems(
    steps: List<DemoStepUi>,
    compact: Boolean
) {
    if (steps.isNotEmpty()) {
        item {
            Text(
                text = "Pasos ejecutados",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(steps) { step ->
            DemoStepCard(
                step = step,
                compact = compact
            )
        }
    }
}

@Composable
private fun DemoTopRow(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    isRunning: Boolean
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (isRunning) {
            TextButton(
                onClick = onCancel
            ) {
                Text(
                    text = "Cancelar",
                    color = Color(0xFFFCA5A5),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DemoHeroCard(
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
                            Color(0xFF111827),
                            Color(0xFF1E1B4B),
                            Color(0xFF0E7490)
                        )
                    )
                )
                .padding(if (compact) 16.dp else 22.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "🎬 Modo demo",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

            Text(
                text = "Demo del sistema",
                color = Color.White,
                style = if (compact) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ejecuta secuencias automáticas para enseñar el funcionamiento de la mano, la cámara y el control por voz.",
                color = Color.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (compact) 3 else 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DemoProgressCard(
    title: String,
    currentStep: Int,
    totalSteps: Int,
    currentStepText: String,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F3B2E)
        )
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 14.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF67E8F9),
                strokeWidth = 3.dp,
                modifier = Modifier.height(if (compact) 34.dp else 42.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Paso $currentStep de $totalSteps",
                    color = Color(0xFFB7F7D8),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = currentStepText,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = if (compact) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DemoActionCard(
    emoji: String,
    title: String,
    subtitle: String,
    buttonText: String,
    enabled: Boolean,
    compact: Boolean,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(if (compact) 14.dp else 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = emoji,
                    style = if (compact) {
                        MaterialTheme.typography.headlineSmall
                    } else {
                        MaterialTheme.typography.headlineLarge
                    }
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
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

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (compact) 2 else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 12.dp else 14.dp))

            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 44.dp else 54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.20f),
                    contentColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.09f),
                    disabledContentColor = Color.White.copy(alpha = 0.55f)
                )
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DemoFeedbackCard(
    title: String,
    text: String,
    isError: Boolean
) {
    val background = if (isError) Color(0xFF4C1D24) else Color(0xFF0F3B2E)
    val titleColor = if (isError) Color(0xFFFFD5DC) else Color(0xFFB7F7D8)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = text,
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DemoStepCard(
    step: DemoStepUi,
    compact: Boolean
) {
    val indicator = when (step.success) {
        true -> "✅"
        false -> "⚠️"
        null -> "•"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 16.dp else 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 12.dp else 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = indicator,
                style = MaterialTheme.typography.bodyLarge
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = step.title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = step.detail,
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (compact) 2 else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DemoInfoCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 14.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Consejo de uso",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "En la demo por voz, pulsa iniciar y di un número del uno al cinco cuando el sistema esté escuchando.",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Puedes cancelar la demo desde esta pantalla o usar la parada de emergencia global.",
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}