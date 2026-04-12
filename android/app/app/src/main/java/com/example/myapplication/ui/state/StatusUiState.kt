package com.example.myapplication.ui.state

data class StatusUiState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val simulation: Boolean? = null,
    val mode: String = "",
    val configPath: String = "",
    val lastPositionMapped: Int? = null,
    val handAvailable: Boolean = false,
    val cameraAvailable: Boolean = false,
    val positions: List<Int> = emptyList(),
    val actionMessage: String? = null,
    val error: String? = null
)