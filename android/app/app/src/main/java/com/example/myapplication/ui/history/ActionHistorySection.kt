package com.example.myapplication.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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

@Composable
fun ActionHistorySection(
    compact: Boolean = false
) {
    val historyItems by ActionHistoryStore.items.collectAsStateWithLifecycle()
    val visibleItems = historyItems.take(if (compact) 4 else 7)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 20.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF111827),
                            Color(0xFF1E1B4B),
                            Color(0xFF164E63)
                        )
                    )
                )
                .padding(
                    horizontal = if (compact) 14.dp else 18.dp,
                    vertical = if (compact) 14.dp else 18.dp
                ),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Historial reciente",
                        color = Color.White,
                        style = if (compact) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Últimas acciones realizadas en la app",
                        color = Color(0xFFCBD5E1),
                        style = if (compact) {
                            MaterialTheme.typography.bodySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (historyItems.isNotEmpty()) {
                    TextButton(
                        onClick = { ActionHistoryStore.clear() }
                    ) {
                        Text(
                            text = "Limpiar",
                            color = Color(0xFFA5F3FC),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))

            if (visibleItems.isEmpty()) {
                Text(
                    text = "Todavía no hay acciones registradas.",
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                visibleItems.forEach { item ->
                    ActionHistoryRow(
                        item = item,
                        compact = compact
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionHistoryRow(
    item: ActionHistoryItem,
    compact: Boolean
) {
    val indicatorColor = if (item.success) Color(0xFF22C55E) else Color(0xFFEF4444)
    val indicatorText = if (item.success) "✅" else "⚠️"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 14.dp else 18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 9.dp else 11.dp
            ),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = indicatorText,
                style = MaterialTheme.typography.bodyLarge,
                color = indicatorColor
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = if (compact) {
                            MaterialTheme.typography.bodyMedium
                        } else {
                            MaterialTheme.typography.titleSmall
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = item.formattedTime,
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = item.source,
                    color = Color(0xFFA5F3FC),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.detail,
                    color = Color.White.copy(alpha = 0.88f),
                    style = if (compact) {
                        MaterialTheme.typography.bodySmall
                    } else {
                        MaterialTheme.typography.bodyMedium
                    },
                    maxLines = if (compact) 2 else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}