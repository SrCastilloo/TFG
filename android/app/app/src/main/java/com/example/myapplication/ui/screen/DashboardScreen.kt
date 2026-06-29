package com.example.myapplication.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// Color tokens
// ─────────────────────────────────────────────────────────────────────────────

private val BgTop = Color(0xFF020617)
private val BgMid = Color(0xFF08111F)
private val BgBottom = Color(0xFF102A43)

private val TextPrimary = Color.White
private val TextMuted = Color(0xFFCBD5E1)
private val TextDim = Color(0xFF94A3B8)

private val DarkText = Color(0xFF0F172A)
private val BodyText = Color(0xFF475569)

private val AccentCyan = Color(0xFF06B6D4)
private val AccentSky = Color(0xFF38BDF8)
private val AccentBlue = Color(0xFF2563EB)
private val AccentViolet = Color(0xFF7C3AED)
private val AccentPurple = Color(0xFFA855F7)
private val AccentPink = Color(0xFFEC4899)
private val AccentCoral = Color(0xFFFF6B6B)
private val AccentOrange = Color(0xFFFF8E53)
private val AccentTeal = Color(0xFF14B8A6)
private val AccentGreen = Color(0xFF10B981)
private val AccentEmerald = Color(0xFF059669)

private val AppBackground = Brush.verticalGradient(
    listOf(
        BgTop,
        BgMid,
        BgBottom
    )
)

private val DemoGradient = Brush.linearGradient(
    listOf(
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
        Color(0xFFA855F7)
    )
)

private val HeroGradient = Brush.linearGradient(
    listOf(
        Color(0xFF111827),
        Color(0xFF1E1B4B),
        Color(0xFF0E7490)
    )
)

private val FeaturedGradient = Brush.linearGradient(
    listOf(
        AccentCoral,
        AccentOrange,
        AccentPink
    )
)

private val StatusGradient = Brush.linearGradient(
    listOf(
        AccentSky,
        AccentBlue
    )
)

private val CameraGradient = Brush.linearGradient(
    listOf(
        AccentPurple,
        AccentViolet
    )
)

private val AssistantGradient = Brush.linearGradient(
    listOf(
        AccentTeal,
        AccentCyan,
        AccentBlue
    )
)

private val VoiceGradient = Brush.linearGradient(
    listOf(
        AccentGreen,
        AccentEmerald,
        AccentBlue
    )
)

private val QuickGradient = Brush.linearGradient(
    listOf(
        Color(0xFFFFFBEB),
        Color(0xFFFDE68A)
    )
)

private val AnalyticsGradient = Brush.linearGradient(
    listOf(
        Color(0xFF0EA5E9),
        Color(0xFF6366F1),
        Color(0xFFA855F7)
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Model
// ─────────────────────────────────────────────────────────────────────────────

private data class DashboardItem(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val badge: String,
    val gradient: Brush,
    val onClick: () -> Unit
)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(
    onGoToStatus: () -> Unit,
    onGoToHand: () -> Unit,
    onGoToCamera: () -> Unit,
    onGoToAssistant: () -> Unit,
    onGoToVoice: () -> Unit,
    onGoToDemo: () -> Unit,
    onGoToAnalytics:() -> Unit
) {
    val featuredItem = DashboardItem(
        title = "Control de la mano",
        subtitle = "Abre, detén y mueve la mano a posiciones predefinidas de forma sencilla.",
        emoji = "🖐️",
        badge = "Función principal",
        gradient = FeaturedGradient,
        onClick = onGoToHand
    )

    val secondaryItems = listOf(
        DashboardItem(
            title = "Estado",
            subtitle = "Comprueba conexión, modo actual y disponibilidad.",
            emoji = "💡",
            badge = "Revisión",
            gradient = StatusGradient,
            onClick = onGoToStatus
        ),
        DashboardItem(
            title = "Estadísticas",
            subtitle = "Consulta qué funciones se usan más, tasa de éxito y actividad reciente.",
            emoji = "📊",
            badge = "Analítica",
            gradient = AnalyticsGradient,
            onClick = onGoToAnalytics
        ),
        DashboardItem(
            title = "Cámara",
            subtitle = "Detecta objetos y mueve la mano según el reconocimiento.",
            emoji = "📷",
            badge = "Automático",
            gradient = CameraGradient,
            onClick = onGoToCamera
        ),
        DashboardItem(
            title = "Control por voz",
            subtitle = "Controla la mano mediante comandos hablados.",
            emoji = "🎙️",
            badge = "Voz",
            gradient = VoiceGradient,
            onClick = onGoToVoice
        ),

        DashboardItem(
            title = "Demo del sistema",
            subtitle = "Ejecuta acciones automáticas y enseña los movimientos.",
            emoji = "🎬",
            badge = "Demo",
            gradient = DemoGradient,
            onClick = onGoToDemo
        ),
        DashboardItem(
            title = "Asistente",
            subtitle = "Pregunta dudas y recibe ayuda clara para usar la aplicación.",
            emoji = "🤖",
            badge = "Ayuda",
            gradient = AssistantGradient,
            onClick = onGoToAssistant
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val isLandscape = screenWidth > screenHeight
        val useTwoPane = isLandscape && screenWidth >= 720.dp
        val compact = screenHeight < 560.dp || screenWidth < 390.dp
        val columns = dashboardColumns(
            maxWidth = screenWidth,
            maxHeight = screenHeight
        )

        BackgroundGlow()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
        ) {
            if (useTwoPane) {
                DashboardLandscapeContent(
                    featuredItem = featuredItem,
                    secondaryItems = secondaryItems,
                    compact = compact,
                    columns = columns
                )
            } else {
                DashboardPortraitContent(
                    featuredItem = featuredItem,
                    secondaryItems = secondaryItems,
                    compact = compact,
                    columns = columns
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Responsive layouts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardPortraitContent(
    featuredItem: DashboardItem,
    secondaryItems: List<DashboardItem>,
    compact: Boolean,
    columns: Int
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = if (compact) 12.dp else 16.dp,
            end = if (compact) 12.dp else 16.dp,
            top = if (compact) 12.dp else 16.dp,
            bottom = if (compact) 34.dp else 48.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            HeroSection(compact = compact)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SystemMiniStatsRow(compact = compact)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            QuickStartSection(compact = compact)
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle(
                title = "Empieza por aquí",
                subtitle = "La forma más rápida de controlar la prótesis",
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            FeaturedCard(
                item = featuredItem,
                compact = compact
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle(
                title = "Más opciones",
                subtitle = "Consulta información, usa cámara, voz o asistente",
                compact = compact
            )
        }

        items(
            items = secondaryItems,
            key = { it.title }
        ) { item ->
            DashboardOptionCard(
                item = item,
                compact = compact
            )
        }
    }
}

@Composable
private fun DashboardLandscapeContent(
    featuredItem: DashboardItem,
    secondaryItems: List<DashboardItem>,
    compact: Boolean,
    columns: Int
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = 12.dp,
                bottom = 18.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(0.43f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeroSection(compact = true)
            }

            item {
                SystemMiniStatsRow(compact = true)
            }

            item {
                QuickStartSection(compact = true)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns.coerceAtLeast(2)),
            modifier = Modifier
                .weight(0.57f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionTitle(
                    title = "Panel de control",
                    subtitle = "Accesos principales de la aplicación",
                    compact = true
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                FeaturedCard(
                    item = featuredItem,
                    compact = true
                )
            }

            items(
                items = secondaryItems,
                key = { it.title }
            ) { item ->
                DashboardOptionCard(
                    item = item,
                    compact = true
                )
            }
        }
    }
}

private fun dashboardColumns(
    maxWidth: Dp,
    maxHeight: Dp
): Int {
    val isLandscape = maxWidth > maxHeight

    return when {
        maxWidth < 370.dp -> 1
        isLandscape && maxWidth >= 1100.dp -> 3
        isLandscape -> 2
        maxWidth >= 900.dp -> 3
        maxWidth >= 600.dp -> 2
        else -> 2
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Background
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BackgroundGlow() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-120).dp, y = (-100).dp)
                .background(
                    color = AccentCyan.copy(alpha = 0.20f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 110.dp)
                .background(
                    color = AccentViolet.copy(alpha = 0.22f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 70.dp)
                .background(
                    color = AccentGreen.copy(alpha = 0.13f),
                    shape = CircleShape
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroSection(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 26.dp else 34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(HeroGradient)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.22f),
                            AccentCyan.copy(alpha = 0.30f)
                        )
                    ),
                    shape = RoundedCornerShape(if (compact) 26.dp else 34.dp)
                )
                .padding(
                    horizontal = if (compact) 16.dp else 22.dp,
                    vertical = if (compact) 16.dp else 24.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 120.dp else 160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 46.dp, y = (-52).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = CircleShape
                    )
            )

            Column {
                LivePill(compact = compact)

                Spacer(modifier = Modifier.height(if (compact) 10.dp else 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Centro de control",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            style = if (compact) {
                                MaterialTheme.typography.headlineSmall
                            } else {
                                MaterialTheme.typography.headlineMedium
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Mano robótica 🤖",
                            color = Color(0xFFA5F3FC),
                            fontWeight = FontWeight.ExtraBold,
                            style = if (compact) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.titleLarge
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "🦾",
                        fontSize = if (compact) 42.sp else 58.sp,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))

                Text(
                    text = "Controla la mano, revisa el estado del sistema, usa la cámara, el control por voz y el asistente desde una sola pantalla.",
                    color = TextMuted,
                    style = if (compact) {
                        MaterialTheme.typography.bodySmall
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    lineHeight = if (compact) 18.sp else 21.sp,
                    maxLines = if (compact) 3 else 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LivePill(
    compact: Boolean
) {
    val transition = rememberInfiniteTransition(label = "dashboard-live")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dashboard-live-dot"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(
                color = AccentCyan.copy(alpha = 0.16f),
                shape = RoundedCornerShape(999.dp)
            )
            .border(
                width = 1.dp,
                color = AccentCyan.copy(alpha = 0.38f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(
                horizontal = if (compact) 11.dp else 14.dp,
                vertical = if (compact) 6.dp else 7.dp
            )
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 7.dp else 8.dp)
                .scale(scale)
                .background(
                    color = AccentCyan,
                    shape = CircleShape
                )
        )

        Text(
            text = "DEMO",
            color = Color(0xFFA5F3FC),
            fontSize = if (compact) 10.sp else 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            maxLines = 1
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mini stats
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SystemMiniStatsRow(
    compact: Boolean
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val useTwoRows = maxWidth < 430.dp

        if (useTwoRows) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatCard(
                        title = "Manual",
                        subtitle = "mano",
                        emoji = "🖐️",
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )

                    MiniStatCard(
                        title = "Voz",
                        subtitle = "habla",
                        emoji = "🎙️",
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MiniStatCard(
                        title = "Visión",
                        subtitle = "cámara",
                        emoji = "📷",
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )

                    MiniStatCard(
                        title = "IA",
                        subtitle = "ayuda",
                        emoji = "🤖",
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
            ) {
                MiniStatCard(
                    title = "Manual",
                    subtitle = "mano",
                    emoji = "🖐️",
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )

                MiniStatCard(
                    title = "Voz",
                    subtitle = "habla",
                    emoji = "🎙️",
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )

                MiniStatCard(
                    title = "Visión",
                    subtitle = "cámara",
                    emoji = "📷",
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )

                MiniStatCard(
                    title = "IA",
                    subtitle = "ayuda",
                    emoji = "🤖",
                    compact = compact,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    title: String,
    subtitle: String,
    emoji: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        color = Color.White.copy(alpha = 0.11f)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 10.dp else 14.dp,
                vertical = if (compact) 10.dp else 14.dp
            )
        ) {
            Text(
                text = emoji,
                fontSize = if (compact) 20.sp else 24.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = TextPrimary,
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
                text = subtitle,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick guide
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickStartSection(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(QuickGradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(if (compact) 24.dp else 30.dp)
                )
                .padding(if (compact) 14.dp else 18.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡",
                        fontSize = if (compact) 22.sp else 26.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Guía rápida",
                            color = DarkText,
                            fontWeight = FontWeight.ExtraBold,
                            style = if (compact) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.titleLarge
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Tres pasos para empezar sin perderte.",
                            color = BodyText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

                QuickStep(
                    number = "1",
                    text = "Comprueba el estado del sistema",
                    compact = compact
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuickStep(
                    number = "2",
                    text = "Entra en Mano para moverla manualmente",
                    compact = compact
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuickStep(
                    number = "3",
                    text = "Usa Voz, Cámara o Asistente cuando necesites",
                    compact = compact
                )
            }
        }
    }
}

@Composable
private fun QuickStep(
    number: String,
    text: String,
    compact: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (compact) 28.dp else 32.dp)
                .background(
                    color = Color(0xFFFFF7ED),
                    shape = CircleShape
                )
                .border(
                    width = 1.5.dp,
                    color = Color(0xFFF59E0B),
                    shape = CircleShape
                )
        ) {
            Text(
                text = number,
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFB45309)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            color = BodyText,
            fontWeight = FontWeight.SemiBold,
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section title
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    compact: Boolean
) {
    Column {
        Text(
            text = title,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            style = if (compact) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = subtitle,
            color = TextDim,
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Featured card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeaturedCard(
    item: DashboardItem,
    compact: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 210.dp else 250.dp)
            .clickable { item.onClick() },
        shape = RoundedCornerShape(if (compact) 28.dp else 34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(item.gradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(if (compact) 28.dp else 34.dp)
                )
                .padding(if (compact) 18.dp else 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 150.dp else 190.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 58.dp, y = (-58).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Badge(text = item.badge)

                    Spacer(modifier = Modifier.height(if (compact) 12.dp else 16.dp))

                    Text(
                        text = item.emoji,
                        fontSize = if (compact) 44.sp else 54.sp
                    )

                    Spacer(modifier = Modifier.height(if (compact) 8.dp else 10.dp))

                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        style = if (compact) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.headlineMedium
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.subtitle,
                        color = Color.White.copy(alpha = 0.92f),
                        style = if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        lineHeight = if (compact) 18.sp else 21.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.20f),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.34f),
                        shape = RoundedCornerShape(999.dp)
                    )
                ) {
                    Text(
                        text = "Abrir panel  →",
                        modifier = Modifier.padding(
                            horizontal = if (compact) 16.dp else 20.dp,
                            vertical = if (compact) 9.dp else 11.dp
                        ),
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        style = if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Option cards
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardOptionCard(
    item: DashboardItem,
    compact: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 172.dp else 196.dp)
            .clickable { item.onClick() },
        shape = RoundedCornerShape(if (compact) 24.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(item.gradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(if (compact) 24.dp else 28.dp)
                )
                .padding(if (compact) 14.dp else 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 88.dp else 110.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 36.dp, y = (-36).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.10f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Badge(text = item.badge)

                    Spacer(modifier = Modifier.height(if (compact) 9.dp else 12.dp))

                    Text(
                        text = item.emoji,
                        fontSize = if (compact) 30.sp else 36.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        style = if (compact) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.subtitle,
                        color = Color.White.copy(alpha = 0.88f),
                        style = if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        lineHeight = if (compact) 17.sp else 20.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "Entrar  →",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Badge(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White.copy(alpha = 0.22f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}