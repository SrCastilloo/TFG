package com.example.myapplication.ui.state


data class HandUiState(
    val isLoading: Boolean = false,
    val positions: List<Int> = emptyList(),
    val actionMessage: String? = null,
    val error: String? = null
)