package com.example.myapplication.data.remote.dto

data class SafeGripRequest(
    val max_seconds: Double = 4.0,
    val poll_interval: Double = 0.15,
    val consecutive_reads: Int = 2
)

data class SafeGripDto(
    val ok: Boolean,
    val message: String?,
    val moved: Boolean?,
    val stopped: Boolean?,
    val contact_detected: Boolean?,
    val reason: String?,
    val elapsed_seconds: Double?,
    val contact_sensor: String?,
    val contact_count: Int?,
    val command: Map<String, String>?,
    val capacitive: CapacitiveDto?
)