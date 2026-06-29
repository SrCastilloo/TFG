    package com.example.myapplication.ui.screen

    import androidx.compose.foundation.Canvas
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.Spacer
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.height
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.layout.size
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.rounded.ArrowBack
    import androidx.compose.material.icons.rounded.BarChart
    import androidx.compose.material.icons.rounded.CheckCircle
    import androidx.compose.material.icons.rounded.Error
    import androidx.compose.material.icons.rounded.History
    import androidx.compose.material3.Card
    import androidx.compose.material3.CardDefaults
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.LinearProgressIndicator
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.geometry.Offset
    import androidx.compose.ui.geometry.Size
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.StrokeCap
    import androidx.compose.ui.graphics.drawscope.Stroke
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import com.example.myapplication.ui.viewmodel.AnalyticsUiState
    import com.example.myapplication.ui.viewmodel.AnalyticsViewModel
    import com.example.myapplication.ui.viewmodel.DailyUsageUi
    import com.example.myapplication.ui.viewmodel.FunctionUsageUi
    import com.example.myapplication.ui.viewmodel.SourceUsageUi
    import kotlin.math.max
    import com.example.myapplication.ui.viewmodel.GripReasonUsageUi
    import com.example.myapplication.ui.viewmodel.GripSensorUsageUi
    import com.example.myapplication.ui.viewmodel.GripTargetUsageUi
    import com.example.myapplication.ui.viewmodel.GripTypeUsageUi
    import java.util.Locale
    import android.content.Intent
    import android.widget.Toast
    import androidx.compose.material.icons.rounded.FileDownload
    import androidx.compose.material3.Button
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.runtime.rememberCoroutineScope
    import androidx.compose.ui.platform.LocalContext
    import com.example.myapplication.ui.export.GripHistoryCsvExporter
    import kotlinx.coroutines.launch

    @Composable
    fun AnalyticsScreen(
        onBack: () -> Unit,
        onOpenGripHistory: () -> Unit,
        viewModel: AnalyticsViewModel = viewModel()
    ) {
        val uiState by viewModel.uiState.collectAsState()

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
                    AnalyticsHeader(onBack = onBack)
                }

                item {
                    AnalyticsSummaryCards(uiState = uiState)
                }

                item {
                    SuccessRateCard(uiState = uiState)
                }
                item {
                    GripAnalyticsSummaryCard(uiState = uiState)
                }
                item {
                    ExportGripHistoryCard(totalGrips = uiState.totalGrips)
                }

                item {
                    OpenGripHistoryCard(
                        totalGrips = uiState.totalGrips,
                        onClick = onOpenGripHistory
                    )
                }

                item {
                    GripTypesCard(types = uiState.gripTypeUsage)
                }

                item {
                    GripContactSensorsCard(sensors = uiState.topContactSensors)
                }

                item {
                    GripTargetPositionsCard(targets = uiState.targetPositionUsage)
                }

                item {
                    GripReasonsCard(reasons = uiState.gripReasonUsage)
                }

                item {
                    TopFunctionsCard(functions = uiState.topFunctions)
                }

                item {
                    SourceUsageCard(sources = uiState.sourceUsage)
                }

                item {
                    WeeklyUsageCard(days = uiState.dailyUsage)
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    @Composable
    private fun AnalyticsHeader(
        onBack: () -> Unit
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
                    text = "Analítica del sistema",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Uso real registrado localmente con Room",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Icon(
                imageVector = Icons.Rounded.BarChart,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(34.dp)
            )
        }
    }

    @Composable
    private fun AnalyticsSummaryCards(
        uiState: AnalyticsUiState
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiniStatCard(
                modifier = Modifier.weight(1f),
                title = "Acciones",
                value = uiState.totalActions.toString(),
                iconColor = Color(0xFF38BDF8)
            )

            MiniStatCard(
                modifier = Modifier.weight(1f),
                title = "Correctas",
                value = uiState.successActions.toString(),
                iconColor = Color(0xFF22C55E)
            )

            MiniStatCard(
                modifier = Modifier.weight(1f),
                title = "Fallos",
                value = uiState.failedActions.toString(),
                iconColor = Color(0xFFFB7185)
            )
        }
    }

    @Composable
    private fun MiniStatCard(
        modifier: Modifier = Modifier,
        title: String,
        value: String,
        iconColor: Color
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = iconColor.copy(alpha = 0.18f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier
                            .padding(7.dp)
                            .size(18.dp)
                    )
                }

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
    private fun SuccessRateCard(
        uiState: AnalyticsUiState
    ) {
        val percentage = (uiState.successRate * 100).toInt()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF111827)
            )
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SuccessRing(
                    progress = uiState.successRate,
                    modifier = Modifier.size(118.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Fiabilidad de acciones",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "$percentage% de acciones correctas",
                        color = Color(0xFF86EFAC),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Calculado a partir del histórico local guardado en la app.",
                        color = Color(0xFF94A3B8),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatusPill(
                            text = "${uiState.successActions} OK",
                            color = Color(0xFF22C55E),
                            iconSuccess = true
                        )

                        StatusPill(
                            text = "${uiState.failedActions} fallos",
                            color = Color(0xFFFB7185),
                            iconSuccess = false
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SuccessRing(
        progress: Float,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 18.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                val arcSize = Size(diameter, diameter)

                drawArc(
                    color = Color.White.copy(alpha = 0.10f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                drawArc(
                    color = Color(0xFF22C55E),
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }

    @Composable
    private fun StatusPill(
        text: String,
        color: Color,
        iconSuccess: Boolean
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = color.copy(alpha = 0.16f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (iconSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )

                Text(
                    text = text,
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun GripAnalyticsSummaryCard(
        uiState: AnalyticsUiState
    ) {
        val percentage = (uiState.gripSuccessRate * 100).toInt()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF111827)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFF14B8A6).copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "🦾",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Analítica de agarres",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Datos calculados desde el histórico específico de agarres.",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (uiState.totalGrips == 0) {
                    EmptyAnalyticsText()
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GripMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Agarres",
                            value = uiState.totalGrips.toString(),
                            detail = "totales"
                        )

                        GripMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Éxito",
                            value = "$percentage%",
                            detail = "${uiState.successfulGrips} correctos"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GripMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Tiempo medio",
                            value = "${formatOneDecimal(uiState.averageGripSeconds)}s",
                            detail = "por agarre"
                        )

                        GripMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Pasos medios",
                            value = formatOneDecimal(uiState.averageGripSteps),
                            detail = "hasta finalizar"
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun GripMetricCard(
        modifier: Modifier = Modifier,
        label: String,
        value: String,
        detail: String
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(22.dp),
            color = Color.White.copy(alpha = 0.08f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = label,
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = value,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Text(
                    text = detail,
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @Composable
    private fun GripTypesCard(
        types: List<GripTypeUsageUi>
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Tipos de agarre utilizados",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                if (types.isEmpty()) {
                    EmptyAnalyticsText()
                } else {
                    val maxCount = max(1, types.maxOf { it.count })

                    types.forEachIndexed { index, item ->
                        UsageBarRow(
                            label = item.label,
                            count = item.count,
                            maxCount = maxCount,
                            color = chartColors[index % chartColors.size]
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun GripContactSensorsCard(
        sensors: List<GripSensorUsageUi>
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Sensores que detectan contacto",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Ranking de sensores que han detenido el agarre seguro.",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (sensors.isEmpty()) {
                    EmptyAnalyticsText()
                } else {
                    val maxCount = max(1, sensors.maxOf { it.count })

                    sensors.forEachIndexed { index, item ->
                        UsageBarRow(
                            label = item.sensor,
                            count = item.count,
                            maxCount = maxCount,
                            color = chartColors[index % chartColors.size]
                        )
                    }
                }
            }
        }
    }
    @Composable
    private fun ExportGripHistoryCard(
        totalGrips: Int
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.18f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FileDownload,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Exportar histórico",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Genera un CSV con todos los agarres registrados.",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = "Registros disponibles: $totalGrips",
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    enabled = totalGrips > 0,
                    onClick = {
                        scope.launch {
                            try {
                                val uri = GripHistoryCsvExporter.exportGripHistory(context)

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Historial de agarres")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "CSV generado desde la app de control de la mano robótica."
                                    )
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }

                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Exportar historial de agarres"
                                    )
                                )
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "No se pudo exportar el CSV: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.10f),
                        disabledContentColor = Color(0xFF94A3B8)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = "Exportar CSV",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }

    @Composable
    private fun OpenGripHistoryCard(
        totalGrips: Int,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFF14B8A6).copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "🦾",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Histórico detallado",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = "Consulta cada agarre ejecutado con sensores, tiempo y resultado.",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = "Registros disponibles: $totalGrips",
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    enabled = totalGrips > 0,
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF14B8A6),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.10f),
                        disabledContentColor = Color(0xFF94A3B8)
                    )
                ) {
                    Text(
                        text = "Ver histórico de agarres",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
    @Composable
    private fun GripTargetPositionsCard(
        targets: List<GripTargetUsageUi>
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Posiciones objetivo más usadas",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Basado en los agarres seguros ejecutados desde la app.",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (targets.isEmpty()) {
                    EmptyAnalyticsText()
                } else {
                    val maxCount = max(1, targets.maxOf { it.count })

                    targets.forEachIndexed { index, item ->
                        UsageBarRow(
                            label = "Posición ${item.targetPositionId}",
                            count = item.count,
                            maxCount = maxCount,
                            color = chartColors[index % chartColors.size]
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun GripReasonsCard(
        reasons: List<GripReasonUsageUi>
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF111827)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Motivos de finalización",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Indica por qué terminó cada agarre: contacto, timeout o error.",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodyMedium
                )

                if (reasons.isEmpty()) {
                    EmptyAnalyticsText()
                } else {
                    val maxCount = max(1, reasons.maxOf { it.count })

                    reasons.forEachIndexed { index, item ->
                        UsageBarRow(
                            label = item.reason,
                            count = item.count,
                            maxCount = maxCount,
                            color = chartColors[index % chartColors.size]
                        )
                    }
                }
            }
        }
    }

    private fun formatOneDecimal(value: Double): String {
        return String.format(Locale.getDefault(), "%.1f", value)
    }

    @Composable
    private fun TopFunctionsCard(
        functions: List<FunctionUsageUi>
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Funciones más utilizadas",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                if (functions.isEmpty()) {
                    EmptyAnalyticsText()
                } else {
                    val maxCount = max(1, functions.maxOf { it.count })

                    functions.forEach { item ->
                        UsageBarRow(
                            label = item.label,
                            count = item.count,
                            maxCount = maxCount,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SourceUsageCard(
        sources: List<SourceUsageUi>
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Uso por módulo",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                if (sources.isEmpty()) {
                    EmptyAnalyticsText()
                } else {
                    val maxCount = max(1, sources.maxOf { it.count })

                    sources.forEachIndexed { index, item ->
                        val color = chartColors[index % chartColors.size]

                        UsageBarRow(
                            label = item.source,
                            count = item.count,
                            maxCount = maxCount,
                            color = color
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun WeeklyUsageCard(
        days: List<DailyUsageUi>
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Actividad últimos 7 días",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )

                if (days.isEmpty()) {
                    EmptyAnalyticsText()
                } else {
                    WeeklyBars(days = days)
                }
            }
        }
    }

    @Composable
    private fun WeeklyBars(
        days: List<DailyUsageUi>
    ) {
        val maxCount = max(1, days.maxOf { it.count })

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            days.forEachIndexed { index, day ->
                val heightFraction = day.count.toFloat() / maxCount.toFloat()
                val barColor = chartColors[index % chartColors.size]

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = day.count.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((110.dp * heightFraction.coerceAtLeast(0.06f)))
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        barColor,
                                        barColor.copy(alpha = 0.35f)
                                    )
                                ),
                                shape = RoundedCornerShape(999.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = day.dayLabel.take(3),
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
        }
    }

    @Composable
    private fun UsageBarRow(
        label: String,
        count: Int,
        maxCount: Int,
        color: Color
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = count.toString(),
                    color = color,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            LinearProgressIndicator(
                progress = {
                    count.toFloat() / maxCount.toFloat()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp),
                color = color,
                trackColor = Color.White.copy(alpha = 0.10f),
                strokeCap = StrokeCap.Round
            )
        }
    }

    @Composable
    private fun EmptyAnalyticsText() {
        Text(
            text = "Todavía no hay suficientes acciones registradas.",
            color = Color(0xFF94A3B8),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    private val chartColors = listOf(
        Color(0xFF38BDF8),
        Color(0xFF22C55E),
        Color(0xFFF97316),
        Color(0xFFA78BFA),
        Color(0xFFFB7185),
        Color(0xFF14B8A6),
        Color(0xFFEAB308)
    )