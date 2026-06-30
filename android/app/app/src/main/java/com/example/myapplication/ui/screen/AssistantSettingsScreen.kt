package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.local.assistant.AssistantProvider
import com.example.myapplication.ui.viewmodel.AssistantSettingsUiState
import com.example.myapplication.ui.viewmodel.AssistantSettingsViewModel

@Composable
fun AssistantSettingsScreen(
    onBack: () -> Unit,
    viewModel: AssistantSettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AssistantSettingsHeader(onBack = onBack)

            CurrentConnectionCard(uiState = uiState)

            ProviderSelectorCard(
                selectedProvider = uiState.selectedProvider,
                onProviderSelected = viewModel::selectProvider
            )

            SelectedProviderHelpCard(provider = uiState.selectedProvider)

            when (uiState.selectedProvider) {
                AssistantProvider.OPENAI -> {
                    OpenAiConfigCard(
                        uiState = uiState,
                        onApiKeyChange = viewModel::setOpenAiApiKey,
                        onModelChange = viewModel::setOpenAiModel,
                        onToggleShowKey = viewModel::toggleShowOpenAiKey
                    )
                }

                AssistantProvider.GEMINI -> {
                    GeminiConfigCard(
                        uiState = uiState,
                        onApiKeyChange = viewModel::setGeminiApiKey,
                        onModelChange = viewModel::setGeminiModel,
                        onToggleShowKey = viewModel::toggleShowGeminiKey
                    )
                }
            }

            if (!uiState.error.isNullOrBlank()) {
                MessageCard(
                    text = uiState.error!!,
                    color = Color(0xFFFB7185),
                    emoji = "⚠️"
                )
            }

            if (!uiState.message.isNullOrBlank()) {
                MessageCard(
                    text = uiState.message!!,
                    color = Color(0xFF86EFAC),
                    emoji = "✅"
                )
            }

            Button(
                onClick = viewModel::saveSettings,
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar configuración del asistente",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun AssistantSettingsHeader(
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

        Column {
            Text(
                text = "Ajustes del asistente IA",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Configura qué IA usará tu cuenta",
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CurrentConnectionCard(
    uiState: AssistantSettingsUiState
) {
    val providerText = when (uiState.selectedProvider) {
        AssistantProvider.OPENAI -> "ChatGPT / OpenAI"
        AssistantProvider.GEMINI -> "Gemini"
    }

    val modelText = when (uiState.selectedProvider) {
        AssistantProvider.OPENAI -> uiState.openAiModel
        AssistantProvider.GEMINI -> uiState.geminiModel
    }

    val hasKey = when (uiState.selectedProvider) {
        AssistantProvider.OPENAI -> uiState.hasSavedOpenAiKey || uiState.openAiApiKeyInput.isNotBlank()
        AssistantProvider.GEMINI -> uiState.hasSavedGeminiKey || uiState.geminiApiKeyInput.isNotBlank()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Estado actual",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Usuario: ${uiState.currentUserName.ifBlank { "Sin usuario" }}",
                color = Color(0xFFCBD5E1),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (hasKey) {
                    Color(0xFF22C55E).copy(alpha = 0.16f)
                } else {
                    Color(0xFFFB7185).copy(alpha = 0.16f)
                }
            ) {
                Text(
                    text = if (hasKey) {
                        "Conectado a: $providerText · Modelo: $modelText"
                    } else {
                        "Falta guardar la clave de $providerText"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    color = if (hasKey) Color(0xFF86EFAC) else Color(0xFFFDA4AF),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ProviderSelectorCard(
    selectedProvider: AssistantProvider,
    onProviderSelected: (AssistantProvider) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Elige la IA que quieres usar",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Cada usuario puede tener su propia configuración. Elige una opción y guarda su clave.",
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProviderOption(
                    modifier = Modifier.weight(1f),
                    title = "ChatGPT",
                    subtitle = "OpenAI",
                    emoji = "🤖",
                    selected = selectedProvider == AssistantProvider.OPENAI,
                    onClick = { onProviderSelected(AssistantProvider.OPENAI) }
                )

                ProviderOption(
                    modifier = Modifier.weight(1f),
                    title = "Gemini",
                    subtitle = "Google",
                    emoji = "✨",
                    selected = selectedProvider == AssistantProvider.GEMINI,
                    onClick = { onProviderSelected(AssistantProvider.GEMINI) }
                )
            }
        }
    }
}

@Composable
private fun ProviderOption(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) {
        Color(0xFF2563EB)
    } else {
        Color.White.copy(alpha = 0.08f)
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                color = Color(0xFFCBD5E1),
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = if (selected) "Seleccionado" else "Tocar para elegir",
                color = if (selected) Color.White else Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SelectedProviderHelpCard(
    provider: AssistantProvider
) {
    val text = when (provider) {
        AssistantProvider.OPENAI ->
            "Para usar ChatGPT/OpenAI, pega aquí una API key de OpenAI. No escribas tu contraseña de ChatGPT."

        AssistantProvider.GEMINI ->
            "Para usar Gemini, pega aquí una API key de Gemini. No escribas tu contraseña de Google."
    }

    MessageCard(
        text = text,
        color = Color(0xFF38BDF8),
        emoji = "ℹ️"
    )
}

@Composable
private fun OpenAiConfigCard(
    uiState: AssistantSettingsUiState,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onToggleShowKey: () -> Unit
) {
    AssistantProviderConfigCard(
        title = "Configuración de ChatGPT / OpenAI",
        savedKeyText = if (uiState.hasSavedOpenAiKey) {
            "Ya hay una clave de OpenAI guardada. Si dejas el campo vacío, se conservará."
        } else {
            "Todavía no hay clave de OpenAI guardada."
        },
        apiKey = uiState.openAiApiKeyInput,
        onApiKeyChange = onApiKeyChange,
        showKey = uiState.showOpenAiKey,
        onToggleShowKey = onToggleShowKey,
        model = uiState.openAiModel,
        onModelChange = onModelChange,
        modelLabel = "Modelo OpenAI"
    )
}

@Composable
private fun GeminiConfigCard(
    uiState: AssistantSettingsUiState,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onToggleShowKey: () -> Unit
) {
    AssistantProviderConfigCard(
        title = "Configuración de Gemini",
        savedKeyText = if (uiState.hasSavedGeminiKey) {
            "Ya hay una clave de Gemini guardada. Si dejas el campo vacío, se conservará."
        } else {
            "Todavía no hay clave de Gemini guardada."
        },
        apiKey = uiState.geminiApiKeyInput,
        onApiKeyChange = onApiKeyChange,
        showKey = uiState.showGeminiKey,
        onToggleShowKey = onToggleShowKey,
        model = uiState.geminiModel,
        onModelChange = onModelChange,
        modelLabel = "Modelo Gemini"
    )
}

@Composable
private fun AssistantProviderConfigCard(
    title: String,
    savedKeyText: String,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    showKey: Boolean,
    onToggleShowKey: () -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    modelLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = savedKeyText,
                color = Color(0xFFCBD5E1),
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = {
                    Text(text = "API key")
                },
                placeholder = {
                    Text(text = "Pega aquí tu clave")
                },
                singleLine = true,
                visualTransformation = if (showKey) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(onClick = onToggleShowKey) {
                        Text(
                            text = if (showKey) "Ocultar" else "Ver",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = assistantTextFieldColors()
            )

            OutlinedTextField(
                value = model,
                onValueChange = onModelChange,
                label = {
                    Text(text = modelLabel)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = assistantTextFieldColors()
            )

            Text(
                text = "Consejo: si no sabes qué poner en el modelo, deja el valor que viene por defecto.",
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MessageCard(
    text: String,
    color: Color,
    emoji: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = "$emoji $text",
            modifier = Modifier.padding(14.dp),
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun assistantTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF38BDF8),
    unfocusedBorderColor = Color(0xFF475569),
    focusedLabelColor = Color(0xFF38BDF8),
    unfocusedLabelColor = Color(0xFF94A3B8),
    cursorColor = Color(0xFF38BDF8)
)