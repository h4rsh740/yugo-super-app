package com.yugo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yugo.app.ui.navigation.YugoApp
import com.yugo.app.ui.theme.YugoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.yugo.app.data.api.ApiClient.initialize(this)
        enableEdgeToEdge()
        setContent {
            YugoTheme {
                YugoApp()
            }
        }
    }
}
