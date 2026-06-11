package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.common.getHandPositionDescription
import com.example.myapplication.ui.common.getHandPositionTitle
import com.example.myapplication.ui.viewmodel.HandViewModel

@Composable
fun HandScreen(
    onBack: () -> Unit,
    viewModel: HandViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TopActionRow(
                    onBack = onBack,
                    onRefresh = { viewModel.loadPositions() },
                    isLoading = uiState.isLoading
                )
            }

            item {
                HeroHandCard()
            }

            if (uiState.error != null) {
                item {
                    FeedbackCard(
                        title = "Algo ha fallado",
                        text = uiState.error ?: "",
                        isError = true
                    )
                }
            }

            if (uiState.actionMessage != null) {
                item {
                    FeedbackCard(
                        title = "Última acción",
                        text = uiState.actionMessage ?: "",
                        isError = false
                    )
                }
            }

            item {
                QuickGuideCard()
            }

            item {
                SectionTitle(
                    title = "Acciones rápidas",
                    subtitle = "Controles básicos para usar la mano"
                )
            }

            item {
                QuickActionsCard(
                    isLoading = uiState.isLoading,
                    onOpen = { viewModel.openHand() },
                    onStop = { viewModel.stopHand() }
                )
            }

            item {
                SectionTitle(
                    title = "Posiciones guardadas",
                    subtitle = "Selecciona el tipo de agarre o gesto que quieras usar"
                )
            }

            items(uiState.positions.chunked(2)) { rowPositions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    rowPositions.forEach { position ->
                        HandPositionCard(
                            position = position,
                            description = getHandPositionDescription(position),
                            enabled = !uiState.isLoading,
                            onClick = { viewModel.moveToPosition(position) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (rowPositions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
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
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
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
            onClick = onRefresh,
            enabled = !isLoading
        ) {
            Text(
                text = "Recargar",
                color = Color(0xFFA5F3FC),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HeroHandCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 30.dp,
            topEnd = 22.dp,
            bottomEnd = 30.dp,
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
                .padding(22.dp)
        ) {
            Column {
                LightPill(text = "Control manual")

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Movimiento de la mano",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Desde aquí puedes abrir la mano, detenerla y elegir posiciones de agarre o gestos ya configurados.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    title: String,
    text: String,
    isError: Boolean
) {
    val background = if (isError) Color(0xFF4C1D24) else Color(0xFF0F3B2E)
    val titleColor = if (isError) Color(0xFFFFD5DC) else Color(0xFFB7F7D8)
    val textColor = if (isError) Color(0xFFFFE4E8) else Color(0xFFE7FFF3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
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
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun QuickGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF5)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Guía rápida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(10.dp))

            GuideStep(number = "1", text = "Pulsa Abrir mano para colocarla en posición inicial")
            Spacer(modifier = Modifier.height(8.dp))
            GuideStep(number = "2", text = "Selecciona una posición según el tipo de agarre")
            Spacer(modifier = Modifier.height(8.dp))
            GuideStep(number = "3", text = "Usa Parar si quieres detener cualquier movimiento")
        }
    }
}

@Composable
private fun GuideStep(
    number: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFFCCFBF1),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = number,
                color = Color(0xFF0F766E),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = Color(0xFF374151),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickActionsCard(
    isLoading: Boolean,
    onOpen: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 18.dp,
            bottomEnd = 28.dp,
            bottomStart = 18.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Control rápido",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF111827),
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Estas acciones son las más útiles para empezar a mover la mano.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B5563),
                modifier = Modifier.padding(top = 6.dp)
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
                    containerColor = Color(0xFF10B981)
                )

                FriendlyActionButton(
                    text = "Parar",
                    enabled = !isLoading,
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFFF97316)
                )
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 230.dp),
        shape = RoundedCornerShape(
            topStart = 26.dp,
            topEnd = 18.dp,
            bottomEnd = 26.dp,
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
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Column {
                    Text(
                        text = "🖐️",
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Posición $position:",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sirve para colocar la mano en esta configuración guardada.",
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.16f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.10f),
                        disabledContentColor = Color.White.copy(alpha = 0.55f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Mover",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFC7D2FE),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FriendlyActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LightPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}