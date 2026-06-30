package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.auth.AppUserEntity
import com.example.myapplication.data.local.auth.AuthSessionEntity
import com.example.myapplication.data.local.local.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

data class AuthUserUi(
    val id: String,
    val username: String,
    val displayName: String,
    val role: String
) {
    val isAdmin: Boolean
        get() = role == "ADMIN"
}

data class AuthUiState(
    val currentUser: AuthUserUi? = null,

    val username: String = "",
    val displayName: String = "",
    val password: String = "",

    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authDao = AppDatabase
        .getInstance(application)
        .authDao()

    private val sessionReady = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            authDao.clearSession()
            sessionReady.value = true
        }
    }

    private val formState = MutableStateFlow(AuthUiState())

    val uiState: StateFlow<AuthUiState> =
        combine(
            authDao.observeCurrentUser(),
            formState,
            sessionReady
        ) { currentUser, form, ready ->
            form.copy(
                currentUser = if (ready) {
                    currentUser?.toUi()
                } else {
                    null
                }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthUiState()
        )



    fun setUsername(value: String) {
        formState.value = formState.value.copy(
            username = value,
            error = null,
            message = null
        )
    }

    fun setDisplayName(value: String) {
        formState.value = formState.value.copy(
            displayName = value,
            error = null,
            message = null
        )
    }

    fun setPassword(value: String) {
        formState.value = formState.value.copy(
            password = value,
            error = null,
            message = null
        )
    }

    fun toggleMode() {
        formState.value = formState.value.copy(
            isRegisterMode = !formState.value.isRegisterMode,
            error = null,
            message = null
        )
    }

    fun login() {
        val username = normalizeUsername(formState.value.username)
        val password = formState.value.password

        if (username.isBlank() || password.isBlank()) {
            formState.value = formState.value.copy(
                error = "Introduce usuario y contraseña."
            )
            return
        }

        viewModelScope.launch {
            formState.value = formState.value.copy(
                isLoading = true,
                error = null,
                message = null
            )

            try {
                val user = authDao.getUserByUsername(username)

                if (user == null) {
                    formState.value = formState.value.copy(
                        isLoading = false,
                        error = "No existe ningún usuario con ese nombre."
                    )
                    return@launch
                }

                val validPassword = PasswordHasher.verifyPassword(
                    password = password,
                    saltBase64 = user.passwordSalt,
                    expectedHashBase64 = user.passwordHash
                )

                if (!validPassword) {
                    formState.value = formState.value.copy(
                        isLoading = false,
                        error = "La contraseña no es correcta."
                    )
                    return@launch
                }

                authDao.saveSession(
                    AuthSessionEntity(
                        userId = user.id,
                        loggedAtMillis = System.currentTimeMillis()
                    )
                )

                formState.value = formState.value.copy(
                    username = "",
                    displayName = "",
                    password = "",
                    isLoading = false,
                    error = null,
                    message = "Sesión iniciada correctamente."
                )
            } catch (e: Exception) {
                formState.value = formState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error iniciando sesión."
                )
            }
        }
    }

    fun register() {
        val username = normalizeUsername(formState.value.username)
        val displayName = formState.value.displayName.trim()
        val password = formState.value.password

        if (username.length < 3) {
            formState.value = formState.value.copy(
                error = "El usuario debe tener al menos 3 caracteres."
            )
            return
        }

        if (password.length < 4) {
            formState.value = formState.value.copy(
                error = "La contraseña debe tener al menos 4 caracteres."
            )
            return
        }

        viewModelScope.launch {
            formState.value = formState.value.copy(
                isLoading = true,
                error = null,
                message = null
            )

            try {
                val existingUser = authDao.getUserByUsername(username)

                if (existingUser != null) {
                    formState.value = formState.value.copy(
                        isLoading = false,
                        error = "Ese nombre de usuario ya existe."
                    )
                    return@launch
                }

                val usersCount = authDao.countUsers()
                val role = if (usersCount == 0) "ADMIN" else "USER"

                val salt = PasswordHasher.generateSalt()
                val hash = PasswordHasher.hashPassword(
                    password = password,
                    saltBase64 = salt
                )

                val newUser = AppUserEntity(
                    id = UUID.randomUUID().toString(),
                    username = username,
                    displayName = displayName.ifBlank { username },
                    passwordHash = hash,
                    passwordSalt = salt,
                    role = role,
                    createdAtMillis = System.currentTimeMillis()
                )

                authDao.insertUser(newUser)

                authDao.saveSession(
                    AuthSessionEntity(
                        userId = newUser.id,
                        loggedAtMillis = System.currentTimeMillis()
                    )
                )

                formState.value = formState.value.copy(
                    username = "",
                    displayName = "",
                    password = "",
                    isLoading = false,
                    error = null,
                    message = "Cuenta creada correctamente."
                )
            } catch (e: Exception) {
                formState.value = formState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error creando la cuenta."
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authDao.clearSession()

            formState.value = AuthUiState(
                message = "Sesión cerrada."
            )
        }
    }

    private fun normalizeUsername(value: String): String {
        return value
            .trim()
            .lowercase(Locale.getDefault())
    }

    private fun AppUserEntity.toUi(): AuthUserUi {
        return AuthUserUi(
            id = id,
            username = username,
            displayName = displayName,
            role = role
        )
    }
}