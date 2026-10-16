package com.yugo.app.ui.screens.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yugo.app.R
import com.yugo.app.data.local.SessionManager
import com.yugo.app.ui.theme.YugoBlack
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: (isLoggedIn: Boolean) -> Unit
) {
    val context = LocalContext.current
    var isFinished by remember { mutableStateOf(false) }

    fun finishSplash() {
        if (!isFinished) {
            isFinished = true
            val isLoggedIn = SessionManager.getInstance(context).isLoggedIn.value
            onSplashFinished(isLoggedIn)
        }
    }

    // Fallback safety timer
    LaunchedEffect(Unit) {
        delay(4000)
        finishSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YugoBlack),
        contentAlignment = Alignment.Center
    ) {
        // Fullscreen Video Player
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    val videoUri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash_animation}")
                    setVideoURI(videoUri)
                    setOnCompletionListener {
                        finishSplash()
                    }
                    setOnErrorListener { _, _, _ ->
                        finishSplash()
                        true
                    }
                    start()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
