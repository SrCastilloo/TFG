package com.example.myapplication.data.remote.dto
data class HandPositionsDto(
    val ok: Boolean,
    val positions: List<Int>,
    val count: Int
)