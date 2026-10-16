package com.yugo.app.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.yugo.app.R
import com.yugo.app.data.model.ActivityItem
import com.yugo.app.data.model.ServiceType
import com.yugo.app.data.repository.YugoRepository
import com.yugo.app.ui.theme.*

@Composable
fun ActivityScreen(
    onReorderClick: (ActivityItem) -> Unit = {}
) {
    val repository = remember { YugoRepository() }
    var activities by remember { mutableStateOf<List<ActivityItem>>(repository.sampleActivities) }
    LaunchedEffect(Unit) {
        activities = repository.fetchLiveActivities()
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Past Trips & Orders",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(activities) { activity ->
                ActivityCardRow(
                    activity = activity,
                    onReorderClick = { onReorderClick(activity) }
                )
            }
        }
    }
}

@Composable
private fun ActivityCardRow(
    activity: ActivityItem,
    onReorderClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = YugoWhite,
        border = androidx.compose.foundation.BorderStroke(1.dp, YugoDivider),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = activity.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
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

            Text(
                text = "${activity.startLocation} → ${activity.endLocation}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = YugoTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (activity.itemsSummary != null) {
                Text(
                    text = activity.itemsSummary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = YugoTextSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = YugoGreenLight
                ) {
                    Text(
                        text = activity.status.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = YugoGreen),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                OutlinedButton(
                    onClick = onReorderClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YugoBlack)
                ) {
                    Text("Reorder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
