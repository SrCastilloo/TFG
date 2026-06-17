package com.example.myapplication.data.remote

import android.content.Context
import com.example.myapplication.data.local.ConnectionSettingsStore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private var appContext: Context? = null
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String = "http://${ConnectionSettingsStore.DEFAULT_IP}:${ConnectionSettingsStore.DEFAULT_PORT}/"

    fun init(context: Context) {
        appContext = context.applicationContext
        currentBaseUrl = ConnectionSettingsStore.getBaseUrl(context.applicationContext)
        rebuildRetrofit()
    }

    val baseUrl: String
        get() = currentBaseUrl

    val apiService: TfgApiService
        get() {
            if (retrofit == null) {
                val context = appContext

                currentBaseUrl = if (context != null) {
                    ConnectionSettingsStore.getBaseUrl(context)
                } else {
                    "http://${ConnectionSettingsStore.DEFAULT_IP}:${ConnectionSettingsStore.DEFAULT_PORT}/"
                }

                rebuildRetrofit()
            }

            return retrofit!!.create(TfgApiService::class.java)
        }

    fun updateConnection(
        context: Context,
        ip: String,
        port: String
    ) {
        ConnectionSettingsStore.save(
            context = context.applicationContext,
            ip = ip,
            port = port
        )

        currentBaseUrl = ConnectionSettingsStore.getBaseUrl(context.applicationContext)

        rebuildRetrofit()
    }

    private fun rebuildRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}