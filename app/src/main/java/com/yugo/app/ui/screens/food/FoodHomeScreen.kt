package com.yugo.app.ui.screens.food

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import com.yugo.app.data.model.Restaurant
import com.yugo.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodHomeScreen(
    viewModel: FoodViewModel,
    onBackClick: () -> Unit,
    onRestaurantClick: (Restaurant) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val cuisineChips = listOf(
        Pair("All Cuisines", null),
        Pair("Biryani", "Biryani"),
        Pair("North Indian", "North Indian"),
        Pair("Pizza", "Pizza"),
        Pair("South Indian", "South Indian"),
        Pair("Kebabs", "Kebabs"),
        Pair("Beverages", "Beverages"),
        Pair("Desserts", "Desserts")
    )

    val visualCuisines = listOf(
        Triple("Biryani", R.drawable.img_chicken_biryani, "Biryani"),
        Triple("North Indian", R.drawable.img_paneer_masala, "North Indian"),
        Triple("Pizza", R.drawable.img_margherita_pizza, "Pizza"),
        Triple("South Indian", R.drawable.img_masala_dosa, "South Indian"),
        Triple("Kebabs", R.drawable.img_galouti_kebab, "Kebabs"),
        Triple("Beverages", R.drawable.img_filter_coffee, "Beverages")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Column {
                        Text(
                            text = "YUGO Food",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Delivering to DLF Phase 2, Gurugram",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = YugoTextSecondary
                        )
                    }
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
            // Search Input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search for 'Biryani' or 'Pizza'", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = YugoTextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = YugoSurfaceVariant,
                    focusedContainerColor = YugoSurfaceVariant,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = YugoBlack
                )
            )

            // Cuisine Category Filter Chips (High-contrast pure white text on selection)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cuisineChips) { (title, filterVal) ->
                    val isSelected = uiState.selectedCuisine == filterVal
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCuisine(if (isSelected && filterVal != null) null else filterVal) },
                        label = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                // Explicitly ensure white text on black background when selected
                                color = if (isSelected) YugoWhite else YugoTextPrimary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = YugoBlack,
                            selectedLabelColor = YugoWhite,
                            containerColor = YugoSurfaceVariant,
                            labelColor = YugoTextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) YugoBlack else YugoDivider,
                            borderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Visual Cuisine Circular Thumbnails
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(visualCuisines) { (label, imageRes, filterVal) ->
                    val isSelected = uiState.selectedCuisine == filterVal
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.selectCuisine(if (isSelected) null else filterVal) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, YugoBlue, CircleShape)
                                    else Modifier.border(1.dp, YugoDivider, CircleShape)
                                )
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = label,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) YugoBlue else YugoTextSecondary
                        )
                    }
                }
            }

            // Restaurants List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    com.yugo.app.ui.components.graphics.PromoBannerGraphic(
                        title = "50% OFF Up To ₹100",
                        subtitle = "On your first YUGO Food order",
                        tag = "SPECIAL OFFER"
                    )
                }

                item {
                    Text(
                        text = "Top Restaurants Nearby",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                items(uiState.restaurants) { restaurant ->
                    RestaurantRowCard(
                        restaurant = restaurant,
                        onClick = {
                            viewModel.selectRestaurant(restaurant)
                            onRestaurantClick(restaurant)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RestaurantRowCard(
    restaurant: Restaurant,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
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
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = if (restaurant.imageRes != 0) restaurant.imageRes else R.drawable.img_biryani),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = restaurant.cuisine,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = YugoTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = YugoGreen
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${restaurant.rating}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = YugoWhite
                                )
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = YugoWhite,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        Text(
                            text = "${restaurant.deliveryTimeMinutes} mins",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = YugoTextSecondary
                        )

                        Text(
                            text = "₹${restaurant.priceForTwo} for two",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = YugoTextSecondary
                        )
                    }
                }
            }

            if (!restaurant.offerText.isNullOrBlank()) {
                HorizontalDivider(color = YugoDivider)
                Text(
                    text = "🏷️ ${restaurant.offerText}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = YugoBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
