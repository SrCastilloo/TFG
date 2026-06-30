package com.example.myapplication.data.local.local

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        return Base64.encodeToString(
            salt,
            Base64.NO_WRAP
        )
    }

    fun hashPassword(
        password: String,
        saltBase64: String
    ): String {
        val salt = Base64.decode(
            saltBase64,
            Base64.NO_WRAP
        )

        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERATIONS,
            KEY_LENGTH
        )

        val factory = runCatching {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        }.getOrElse {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        }

        val hash = factory.generateSecret(spec).encoded

        return Base64.encodeToString(
            hash,
            Base64.NO_WRAP
        )
    }

    fun verifyPassword(
        password: String,
        saltBase64: String,
        expectedHashBase64: String
    ): Boolean {
        val calculatedHash = hashPassword(
            password = password,
            saltBase64 = saltBase64
        )

        return calculatedHash == expectedHashBase64
    }
}