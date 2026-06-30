package com.example.myapplication.data.local.auth

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_users",
    indices = [
        Index(value = ["username"], unique = true)
    ]
)
data class AppUserEntity(
    @PrimaryKey
    val id: String,

    val username: String,
    val displayName: String,

    val passwordHash: String,
    val passwordSalt: String,

    // "ADMIN" o "USER"
    val role: String,

    val createdAtMillis: Long
)