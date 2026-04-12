package com.example.myapplication.data.remote.dto

data class SystemInfoDto(
    val ok: Boolean,
    val simulation: Boolean,
    val mode: String,
    val config_path: String,
    val last_position_mapped: Int?,
    val hand_available: Boolean,
    val camera_available: Boolean
)