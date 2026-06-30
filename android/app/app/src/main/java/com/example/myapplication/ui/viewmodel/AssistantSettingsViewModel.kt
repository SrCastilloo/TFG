package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.assistant.AssistantAccountEntity
import com.example.myapplication.data.local.assistant.AssistantProvider
import com.example.myapplication.data.local.assistant.SecretCipher
import com.example.myapplication.ui.auth.AuthSessionStore
import com.example.myapplication.ui.auth.SessionUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AssistantSettingsUiState(
    val currentUserName: String = "",
    val selectedProvider: AssistantProvider = AssistantProvider.OPENAI,

    val openAiApiKeyInput: String = "",
    val geminiApiKeyInput: String = "",

    val openAiModel: String = "gpt-4o-mini",
    val geminiModel: String = "gemini-1.5-flash",

    val hasSavedOpenAiKey: Boolean = false,
    val hasSavedGeminiKey: Boolean = false,

    val showOpenAiKey: Boolean = false,
    val showGeminiKey: Boolean = false,

    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class AssistantSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val assistantAccountDao = AppDatabase
        .getInstance(application)
        .assistantAccountDao()

    private val _uiState = MutableStateFlow(AssistantSettingsUiState())
    val uiState: StateFlow<AssistantSettingsUiState> = _uiState.asStateFlow()

    private var currentUser: SessionUser? = null
    private var loadedAccount: AssistantAccountEntity? = null

    init {
        observeCurrentUserSettings()
    }

    private fun observeCurrentUserSettings() {
        viewModelScope.launch {
            combine(
                AuthSessionStore.currentUser,
                assistantAccountDao.observeAllAccounts()
            ) { user, accounts ->
                val account = user?.let { activeUser ->
                    accounts.firstOrNull { it.userId == activeUser.id }
                }

                user to account
            }.collect { pair ->
                val user = pair.first
                val account = pair.second

                currentUser = user
                loadedAccount = account

                val provider = parseProvider(account?.selectedProvider)

                _uiState.value = _uiState.value.copy(
                    currentUserName = user?.displayName.orEmpty(),
                    selectedProvider = provider,
                    openAiApiKeyInput = "",
                    geminiApiKeyInput = "",
                    openAiModel = account?.openAiModel ?: "gpt-4o-mini",
                    geminiModel = account?.geminiModel ?: "gemini-1.5-flash",
                    hasSavedOpenAiKey = !account?.openAiApiKeyEncrypted.isNullOrBlank(),
                    hasSavedGeminiKey = !account?.geminiApiKeyEncrypted.isNullOrBlank(),
                    error = null
                )
            }
        }
    }

    fun selectProvider(provider: AssistantProvider) {
        _uiState.value = _uiState.value.copy(
            selectedProvider = provider,
            error = null,
            message = null
        )
    }

    fun setOpenAiApiKey(value: String) {
        _uiState.value = _uiState.value.copy(
            openAiApiKeyInput = value,
            error = null,
            message = null
        )
    }

    fun setGeminiApiKey(value: String) {
        _uiState.value = _uiState.value.copy(
            geminiApiKeyInput = value,
            error = null,
            message = null
        )
    }

    fun setOpenAiModel(value: String) {
        _uiState.value = _uiState.value.copy(
            openAiModel = value,
            error = null,
            message = null
        )
    }

    fun setGeminiModel(value: String) {
        _uiState.value = _uiState.value.copy(
            geminiModel = value,
            error = null,
            message = null
        )
    }

    fun toggleShowOpenAiKey() {
        _uiState.value = _uiState.value.copy(
            showOpenAiKey = !_uiState.value.showOpenAiKey
        )
    }

    fun toggleShowGeminiKey() {
        _uiState.value = _uiState.value.copy(
            showGeminiKey = !_uiState.value.showGeminiKey
        )
    }

    fun saveSettings() {
        val user = currentUser

        if (user == null) {
            _uiState.value = _uiState.value.copy(
                error = "No hay ningún usuario con sesión iniciada."
            )
            return
        }

        val state = _uiState.value
        val previousAccount = loadedAccount

        val cleanOpenAiModel = state.openAiModel.trim().ifBlank { "gpt-4o-mini" }
        val cleanGeminiModel = state.geminiModel.trim().ifBlank { "gemini-1.5-flash" }

        viewModelScope.launch {
            _uiState.value = state.copy(
                isLoading = true,
                error = null,
                message = null
            )

            try {
                val openAiEncrypted = resolveEncryptedKey(
                    newInput = state.openAiApiKeyInput,
                    previousEncrypted = previousAccount?.openAiApiKeyEncrypted
                )

                val geminiEncrypted = resolveEncryptedKey(
                    newInput = state.geminiApiKeyInput,
                    previousEncrypted = previousAccount?.geminiApiKeyEncrypted
                )

                val selectedProviderHasKey = when (state.selectedProvider) {
                    AssistantProvider.OPENAI -> !openAiEncrypted.isNullOrBlank()
                    AssistantProvider.GEMINI -> !geminiEncrypted.isNullOrBlank()
                }

                if (!selectedProviderHasKey) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = when (state.selectedProvider) {
                            AssistantProvider.OPENAI ->
                                "Para usar ChatGPT/OpenAI tienes que pegar una API key de OpenAI."

                            AssistantProvider.GEMINI ->
                                "Para usar Gemini tienes que pegar una API key de Gemini."
                        }
                    )
                    return@launch
                }

                assistantAccountDao.saveAccount(
                    AssistantAccountEntity(
                        userId = user.id,
                        selectedProvider = state.selectedProvider.name,
                        openAiApiKeyEncrypted = openAiEncrypted,
                        geminiApiKeyEncrypted = geminiEncrypted,
                        openAiModel = cleanOpenAiModel,
                        geminiModel = cleanGeminiModel,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    openAiApiKeyInput = "",
                    geminiApiKeyInput = "",
                    openAiModel = cleanOpenAiModel,
                    geminiModel = cleanGeminiModel,
                    hasSavedOpenAiKey = !openAiEncrypted.isNullOrBlank(),
                    hasSavedGeminiKey = !geminiEncrypted.isNullOrBlank(),
                    message = "Configuración guardada correctamente."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "No se pudo guardar la configuración."
                )
            }
        }
    }

    private fun resolveEncryptedKey(
        newInput: String,
        previousEncrypted: String?
    ): String? {
        val cleanInput = newInput.trim()

        if (cleanInput.isBlank()) {
            return previousEncrypted
        }

        return SecretCipher.encrypt(cleanInput)
    }

    private fun parseProvider(value: String?): AssistantProvider {
        return runCatching {
            AssistantProvider.valueOf(value ?: AssistantProvider.OPENAI.name)
        }.getOrDefault(AssistantProvider.OPENAI)
    }
}