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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Color tokens ────────────────────────────────────────────────────────────

private val BgDeep        = Color(0xFF080A18)
private val BgCard        = Color(0xFF12163A)
private val BgCardAlt     = Color(0xFF1A1F47)
private val AccentViolet  = Color(0xFFA855F7)
private val AccentViolet2 = Color(0xFF6366F1)
private val AccentCoral   = Color(0xFFFF6B6B)
private val AccentOrange  = Color(0xFFFF8E53)
private val AccentPink    = Color(0xFFFF6BB5)
private val AccentSky     = Color(0xFF38BDF8)
private val AccentTeal    = Color(0xFF2DD4BF)
private val AccentCyan    = Color(0xFF06B6D4)
private val TextPrimary   = Color(0xFFFFFFFF)
private val TextMuted     = Color(0xFF94A3B8)
private val TextDim       = Color(0xFF64748B)
private val YellowBg      = Color(0xFFFEFCE8)
private val YellowCircle  = Color(0xFFFEF3C7)
private val YellowBorder  = Color(0xFFF59E0B)
private val YellowText    = Color(0xFFB45309)
private val DarkText      = Color(0xFF1C1917)
private val BodyText      = Color(0xFF374151)


private val bgGradient = Brush.verticalGradient(listOf(BgDeep, BgDeep))
private val heroGradient = Brush.linearGradient(listOf(BgCard, BgCardAlt))
private val featuredGradient = Brush.linearGradient(
    listOf(AccentCoral, AccentOrange, AccentPink)
)
private val blueCardGradient = Brush.linearGradient(listOf(AccentSky, AccentViolet2))
private val purpleCardGradient = Brush.linearGradient(listOf(AccentViolet, AccentViolet2))
private val tealCardGradient = Brush.linearGradient(listOf(AccentTeal, AccentCyan))


data class DashboardItem(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val badge: String,
    val gradient: Brush,
    val shape: RoundedCornerShape,
    val onClick: () -> Unit
)


@Composable
fun DashboardScreen(
    onGoToStatus: () -> Unit,
    onGoToHand: () -> Unit,
    onGoToCamera: () -> Unit,
    onGoToAssistant: () -> Unit
) {
    val featuredItem = DashboardItem(
        title    = "Control de la mano",
        subtitle = "Abre, cierra y mueve a posiciones predefinidas de forma sencilla.",
        emoji    = "🖐️",
        badge    = "✦ Función principal",
        gradient = featuredGradient,
        shape    = RoundedCornerShape(28.dp),
        onClick  = onGoToHand
    )

    val secondaryItems = listOf(
        DashboardItem(
            title    = "Estado",
            subtitle = "Conexiones y disponibilidad del sistema.",
            emoji    = "💡",
            badge    = "Primero",
            gradient = blueCardGradient,
            shape    = RoundedCornerShape(22.dp),
            onClick  = onGoToStatus
        ),
        DashboardItem(
            title    = "Cámara",
            subtitle = "Detecta objetos y automatiza acciones.",
            emoji    = "📷",
            badge    = "Automático",
            gradient = purpleCardGradient,
            shape    = RoundedCornerShape(22.dp),
            onClick  = onGoToCamera
        ),
        DashboardItem(
            title    = "Asistente",
            subtitle = "Resuelve dudas y te orienta dentro de la app.",
            emoji    = "🤖",
            badge    = "Ayuda",
            gradient = tealCardGradient,
            shape    = RoundedCornerShape(22.dp),
            onClick  = onGoToAssistant
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        LazyColumn(
            modifier       = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = 16.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeroSection() }
            item { QuickStartSection() }
            item {
                SectionTitle(
                    title    = "Empieza por aquí",
                    subtitle = "La forma más rápida de controlar la mano"
                )
            }
            item { FeaturedCard(item = featuredItem) }
            item {
                SectionTitle(
                    title    = "Más opciones",
                    subtitle = "Información del sistema y funciones automáticas"
                )
            }
            // Top row: Estado + Cámara side-by-side
            item {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    secondaryItems.take(2).forEach { item ->
                        SmallCard(
                            item     = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            // Bottom row: Asistente full-width horizontal
            item {
                AssistantCard(item = secondaryItems[2])
            }
        }
    }
}

// ── Hero ──────────────────────────────────────────────────────────────────────

@Composable
private fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart    = 30.dp,
                    topEnd      = 30.dp,
                    bottomEnd   = 24.dp,
                    bottomStart = 24.dp
                )
            )
            .background(heroGradient)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        AccentViolet.copy(alpha = 0.45f),
                        AccentViolet2.copy(alpha = 0.20f)
                    )
                ),
                shape = RoundedCornerShape(
                    topStart    = 30.dp,
                    topEnd      = 30.dp,
                    bottomEnd   = 24.dp,
                    bottomStart = 24.dp
                )
            )
            .padding(22.dp)
    ) {
        Column {
            LivePill()
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text       = "Control de mano robótica 🤖",
                fontSize   = 30.sp,
                fontWeight = FontWeight.Black,
                color      = TextPrimary,
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text       = "Controla la mano, revisa el estado del sistema y usa la cámara desde una sola pantalla.",
                style      = MaterialTheme.typography.bodyMedium,
                color      = TextMuted,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun LivePill() {
    val transition = rememberInfiniteTransition(label = "dot")
    val scale by transition.animateFloat(
        initialValue   = 1f,
        targetValue    = 0.6f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot-scale"
    )

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(
                color = AccentViolet.copy(alpha = 0.18f),
                shape = RoundedCornerShape(999.dp)
            )
            .border(
                width = 1.dp,
                color = AccentViolet.copy(alpha = 0.35f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .scale(scale)
                .background(color = AccentViolet, shape = CircleShape)
        )
        Text(
            text       = "CENTRO DE CONTROL",
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFFC4B5FD),
            letterSpacing = 0.8.sp
        )
    }
}

// ── Quick-start ───────────────────────────────────────────────────────────────

@Composable
private fun QuickStartSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(22.dp),
        colors   = CardDefaults.cardColors(containerColor = YellowBg)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text       = "⚡ Guía rápida",
                fontSize   = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = DarkText
            )
            Spacer(modifier = Modifier.height(12.dp))
            QuickStep(number = "1", text = "Comprueba el estado del sistema")
            Spacer(modifier = Modifier.height(8.dp))
            QuickStep(number = "2", text = "Entra en Mano para moverla")
            Spacer(modifier = Modifier.height(8.dp))
            QuickStep(number = "3", text = "Usa Cámara o Asistente cuando necesites")
        }
    }
}

@Composable
private fun QuickStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp)
                .background(color = YellowCircle, shape = CircleShape)
                .border(width = 1.5.dp, color = YellowBorder, shape = CircleShape)
        ) {
            Text(
                text       = number,
                fontSize   = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = YellowText
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text       = text,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Medium,
            color      = BodyText
        )
    }
}

// ── Section title ─────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(
            text       = title,
            fontSize   = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = TextPrimary
        )
        Text(
            text     = subtitle,
            style    = MaterialTheme.typography.bodySmall,
            color    = TextDim,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

// ── Featured card ─────────────────────────────────────────────────────────────

@Composable
private fun FeaturedCard(item: DashboardItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(item.shape)
            .background(item.gradient)
            .clickable { item.onClick() }
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = Color.White.copy(alpha = 0.07f),
                    shape = CircleShape
                )
        )
        Column(modifier = Modifier.padding(24.dp)) {
            Badge(text = item.badge)
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = item.emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text       = item.title,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Black,
                color      = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text     = item.subtitle,
                style    = MaterialTheme.typography.bodyMedium,
                color    = Color.White.copy(alpha = 0.92f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            ) {
                Text(
                    text       = "Abrir panel  →",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
            }
        }
    }
}

// ── Small card (2-column grid) ────────────────────────────────────────────────

@Composable
private fun SmallCard(item: DashboardItem, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(item.shape)
            .background(item.gradient)
            .clickable { item.onClick() }
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Badge(text = item.badge)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = item.emoji, fontSize = 30.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text       = item.title,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = item.subtitle,
                    fontSize   = 12.sp,
                    color      = Color.White.copy(alpha = 0.88f),
                    lineHeight = 16.sp
                )
            }
            Text(
                text       = "Entrar  →",
                fontSize   = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = TextPrimary
            )
        }
    }
}

// ── Assistant card (full-width horizontal) ────────────────────────────────────

@Composable
private fun AssistantCard(item: DashboardItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(item.shape)
            .background(item.gradient)
            .clickable { item.onClick() }
    ) {
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Badge(text = item.badge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text       = item.title,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text       = item.subtitle,
                    fontSize   = 13.sp,
                    color      = Color.White.copy(alpha = 0.88f),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text       = "Entrar  →",
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = "🧠", fontSize = 48.sp)
        }
    }
}

// ── Badge pill ────────────────────────────────────────────────────────────────

@Composable
private fun Badge(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text       = text,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White
        )
    }
}