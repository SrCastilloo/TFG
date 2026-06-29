package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.TfgApiService
import com.example.myapplication.data.remote.dto.BasicResponseDto
import com.example.myapplication.data.remote.dto.HandPositionsDto
import com.example.myapplication.data.remote.dto.SystemInfoDto
import com.example.myapplication.data.remote.dto.CameraDetectionDto
import com.example.myapplication.data.remote.dto.AssistantChatRequestDto
import com.example.myapplication.data.remote.dto.AssistantChatResponseDto
import com.example.myapplication.data.remote.dto.VoiceDetectResponse
import com.example.myapplication.data.remote.dto.VoiceMoveResponse
import com.example.myapplication.data.remote.dto.HealthDto
import com.example.myapplication.data.remote.dto.CapacitiveDto
import com.example.myapplication.data.remote.dto.FullGripDto
import com.example.myapplication.data.remote.dto.FullGripRequest
import com.example.myapplication.data.remote.dto.ModeDto
import com.example.myapplication.data.remote.dto.SafeGripDto
import com.example.myapplication.data.remote.dto.SafeGripRequest


class TfgRepository(
    private val apiService: TfgApiService
) {
    suspend fun getSystemInfo(): SystemInfoDto {
        return apiService.getSystemInfo()
    }

    suspend fun getHandPositions(): HandPositionsDto {
        return apiService.getHandPositions()
    }

    suspend fun getHealth(): HealthDto {
        return apiService.getHealth()
    }

    suspend fun setModeHand(): BasicResponseDto {
        return apiService.setModeHand()
    }

    suspend fun setModeVoice(): BasicResponseDto {
        return apiService.setModeVoice()
    }

    suspend fun setModeCamera(): BasicResponseDto {
        return apiService.setModeCamera()
    }

    suspend fun openHand(): BasicResponseDto {
        return apiService.openHand()
    }

    suspend fun stopHand(): BasicResponseDto {
        return apiService.stopHand()
    }

    suspend fun moveToPosition(positionId: Int): BasicResponseDto {
        return apiService.moveToPosition(positionId)
    }

    suspend fun detectObject(): CameraDetectionDto {
        return apiService.detectObject()
    }

    suspend fun detectAndMove(): CameraDetectionDto {
        return apiService.detectAndMove()
    }

    suspend fun chatWithAssistant(message: String): AssistantChatResponseDto {
        return apiService.chatWithAssistant(
            AssistantChatRequestDto(message = message)
        )
    }

    suspend fun detectVoice(): VoiceDetectResponse {
        return apiService.detectVoice()
    }

    suspend fun detectVoiceAndMove(): VoiceMoveResponse {
        return apiService.detectVoiceAndMove()
    }

    suspend fun getCapacitiveStatus(): CapacitiveDto {
        return apiService.getCapacitiveStatus()
    }

    suspend fun refreshCapacitiveStatus(): CapacitiveDto {
        return apiService.refreshCapacitiveStatus()
    }

    suspend fun safeGrip(): SafeGripDto {
        return apiService.safeGrip(
            SafeGripRequest(
                max_seconds = 15.0,
                poll_interval = 0.08,
                consecutive_reads = 2,
                ignored_sensors = listOf("ring", "palm"),
                start_from_open = true,
                open_wait_seconds = 3.0,
                target_position_id = 2,
                close_step = 30,
                step_settle_seconds = 0.20,
                pause_between_steps = 0.0
            )
        )
    }
    suspend fun fullGrip(): FullGripDto {
        return apiService.fullGrip(
            FullGripRequest(
                max_seconds = 15.0,
                poll_interval = 0.08,
                consecutive_reads = 2,
                ignored_sensors = listOf("ring"),
                required_sensors = null,
                start_from_open = true,
                open_wait_seconds = 3.0,
                close_step = 20,
                step_settle_seconds = 0.12,
                pause_between_steps = 0.20
            )
        )
    }
    suspend fun setModeHandVoice(): ModeDto {
        return apiService.setModeHandVoice()
    }

    suspend fun setModeVoiceVoice(): ModeDto {
        return apiService.setModeVoiceVoice()
    }

    suspend fun setModeCameraVoice(): ModeDto {
        return apiService.setModeCameraVoice()
    }

}