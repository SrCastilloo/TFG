package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.AccentViolet
import com.example.myapplication.ui.components.PremiumGlassCard
import com.example.myapplication.ui.components.PremiumText
import com.example.myapplication.ui.components.PremiumTextSoft
import com.example.myapplication.ui.components.PremiumTopPill

data class DashboardItem(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val colors: List<Color>,
    val onClick: () -> Unit
)

@Composable
fun DashboardScreen(
    onGoToStatus: () -> Unit,
    onGoToHand: () -> Unit,
    onGoToCamera: () -> Unit
) {
    val items = listOf(
        DashboardItem(
            title = "Estado",
            subtitle = "Información general del sistema",
            emoji = "📊",
            colors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1)),
            onClick = onGoToStatus
        ),
        DashboardItem(
            title = "Mano",
            subtitle = "Control de movimientos y posiciones",
            emoji = "🖐️",
            colors = listOf(Color(0xFF10B981), Color(0xFF059669)),
            onClick = onGoToHand
        ),
        DashboardItem(
            title = "Cámara",
            subtitle = "Detección de objetos",
            emoji = "📷",
            colors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED)),
            onClick = onGoToCamera
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B1020),
                        Color(0xFF111827),
                        Color(0xFF1F2937)
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                HeroSection()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(items) { item ->
                        DashboardCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSection() {
    PremiumGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 20.dp),
        accent = AccentViolet
    ) {
        Column {
            PremiumTopPill(
                text = "TFG · Control App",
                accent = AccentViolet
            )

            Text(
                text = "Robotic Hand",
                style = MaterialTheme.typography.headlineLarge,
                color = PremiumText,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Interfaz principal para controlar la mano, la cámara y consultar el estado del sistema.",
                style = MaterialTheme.typography.bodyLarge,
                color = PremiumTextSoft,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun DashboardCard(item: DashboardItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(item.colors))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = item.emoji,
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}