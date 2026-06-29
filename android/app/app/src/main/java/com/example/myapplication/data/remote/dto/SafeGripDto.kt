package com.example.myapplication.data.remote.dto

data class SafeGripRequest(
    val max_seconds: Double = 15.0,
    val poll_interval: Double = 0.08,
    val consecutive_reads: Int = 2,
    val ignored_sensors: List<String> = listOf("ring", "palm"),
    val start_from_open: Boolean = true,
    val open_wait_seconds: Double = 3.0,
    val target_position_id: Int = 2,
    val close_step: Int = 30,
    val step_settle_seconds: Double = 0.20,
    val pause_between_steps: Double = 0.0
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
    val ignored_sensors: List<String>?,
    val start_from_open: Boolean?,
    val step_count: Int?,
    val close_step: Int?,
    val target_position_id: Int?,
    val last_step_target: Map<String, Int>?,
    val command: Map<String, Any>?,
    val capacitive: CapacitiveDto?
)