package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val PremiumBgTop = Color(0xFF08101E)
val PremiumBgMid = Color(0xFF0F172A)
val PremiumBgBottom = Color(0xFF1E293B)

val PremiumCard = Color(0xCC111827)
val PremiumBorder = Color(0x33FFFFFF)

val PremiumText = Color(0xFFF8FAFC)
val PremiumTextSoft = Color(0xFFCBD5E1)

val AccentBlue = Color(0xFF60A5FA)
val AccentGreen = Color(0xFF34D399)
val AccentViolet = Color(0xFFA78BFA)
val AccentCyan = Color(0xFF22D3EE)
val AccentRed = Color(0xFFFB7185)

@Composable
fun PremiumScreenBackground(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        PremiumBgTop,
                        PremiumBgMid,
                        PremiumBgBottom
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    accent: Color = AccentBlue,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCard),
        border = BorderStroke(1.dp, PremiumBorder)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun PremiumTopPill(
    text: String,
    accent: Color = AccentBlue
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.28f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = PremiumText,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun PremiumSectionTitle(
    title: String,
    subtitle: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = PremiumText,
            fontWeight = FontWeight.ExtraBold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = PremiumTextSoft
            )
        }
    }
}

@Composable
fun PremiumActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = AccentBlue
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PremiumInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = PremiumTextSoft,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            color = PremiumText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PremiumMessageCard(
    text: String,
    isError: Boolean = false
) {
    val accent = if (isError) AccentRed else AccentGreen

    PremiumGlassCard(accent = accent, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            color = PremiumText,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}