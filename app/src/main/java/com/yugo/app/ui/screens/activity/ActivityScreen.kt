package com.yugo.app.ui.screens.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.R
import com.yugo.app.data.model.ActivityItem
import com.yugo.app.data.model.ServiceType
import com.yugo.app.data.repository.YugoRepository
import com.yugo.app.ui.components.YugoEmptyState
import com.yugo.app.ui.components.animation.OrderCardSkeleton
import com.yugo.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    onReorderClick: (ActivityItem) -> Unit = {}
) {
    val repository = remember { YugoRepository() }
    var activities by remember { mutableStateOf<List<ActivityItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedActivityForReceipt by remember { mutableStateOf<ActivityItem?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        delay(400) // Brief realistic shimmer
        activities = repository.fetchLiveActivities()
        isLoading = false
    }

    val filteredActivities = remember(activities, selectedFilter) {
        when (selectedFilter) {
            "Rides" -> activities.filter { it.serviceType == ServiceType.CAB || it.serviceType == ServiceType.BIKE }
            "Food" -> activities.filter { it.serviceType == ServiceType.FOOD }
            "Parcels" -> activities.filter { it.serviceType == ServiceType.PARCEL }
            else -> activities
        }
    }

    Scaffold(
        topBar = {
            Surface(color = YugoWhite) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Activity",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "View and track past rides, food orders, and parcels",
                        style = MaterialTheme.typography.bodyMedium,
                        color = YugoTextSecondary
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(YugoWhite),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf("All", "Rides", "Food", "Parcels")
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter,
                                    fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = YugoBlack,
                                selectedLabelColor = YugoWhite
                            )
                        )
                    }
                }
            }

            // Loading Skeleton State
            if (isLoading) {
                items(4) {
                    OrderCardSkeleton()
                }
            } else if (filteredActivities.isEmpty()) {
                // Empty State
                item {
                    YugoEmptyState(
                        title = "No $selectedFilter activities yet",
                        message = "Your completed rides, delicious meals, and delivered packages will show up here.",
                        icon = Icons.Default.ReceiptLong
                    )
                }
            } else {
                // Activities List
                items(filteredActivities, key = { it.id }) { activity ->
                    ActivityCardRow(
                        activity = activity,
                        onCardClick = { selectedActivityForReceipt = activity },
                        onReorderClick = { onReorderClick(activity) }
                    )
                }
            }
        }
    }

    // Detailed Receipt Modal Dialog
    selectedActivityForReceipt?.let { activity ->
        ReceiptDialog(
            activity = activity,
            onDismiss = { selectedActivityForReceipt = null },
            onReorder = {
                val act = activity
                selectedActivityForReceipt = null
                onReorderClick(act)
            }
        )
    }
}

@Composable
private fun ActivityCardRow(
    activity: ActivityItem,
    onCardClick: () -> Unit,
    onReorderClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = YugoWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, YugoDivider),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onCardClick)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(
                            id = if (activity.imageRes != 0) activity.imageRes else when (activity.serviceType) {
                                ServiceType.CAB -> R.drawable.img_cab_go
                                ServiceType.BIKE -> R.drawable.img_moto
                                ServiceType.FOOD -> R.drawable.img_chicken_biryani
                                ServiceType.PARCEL -> R.drawable.img_service_parcel
                            }
                        ),
                        contentDescription = activity.title,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = activity.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activity.dateFormatted,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = YugoTextSecondary
                        )
                    }
                }

                Text(
                    text = activity.priceFormatted,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            HorizontalDivider(color = YugoDivider)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, tint = YugoTextMuted, modifier = Modifier.size(14.dp))
                Text(
                    text = "${activity.startLocation} → ${activity.endLocation}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = YugoTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!activity.itemsSummary.isNullOrBlank()) {
                Text(
                    text = activity.itemsSummary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = YugoTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (activity.status.lowercase()) {
                        "delivered", "completed" -> YugoGreenLight
                        "cancelled" -> Color(0xFFFEE2E2)
                        else -> YugoSurfaceVariant
                    }
                ) {
                    Text(
                        text = activity.status.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (activity.status.lowercase()) {
                                "delivered", "completed" -> YugoGreen
                                "cancelled" -> YugoRed
                                else -> YugoBlack
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onCardClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("View Receipt", fontSize = 12.sp, color = YugoTextSecondary)
                    }

                    OutlinedButton(
                        onClick = onReorderClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = YugoBlack)
                    ) {
                        Text(
                            text = when (activity.serviceType) {
                                ServiceType.CAB, ServiceType.BIKE -> "Book Again"
                                ServiceType.FOOD -> "Reorder"
                                ServiceType.PARCEL -> "Send Again"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptDialog(
    activity: ActivityItem,
    onDismiss: () -> Unit,
    onReorder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Receipt Summary",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = YugoGreenLight
                ) {
                    Text(
                        text = activity.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = YugoGreen
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = YugoSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Booking Ref", fontSize = 11.sp, color = YugoTextMuted)
                            Text("#YG-${activity.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Service", fontSize = 11.sp, color = YugoTextMuted)
                            Text(activity.serviceType.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Text(
                    text = activity.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = activity.dateFormatted,
                    fontSize = 12.sp,
                    color = YugoTextSecondary
                )

                HorizontalDivider()

                // Route details
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(YugoGreen)
                                .align(Alignment.CenterVertically)
                        )
                        Column {
                            Text("Pickup / Start", fontSize = 10.sp, color = YugoTextMuted)
                            Text(activity.startLocation, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(YugoRed)
                                .align(Alignment.CenterVertically)
                        )
                        Column {
                            Text("Drop / Destination", fontSize = 10.sp, color = YugoTextMuted)
                            Text(activity.endLocation, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (!activity.itemsSummary.isNullOrBlank()) {
                    HorizontalDivider()
                    Text("Items Ordered", fontSize = 11.sp, color = YugoTextMuted)
                    Text(activity.itemsSummary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                HorizontalDivider()

                // Bill Breakdown
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Fare / Items", fontSize = 12.sp, color = YugoTextSecondary)
                        Text(activity.priceFormatted, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Platform Convenience Fee", fontSize = 12.sp, color = YugoTextSecondary)
                        Text("₹5.00", fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Taxes & GST (5%)", fontSize = 12.sp, color = YugoTextSecondary)
                        Text("Included", fontSize = 12.sp, color = YugoGreen)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount Paid", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(activity.priceFormatted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(
                        text = "Paid via Safe Test Mode (YUGO Cash / UPI)",
                        fontSize = 11.sp,
                        color = YugoTextMuted
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onReorder,
                colors = ButtonDefaults.buttonColors(containerColor = YugoBlack)
            ) {
                Text(
                    text = when (activity.serviceType) {
                        ServiceType.CAB, ServiceType.BIKE -> "Book Again"
                        ServiceType.FOOD -> "Reorder"
                        ServiceType.PARCEL -> "Send Again"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = YugoTextSecondary)
            }
        }
    )
}
