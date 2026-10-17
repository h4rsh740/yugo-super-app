package com.yugo.app.ui.screens.ride

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.model.RideCategory
import com.yugo.app.data.model.RideOption
import com.yugo.app.ui.components.map.InteractiveMapView
import com.yugo.app.ui.theme.*

@Composable
fun ChooseRideScreen(
    viewModel: RideBookingViewModel,
    onBackClick: () -> Unit,
    onConfirmBooking: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPaymentSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(YugoWhite)) {
        // Map showing route
        InteractiveMapView(
            modifier = Modifier.fillMaxSize(),
            showRoute = true,
            pickupLocationName = uiState.pickupLocation.title,
            destinationName = uiState.destinationLocation?.title ?: "Destination",
            pickupLatitude = uiState.pickupLocation.latitude,
            pickupLongitude = uiState.pickupLocation.longitude,
            destinationLatitude = uiState.destinationLocation?.latitude,
            destinationLongitude = uiState.destinationLocation?.longitude
        )

        // Floating Back Button
        FloatingActionButton(
            onClick = onBackClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .size(40.dp),
            shape = CircleShape,
            containerColor = YugoWhite,
            contentColor = YugoBlack,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        // Bottom Ride Selection Sheet
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(YugoDivider)
                )

                Text(
                    text = "Choose a ride",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Ride options list
                if (uiState.rideOptions.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.yugo.app.ui.components.animation.VehicleChoiceSkeleton()
                        com.yugo.app.ui.components.animation.VehicleChoiceSkeleton()
                        com.yugo.app.ui.components.animation.VehicleChoiceSkeleton()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.rideOptions) { option ->
                            val isSelected = uiState.selectedOption?.id == option.id
                            RideOptionRow(
                                option = option,
                                isSelected = isSelected,
                                onSelect = { viewModel.selectRideOption(option) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = YugoDivider)

                // Payment Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showPaymentSheet = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = YugoBlack)
                        Text(
                            text = uiState.paymentMethod,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = YugoTextSecondary)
                }

                // Confirm Ride Button
                Button(
                    onClick = {
                        viewModel.confirmBooking()
                        onConfirmBooking()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = YugoBlack,
                        contentColor = YugoWhite
                    )
                ) {
                    Text(
                        text = "Confirm ${uiState.selectedOption?.name ?: "Ride"}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Animated Payment Selection Bottom Sheet
        if (showPaymentSheet) {
            com.yugo.app.ui.components.payment.PaymentSelectionBottomSheet(
                amount = uiState.selectedOption?.fareAmount ?: 120.0,
                selectedMethod = uiState.paymentMethod,
                onMethodSelected = { method ->
                    viewModel.setPaymentMethod(method)
                },
                onDismissRequest = { showPaymentSheet = false }
            )
        }
    }
}

@Composable
private fun RideOptionRow(
    option: RideOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) androidx.compose.ui.graphics.Color(0xFFEFF6FF) else YugoWhite,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, YugoBlue) else androidx.compose.foundation.BorderStroke(1.dp, YugoDivider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vehicle Photo Thumbnail
            com.yugo.app.ui.components.image.VehicleImageThumbnail(
                category = option.category,
                modifier = Modifier.size(56.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = YugoTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${option.capacity}",
                            style = MaterialTheme.typography.labelMedium,
                            color = YugoTextSecondary
                        )
                    }
                }
                Text(
                    text = "${option.etaMinutes} min away • ${option.description}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = YugoTextSecondary
                )
            }

            Text(
                text = "₹${option.fareAmount.toInt()}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
