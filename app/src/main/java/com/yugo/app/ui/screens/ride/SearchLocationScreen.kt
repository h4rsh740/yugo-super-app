package com.yugo.app.ui.screens.ride

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.model.LocationItem
import com.yugo.app.data.model.LocationType
import com.yugo.app.data.repository.YugoRepository
import com.yugo.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLocationScreen(
    repository: YugoRepository = remember { YugoRepository() },
    onBackClick: () -> Unit,
    onLocationSelected: (LocationItem) -> Unit
) {
    var pickupText by remember { mutableStateOf("Sector 54, Golf Course Road, Gurugram") }
    var destinationText by remember { mutableStateOf("") }
    var hasLocationPermission by remember { mutableStateOf(true) }
    var showPermissionBanner by remember { mutableStateOf(false) }

    val recentLocations by repository.cachedLocations.collectAsState()

    LaunchedEffect(Unit) {
        repository.fetchLocations()
    }

    val filteredLocations = remember(destinationText, recentLocations) {
        if (destinationText.isBlank()) {
            recentLocations
        } else {
            recentLocations.filter {
                it.title.contains(destinationText, ignoreCase = true) ||
                        it.address.contains(destinationText, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Where to?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = YugoBlack)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YugoWhite)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(YugoWhite)
        ) {
            // Location Permission Explainer Banner
            if (showPermissionBanner) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = YugoBlue, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Location Access", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = YugoBlue)
                            Text("Used for exact doorstep pickup point and nearest driver matching.", fontSize = 11.sp, color = YugoTextSecondary)
                        }
                        TextButton(onClick = { showPermissionBanner = false }) {
                            Text("Got it", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = YugoBlue)
                        }
                    }
                }
            }

            // Address Inputs Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = YugoSurfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pickup Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(YugoGreen)
                        )
                        OutlinedTextField(
                            value = pickupText,
                            onValueChange = { pickupText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Pickup Location", fontSize = 14.sp) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    pickupText = "Current Location (Sector 54, Gurugram)"
                                    showPermissionBanner = true
                                }) {
                                    Icon(Icons.Default.MyLocation, contentDescription = "Use GPS", tint = YugoGreen, modifier = Modifier.size(18.dp))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                focusedContainerColor = YugoWhite,
                                unfocusedContainerColor = YugoWhite
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    HorizontalDivider(color = YugoDivider)

                    // Destination Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(YugoBlack)
                        )
                        OutlinedTextField(
                            value = destinationText,
                            onValueChange = { destinationText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Where to?", fontSize = 14.sp) },
                            singleLine = true,
                            trailingIcon = {
                                if (destinationText.isNotEmpty()) {
                                    IconButton(onClick = { destinationText = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = YugoTextSecondary)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                focusedContainerColor = YugoWhite,
                                unfocusedContainerColor = YugoWhite
                            ),
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                        )
                    }
                }
            }

            // Saved Quick Chips (Home & Work)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AssistChip(
                        onClick = {
                            val home = recentLocations.firstOrNull { it.type == LocationType.HOME } ?: recentLocations.first()
                            onLocationSelected(home)
                        },
                        label = { Text("Home", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            val work = recentLocations.firstOrNull { it.type == LocationType.WORK } ?: recentLocations.first()
                            onLocationSelected(work)
                        },
                        label = { Text("Cyber City Work", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            // Suggestions / History
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(
                        text = if (destinationText.isBlank()) "Recent & Saved Destinations" else "Matching Destinations",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = YugoTextSecondary,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                items(filteredLocations) { location ->
                    LocationSearchRow(
                        location = location,
                        onClick = { onLocationSelected(location) }
                    )
                    HorizontalDivider(color = YugoDivider)
                }
            }
        }
    }
}

@Composable
private fun LocationSearchRow(
    location: LocationItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        com.yugo.app.ui.components.image.LocationImageThumbnail(
            title = location.title,
            modifier = Modifier.size(42.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
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
