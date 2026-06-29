package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.data.remote.ApiClient
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.history.ActionHistoryStore
import com.example.myapplication.ui.settings.GripSettingsStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(applicationContext)
        ActionHistoryStore.init(applicationContext)
        GripSettingsStore.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}