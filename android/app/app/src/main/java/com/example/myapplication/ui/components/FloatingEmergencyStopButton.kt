package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.myapplication.ui.viewmodel.EmergencyStopViewModel
import kotlinx.coroutines.delay

@Composable
fun FloatingEmergencyStopButton(
    modifier: Modifier = Modifier,
    viewModel: EmergencyStopViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.message, uiState.error) {
        if (uiState.message != null || uiState.error != null) {
            delay(2500)
            viewModel.clearFeedback()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (uiState.message != null || uiState.error != null) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (uiState.error != null) {
                    Color(0xFF7F1D1D)
                } else {
                    Color(0xFF14532D)
                },
                shadowElevation = 8.dp
            ) {
                Text(
                    text = uiState.error ?: uiState.message ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Surface(
            onClick = {
                if (!uiState.isLoading) {
                    viewModel.emergencyStop()
                }
            },
            shape = RoundedCornerShape(999.dp),
            shadowElevation = 10.dp,
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFDC2626),
                                Color(0xFFEF4444),
                                Color(0xFFF97316)
                            )
                        ),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Parando...",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = "🛑 STOP",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}