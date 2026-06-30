package com.example.myapplication.data.remote.dto

data class AssistantChatRequestDto(
    val message: String,
    val provider: String,
    val api_key: String,
    val model: String?
)