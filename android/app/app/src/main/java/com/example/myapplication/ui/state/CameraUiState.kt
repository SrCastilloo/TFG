package com.example.myapplication.ui.state

data class CameraUiState(
    val isLoading: Boolean = false,
    val detectedObject: String? = null,
    val detectionQuality: Double? = null,
    val targetPosition: Int? = null,
    val actionMessage: String? = null,
    val error: String? = null
)