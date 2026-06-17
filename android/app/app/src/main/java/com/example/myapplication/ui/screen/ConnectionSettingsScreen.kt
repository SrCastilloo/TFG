package com.example.myapplication.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.viewmodel.ConnectionSettingsViewModel

@Composable
fun ConnectionSettingsScreen(
    onBack: () -> Unit,
    scaffoldPadding: PaddingValues = PaddingValues(),
    viewModel: ConnectionSettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
            .consumeWindowInsets(scaffoldPadding)
            .imePadding()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF071A20),
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
    ) {
        val isLandscape = maxWidth > maxHeight
        val horizontalPadding = if (isLandscape) 18.dp else 16.dp
        val itemSpacing = if (isLandscape) 12.dp else 16.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal
                    )
                ),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 16.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            item {
                TopSettingsRow(
                    onBack = onBack,
                    onTest = { viewModel.testConnection() },
                    isTesting = uiState.isTesting
                )
            }

            item {
                SettingsHeroCard(
                    compact = isLandscape
                )
            }

            item {
                ConnectionFormCard(
                    ip = uiState.ip,
                    port = uiState.port,
                    baseUrl = uiState.baseUrl,
                    isTesting = uiState.isTesting,
                    onIpChange = { viewModel.onIpChange(it) },
                    onPortChange = { viewModel.onPortChange(it) },
                    onSave = { viewModel.saveSettings() },
                    onTest = { viewModel.testConnection() },
                    compact = isLandscape
                )
            }

            if (uiState.message != null) {
                item {
                    FeedbackConnectionCard(
                        title = "Conexión correcta",
                        text = uiState.message ?: "",
                        isError = false
                    )
                }
            }

            if (uiState.error != null) {
                item {
                    FeedbackConnectionCard(
                        title = "Problema de conexión",
                        text = uiState.error ?: "",
                        isError = true
                    )
                }
            }

            item {
                HelpConnectionCard()
            }
        }
    }
}

@Composable
private fun TopSettingsRow(
    onBack: () -> Unit,
    onTest: () -> Unit,
    isTesting: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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

        TextButton(
            onClick = onTest,
            enabled = !isTesting
        ) {
            Text(
                text = if (isTesting) "Probando..." else "Probar conexión",
                color = Color(0xFFA5F3FC),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsHeroCard(
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 24.dp else 30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF0EA5E9),
                            Color(0xFF2563EB),
                            Color(0xFF7C3AED)
                        )
                    )
                )
                .padding(if (compact) 18.dp else 22.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "⚙️ Ajustes de conexión",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 14.dp))

            Text(
                text = "Conexión con Raspberry",
                color = Color.White,
                style = if (compact) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Configura la IP y el puerto del backend para conectar la app con la Raspberry sin recompilar.",
                color = Color.White.copy(alpha = 0.94f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ConnectionFormCard(
    ip: String,
    port: String,
    baseUrl: String,
    isTesting: Boolean,
    onIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
    compact: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 22.dp else 26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Datos de conexión",
                color = Color(0xFF111827),
                style = if (compact) {
                    MaterialTheme.typography.titleMedium
                } else {
                    MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.ExtraBold
            )

            OutlinedTextField(
                value = ip,
                onValueChange = onIpChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("IP de la Raspberry") },
                placeholder = { Text("Ejemplo: 192.168.100.201") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    focusedLabelColor = Color(0xFF2563EB),
                    cursorColor = Color(0xFF2563EB)
                )
            )

            OutlinedTextField(
                value = port,
                onValueChange = onPortChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Puerto") },
                placeholder = { Text("Ejemplo: 8000") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    focusedLabelColor = Color(0xFF2563EB),
                    cursorColor = Color(0xFF2563EB)
                )
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE0F2FE)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Text(
                        text = "URL que usará la app",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = baseUrl,
                        color = Color(0xFF0369A1),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (compact) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TestButton(
                        isTesting = isTesting,
                        onClick = onTest,
                        modifier = Modifier.weight(1f)
                    )

                    SaveButton(
                        isTesting = isTesting,
                        onClick = onSave,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                TestButton(
                    isTesting = isTesting,
                    onClick = onTest,
                    modifier = Modifier.fillMaxWidth()
                )

                SaveButton(
                    isTesting = isTesting,
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TestButton(
    isTesting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isTesting,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0EA5E9),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF0EA5E9).copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.65f)
        )
    ) {
        if (isTesting) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.5.dp,
                modifier = Modifier.height(20.dp)
            )
        } else {
            Text(
                text = "Probar conexión",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SaveButton(
    isTesting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isTesting,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF10B981),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.45f),
            disabledContentColor = Color.White.copy(alpha = 0.65f)
        )
    ) {
        Text(
            text = "Guardar ajustes",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FeedbackConnectionCard(
    title: String,
    text: String,
    isError: Boolean
) {
    val background = if (isError) Color(0xFF4C1D24) else Color(0xFF0F3B2E)
    val titleColor = if (isError) Color(0xFFFFD5DC) else Color(0xFFB7F7D8)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = text,
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HelpConnectionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFBF5)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ayuda rápida",
                color = Color(0xFF111827),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "1. La Raspberry y el móvil deben estar en la misma red Wi-Fi.",
                color = Color(0xFF374151)
            )

            Text(
                text = "2. El backend debe estar arrancado con Uvicorn.",
                color = Color(0xFF374151)
            )

            Text(
                text = "3. Ejemplo: IP 192.168.100.201 y puerto 8000.",
                color = Color(0xFF374151)
            )
        }
    }
}