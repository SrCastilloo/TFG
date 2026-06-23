package com.example.myapplication.data.remote.dto

data class CapacitiveDto(
    val ok: Boolean,
    val available: Boolean,
    val simulation: Boolean,
    val status: Map<String, Int>?,
    val heights: Map<String, Int>?,
    val contacts: Map<String, Boolean>?,
    val contact_count: Int,
    val message: String?
)

