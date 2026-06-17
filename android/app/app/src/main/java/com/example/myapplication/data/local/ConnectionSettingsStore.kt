package com.example.myapplication.data.local

import android.content.Context
import androidx.core.content.edit

object ConnectionSettingsStore {

    private const val PREFS_NAME = "connection_settings"
    private const val KEY_IP = "raspberry_ip"
    private const val KEY_PORT = "raspberry_port"

    const val DEFAULT_IP = "192.168.100.201"
    const val DEFAULT_PORT = "8000"

    fun getIp(context: Context): String {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_IP, DEFAULT_IP)
            ?: DEFAULT_IP
    }

    fun getPort(context: Context): String {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PORT, DEFAULT_PORT)
            ?: DEFAULT_PORT
    }

    fun getBaseUrl(context: Context): String {
        return buildBaseUrl(
            ip = getIp(context),
            port = getPort(context)
        )
    }

    fun save(
        context: Context,
        ip: String,
        port: String
    ) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_IP, cleanIp(ip))
                    .putString(KEY_PORT, cleanPort(port))
            }
    }

    fun buildBaseUrl(
        ip: String,
        port: String
    ): String {
        val cleanIp = cleanIp(ip)
        val cleanPort = cleanPort(port)

        return if (cleanPort.isBlank()) {
            "http://$cleanIp/"
        } else {
            "http://$cleanIp:$cleanPort/"
        }
    }

    private fun cleanIp(value: String): String {
        return value
            .trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/")
    }

    private fun cleanPort(value: String): String {
        return value.trim()
    }
}