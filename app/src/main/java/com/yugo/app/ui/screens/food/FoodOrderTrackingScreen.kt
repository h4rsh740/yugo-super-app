package com.yugo.app.ui.screens.food

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.ui.components.map.InteractiveMapView
import com.yugo.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodOrderTrackingScreen(
    viewModel: FoodViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.activeOrder
    val status = uiState.activeOrderStatus ?: order?.status ?: "CONFIRMED"

    // Map stages to index 0..4
    val stageIndex = when (status.uppercase()) {
        "CONFIRMED" -> 0
        "PREPARING" -> 1
        "PICKED_UP" -> 2
        "ARRIVING" -> 3
        "DELIVERED" -> 4
        else -> 0
    }

    Box(modifier = Modifier.fillMaxSize().background(YugoWhite)) {
        // Upper Map View showing delivery path
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.48f)
        ) {
            InteractiveMapView(
                modifier = Modifier.fillMaxSize(),
                showRoute = true,
                pickupLocationName = order?.restaurantName ?: uiState.selectedRestaurant?.name ?: "Bikanervala",
                destinationName = order?.deliveryAddress?.take(20) ?: "DLF Phase 2"
            )

            // Top Header Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = YugoWhite,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = YugoBlack)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Live Order Tracking",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (order != null) {
                                Surface(shape = RoundedCornerShape(4.dp), color = YugoSurfaceVariant) {
                                    Text(
                                        text = "#${order.orderId.takeLast(6).uppercase()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = order?.restaurantName ?: uiState.selectedRestaurant?.name ?: "Bikanervala, Sector 29",
                            style = MaterialTheme.typography.bodySmall.copy(color = YugoTextSecondary)
                        )
                    }
                }
            }
        }

        // Lower Status and Stepper Card
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.56f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = YugoWhite,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Headline Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (stageIndex) {
                                0 -> "Order Confirmed!"
                                1 -> "Kitchen Preparing Food"
                                2 -> "Delivery Partner Picked Up"
                                3 -> "Rider Arriving Soon"
                                else -> "Order Delivered!"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = YugoBlack
                            )
                        )
                        Text(
                            text = when (stageIndex) {
                                0 -> "Restaurant accepted your order"
                                1 -> "Fresh ingredients simmering in kitchen"
                                2 -> "Rider has your meal and is on the way"
                                3 -> "Reaching your doorstep in 3 mins"
                                else -> "Enjoy your meal!"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(color = YugoTextSecondary)
                        )
                    }

                    // ETA Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (stageIndex == 4) Color(0xFFDCFCE7) else YugoGreenLight
                    ) {
                        Text(
                            text = when (stageIndex) {
                                0 -> "28 MINS"
                                1 -> "22 MINS"
                                2 -> "14 MINS"
                                3 -> "4 MINS"
                                else -> "DELIVERED"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = if (stageIndex == 4) Color(0xFF16A34A) else YugoGreen
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                HorizontalDivider(color = YugoDivider)

                // Animated 5-Stage Stepper
                FoodOrderStepper(currentStage = stageIndex)

                HorizontalDivider(color = YugoDivider)

                // Delivery Partner Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = YugoSurfaceVariant,
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(YugoBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = YugoWhite, modifier = Modifier.size(24.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rahul Sharma",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            )
                            Text(
                                text = "YUGO Delivery Partner • Hero Splendor (HR 26 BY 4410)",
                                style = MaterialTheme.typography.bodySmall.copy(color = YugoTextSecondary)
                            )
                        }

                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(YugoWhite)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call Rider", tint = YugoGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Order Items Summary
                if (uiState.cartItems.isNotEmpty()) {
                    Text(
                        text = "Items in this order",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        uiState.cartItems.forEach { itm ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${itm.quantity}x ${itm.menuItem.name}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                                Text(
                                    text = "₹${(itm.menuItem.price * itm.quantity).toInt()}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = YugoDivider)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid via ${order?.paymentMethod ?: "Online"}", color = YugoTextSecondary, fontSize = 13.sp)
                        Text("Total: ₹${(order?.totalAmount ?: uiState.cartTotalAmount).toInt()}", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }
            }
        }

        // Delivered Modal Dialog
        if (stageIndex == 4 || uiState.isOrderDelivered) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.resetOrder()
                    onHomeClick()
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(26.dp))
                        Text("Order Delivered!", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Your delicious food from ${order?.restaurantName ?: uiState.selectedRestaurant?.name ?: "Bikanervala"} has arrived. We hope you enjoy your meal!")
                        HorizontalDivider()
                        Text("Delivered to: ${order?.deliveryAddress ?: "Sector 54, Gurugram"}", fontSize = 12.sp, color = YugoTextSecondary)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetOrder()
                            onHomeClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

@Composable
private fun FoodOrderStepper(currentStage: Int) {
    val stages = listOf(
        Pair("Confirmed", Icons.Default.Check),
        Pair("Preparing", Icons.Default.Restaurant),
        Pair("Picked Up", Icons.Default.TwoWheeler),
        Pair("Arriving", Icons.Default.NearMe),
        Pair("Delivered", Icons.Default.CheckCircle)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { idx, (label, icon) ->
            val isPassed = idx < currentStage
            val isCurrent = idx == currentStage
            val isUpcoming = idx > currentStage

            // Pulse animation for current stage icon
            val infiniteTransition = rememberInfiniteTransition(label = "StepPulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Scale"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .scale(if (isCurrent) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPassed -> Color(0xFF16A34A)
                                isCurrent -> YugoBlack
                                else -> YugoSurfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = when {
                            isPassed || isCurrent -> YugoWhite
                            else -> YugoTextMuted
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isCurrent) FontWeight.Black else if (isPassed) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp,
                        color = when {
                            isCurrent -> YugoBlack
                            isPassed -> Color(0xFF16A34A)
                            else -> YugoTextMuted
                        }
                    ),
                    maxLines = 1
                )
            }

            // Connector line between steps
            if (idx < stages.size - 1) {
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .weight(0.6f)
                        .background(
                            if (idx < currentStage) Color(0xFF16A34A) else YugoDivider
                        )
                )
            }
        }
    }
}
