package com.yugo.app.ui.components.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import com.yugo.app.ui.theme.YugoSurfaceVariant

fun Modifier.shimmerLoading(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "Shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
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
        start = Offset(translateAnim.value, translateAnim.value * 0.5f),
        end = Offset(translateAnim.value + 300f, (translateAnim.value + 300f) * 0.5f)
    )

    this.background(brush = brush, shape = shape)
}

@Composable
fun RestaurantCardSkeleton() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerLoading(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(18.dp)
                            .shimmerLoading(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(14.dp)
                            .shimmerLoading(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(16.dp)
                                .shimmerLoading(RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                                .shimmerLoading(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .shimmerLoading(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun VehicleChoiceSkeleton() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .shimmerLoading(RoundedCornerShape(10.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp)
                        .shimmerLoading(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(12.dp)
                        .shimmerLoading(RoundedCornerShape(4.dp))
                )
            }
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(20.dp)
                    .shimmerLoading(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun OrderCardSkeleton() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .shimmerLoading(RoundedCornerShape(10.dp))
                    )
                    Column {
                        Box(modifier = Modifier.width(130.dp).height(16.dp).shimmerLoading(RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.width(80.dp).height(12.dp).shimmerLoading(RoundedCornerShape(4.dp)))
                    }
                }
                Box(modifier = Modifier.width(55.dp).height(18.dp).shimmerLoading(RoundedCornerShape(4.dp)))
            }
            Box(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp).shimmerLoading(RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
fun MapSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .shimmerLoading(RoundedCornerShape(0.dp))
    )
}
