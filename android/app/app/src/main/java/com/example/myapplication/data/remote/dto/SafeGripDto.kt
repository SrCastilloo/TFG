package com.example.myapplication.data.remote.dto
data class SafeGripRequest(
    val max_seconds: Double = 8.0,
    val poll_interval: Double = 0.08,
    val consecutive_reads: Int = 2,
    val ignored_sensors: List<String> = listOf("ring"),
    val start_from_open: Boolean = true,
    val open_wait_seconds: Double = 3.0,
    val close_pulse_seconds: Double = 0.20,
    val pause_between_pulses: Double = 0.12
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
    val capacitive: CapacitiveDto?,
    val ignored_sensors: List<String>?,
    val start_from_open: Boolean?,
    val pulse_count: Int?,
    val close_pulse_seconds: Double?,
)