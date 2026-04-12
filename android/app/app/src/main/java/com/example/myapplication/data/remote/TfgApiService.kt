package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.dto.BasicResponseDto
import com.example.myapplication.data.remote.dto.HandPositionsDto
import com.example.myapplication.data.remote.dto.SystemInfoDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.example.myapplication.data.remote.dto.CameraDetectionDto

interface TfgApiService {

    @GET("system/info")
    suspend fun getSystemInfo(): SystemInfoDto

    @GET("hand/positions")
    suspend fun getHandPositions(): HandPositionsDto

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
}