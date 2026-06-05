package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AccentCyan
import com.example.myapplication.ui.components.AccentViolet
import com.example.myapplication.ui.components.PremiumActionButton
import com.example.myapplication.ui.components.PremiumGlassCard
import com.example.myapplication.ui.components.PremiumScreenBackground
import com.example.myapplication.ui.components.PremiumText
import com.example.myapplication.ui.components.PremiumTextSoft
import com.example.myapplication.ui.components.PremiumTopPill
import com.example.myapplication.ui.state.ChatMessage
import com.example.myapplication.ui.viewmodel.AssistantViewModel

@Composable
fun AssistantScreen(
    onBack: () -> Unit,
    viewModel: AssistantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PremiumScreenBackground {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                containerColor = Color.Transparent
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .imePadding()
                ) {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }

                    PremiumGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        accent = AccentViolet
                    ) {
                        Column {
                            PremiumTopPill(
                                text = "Asistente inteligente",
                                accent = AccentViolet
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Asistente",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PremiumText,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Puedes preguntarme por el estado del sistema, los modos, la simulación o las posiciones disponibles.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = PremiumTextSoft
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.messages) { message ->
                            ChatBubble(message = message)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.currentInput,
                        onValueChange = { viewModel.onInputChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Escribe tu mensaje") },
                        shape = RoundedCornerShape(18.dp),
                        enabled = !uiState.isLoading,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = PremiumText
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PremiumText,
                            unfocusedTextColor = PremiumText,
                            cursorColor = AccentCyan,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = PremiumTextSoft,
                            focusedLabelColor = AccentCyan,
                            unfocusedLabelColor = PremiumTextSoft,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PremiumActionButton(
                        text = if (uiState.isLoading) "Pensando..." else "Enviar",
                        onClick = { viewModel.sendMessage() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        color = AccentCyan
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 110.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    CircularProgressIndicator(color = AccentViolet)
                }
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
        PremiumGlassCard(
            modifier = Modifier.fillMaxWidth(0.82f),
            accent = if (message.isUser) AccentCyan else AccentViolet
        ) {
            Text(
                text = message.text,
                color = PremiumText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}