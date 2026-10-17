package com.yugo.app.ui.screens.auth

import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.yugo.app.R
import kotlinx.coroutines.delay

@Composable
fun AuthLoadingVideoScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var hasFinished by remember { mutableStateOf(false) }

    fun triggerFinish() {
        if (!hasFinished) {
            hasFinished = true
            onFinished()
        }
    }

    // Fallback safety timer matching video duration (~5 seconds)
    LaunchedEffect(Unit) {
        delay(5200)
        triggerFinish()
    }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    videoViewRef = this
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val uri = Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash_animation}")
                    setVideoURI(uri)

                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        try {
                            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                        } catch (_: Exception) {}
                        start()
                    }

                    setOnCompletionListener {
                        triggerFinish()
                    }

                    setOnErrorListener { _, _, _ ->
                        triggerFinish()
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
