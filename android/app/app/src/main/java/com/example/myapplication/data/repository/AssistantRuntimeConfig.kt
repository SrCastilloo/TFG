package com.example.myapplication.data.repository

import com.example.myapplication.data.local.assistant.AssistantProvider

data class AssistantRuntimeConfig(
    val userId: String,
    val userName: String,
    val provider: AssistantProvider,
    val providerLabel: String,
    val model: String,
    val apiKey: String
)