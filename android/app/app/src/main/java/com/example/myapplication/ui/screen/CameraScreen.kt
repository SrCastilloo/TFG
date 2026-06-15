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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.ApiConfig
import com.example.myapplication.ui.common.getHandPositionTitle
import com.example.myapplication.ui.viewmodel.CameraViewModel

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    viewModel: CameraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var lastFrameRequestKey by remember { mutableStateOf<Long?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B132B),
                        Color(0xFF13213A),
                        Color(0xFF1C2F52)
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
                    onRefresh = { },
                    refreshEnabled = false
                )
            }

            item {
                HeroCameraCard()
            }

            item {
                SectionTitle(
                    title = "Vista de la cámara",
                    subtitle = "La imagen solo se actualiza al pulsar una acción de detección"
                )
            }

            item {
                CameraPreviewCard(
                    frameRequestKey = lastFrameRequestKey
                )
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
                    title = "Acciones disponibles",
                    subtitle = "Puedes detectar un objeto o detectar y mover la mano automáticamente"
                )
            }

            item {
                CameraActionsCard(
                    isLoading = uiState.isLoading,
                    onDetect = {
                        viewModel.detectObject()
                        lastFrameRequestKey = System.currentTimeMillis()
                    },
                    onDetectAndMove = {
                        viewModel.detectAndMove()
                        lastFrameRequestKey = System.currentTimeMillis()
                    }
                )
            }

            item {
                SectionTitle(
                    title = "Resultado de la detección",
                    subtitle = "Información actual devuelta por la cámara"
                )
            }

            item {
                DetectionResultCard(
                    detectedObject = uiState.detectedObject,
                    detectionQuality = uiState.detectionQuality,
                    targetPosition = uiState.targetPosition
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    color = Color(0xFF0B132B).copy(alpha = 0.82f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFC084FC),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Procesando cámara...",
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
private fun CameraPreviewCard(
    frameRequestKey: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F1B33)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (frameRequestKey == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📷",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Aún no hay imagen cargada",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Pulsa “Detectar objeto” o “Detectar y mover” para capturar una imagen.",
                            color = Color(0xFFCBD5E1),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                AsyncImage(
                    model = "${ApiConfig.BASE_URL}camera/frame?t=$frameRequestKey",
                    contentDescription = "Vista actual de la cámara",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun TopActionRow(
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    refreshEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
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
            enabled = refreshEnabled
        ) {
            Text(
                text = "Recargar",
                color = Color(0xFFE9D5FF),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HeroCameraCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
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
                            Color(0xFF8B5CF6),
                            Color(0xFFC084FC),
                            Color(0xFF38BDF8)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                LightPill(text = "Visión artificial")

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Cámara y detección",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Desde aquí puedes detectar objetos y, si lo deseas, lanzar la acción automática para detectar y mover la mano.",
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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

            GuideStep(number = "1", text = "Pulsa Detectar objeto para ver qué reconoce la cámara")
            Spacer(modifier = Modifier.height(8.dp))
            GuideStep(number = "2", text = "Revisa el objeto detectado y la calidad de detección")
            Spacer(modifier = Modifier.height(8.dp))
            GuideStep(number = "3", text = "Usa Detectar y mover para automatizar la acción")
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
                    color = Color(0xFFF3E8FF),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = number,
                color = Color(0xFF7C3AED),
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
private fun CameraActionsCard(
    isLoading: Boolean,
    onDetect: () -> Unit,
    onDetectAndMove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
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
                text = "Acciones de cámara",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF111827),
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Primero puedes detectar un objeto y después decidir si quieres mover la mano automáticamente.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B5563),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FriendlyActionButton(
                text = "Detectar objeto",
                enabled = !isLoading,
                onClick = onDetect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                containerColor = Color(0xFF06B6D4)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FriendlyActionButton(
                text = "Detectar y mover",
                enabled = !isLoading,
                onClick = onDetectAndMove,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                containerColor = Color(0xFF8B5CF6)
            )
        }
    }
}

@Composable
private fun DetectionResultCard(
    detectedObject: String?,
    detectionQuality: Double?,
    targetPosition: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F1B33)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Resultado actual",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            ResultInfoCard(
                emoji = "📦",
                label = "Objeto detectado",
                value = friendlyObjectName(detectedObject)
            )

            ResultInfoCard(
                emoji = "🎯",
                label = "Calidad de detección",
                value = detectionQuality?.let { "${it}%" } ?: "Sin datos"
            )

            ResultInfoCard(
                emoji = "🖐️",
                label = "Posición objetivo",
                value = targetPosition?.let { getHandPositionTitle(it) } ?: "Sin posición asignada"
            )
        }
    }
}

@Composable
private fun ResultInfoCard(
    emoji: String,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = label,
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
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
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
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
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun friendlyObjectName(objectName: String?): String {
    return when (objectName?.lowercase()) {
        "person" -> "Persona"
        "cup" -> "Taza"
        "tv" -> "Televisión"
        "chair" -> "Silla"
        "scissors" -> "Tijeras"
        "bottle" -> "Botella"
        "mouse" -> "Ratón"
        "keyboard" -> "Teclado"
        null -> "Sin datos"
        else -> objectName.replaceFirstChar { it.uppercase() }
    }
}