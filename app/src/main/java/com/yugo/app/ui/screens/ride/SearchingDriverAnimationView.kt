package com.yugo.app.ui.screens.ride

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.ui.theme.*

@Composable
fun SearchingDriverAnimationView(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Radar")

    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring1"
    )

    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring1Alpha"
    )

    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring2"
    )

    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring2Alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animated Pulsing Radar Rings
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(ring1Scale)
                    .clip(CircleShape)
                    .background(YugoBlack.copy(alpha = ring1Alpha))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(ring2Scale)
                    .clip(CircleShape)
                    .background(YugoBlack.copy(alpha = ring2Alpha))
            )

            // Center Vehicle Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(YugoBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Searching",
                    tint = YugoWhite,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Text(
            text = "Finding your YUGO driver...",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = YugoBlack,
            trackColor = YugoSurfaceVariant
        )
    }
}
