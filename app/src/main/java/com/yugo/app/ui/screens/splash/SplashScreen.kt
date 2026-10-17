package com.yugo.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.local.SessionManager
import com.yugo.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: (isLoggedIn: Boolean) -> Unit
) {
    val context = LocalContext.current
    val isLoggedIn by SessionManager.getInstance(context).isLoggedIn.collectAsState()

    if (isLoggedIn) {
        // Once the user is onboarded, opening the app shows the video animation, then opens dashboard
        com.yugo.app.ui.screens.auth.AuthLoadingVideoScreen(
            onFinished = { onSplashFinished(true) }
        )
        return
    }

    var isFinished by remember { mutableStateOf(false) }

    fun finishSplash() {
        if (!isFinished) {
            isFinished = true
            onSplashFinished(false)
        }
    }

    // Motion states
    var startAnimation by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ContentAlpha"
    )

    // Infinite gentle breathing pulse
    val infiniteTransition = rememberInfiniteTransition(label = "Glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2600)
        finishSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090D16),
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Ambient Aura Glow behind Logo
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x403B82F6),
                                    Color(0x153B82F6),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // YUGO Logo Tile
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = YugoWhite,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .size(92.dp)
                        .scale(logoScale)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Y",
                            color = YugoBlack,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 54.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // YUGO Brand Wordmark
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha)
            ) {
                Text(
                    text = "YUGO",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp,
                        letterSpacing = 6.sp,
                        color = YugoWhite
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "INDIA'S ALL-IN-ONE SUPER APP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Service Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SplashServiceChip("🚕 Rides")
                    Text("•", color = Color(0xFF475569))
                    SplashServiceChip("🍛 Food")
                    Text("•", color = Color(0xFF475569))
                    SplashServiceChip("📦 Parcel")
                }
            }
        }

        // Bottom Animated Progress Indicator
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF1E293B))
            ) {
                val progressAnim by infiniteTransition.animateFloat(
                    initialValue = -120f,
                    targetValue = 120f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "Progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(50.dp)
                        .offset(x = progressAnim.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF60A5FA), Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun SplashServiceChip(label: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1E293B)
    ) {
        Text(
            text = label,
            color = Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
