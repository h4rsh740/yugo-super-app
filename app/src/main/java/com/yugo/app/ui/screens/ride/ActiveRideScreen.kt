package com.yugo.app.ui.screens.ride

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.model.RideStatus
import com.yugo.app.ui.components.map.NavigationManeuver
import com.yugo.app.ui.components.map.ProfessionalNavigationMap
import com.yugo.app.ui.theme.*

@Composable
fun ActiveRideScreen(
    viewModel: RideBookingViewModel,
    onCancelClick: () -> Unit,
    onCompleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val driver = uiState.assignedDriver

    var currentManeuver by remember {
        mutableStateOf(
            NavigationManeuver(
                instruction = "In 200 m",
                distanceText = "200 m",
                streetName = "Turn Left onto DLF Central Blvd",
                nextStep = "Then continue 350 m to Cyber City",
                isTurnRight = false
            )
        )
    }

    var isVoiceMuted by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(YugoWhite)) {
        // 1. Real Interactive World Street Map Engine (CartoDB / OpenStreetMap)
        com.yugo.app.ui.components.map.InteractiveMapView(
            modifier = Modifier.fillMaxSize(),
            showRoute = true,
            pickupLocationName = uiState.pickupLocation.title,
            destinationName = uiState.destinationLocation?.title ?: "Cyber City Tower B",
            pickupLatitude = uiState.pickupLocation.latitude,
            pickupLongitude = uiState.pickupLocation.longitude,
            destinationLatitude = uiState.destinationLocation?.latitude,
            destinationLongitude = uiState.destinationLocation?.longitude,
            driverLatitude = driver?.latitude,
            driverLongitude = driver?.longitude
        )

        // 2. Top Turn-by-Turn GPS Navigation HUD Banner (Uber / Google Maps style)
        if (uiState.rideStatus == RideStatus.IN_PROGRESS) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF064E3B), // Deep Navigation Green
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Navigation Turn Maneuver Icon
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF047857),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (currentManeuver.isTurnRight) Icons.Default.TurnRight else Icons.Default.TurnLeft,
                                        contentDescription = "Turn Maneuver",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = currentManeuver.instruction,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = currentManeuver.streetName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFD1FAE5)
                                    ),
                                    maxLines = 1
                                )
                            }
                        }

                        // Audio Voice Guidance Toggle Button
                        IconButton(
                            onClick = { isVoiceMuted = !isVoiceMuted },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF047857))
                        ) {
                            Icon(
                                imageVector = if (isVoiceMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Voice Guidance",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF047857), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Next Maneuver Line
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color(0xFFA7F3D0),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = currentManeuver.nextStep,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFFA7F3D0),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Floating Live Trip Stat Pill (ETA & Distance)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speedometer & Speed Limit Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "38",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 16.sp),
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "km/h",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = YugoTextSecondary
                            )
                        }
                    }

                    // Speed limit badge
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDC2626)),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "50",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color(0xFF0F172A)
                                )
                            )
                        }
                    }
                }

                // ETA & Distance Remaining Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16A34A))
                        )
                        Text(
                            text = "${uiState.etaMinutesRemaining} min • 1.4 km",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }
                }
            }
        }
    } else {
            // Clean Floating Top Bar during SEARCHING / DISPATCH: Leaves 100% of upper & center screen for Map!
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = {
                        viewModel.cancelRide()
                        onCancelClick()
                    },
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    containerColor = YugoWhite,
                    contentColor = YugoBlack,
                    elevation = FloatingActionButtonDefaults.elevation(3.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = YugoWhite,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (uiState.rideStatus == RideStatus.SEARCHING) YugoBlue else Color(0xFF16A34A))
                        )
                        Text(
                            text = when (uiState.rideStatus) {
                                RideStatus.SEARCHING -> "Finding nearby driver..."
                                RideStatus.DRIVER_ASSIGNED -> "Driver arriving in ${uiState.etaMinutesRemaining}m"
                                RideStatus.DRIVER_ARRIVING -> "Driver arriving now!"
                                else -> "Active Trip"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = YugoBlack
                            )
                        )
                    }
                }
            }
        }

        // 3. Bottom Driver & Vehicle Status Overlay Card
        if (uiState.rideStatus == RideStatus.SEARCHING) {
            SearchingDriverAnimationView(
                modifier = Modifier.align(Alignment.BottomCenter),
                onCancelClick = {
                    viewModel.cancelRide()
                    onCancelClick()
                }
            )
        } else {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                color = YugoWhite,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Driver Matched Celebration Banner
                    if (uiState.rideStatus == RideStatus.DRIVER_ASSIGNED) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✨", fontSize = 16.sp)
                                Text(
                                    text = "Driver Matched! ${driver?.name ?: "Driver"} is on the way (${uiState.etaMinutesRemaining} mins away)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                )
                            }
                        }
                    }
                // Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (uiState.rideStatus) {
                                RideStatus.SEARCHING -> "Connecting to nearby driver..."
                                RideStatus.DRIVER_ASSIGNED -> "Driver is on the way"
                                RideStatus.DRIVER_ARRIVING -> "Driver arriving now"
                                RideStatus.IN_PROGRESS -> "On the way to destination"
                                else -> "Arriving shortly"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = YugoBlack
                            )
                        )
                        Text(
                            text = "${uiState.selectedOption?.name ?: "YUGO Go"} • ${uiState.destinationLocation?.title ?: "Cyber City Tower B"}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = YugoTextSecondary)
                        )
                    }

                    // Green ETA Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "${uiState.etaMinutesRemaining} MIN",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF16A34A)
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                HorizontalDivider(color = YugoDivider)

                if (driver != null) {
                    // Security PIN display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(YugoSurfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Start Ride PIN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = YugoTextPrimary
                            )
                        )
                        Text(
                            text = driver.otpPin,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = YugoBlack
                            )
                        )
                    }

                    // Driver Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        com.yugo.app.ui.components.image.DriverAvatarBadge(
                            driverName = driver.name,
                            rating = driver.rating
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = driver.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YugoTextPrimary
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = YugoAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${driver.rating} • ${driver.vehicleModel}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = YugoTextSecondary
                                )
                            }
                            Text(
                                text = driver.plateNumber,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YugoTextPrimary
                                )
                            )
                        }

                        com.yugo.app.ui.components.image.VehicleImageThumbnail(
                            category = uiState.selectedOption?.category ?: com.yugo.app.data.model.RideCategory.GO,
                            modifier = Modifier.size(54.dp)
                        )

                        // Call & Message buttons
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(YugoSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call Driver", tint = YugoBlack)
                        }
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(YugoSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = "Message", tint = YugoBlack)
                        }
                    }

                    // Safety & Cancel Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = YugoRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Safety", color = YugoRed, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.cancelRide()
                                onCancelClick()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel Ride", color = YugoTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    // Searching loading state
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = YugoBlack,
                        trackColor = YugoSurfaceVariant
                    )
                }
            }
        }
    }

        // 4. Trip Completed Modal Dialog
        if (uiState.rideStatus == RideStatus.COMPLETED || uiState.isCompleted) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.resetRide()
                    onCompleted()
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎉", fontSize = 24.sp)
                        Text("Trip Completed!", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("You have safely arrived at ${uiState.destinationLocation?.title ?: "Cyber City"}.")
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Fare", color = YugoTextSecondary)
                            Text("₹${uiState.selectedOption?.fareAmount?.toInt() ?: 160}.00", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Method", color = YugoTextSecondary)
                            Text(uiState.paymentMethod, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Driver", color = YugoTextSecondary)
                            Text(driver?.name ?: "Rajesh Sharma", fontWeight = FontWeight.Medium)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetRide()
                            onCompleted()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
                    ) {
                        Text("View in Activity")
                    }
                }
            )
        }
    }
}
