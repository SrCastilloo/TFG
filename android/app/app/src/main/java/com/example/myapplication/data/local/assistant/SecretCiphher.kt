package com.example.myapplication.data.local.assistant


//sirve para que no se guarde la API Key en texto plano.

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object SecretCipher {

    private const val KEY_ALIAS = "tfg_assistant_api_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE_BYTES = 12
    private const val TAG_SIZE_BITS = 128

    fun encrypt(value: String): String? {
        if (value.isBlank()) return null

        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

            val iv = cipher.iv
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

            val combined = iv + encrypted

            Base64.encodeToString(
                combined,
                Base64.NO_WRAP
            )
        }.getOrNull()
    }

    fun decrypt(value: String?): String {
        if (value.isNullOrBlank()) return ""

        return runCatching {
            val combined = Base64.decode(
                value,
                Base64.NO_WRAP
            )

            val iv = combined.copyOfRange(
                0,
                IV_SIZE_BYTES
            )

            val encrypted = combined.copyOfRange(
                IV_SIZE_BYTES,
                combined.size
            )

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(TAG_SIZE_BITS, iv)
            )

            String(
                cipher.doFinal(encrypted),
                Charsets.UTF_8
            )
        }.getOrDefault("")
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existingKey = keyStore.getKey(KEY_ALIAS, null)

        if (existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)

        return keyGenerator.generateKey()
    }
}