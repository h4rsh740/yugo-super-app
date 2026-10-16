package com.yugo.app.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.model.MenuItem
import com.yugo.app.data.model.Restaurant
import com.yugo.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    viewModel: FoodViewModel,
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val restaurant = uiState.selectedRestaurant
        ?: uiState.restaurants.firstOrNull()
        ?: com.yugo.app.data.repository.YugoRepository().sampleRestaurants.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = restaurant.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = YugoBlack)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YugoWhite)
            )
        },
        bottomBar = {
            if (uiState.cartItemCount > 0) {
                Surface(
                    color = YugoWhite,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = onCheckoutClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = YugoBlack,
                                contentColor = YugoWhite
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${uiState.cartItemCount} item${if (uiState.cartItemCount > 1) "s" else ""} • ₹${uiState.cartTotalAmount.toInt()}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "View Cart →",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
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
            // Header Banner
            item {
                com.yugo.app.ui.components.image.RestaurantHeaderBanner(
                    restaurantName = restaurant.name,
                    cuisine = restaurant.cuisine
                )
            }

            item {
                Text(
                    text = "Recommended Menu",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Menu Items
            items(restaurant.menuItems) { menuItem ->
                val quantityInCart = uiState.cartItems.find { it.menuItem.id == menuItem.id }?.quantity ?: 0
                MenuItemRow(
                    item = menuItem,
                    quantityInCart = quantityInCart,
                    onAdd = { viewModel.addToCart(menuItem) },
                    onRemove = { viewModel.removeFromCart(menuItem) }
                )
                HorizontalDivider(color = YugoDivider, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    item: MenuItem,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            // Veg / Non-Veg Indicator Dot & Bestseller
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .border(1.dp, if (item.isVeg) YugoGreen else YugoRed, RoundedCornerShape(2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (item.isVeg) YugoGreen else YugoRed)
                    )
                }

                if (item.isBestseller) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = YugoAmber.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "BESTSELLER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "₹${item.price.toInt()}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = YugoTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Product Photo with Add Button
        Box(
            modifier = Modifier.width(115.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            com.yugo.app.ui.components.image.DishItemImageThumbnail(
                dishName = item.name,
                imageRes = item.imageRes,
                modifier = Modifier
                    .size(115.dp, 105.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            // Overlapping Quantity Control
            Box(
                modifier = Modifier
                    .offset(y = 12.dp)
                    .shadow(4.dp, RoundedCornerShape(8.dp))
            ) {
                if (quantityInCart == 0) {
                    Surface(
                        onClick = onAdd,
                        shape = RoundedCornerShape(8.dp),
                        color = YugoWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, YugoGreen),
                        modifier = Modifier
                            .height(34.dp)
                            .width(84.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "ADD",
                                fontWeight = FontWeight.Bold,
                                color = YugoGreen,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = YugoGreenLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, YugoGreen),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            IconButton(onClick = onRemove, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = YugoGreen, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "$quantityInCart",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = YugoGreen)
                            )
                            IconButton(onClick = onAdd, modifier = Modifier.size(26.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", tint = YugoGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

