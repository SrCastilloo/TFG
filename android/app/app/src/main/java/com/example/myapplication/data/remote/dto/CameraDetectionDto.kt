package com.example.myapplication.data.remote.dto

data class CameraDetectionDto(
    val ok: Boolean,
    val mode: String? = null,
    val message: String? = null,
    val `object`: String? = null,
    val detection_quality: Double? = null,
    val target_position: Int? = null
)