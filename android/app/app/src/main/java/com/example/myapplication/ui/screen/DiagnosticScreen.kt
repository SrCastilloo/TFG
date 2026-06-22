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
import androidx.compose.foundation.layout.width
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
import com.example.myapplication.ui.viewmodel.DiagnosticCheckUi
import com.example.myapplication.ui.viewmodel.DiagnosticStatus
import com.example.myapplication.ui.viewmodel.DiagnosticUiState
import com.example.myapplication.ui.viewmodel.DiagnosticViewModel

@Composable
fun DiagnosticScreen(
    onBack: () -> Unit,
    scaffoldPadding: PaddingValues = PaddingValues(),
    viewModel: DiagnosticViewModel = viewModel()
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
                        Color(0xFF071A2A),
                        Color(0xFF132E44)
                    )
                )
            )
    ) {
        val isLandscape = maxWidth > maxHeight
        val compact = maxHeight < 560.dp || maxWidth < 390.dp

        if (isLandscape) {
            DiagnosticLandscapeContent(
                uiState = uiState,
                onBack = onBack,
                onQuickDiagnostic = { viewModel.runQuickDiagnostic() },
                onFullDiagnostic = { viewModel.runFullDiagnostic() },
                compact = true
            )
        } else {
            DiagnosticPortraitContent(
                uiState = uiState,
                onBack = onBack,
                onQuickDiagnostic = { viewModel.runQuickDiagnostic() },
                onFullDiagnostic = { viewModel.runFullDiagnostic() },
                compact = compact
            )
        }
    }
}

@Composable
private fun DiagnosticPortraitContent(
    uiState: DiagnosticUiState,
    onBack: () -> Unit,
    onQuickDiagnostic: () -> Unit,
    onFullDiagnostic: () -> Unit,
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
        verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp)
    ) {
        item {
            DiagnosticTopRow(
                onBack = onBack,
                isRunning = uiState.isRunning
            )
        }

        item {
            DiagnosticHeroCard(
                baseUrl = uiState.baseUrl,
                compact = compact
            )
        }

        diagnosticControlItems(
            uiState = uiState,
            onQuickDiagnostic = onQuickDiagnostic,
            onFullDiagnostic = onFullDiagnostic,
            compact = compact
        )

        diagnosticResultItems(
            uiState = uiState,
            compact = compact
        )

        item {
            DiagnosticHelpCard(
                compact = compact
            )
        }
    }
}

@Composable
private fun DiagnosticLandscapeContent(
    uiState: DiagnosticUiState,
    onBack: () -> Unit,
    onQuickDiagnostic: () -> Unit,
    onFullDiagnostic: () -> Unit,
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
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DiagnosticTopRow(
                    onBack = onBack,
                    isRunning = uiState.isRunning
                )
            }

            item {
                DiagnosticHeroCard(
                    baseUrl = uiState.baseUrl,
                    compact = compact
                )
            }

            diagnosticControlItems(
                uiState = uiState,
                onQuickDiagnostic = onQuickDiagnostic,
                onFullDiagnostic = onFullDiagnostic,
                compact = compact
            )

            item {
                DiagnosticHelpCard(
                    compact = compact
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1.05f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Resultados",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            diagnosticResultItems(
                uiState = uiState,
                compact = compact
            )
        }
    }
}

private fun LazyListScope.diagnosticControlItems(
    uiState: DiagnosticUiState,
    onQuickDiagnostic: () -> Unit,
    onFullDiagnostic: () -> Unit,
    compact: Boolean
) {
    item {
        DiagnosticSummaryCard(
            uiState = uiState,
            compact = compact
        )
    }

    item {
        DiagnosticActionsCard(
            isRunning = uiState.isRunning,
            onQuickDiagnostic = onQuickDiagnostic,
            onFullDiagnostic = onFullDiagnostic,
            compact = compact
        )
    }

    if (uiState.message != null) {
        item {
            DiagnosticFeedbackCard(
                title = "Diagnóstico finalizado",
                text = uiState.message,
                isError = uiState.errorChecks > 0,
                compact = compact
            )
        }
    }
}

private fun LazyListScope.diagnosticResultItems(
    uiState: DiagnosticUiState,
    compact: Boolean
) {
    if (uiState.checks.isEmpty()) {
        item {
            EmptyDiagnosticCard(compact = compact)
        }
    } else {
        items(uiState.checks) { check ->
            DiagnosticCheckCard(
                check = check,
                compact = compact
            )
        }
    }
}

@Composable
private fun DiagnosticTopRow(
    onBack: () -> Unit,
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
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFF0F3B2E)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF67E8F9),
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.height(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Ejecutando",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "Cerrar",
                    color = Color(0xFFA5F3FC),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DiagnosticHeroCard(
    baseUrl: String,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF06B6D4),
                            Color(0xFF2563EB),
                            Color(0xFF7C3AED)
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
                    text = "🧪 Diagnóstico técnico",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

            Text(
                text = "Comprobación del sistema",
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
                text = "Ejecuta pruebas sobre la API, la conexión, el estado general y los módulos principales de la Raspberry.",
                color = Color.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (compact) 3 else 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.14f)
            ) {
                Text(
                    text = baseUrl,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DiagnosticSummaryCard(
    uiState: DiagnosticUiState,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F1B33)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Resumen",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryChip(
                    title = "OK",
                    value = uiState.okChecks.toString(),
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )

                SummaryChip(
                    title = "Avisos",
                    value = uiState.warningChecks.toString(),
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )

                SummaryChip(
                    title = "Errores",
                    value = uiState.errorChecks.toString(),
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = uiState.lastRunText ?: "Todavía no se ha ejecutado ningún diagnóstico.",
                color = Color(0xFFCBD5E1),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SummaryChip(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.18f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = title,
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DiagnosticActionsCard(
    isRunning: Boolean,
    onQuickDiagnostic: () -> Unit,
    onFullDiagnostic: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Pruebas disponibles",
                color = Color(0xFF111827),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "El diagnóstico rápido no mueve la mano. El completo también prueba STOP, cámara y voz.",
                color = Color(0xFF475569),
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onQuickDiagnostic,
                enabled = !isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 48.dp else 54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06B6D4),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Diagnóstico rápido",
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onFullDiagnostic,
                enabled = !isRunning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 48.dp else 54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Diagnóstico completo",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DiagnosticCheckCard(
    check: DiagnosticCheckUi,
    compact: Boolean
) {
    val statusColor = when (check.status) {
        DiagnosticStatus.OK -> Color(0xFF10B981)
        DiagnosticStatus.WARNING -> Color(0xFFF59E0B)
        DiagnosticStatus.ERROR -> Color(0xFFEF4444)
        DiagnosticStatus.RUNNING -> Color(0xFF38BDF8)
        DiagnosticStatus.WAITING -> Color(0xFF94A3B8)
    }

    val icon = when (check.status) {
        DiagnosticStatus.OK -> "✅"
        DiagnosticStatus.WARNING -> "⚠️"
        DiagnosticStatus.ERROR -> "❌"
        DiagnosticStatus.RUNNING -> "⏳"
        DiagnosticStatus.WAITING -> "•"
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleLarge
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = check.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = check.endpoint,
                        color = statusColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                check.latencyMs?.let {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = statusColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "${it} ms",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text(
                text = check.description,
                color = Color(0xFFCBD5E1),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (compact) 2 else 3,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = check.detail,
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (compact) 3 else 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DiagnosticFeedbackCard(
    title: String,
    text: String,
    isError: Boolean,
    compact: Boolean
) {
    val background = if (isError) Color(0xFF4C1D24) else Color(0xFF0F3B2E)
    val titleColor = if (isError) Color(0xFFFFD5DC) else Color(0xFFB7F7D8)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 18.dp)
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
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DiagnosticHelpCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF5)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Cómo interpretar el diagnóstico",
                color = Color(0xFF111827),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "✅ Correcto: la prueba ha respondido bien.",
                color = Color(0xFF374151)
            )

            Text(
                text = "⚠️ Aviso: el módulo responde, pero puede no haber reconocido datos útiles.",
                color = Color(0xFF374151)
            )

            Text(
                text = "❌ Error: la app no pudo conectar o el backend devolvió un fallo.",
                color = Color(0xFF374151)
            )
        }
    }
}

@Composable
private fun EmptyDiagnosticCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Text(
            text = "Ejecuta un diagnóstico para ver los resultados.",
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}