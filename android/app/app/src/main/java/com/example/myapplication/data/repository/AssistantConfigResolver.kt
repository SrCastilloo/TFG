package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.assistant.AssistantAccountEntity
import com.example.myapplication.data.local.assistant.AssistantProvider
import com.example.myapplication.data.local.assistant.SecretCipher
import com.example.myapplication.ui.auth.AuthSessionStore

class AssistantConfigResolver(
    context: Context
) {
    private val assistantAccountDao = AppDatabase
        .getInstance(context)
        .assistantAccountDao()

    suspend fun resolve(): AssistantRuntimeConfig {
        val user = AuthSessionStore.currentUser.value
            ?: throw IllegalStateException("No hay ningún usuario con sesión iniciada.")

        val account = assistantAccountDao.getAccountForUser(user.id)
            ?: throw IllegalStateException("Este usuario todavía no tiene configurado el asistente IA.")

        val provider = parseProvider(account.selectedProvider)

        val encryptedApiKey = when (provider) {
            AssistantProvider.OPENAI -> account.openAiApiKeyEncrypted
            AssistantProvider.GEMINI -> account.geminiApiKeyEncrypted
        }

        val apiKey = SecretCipher.decrypt(encryptedApiKey)

        if (apiKey.isBlank()) {
            throw IllegalStateException(
                when (provider) {
                    AssistantProvider.OPENAI ->
                        "Falta la API key de ChatGPT/OpenAI para este usuario."

                    AssistantProvider.GEMINI ->
                        "Falta la API key de Gemini para este usuario."
                }
            )
        }

        val model = when (provider) {
            AssistantProvider.OPENAI -> account.openAiModel.ifBlank { "gpt-4o-mini" }
            AssistantProvider.GEMINI -> account.geminiModel.ifBlank { "gemini-1.5-flash" }
        }

        return AssistantRuntimeConfig(
            userId = user.id,
            userName = user.displayName,
            provider = provider,
            providerLabel = provider.label,
            model = model,
            apiKey = apiKey
        )
    }

    private fun parseProvider(value: String?): AssistantProvider {
        return runCatching {
            AssistantProvider.valueOf(value ?: AssistantProvider.OPENAI.name)
        }.getOrDefault(AssistantProvider.OPENAI)
    }
}