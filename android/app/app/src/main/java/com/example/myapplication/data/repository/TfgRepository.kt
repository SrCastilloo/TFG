package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.TfgApiService
import com.example.myapplication.data.remote.dto.BasicResponseDto
import com.example.myapplication.data.remote.dto.HandPositionsDto
import com.example.myapplication.data.remote.dto.SystemInfoDto
import com.example.myapplication.data.remote.dto.CameraDetectionDto

class TfgRepository(
    private val apiService: TfgApiService
) {
    suspend fun getSystemInfo(): SystemInfoDto {
        return apiService.getSystemInfo()
    }

    suspend fun getHandPositions(): HandPositionsDto {
        return apiService.getHandPositions()
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
}