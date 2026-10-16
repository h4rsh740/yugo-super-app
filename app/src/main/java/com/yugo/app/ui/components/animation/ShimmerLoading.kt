package com.yugo.app.ui.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yugo.app.ui.theme.YugoDivider
import com.yugo.app.ui.theme.YugoSurfaceVariant

fun Modifier.shimmerLoading(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerFloat"
    )

    val shimmerColors = listOf(
        YugoSurfaceVariant,
        Color(0xFFFFFFFF),
        YugoSurfaceVariant
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim.value - 200f, translateAnim.value - 200f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    this.background(brush = brush, shape = RoundedCornerShape(12.dp))
}

@Composable
fun RestaurantCardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shimmerLoading()
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .shimmerLoading()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(14.dp)
                        .shimmerLoading()
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .shimmerLoading()
        )
    }
}
