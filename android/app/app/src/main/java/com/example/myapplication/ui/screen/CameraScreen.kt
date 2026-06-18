package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
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
    scaffoldPadding: PaddingValues = PaddingValues(),
    viewModel: CameraViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var lastFrameRequestKey by remember { mutableStateOf<Long?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
            .consumeWindowInsets(scaffoldPadding)
            .imePadding()
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
        val isLandscape = maxWidth > maxHeight
        val compact = maxHeight < 560.dp || maxWidth < 390.dp

        if (isLandscape) {
            CameraLandscapeContent(
                onBack = onBack,
                isLoading = uiState.isLoading,
                error = uiState.error,
                actionMessage = uiState.actionMessage,
                detectedObject = uiState.detectedObject,
                detectionQuality = uiState.detectionQuality,
                targetPosition = uiState.targetPosition,
                frameRequestKey = lastFrameRequestKey,
                onDetect = {
                    viewModel.detectObject()
                    lastFrameRequestKey = System.currentTimeMillis()
                },
                onDetectAndMove = {
                    viewModel.detectAndMove()
                    lastFrameRequestKey = System.currentTimeMillis()
                },
                compact = true
            )
        } else {
            CameraPortraitContent(
                onBack = onBack,
                isLoading = uiState.isLoading,
                error = uiState.error,
                actionMessage = uiState.actionMessage,
                detectedObject = uiState.detectedObject,
                detectionQuality = uiState.detectionQuality,
                targetPosition = uiState.targetPosition,
                frameRequestKey = lastFrameRequestKey,
                onDetect = {
                    viewModel.detectObject()
                    lastFrameRequestKey = System.currentTimeMillis()
                },
                onDetectAndMove = {
                    viewModel.detectAndMove()
                    lastFrameRequestKey = System.currentTimeMillis()
                },
                compact = compact
            )
        }

        if (uiState.isLoading) {
            LoadingCameraOverlay()
        }
    }
}

@Composable
private fun CameraPortraitContent(
    onBack: () -> Unit,
    isLoading: Boolean,
    error: String?,
    actionMessage: String?,
    detectedObject: String?,
    detectionQuality: Double?,
    targetPosition: Int?,
    frameRequestKey: Long?,
    onDetect: () -> Unit,
    onDetectAndMove: () -> Unit,
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
            bottom = 110.dp
        ),
        verticalArrangement = Arrangement.spacedBy(
            if (compact) 12.dp else 16.dp
        )
    ) {
        item {
            TopActionRow(
                onBack = onBack,
                onRefresh = { },
                refreshEnabled = false,
                compact = compact
            )
        }

        item {
            HeroCameraCard(
                compact = compact
            )
        }

        cameraPreviewItems(
            frameRequestKey = frameRequestKey,
            compact = compact
        )

        cameraFeedbackItems(
            error = error,
            actionMessage = actionMessage,
            compact = compact
        )

        item {
            QuickGuideCard(
                compact = compact
            )
        }

        cameraActionItems(
            isLoading = isLoading,
            onDetect = onDetect,
            onDetectAndMove = onDetectAndMove,
            compact = compact
        )

        cameraResultItems(
            detectedObject = detectedObject,
            detectionQuality = detectionQuality,
            targetPosition = targetPosition,
            compact = compact
        )
    }
}

@Composable
private fun CameraLandscapeContent(
    onBack: () -> Unit,
    isLoading: Boolean,
    error: String?,
    actionMessage: String?,
    detectedObject: String?,
    detectionQuality: Double?,
    targetPosition: Int?,
    frameRequestKey: Long?,
    onDetect: () -> Unit,
    onDetectAndMove: () -> Unit,
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
                .weight(1.05f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                bottom = 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TopActionRow(
                    onBack = onBack,
                    onRefresh = { },
                    refreshEnabled = false,
                    compact = compact
                )
            }

            item {
                HeroCameraCard(
                    compact = compact
                )
            }

            cameraPreviewItems(
                frameRequestKey = frameRequestKey,
                compact = compact
            )

            item {
                QuickGuideCard(
                    compact = compact
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(0.95f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                bottom = 90.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            cameraFeedbackItems(
                error = error,
                actionMessage = actionMessage,
                compact = compact
            )

            cameraActionItems(
                isLoading = isLoading,
                onDetect = onDetect,
                onDetectAndMove = onDetectAndMove,
                compact = compact
            )

            cameraResultItems(
                detectedObject = detectedObject,
                detectionQuality = detectionQuality,
                targetPosition = targetPosition,
                compact = compact
            )
        }
    }
}

private fun LazyListScope.cameraPreviewItems(
    frameRequestKey: Long?,
    compact: Boolean
) {
    item {
        SectionTitle(
            title = "Vista de la cámara",
            subtitle = "La imagen solo se actualiza al pulsar una acción de detección",
            compact = compact
        )
    }

    item {
        CameraPreviewCard(
            frameRequestKey = frameRequestKey,
            compact = compact
        )
    }
}

private fun LazyListScope.cameraFeedbackItems(
    error: String?,
    actionMessage: String?,
    compact: Boolean
) {
    if (error != null) {
        item {
            FeedbackCard(
                title = "Algo ha fallado",
                text = error,
                isError = true,
                compact = compact
            )
        }
    }

    if (actionMessage != null) {
        item {
            FeedbackCard(
                title = "Última acción",
                text = actionMessage,
                isError = false,
                compact = compact
            )
        }
    }
}

private fun LazyListScope.cameraActionItems(
    isLoading: Boolean,
    onDetect: () -> Unit,
    onDetectAndMove: () -> Unit,
    compact: Boolean
) {
    item {
        SectionTitle(
            title = "Acciones disponibles",
            subtitle = "Puedes detectar un objeto o detectar y mover la mano automáticamente",
            compact = compact
        )
    }

    item {
        CameraActionsCard(
            isLoading = isLoading,
            onDetect = onDetect,
            onDetectAndMove = onDetectAndMove,
            compact = compact
        )
    }
}

private fun LazyListScope.cameraResultItems(
    detectedObject: String?,
    detectionQuality: Double?,
    targetPosition: Int?,
    compact: Boolean
) {
    item {
        SectionTitle(
            title = "Resultado de la detección",
            subtitle = "Información actual devuelta por la cámara",
            compact = compact
        )
    }

    item {
        DetectionResultCard(
            detectedObject = detectedObject,
            detectionQuality = detectionQuality,
            targetPosition = targetPosition,
            compact = compact
        )
    }
}

@Composable
private fun LoadingCameraOverlay() {
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

@Composable
private fun CameraPreviewCard(
    frameRequestKey: Long?,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            if (compact) 22.dp else 28.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F1B33)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp)
        ) {
            if (frameRequestKey == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 190.dp else 240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "📷",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Aún no hay imagen cargada",
                            color = Color.White,
                            style = if (compact) {
                                MaterialTheme.typography.titleSmall
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Pulsa “Detectar objeto” o “Detectar y mover” para capturar una imagen.",
                            color = Color(0xFFCBD5E1),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (compact) 2 else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                AsyncImage(
                    model = "${ApiConfig.BASE_URL}camera/frame?t=$frameRequestKey",
                    contentDescription = "Vista actual de la cámara",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 190.dp else 240.dp),
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
    refreshEnabled: Boolean,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
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
            onClick = onRefresh,
            enabled = refreshEnabled
        ) {
            Text(
                text = "Recargar",
                color = Color(0xFFE9D5FF),
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
private fun HeroCameraCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = if (compact) 24.dp else 30.dp,
            topEnd = if (compact) 18.dp else 22.dp,
            bottomEnd = if (compact) 24.dp else 30.dp,
            bottomStart = if (compact) 18.dp else 22.dp
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
                .padding(if (compact) 16.dp else 22.dp)
        ) {
            Column {
                LightPill(text = "Visión artificial")

                Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

                Text(
                    text = "Cámara y detección",
                    style = if (compact) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.headlineSmall
                    },
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Desde aquí puedes detectar objetos y, si lo deseas, lanzar la acción automática para detectar y mover la mano.",
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = if (compact) 3 else 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    title: String,
    text: String,
    isError: Boolean,
    compact: Boolean
) {
    val background = if (isError) Color(0xFF4C1D24) else Color(0xFF0F3B2E)
    val titleColor = if (isError) Color(0xFFFFD5DC) else Color(0xFFB7F7D8)
    val textColor = if (isError) Color(0xFFFFE4E8) else Color(0xFFE7FFF3)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            if (compact) 20.dp else 24.dp
        ),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 14.dp else 18.dp)
        ) {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = text,
                color = textColor,
                style = if (compact) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                maxLines = if (compact) 3 else 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickGuideCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            if (compact) 20.dp else 24.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF5)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 14.dp else 18.dp)
        ) {
            Text(
                text = "Guía rápida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F2937),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CameraActionsCard(
    isLoading: Boolean,
    onDetect: () -> Unit,
    onDetectAndMove: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = if (compact) 22.dp else 28.dp,
            topEnd = if (compact) 16.dp else 18.dp,
            bottomEnd = if (compact) 22.dp else 28.dp,
            bottomStart = if (compact) 16.dp else 18.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp)
        ) {
            Text(
                text = "Acciones de cámara",
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = Color(0xFF111827),
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Primero puedes detectar un objeto y después decidir si quieres mover la mano automáticamente.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4B5563),
                modifier = Modifier.padding(top = 6.dp),
                maxLines = if (compact) 2 else 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(if (compact) 14.dp else 16.dp))

            FriendlyActionButton(
                text = "Detectar objeto",
                enabled = !isLoading,
                onClick = onDetect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 48.dp else 54.dp),
                containerColor = Color(0xFF06B6D4)
            )

            Spacer(modifier = Modifier.height(12.dp))

            FriendlyActionButton(
                text = "Detectar y mover",
                enabled = !isLoading,
                onClick = onDetectAndMove,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 48.dp else 54.dp),
                containerColor = Color(0xFF8B5CF6)
            )
        }
    }
}

@Composable
private fun DetectionResultCard(
    detectedObject: String?,
    detectionQuality: Double?,
    targetPosition: Int?,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            if (compact) 22.dp else 28.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F1B33)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
        ) {
            Text(
                text = "Resultado actual",
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            ResultInfoCard(
                emoji = "📦",
                label = "Objeto detectado",
                value = friendlyObjectName(detectedObject),
                compact = compact
            )

            ResultInfoCard(
                emoji = "🎯",
                label = "Calidad de detección",
                value = detectionQuality?.let { "${it}%" } ?: "Sin datos",
                compact = compact
            )

            ResultInfoCard(
                emoji = "🖐️",
                label = "Posición objetivo",
                value = targetPosition?.let { getHandPositionTitle(it) } ?: "Sin posición asignada",
                compact = compact
            )
        }
    }
}

@Composable
private fun ResultInfoCard(
    emoji: String,
    label: String,
    value: String,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            if (compact) 18.dp else 22.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
        )
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = if (compact) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.headlineMedium
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    color = Color.White,
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    compact: Boolean
) {
    Column {
        Text(
            text = title,
            style = if (compact) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(top = 4.dp),
            maxLines = if (compact) 2 else 3,
            overflow = TextOverflow.Ellipsis
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