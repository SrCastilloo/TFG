package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.assistant.AssistantAccountEntity
import com.example.myapplication.data.local.assistant.AssistantProvider
import com.example.myapplication.ui.auth.AuthSessionStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AssistantAccessUiState(
    val isLoading: Boolean = true,
    val currentUserName: String = "",
    val selectedProvider: AssistantProvider = AssistantProvider.OPENAI,
    val providerLabel: String = "ChatGPT / OpenAI",
    val model: String = "",
    val canUseAssistant: Boolean = false,
    val missingReason: String = "Configura una API key para usar el asistente."
)

class AssistantAccessViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val assistantAccountDao = AppDatabase
        .getInstance(application)
        .assistantAccountDao()

    val uiState: StateFlow<AssistantAccessUiState> =
        combine(
            AuthSessionStore.currentUser,
            assistantAccountDao.observeAllAccounts()
        ) { user, accounts ->

            if (user == null) {
                return@combine AssistantAccessUiState(
                    isLoading = false,
                    canUseAssistant = false,
                    missingReason = "No hay ningún usuario con sesión iniciada."
                )
            }

            val account = accounts.firstOrNull { it.userId == user.id }

            buildAccessState(
                userName = user.displayName,
                account = account
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AssistantAccessUiState()
        )

    private fun buildAccessState(
        userName: String,
        account: AssistantAccountEntity?
    ): AssistantAccessUiState {
        if (account == null) {
            return AssistantAccessUiState(
                isLoading = false,
                currentUserName = userName,
                canUseAssistant = false,
                missingReason = "Todavía no has configurado tu asistente IA."
            )
        }

        val provider = parseProvider(account.selectedProvider)

        val hasRequiredKey = when (provider) {
            AssistantProvider.OPENAI -> !account.openAiApiKeyEncrypted.isNullOrBlank()
            AssistantProvider.GEMINI -> !account.geminiApiKeyEncrypted.isNullOrBlank()
        }

        val model = when (provider) {
            AssistantProvider.OPENAI -> account.openAiModel
            AssistantProvider.GEMINI -> account.geminiModel
        }

        val providerLabel = when (provider) {
            AssistantProvider.OPENAI -> "ChatGPT / OpenAI"
            AssistantProvider.GEMINI -> "Gemini"
        }

        return AssistantAccessUiState(
            isLoading = false,
            currentUserName = userName,
            selectedProvider = provider,
            providerLabel = providerLabel,
            model = model,
            canUseAssistant = hasRequiredKey,
            missingReason = if (hasRequiredKey) {
                ""
            } else {
                "Has elegido $providerLabel, pero falta guardar su API key."
            }
        )
    }

    private fun parseProvider(value: String?): AssistantProvider {
        return runCatching {
            AssistantProvider.valueOf(value ?: AssistantProvider.OPENAI.name)
        }.getOrDefault(AssistantProvider.OPENAI)
    }
}