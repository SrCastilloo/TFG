package com.example.myapplication.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

private enum class HistoryResultFilter(
    val label: String
) {
    ALL("Todas"),
    SUCCESS("Correctas"),
    ERROR("Errores")
}

private data class HistoryTypeFilter(
    val id: String,
    val label: String
)

private val historyTypeFilters = listOf(
    HistoryTypeFilter("all", "Todo"),
    HistoryTypeFilter("hand", "Mano"),
    HistoryTypeFilter("voice", "Voz"),
    HistoryTypeFilter("camera", "Cámara"),
    HistoryTypeFilter("assistant", "Asistente"),
    HistoryTypeFilter("system", "Sistema"),
    HistoryTypeFilter("sensors", "Sensores"),
    HistoryTypeFilter("grip", "Agarres"),
    HistoryTypeFilter("other", "Otros")
)

@Composable
fun ActionHistorySection(
    compact: Boolean = false
) {
    val historyItems by ActionHistoryStore.items.collectAsStateWithLifecycle()

    var selectedResultFilter by remember {
        mutableStateOf(HistoryResultFilter.ALL)
    }

    var selectedTypeFilter by remember {
        mutableStateOf("all")
    }

    val filteredItems = historyItems.filter { item ->
        item.matchesResultFilter(selectedResultFilter) &&
                item.matchesTypeFilter(selectedTypeFilter)
    }

    val visibleItems = filteredItems.take(
        if (compact) 4 else 12
    )

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
            ActionHistoryHeader(
                totalItems = historyItems.size,
                filteredItems = filteredItems.size,
                compact = compact
            )

            if (historyItems.isNotEmpty()) {
                ActionHistoryFilters(
                    selectedResultFilter = selectedResultFilter,
                    onResultFilterSelected = { selectedResultFilter = it },
                    selectedTypeFilter = selectedTypeFilter,
                    onTypeFilterSelected = { selectedTypeFilter = it },
                    compact = compact
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))

            when {
                historyItems.isEmpty() -> {
                    Text(
                        text = "Todavía no hay acciones registradas.",
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                visibleItems.isEmpty() -> {
                    Text(
                        text = "No hay acciones que coincidan con los filtros seleccionados.",
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
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
}

@Composable
private fun ActionHistoryHeader(
    totalItems: Int,
    filteredItems: Int,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
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
                text = if (totalItems == 0) {
                    "Últimas acciones realizadas en la app"
                } else {
                    "$filteredItems de $totalItems acciones mostradas"
                },
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

        if (totalItems > 0) {
            TextButton(
                onClick = { ActionHistoryStore.clear() }
            ) {
                Text(
                    text = if (compact) "Limpiar" else "Limpiar todo",
                    color = Color(0xFFA5F3FC),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ActionHistoryFilters(
    selectedResultFilter: HistoryResultFilter,
    onResultFilterSelected: (HistoryResultFilter) -> Unit,
    selectedTypeFilter: String,
    onTypeFilterSelected: (String) -> Unit,
    compact: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoryResultFilter.entries.forEach { filter ->
                HistoryFilterChip(
                    label = filter.label,
                    selected = selectedResultFilter == filter,
                    compact = compact,
                    onClick = { onResultFilterSelected(filter) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            historyTypeFilters.forEach { filter ->
                HistoryFilterChip(
                    label = filter.label,
                    selected = selectedTypeFilter == filter.id,
                    compact = compact,
                    onClick = { onTypeFilterSelected(filter.id) }
                )
            }
        }
    }
}

@Composable
private fun HistoryFilterChip(
    label: String,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        Color.White.copy(alpha = 0.22f)
    } else {
        Color.White.copy(alpha = 0.08f)
    }

    val textColor = if (selected) {
        Color.White
    } else {
        Color(0xFFCBD5E1)
    }

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            modifier = Modifier.padding(
                horizontal = if (compact) 10.dp else 12.dp,
                vertical = if (compact) 6.dp else 7.dp
            )
        )
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

private fun ActionHistoryItem.matchesResultFilter(
    filter: HistoryResultFilter
): Boolean {
    return when (filter) {
        HistoryResultFilter.ALL -> true
        HistoryResultFilter.SUCCESS -> success
        HistoryResultFilter.ERROR -> !success
    }
}

private fun ActionHistoryItem.matchesTypeFilter(
    filter: String
): Boolean {
    if (filter == "all") return true

    val normalizedText = "$source $title $detail $actionType".lowercase(Locale.getDefault())

    return when (filter) {
        "assistant" ->
            actionType == "assistant" ||
                    "asistente" in normalizedText ||
                    "assistant" in normalizedText ||
                    "ia" in normalizedText

        "hand" ->
            actionType == "hand" ||
                    "mano" in normalizedText ||
                    "posición" in normalizedText ||
                    "posicion" in normalizedText

        "voice" ->
            actionType == "voice" ||
                    "voz" in normalizedText ||
                    "comando" in normalizedText ||
                    "voice" in normalizedText

        "camera" ->
            actionType == "camera" ||
                    "cámara" in normalizedText ||
                    "camara" in normalizedText ||
                    "camera" in normalizedText ||
                    "objeto" in normalizedText

        "system" ->
            actionType == "system" ||
                    "sistema" in normalizedText ||
                    "estado" in normalizedText ||
                    "modo" in normalizedText

        "sensors" ->
            actionType == "sensors" ||
                    "sensor" in normalizedText ||
                    "capacit" in normalizedText

        "grip" ->
            actionType == "grip" ||
                    "agarre" in normalizedText ||
                    "grip" in normalizedText

        "other" ->
            actionType == "other"

        else -> true
    }
}