package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.viewmodel.GripHistoryUiItem
import com.example.myapplication.ui.viewmodel.GripHistoryUiState
import com.example.myapplication.ui.viewmodel.GripHistoryViewModel

@Composable
fun GripHistoryScreen(
    onBack: () -> Unit,
    viewModel: GripHistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020617),
                        Color(0xFF0F172A),
                        Color(0xFF111827)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GripHistoryHeader(
                    onBack = onBack,
                    onClear = { showClearDialog = true },
                    canClear = uiState.total > 0
                )
            }

            item {
                GripHistorySummaryCard(uiState = uiState)
            }

            if (uiState.items.isEmpty()) {
                item {
                    EmptyGripHistoryCard()
                }
            } else {
                items(
                    items = uiState.items,
                    key = { it.id }
                ) { item ->
                    GripHistoryCard(item = item)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = {
                Text(text = "Borrar histórico de agarres")
            },
            text = {
                Text(
                    text = "Se eliminarán todos los agarres guardados localmente. Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text(text = "Borrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDialog = false }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun GripHistoryHeader(
    onBack: () -> Unit,
    onClear: () -> Unit,
    canClear: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Histórico de agarres",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Detalle de cada agarre registrado en Room",
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            enabled = canClear,
            onClick = onClear,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7F1D1D),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.08f),
                disabledContentColor = Color(0xFF94A3B8)
            )
        ) {
            Text(
                text = "Borrar",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GripHistorySummaryCard(
    uiState: GripHistoryUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryBox(
                modifier = Modifier.weight(1f),
                title = "Total",
                value = uiState.total.toString(),
                color = Color(0xFF38BDF8)
            )

            SummaryBox(
                modifier = Modifier.weight(1f),
                title = "Correctos",
                value = uiState.successful.toString(),
                color = Color(0xFF22C55E)
            )

            SummaryBox(
                modifier = Modifier.weight(1f),
                title = "Fallidos",
                value = uiState.failed.toString(),
                color = Color(0xFFFB7185)
            )
        }
    }
}

@Composable
private fun SummaryBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = color.copy(alpha = 0.13f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = title,
                color = Color(0xFFCBD5E1),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyGripHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "🦾",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Todavía no hay agarres registrados",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Ejecuta un agarre seguro o completo desde la pantalla de sensores capacitivos para verlo aquí.",
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun GripHistoryCard(
    item: GripHistoryUiItem
) {
    val statusColor = if (item.success) {
        Color(0xFF22C55E)
    } else {
        Color(0xFFFB7185)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 230.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.10f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = item.dateText,
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = statusColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "${if (item.success) "✅" else "❌"} ${item.resultText}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Motivo: ${item.reasonText}",
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (!item.message.isNullOrBlank()) {
                    Text(
                        text = item.message,
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            GripDetailsGrid(item = item)
        }
    }
}

@Composable
private fun GripDetailsGrid(
    item: GripHistoryUiItem
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailChip(
                modifier = Modifier.weight(1f),
                label = "Tiempo",
                value = item.elapsedText
            )

            DetailChip(
                modifier = Modifier.weight(1f),
                label = "Pasos",
                value = item.stepsText
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailChip(
                modifier = Modifier.weight(1f),
                label = "Close step",
                value = item.closeStepText
            )

            DetailChip(
                modifier = Modifier.weight(1f),
                label = "Objetivo",
                value = item.targetPositionText
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailChip(
                modifier = Modifier.weight(1f),
                label = "Sensor contacto",
                value = item.contactSensorText
            )

            DetailChip(
                modifier = Modifier.weight(1f),
                label = "Contactos",
                value = item.contactsText
            )
        }

        DetailChip(
            modifier = Modifier.fillMaxWidth(),
            label = "Sensores ignorados",
            value = item.ignoredSensorsText
        )

        DetailChip(
            modifier = Modifier.fillMaxWidth(),
            label = "Sensores faltantes",
            value = item.missingSensorsText
        )
    }
}

@Composable
private fun DetailChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.07f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}