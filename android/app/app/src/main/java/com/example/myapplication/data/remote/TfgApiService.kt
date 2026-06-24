package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.dto.BasicResponseDto
import com.example.myapplication.data.remote.dto.HandPositionsDto
import com.example.myapplication.data.remote.dto.SystemInfoDto
import com.example.myapplication.data.remote.dto.HealthDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.example.myapplication.data.remote.dto.CameraDetectionDto
import com.example.myapplication.data.remote.dto.AssistantChatRequestDto
import com.example.myapplication.data.remote.dto.AssistantChatResponseDto
import com.example.myapplication.data.remote.dto.CapacitiveDto
import com.example.myapplication.data.remote.dto.FullGripDto
import com.example.myapplication.data.remote.dto.FullGripRequest
import com.example.myapplication.data.remote.dto.VoiceDetectResponse
import com.example.myapplication.data.remote.dto.VoiceMoveResponse
import retrofit2.http.Body
import com.example.myapplication.data.remote.dto.SafeGripDto
import com.example.myapplication.data.remote.dto.SafeGripRequest

interface TfgApiService {

    @GET("system/info")
    suspend fun getSystemInfo(): SystemInfoDto

    @GET("hand/positions")
    suspend fun getHandPositions(): HandPositionsDto

    @GET("health/")
    suspend fun getHealth(): HealthDto


    @GET("capacitive/status")
    suspend fun getCapacitiveStatus(): CapacitiveDto

    @POST("capacitive/refresh")
    suspend fun refreshCapacitiveStatus(): CapacitiveDto

    @POST("modes/hand")
    suspend fun setModeHand(): BasicResponseDto

    @POST("modes/voice")
    suspend fun setModeVoice(): BasicResponseDto

    @POST("modes/camera")
    suspend fun setModeCamera(): BasicResponseDto

    @POST("hand/open")
    suspend fun openHand(): BasicResponseDto

    @POST("hand/stop")
    suspend fun stopHand(): BasicResponseDto

    @POST("hand/position/{positionId}")
    suspend fun moveToPosition(
        @Path("positionId") positionId: Int
    ): BasicResponseDto

    @POST("camera/detect")
    suspend fun detectObject(): CameraDetectionDto

    @POST("camera/detect-and-move")
    suspend fun detectAndMove(): CameraDetectionDto

    @POST("assistant/chat")
    suspend fun chatWithAssistant(
        @Body request: AssistantChatRequestDto
    ): AssistantChatResponseDto

    @POST("/voice/detect")
    suspend fun detectVoice(): VoiceDetectResponse

    @POST("/voice/detect-and-move")
    suspend fun detectVoiceAndMove(): VoiceMoveResponse

    @POST("hand/safe-grip")
    suspend fun safeGrip(
        @Body request: SafeGripRequest
    ): SafeGripDto

    @POST("hand/full-grip")
    suspend fun fullGrip(
        @Body request: FullGripRequest
    ): FullGripDto
}