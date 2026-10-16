package com.yugo.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.yugo.app.R
import com.yugo.app.data.model.LocationItem
import com.yugo.app.ui.components.YugoTopAppBar
import com.yugo.app.ui.components.image.LocationImageThumbnail
import com.yugo.app.ui.components.map.InteractiveMapView
import com.yugo.app.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToRideSearch: () -> Unit,
    onNavigateToFood: () -> Unit,
    onNavigateToParcel: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToActivity: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            YugoTopAppBar(
                onProfileClick = onNavigateToAccount,
                onNotificationClick = {}
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(YugoWhite)
        ) {
            // Interactive Map Background
            InteractiveMapView(
                modifier = Modifier.fillMaxSize()
            )

            // Bottom Content Sheet Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(YugoWhite)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Drag handle indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(YugoDivider)
                )

                // Service Quick Selector (4 Core Services with Product Imagery)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ServiceShortcutItem(
                        title = "Cab",
                        imageRes = R.drawable.img_cab_go,
                        isPrimary = true,
                        onClick = onNavigateToRideSearch
                    )
                    ServiceShortcutItem(
                        title = "Bike",
                        imageRes = R.drawable.img_moto,
                        isPrimary = false,
                        onClick = onNavigateToRideSearch
                    )
                    ServiceShortcutItem(
                        title = "Food",
                        imageRes = R.drawable.img_chicken_biryani,
                        isPrimary = false,
                        onClick = onNavigateToFood
                    )
                    ServiceShortcutItem(
                        title = "Parcel",
                        imageRes = R.drawable.img_service_parcel,
                        isPrimary = false,
                        onClick = onNavigateToParcel
                    )
                }

                // Prominent "Where to?" Search Input Box
                Surface(
                    onClick = onNavigateToRideSearch,
                    shape = RoundedCornerShape(12.dp),
                    color = YugoSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = YugoBlack,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Where to?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = YugoTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = CircleShape,
                            color = YugoWhite
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Now",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = YugoBlack
                                )
                            }
                        }
                    }
                }

                // Promo Banner
                com.yugo.app.ui.components.graphics.PromoBannerGraphic(
                    title = "Get 20% OFF Your First 5 Rides",
                    subtitle = "Use code YUGOWELCOME at checkout",
                    tag = "EXCLUSIVE DEAL"
                )

                // Recent Locations horizontal list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Recent destinations",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = YugoTextSecondary
                    )

                    uiState.recentLocations.take(3).forEach { location ->
                        RecentLocationRow(
                            location = location,
                            onClick = onNavigateToRideSearch
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceShortcutItem(
    title: String,
    imageRes: Int,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(YugoSurfaceVariant)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = YugoTextPrimary
        )
    }
}

@Composable
private fun RecentLocationRow(
    location: LocationItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LocationImageThumbnail(
            title = location.title,
            modifier = Modifier.size(42.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = YugoTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = location.address,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = YugoTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
