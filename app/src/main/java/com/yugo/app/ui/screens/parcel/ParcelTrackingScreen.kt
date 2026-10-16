package com.yugo.app.ui.screens.parcel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.model.ParcelStatus
import com.yugo.app.ui.components.map.InteractiveMapView
import com.yugo.app.ui.theme.*

@Composable
fun ParcelTrackingScreen(
    viewModel: ParcelViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(YugoWhite)) {
        InteractiveMapView(
            modifier = Modifier.fillMaxSize(),
            showRoute = true
        )

        // Top Header Overlay
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = YugoWhite,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = YugoBlack)
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Parcel Tracking",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (uiState.trackingId != null) {
                            Surface(shape = RoundedCornerShape(4.dp), color = YugoSurfaceVariant) {
                                Text(
                                    text = uiState.trackingId!!,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "To: ${uiState.recipientName} • ${uiState.recipientAddress}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = YugoTextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        // Bottom Delivery Card
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = YugoWhite,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (uiState.parcelStatus) {
                                ParcelStatus.CREATED -> "Assigning courier nearby..."
                                ParcelStatus.COURIER_ASSIGNED -> "Courier on the way to pickup"
                                ParcelStatus.PICKED_UP -> "Package picked up from you"
                                ParcelStatus.IN_TRANSIT -> "Parcel out for delivery"
                                ParcelStatus.DELIVERED -> "Delivered Successfully!"
                                else -> "Delivery in progress"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        )
                        Text(
                            text = if (uiState.parcelStatus == ParcelStatus.DELIVERED) "Handed over to ${uiState.recipientName}" else "Guaranteed express delivery in 35 mins",
                            style = MaterialTheme.typography.bodyMedium,
                            color = YugoTextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (uiState.parcelStatus == ParcelStatus.DELIVERED) Color(0xFFDCFCE7) else YugoGreenLight
                    ) {
                        Text(
                            text = if (uiState.parcelStatus == ParcelStatus.DELIVERED) "DELIVERED" else "ON TIME",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.parcelStatus == ParcelStatus.DELIVERED) Color(0xFF16A34A) else YugoGreen
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                HorizontalDivider(color = YugoDivider)

                // Courier Details & PIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(YugoBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalMall, contentDescription = null, tint = YugoWhite)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.courierName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Delivery PIN: ${uiState.deliveryPin}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = YugoGreen)
                        )
                    }

                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(YugoSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call Courier", tint = YugoBlack)
                    }
                }
            }
        }

        // Delivered Celebration Dialog
        if (uiState.parcelStatus == ParcelStatus.DELIVERED || uiState.isDelivered) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.resetParcel()
                    onBackClick()
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                        Text("Parcel Delivered!", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Your parcel shipment has been safely delivered to ${uiState.recipientName} at ${uiState.recipientAddress}.")
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tracking ID", color = YugoTextSecondary)
                            Text(uiState.trackingId ?: "PARCEL-889123", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid", color = YugoTextSecondary)
                            Text("₹${uiState.estimatedPrice.toInt()}.00", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetParcel()
                            onBackClick()
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
