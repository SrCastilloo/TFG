package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AccentCyan
import com.example.myapplication.ui.components.AccentViolet
import com.example.myapplication.ui.state.ChatMessage
import com.example.myapplication.ui.viewmodel.AssistantViewModel

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B1020),
                        Color(0xFF111C33),
                        Color(0xFF1A2750)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            TopActionRow(onBack = onBack)

            Spacer(modifier = Modifier.height(10.dp))

            CompactHeroAssistantCard()

            if (uiState.messages.isEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                CompactSuggestionSection(
                    onSuggestionClick = { suggestion ->
                        viewModel.onInputChange(suggestion)
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ChatContainer(
                messages = uiState.messages,
                isLoading = uiState.isLoading,
                listState = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 260.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            ComposerCard(
                value = uiState.currentInput,
                isLoading = uiState.isLoading,
                onValueChange = { viewModel.onInputChange(it) },
                onSend = { viewModel.sendMessage() }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun TopActionRow(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
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
    }
}

@Composable
private fun CompactHeroAssistantCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 20.dp,
            bottomEnd = 28.dp,
            bottomStart = 20.dp
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
                            Color(0xFF22D3EE)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                LightPill(text = "Asistente inteligente")

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Habla con el sistema",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Consulta estado, modos, mano, cámara o posiciones configuradas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}

@Composable
private fun CompactSuggestionSection(
    onSuggestionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Pruebas rápidas",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = "Toca una sugerencia para rellenar la pregunta",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            item {
                SuggestionChip(
                    text = "¿En qué modo está?",
                    onClick = { onSuggestionClick("¿En qué modo está ahora el sistema?") }
                )
            }
            item {
                SuggestionChip(
                    text = "¿La mano está disponible?",
                    onClick = { onSuggestionClick("¿La mano está disponible ahora mismo?") }
                )
            }
            item {
                SuggestionChip(
                    text = "¿Hay cámara?",
                    onClick = { onSuggestionClick("¿La cámara está disponible ahora mismo?") }
                )
            }
            item {
                SuggestionChip(
                    text = "¿Qué posiciones hay?",
                    onClick = { onSuggestionClick("¿Qué posiciones hay disponibles?") }
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.10f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ChatContainer(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.06f)
        )
    ) {
        if (messages.isEmpty() && !isLoading) {
            EmptyAssistantState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }

                if (isLoading) {
                    item {
                        AssistantTypingBubble()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAssistantState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "🤖",
            style = MaterialTheme.typography.displaySmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Todavía no has enviado ningún mensaje",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Escribe una pregunta en la parte inferior para empezar a hablar con el asistente.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFCBD5E1)
        )
    }
}

@Composable
private fun ComposerCard(
    value: String,
    isLoading: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.88f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Escribe tu mensaje") },
                shape = RoundedCornerShape(18.dp),
                enabled = !isLoading,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AccentCyan,
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = Color(0xFF94A3B8),
                    focusedLabelColor = AccentCyan,
                    unfocusedLabelColor = Color(0xFFCBD5E1),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSend,
                enabled = !isLoading && value.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = Color.White,
                    disabledContainerColor = AccentCyan.copy(alpha = 0.45f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = if (isLoading) "Pensando..." else "Enviar mensaje",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.84f),
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 22.dp else 10.dp,
                topEnd = if (message.isUser) 10.dp else 22.dp,
                bottomEnd = 22.dp,
                bottomStart = 22.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    Color(0xFF06B6D4).copy(alpha = 0.22f)
                } else {
                    Color(0xFF8B5CF6).copy(alpha = 0.20f)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = if (message.isUser) "Tú" else "Asistente",
                    color = if (message.isUser) Color(0xFF67E8F9) else Color(0xFFD8B4FE),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun AssistantTypingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.58f),
            shape = RoundedCornerShape(
                topStart = 10.dp,
                topEnd = 22.dp,
                bottomEnd = 22.dp,
                bottomStart = 22.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF8B5CF6).copy(alpha = 0.20f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.5.dp,
                    color = AccentViolet
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Pensando...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LightPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
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