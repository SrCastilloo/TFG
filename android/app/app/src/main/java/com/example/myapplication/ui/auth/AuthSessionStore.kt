package com.example.myapplication.ui.auth

import android.content.Context
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.auth.AppUserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionUser(
    val id: String,
    val username: String,
    val displayName: String,
    val role: String
) {
    val isAdmin: Boolean
        get() = role == "ADMIN"
}

object AuthSessionStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentUser = MutableStateFlow<SessionUser?>(null)
    val currentUser: StateFlow<SessionUser?> = _currentUser.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        val authDao = AppDatabase
            .getInstance(context)
            .authDao()

        scope.launch {
            authDao.observeCurrentUser().collect { user ->
                _currentUser.value = user?.toSessionUser()
            }
        }
    }

    fun currentUserId(): String? {
        return _currentUser.value?.id
    }

    fun currentUserIsAdmin(): Boolean {
        return _currentUser.value?.isAdmin == true
    }
}

private fun AppUserEntity.toSessionUser(): SessionUser {
    return SessionUser(
        id = id,
        username = username,
        displayName = displayName,
        role = role
    )
}