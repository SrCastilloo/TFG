package com.example.myapplication.data.local.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_session")
data class AuthSessionEntity(
    @PrimaryKey
    val id: String = "current",
    val userId: String,
    val loggedAtMillis: Long
)