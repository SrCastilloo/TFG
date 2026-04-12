package com.example.myapplication.data.remote.dto

data class BasicResponseDto(
    val ok: Boolean,
    val message: String? = null,
    val mode: String? = null,
    val position_id: Int? = null,
    val target_position: Int? = null
)