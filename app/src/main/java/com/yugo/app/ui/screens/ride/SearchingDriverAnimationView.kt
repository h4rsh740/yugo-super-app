package com.yugo.app.ui.screens.ride

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SearchingDriverAnimationView(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit = {}
) {
    var searchStep by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(1800)
        searchStep = 1
        delay(2200)
        searchStep = 2
    }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarSonar")

    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 2.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring1"
    )

    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring1Alpha"
    )

    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 2.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring2"
    )

    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring2Alpha"
    )

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = YugoWhite,
        shadowElevation = 16.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(YugoDivider)
            )

            // Animated Sonar Search Area (Compact 90dp)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding Radar Wave 1
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(ring1Scale)
                        .clip(CircleShape)
                        .background(
                            if (searchStep == 2) Color(0xFF16A34A).copy(alpha = ring1Alpha)
                            else YugoBlack.copy(alpha = ring1Alpha)
                        )
                )

                // Expanding Radar Wave 2
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .scale(ring2Scale)
                        .clip(CircleShape)
                        .background(
                            if (searchStep == 2) Color(0xFF16A34A).copy(alpha = ring2Alpha)
                            else YugoBlack.copy(alpha = ring2Alpha)
                        )
                )

                // Center Icon Badge
                Surface(
                    shape = CircleShape,
                    color = if (searchStep == 2) Color(0xFF16A34A) else YugoBlack,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (searchStep) {
                                0 -> Icons.Default.Radar
                                1 -> Icons.Default.NearMe
                                else -> Icons.Default.DirectionsCar
                            },
                            contentDescription = "Searching",
                            tint = YugoWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Search Step Indicator & Headline
            AnimatedContent(
                targetState = searchStep,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                },
                label = "SearchStatus"
            ) { step ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = when (step) {
                            0 -> "Scanning nearby drivers..."
                            1 -> "Expanding search radius to 5 km..."
                            else -> "Driver matched! Finalizing trip..."
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = YugoBlack
                        )
                    )
                    Text(
                        text = when (step) {
                            0 -> "Contacting available cabs within 2.5 km"
                            1 -> "Connecting to highest rated drivers nearby"
                            else -> "Assigning vehicle and generating Start PIN"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = YugoTextSecondary
                        )
                    )
                }
            }

            // Animated Radius Progress Bar
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (searchStep == 2) Color(0xFF16A34A) else YugoBlack,
                trackColor = YugoSurfaceVariant
            )

            // Cancel Search Button
            OutlinedButton(
                onClick = onCancelClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = YugoRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Text(
                    text = "Cancel Search",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
