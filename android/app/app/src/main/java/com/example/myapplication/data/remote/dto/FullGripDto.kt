package com.example.myapplication.data.remote.dto

data class FullGripRequest(
    val max_seconds: Double = 15.0,
    val poll_interval: Double = 0.08,
    val consecutive_reads: Int = 2,
    val ignored_sensors: List<String> = listOf("ring"),
    val required_sensors: List<String>? = null,
    val start_from_open: Boolean = true,
    val open_wait_seconds: Double = 3.0,
    val close_step: Int = 20,
    val step_settle_seconds: Double = 0.12,
    val pause_between_steps: Double = 0.20
)

data class FullGripDto(
    val ok: Boolean,
    val message: String?,
    val moved: Boolean?,
    val stopped: Boolean?,
    val all_contacts_detected: Boolean?,
    val reason: String?,
    val elapsed_seconds: Double?,
    val active_sensors: List<String>?,
    val contact_sensors: List<String>?,
    val missing_sensors: List<String>?,
    val contact_count: Int?,
    val required_contact_count: Int?,
    val ignored_sensors: List<String>?,
    val start_from_open: Boolean?,
    val step_count: Int?,
    val close_step: Int?,
    val command: Map<String, String>?,
    val last_step_target: Map<String, Int>? = null,
    val capacitive: CapacitiveDto?
)