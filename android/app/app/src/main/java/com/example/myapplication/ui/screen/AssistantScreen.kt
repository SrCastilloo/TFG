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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.state.ChatMessage
import com.example.myapplication.ui.viewmodel.AssistantViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Color tokens
// ─────────────────────────────────────────────────────────────────────────────

private val BgTop = Color(0xFF020617)
private val BgMiddle = Color(0xFF071A2A)
private val BgBottom = Color(0xFF1E1B4B)

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
private val AccentGreen = Color(0xFF10B981)
private val AccentOrange = Color(0xFFF97316)

private val AppBackground = Brush.verticalGradient(
    listOf(
        BgTop,
        BgMiddle,
        BgBottom
    )
)

private val HeroGradient = Brush.linearGradient(
    listOf(
        AccentViolet,
        AccentPurple,
        AccentCyan
    )
)

private val ChatPanelGradient = Brush.linearGradient(
    listOf(
        Color(0xFF0F172A).copy(alpha = 0.92f),
        Color(0xFF111827).copy(alpha = 0.88f)
    )
)

private val ComposerGradient = Brush.linearGradient(
    listOf(
        Color(0xFF0F172A).copy(alpha = 0.96f),
        Color(0xFF111827).copy(alpha = 0.92f)
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────────────────────────────────────

private data class SuggestionItem(
    val title: String,
    val message: String,
    val emoji: String
)

private val assistantSuggestions = listOf(
    SuggestionItem(
        title = "Modo actual",
        message = "¿En qué modo está ahora el sistema?",
        emoji = "🧭"
    ),
    SuggestionItem(
        title = "Mano disponible",
        message = "¿La mano está disponible ahora mismo?",
        emoji = "🖐️"
    ),
    SuggestionItem(
        title = "Cámara",
        message = "¿La cámara está disponible ahora mismo?",
        emoji = "📷"
    ),
    SuggestionItem(
        title = "Posiciones",
        message = "¿Qué posiciones hay disponibles?",
        emoji = "📍"
    ),
    SuggestionItem(
        title = "Ayuda",
        message = "Explícame cómo usar esta aplicación de forma sencilla.",
        emoji = "💡"
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AssistantScreen(
    onBack: () -> Unit,
    viewModel: AssistantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        val extra = if (uiState.isLoading) 1 else 0
        val targetIndex = uiState.messages.lastIndex + extra

        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .imePadding()
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val isLandscape = screenWidth > screenHeight
        val useTwoPane = isLandscape && screenWidth >= 700.dp
        val compact = screenHeight < 560.dp || screenWidth < 390.dp
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
                AssistantLandscapeContent(
                    messages = uiState.messages,
                    isLoading = uiState.isLoading,
                    currentInput = uiState.currentInput,
                    listState = listState,
                    onBack = onBack,
                    onInputChange = { viewModel.onInputChange(it) },
                    onSend = { viewModel.sendMessage() }
                )
            } else {
                AssistantPortraitContent(
                    messages = uiState.messages,
                    isLoading = uiState.isLoading,
                    currentInput = uiState.currentInput,
                    listState = listState,
                    compact = compact,
                    screenHeight = screenHeight,
                    onBack = onBack,
                    onInputChange = { viewModel.onInputChange(it) },
                    onSend = { viewModel.sendMessage() }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Responsive layouts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AssistantPortraitContent(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    currentInput: String,
    listState: LazyListState,
    compact: Boolean,
    screenHeight: Dp,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val verySmallHeight = screenHeight < 620.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = if (compact) 12.dp else 16.dp,
                end = if (compact) 12.dp else 16.dp,
                top = if (compact) 10.dp else 14.dp,
                bottom = if (compact) 10.dp else 14.dp
            )
    ) {
        TopActionRow(
            onBack = onBack,
            compact = compact
        )

        Spacer(modifier = Modifier.height(if (compact) 8.dp else 10.dp))

        if (!verySmallHeight || messages.isEmpty()) {
            HeroAssistantCard(
                compact = compact
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))
        }

        if (messages.isEmpty()) {
            SuggestionSectionHorizontal(
                compact = compact,
                onSuggestionClick = onInputChange
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))
        }

        ChatContainer(
            messages = messages,
            isLoading = isLoading,
            listState = listState,
            compact = compact,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        ComposerCard(
            value = currentInput,
            isLoading = isLoading,
            landscape = false,
            compact = compact,
            onValueChange = onInputChange,
            onSend = onSend
        )
    }
}

@Composable
private fun AssistantLandscapeContent(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    currentInput: String,
    listState: LazyListState,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
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
                .weight(0.36f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            item {
                TopActionRow(
                    onBack = onBack,
                    compact = true
                )
            }

            item {
                HeroAssistantCard(
                    compact = true
                )
            }

            item {
                AssistantGuideCard(compact = true)
            }

            item {
                SuggestionSectionVertical(
                    onSuggestionClick = onInputChange
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(0.64f)
                .fillMaxHeight()
        ) {
            ChatHeaderCard(
                messagesCount = messages.size,
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(10.dp))

            ChatContainer(
                messages = messages,
                isLoading = isLoading,
                listState = listState,
                compact = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            ComposerCard(
                value = currentInput,
                isLoading = isLoading,
                landscape = true,
                compact = true,
                onValueChange = onInputChange,
                onSend = onSend
            )
        }
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
                .size(280.dp)
                .offset(x = (-110).dp, y = (-100).dp)
                .background(
                    color = AccentCyan.copy(alpha = 0.18f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 120.dp, y = 110.dp)
                .background(
                    color = AccentViolet.copy(alpha = 0.24f),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 70.dp)
                .background(
                    color = AccentPink.copy(alpha = 0.13f),
                    shape = CircleShape
                )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopActionRow(
    onBack: () -> Unit,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .clickable { onBack() }
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp)
                ),
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.13f)
        ) {
            Text(
                text = "← Volver",
                modifier = Modifier.padding(
                    horizontal = if (compact) 13.dp else 16.dp,
                    vertical = if (compact) 8.dp else 10.dp
                ),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
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

@Composable
private fun HeroAssistantCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 34.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(HeroGradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(if (compact) 24.dp else 34.dp)
                )
                .padding(
                    horizontal = if (compact) 16.dp else 22.dp,
                    vertical = if (compact) 16.dp else 24.dp
                )
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 110.dp else 150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 42.dp, y = (-48).dp)
                    .background(
                        color = Color.White.copy(alpha = 0.12f),
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
                            text = "Asistente inteligente",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            style = if (compact) {
                                MaterialTheme.typography.titleLarge
                            } else {
                                MaterialTheme.typography.headlineMedium
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Pregunta sobre estado, modos, cámara, posiciones o uso de la mano robótica.",
                            color = Color.White.copy(alpha = 0.92f),
                            style = if (compact) {
                                MaterialTheme.typography.bodySmall
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            maxLines = if (compact) 3 else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "🤖",
                        style = if (compact) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.displaySmall
                        },
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LivePill(
    compact: Boolean
) {
    val transition = rememberInfiniteTransition(label = "assistant-live")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "assistant-live-dot"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.17f),
                shape = RoundedCornerShape(999.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
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
                    color = Color.White,
                    shape = CircleShape
                )
        )

        Text(
            text = "CHAT DEL SISTEMA",
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Guide and suggestions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AssistantGuideCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC).copy(alpha = 0.96f))
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 14.dp else 18.dp)
        ) {
            Text(
                text = "💡 Cómo usarlo",
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Escribe una pregunta o toca una sugerencia. El asistente responderá con información útil del sistema.",
                color = BodyText,
                style = if (compact) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SuggestionSectionHorizontal(
    compact: Boolean,
    onSuggestionClick: (String) -> Unit
) {
    Column {
        SectionTitle(
            title = "Pruebas rápidas",
            subtitle = "Toca una sugerencia para rellenar la pregunta",
            compact = compact
        )

        Spacer(modifier = Modifier.height(if (compact) 8.dp else 10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(assistantSuggestions) { suggestion ->
                SuggestionChip(
                    suggestion = suggestion,
                    compact = compact,
                    onClick = { onSuggestionClick(suggestion.message) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionSectionVertical(
    onSuggestionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionTitle(
                title = "Pruebas rápidas",
                subtitle = "Pulsa para preparar una pregunta",
                compact = true
            )

            assistantSuggestions.forEach { suggestion ->
                SuggestionRow(
                    suggestion = suggestion,
                    onClick = { onSuggestionClick(suggestion.message) }
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    suggestion: SuggestionItem,
    compact: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
        color = Color.White.copy(alpha = 0.11f),
        modifier = Modifier
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(if (compact) 18.dp else 22.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 10.dp else 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = suggestion.emoji,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = suggestion.title,
                color = TextPrimary,
                style = if (compact) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: SuggestionItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.10f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = suggestion.emoji,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = suggestion.message,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Chat
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatHeaderCard(
    messagesCount: Int,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.09f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💬",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Conversación",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isLoading) {
                        "El asistente está preparando una respuesta..."
                    } else {
                        "$messagesCount mensajes en este chat"
                    },
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ChatContainer(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: LazyListState,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatPanelGradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(if (compact) 24.dp else 30.dp)
                )
        ) {
            if (messages.isEmpty() && !isLoading) {
                EmptyAssistantState(
                    compact = compact,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (compact) 18.dp else 22.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = if (compact) 12.dp else 16.dp,
                        end = if (compact) 12.dp else 16.dp,
                        top = if (compact) 12.dp else 16.dp,
                        bottom = if (compact) 12.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)
                ) {
                    itemsIndexed(messages) { _, message ->
                        ChatBubble(
                            message = message,
                            compact = compact
                        )
                    }

                    if (isLoading) {
                        item {
                            AssistantTypingBubble(compact = compact)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAssistantState(
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "🤖",
            style = if (compact) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.displaySmall
            }
        )

        Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

        Text(
            text = "Todavía no has enviado ningún mensaje",
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            style = if (compact) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Escribe una pregunta abajo o usa una sugerencia para empezar.",
            color = TextMuted,
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyLarge
            },
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(
                if (compact) 0.90f else 0.84f
            ),
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 22.dp else 10.dp,
                topEnd = if (message.isUser) 10.dp else 22.dp,
                bottomEnd = 22.dp,
                bottomStart = 22.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    AccentCyan.copy(alpha = 0.24f)
                } else {
                    AccentViolet.copy(alpha = 0.24f)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(if (compact) 12.dp else 14.dp)
            ) {
                Text(
                    text = if (message.isUser) "Tú" else "Asistente",
                    color = if (message.isUser) {
                        Color(0xFF67E8F9)
                    } else {
                        Color(0xFFD8B4FE)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.text,
                    color = TextPrimary,
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    }
                )
            }
        }
    }
}

@Composable
private fun AssistantTypingBubble(
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(
                if (compact) 0.68f else 0.58f
            ),
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 22.dp,
                bottomEnd = 22.dp,
                bottomStart = 22.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = AccentViolet.copy(alpha = 0.24f)
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = if (compact) 12.dp else 14.dp,
                    vertical = if (compact) 11.dp else 12.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(if (compact) 16.dp else 18.dp),
                    strokeWidth = 2.5.dp,
                    color = AccentPurple
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Pensando...",
                    color = TextPrimary,
                    style = if (compact) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ComposerCard(
    value: String,
    isLoading: Boolean,
    landscape: Boolean,
    compact: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(ComposerGradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.11f),
                    shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
                )
                .padding(if (compact) 12.dp else 14.dp)
        ) {
            if (landscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MessageTextField(
                        value = value,
                        isLoading = isLoading,
                        compact = true,
                        singleLineMode = true,
                        modifier = Modifier.weight(1f),
                        onValueChange = onValueChange
                    )

                    SendButton(
                        value = value,
                        isLoading = isLoading,
                        compact = true,
                        modifier = Modifier
                            .width(150.dp)
                            .height(56.dp),
                        onSend = onSend
                    )
                }
            } else {
                Column {
                    MessageTextField(
                        value = value,
                        isLoading = isLoading,
                        compact = compact,
                        singleLineMode = false,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = onValueChange
                    )

                    Spacer(modifier = Modifier.height(if (compact) 10.dp else 12.dp))

                    SendButton(
                        value = value,
                        isLoading = isLoading,
                        compact = compact,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (compact) 50.dp else 54.dp),
                        onSend = onSend
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageTextField(
    value: String,
    isLoading: Boolean,
    compact: Boolean,
    singleLineMode: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = {
            Text(
                text = "Escribe tu mensaje"
            )
        },
        shape = RoundedCornerShape(if (compact) 16.dp else 18.dp),
        enabled = !isLoading,
        singleLine = singleLineMode,
        minLines = if (singleLineMode) 1 else 1,
        maxLines = if (singleLineMode) 1 else 4,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = TextPrimary
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            disabledTextColor = TextMuted,
            cursorColor = AccentCyan,
            focusedBorderColor = AccentCyan,
            unfocusedBorderColor = Color(0xFF94A3B8),
            disabledBorderColor = Color(0xFF64748B),
            focusedLabelColor = AccentCyan,
            unfocusedLabelColor = TextMuted,
            disabledLabelColor = TextDim,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        )
    )
}

@Composable
private fun SendButton(
    value: String,
    isLoading: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onSend: () -> Unit
) {
    Button(
        onClick = onSend,
        enabled = !isLoading && value.isNotBlank(),
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 16.dp else 18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentCyan,
            contentColor = TextPrimary,
            disabledContainerColor = AccentCyan.copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.62f)
        ),
        contentPadding = PaddingValues(horizontal = 10.dp)
    ) {
        Text(
            text = if (isLoading) "Pensando..." else "Enviar",
            fontWeight = FontWeight.ExtraBold,
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

// ─────────────────────────────────────────────────────────────────────────────
// Common
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
            color = TextMuted,
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