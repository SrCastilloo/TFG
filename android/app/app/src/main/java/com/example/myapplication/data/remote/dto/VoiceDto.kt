package com.example.myapplication.data.remote.dto

data class VoiceDetectResponse(
    val ok: Boolean,
    val command: String?,
    val detection_quality: Int?,
    val error: String? = null
)

data class VoiceMoveResponse(
    val ok: Boolean,
    val moved: Boolean?,
    val voice: VoiceDetectResponse?,
    val position_id: Int?,
    val message: String?,
    val move_result: MoveResultResponse? = null,
    val error: String? = null
)

data class MoveResultResponse(
    val ok: Boolean,
    val message: String?,
    val position_id: Int?
)