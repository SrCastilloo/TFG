package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FloatingSettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 10.dp,
        color = Color.Transparent,
        modifier = modifier
    ) {
        Text(
            text = "⚙️ Ajustes",
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF334155),
                            Color(0xFF475569),
                            Color(0xFF64748B)
                        )
                    ),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 16.dp, vertical = 11.dp),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}