package com.yugo.app.ui.screens.services

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.R
import com.yugo.app.ui.theme.*

@Composable
fun ServicesScreen(
    onNavigateToRide: () -> Unit,
    onNavigateToFood: () -> Unit,
    onNavigateToParcel: () -> Unit
) {
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
                        text = "Services",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "All YUGO offerings at your fingertips",
                        style = MaterialTheme.typography.bodyMedium,
                        color = YugoTextSecondary
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(YugoWhite),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ServiceGridCard(
                    title = "YUGO Cab",
                    subtitle = "Hatchbacks & luxury sedans",
                    imageRes = R.drawable.img_service_cab,
                    icon = Icons.Default.DirectionsCar,
                    onClick = onNavigateToRide
                )
            }

            item {
                ServiceGridCard(
                    title = "YUGO Bike",
                    subtitle = "Fastest through traffic",
                    imageRes = R.drawable.img_service_bike,
                    icon = Icons.Default.TwoWheeler,
                    onClick = onNavigateToRide
                )
            }

            item {
                ServiceGridCard(
                    title = "YUGO Food",
                    subtitle = "Biryani, pizza & top restaurants",
                    imageRes = R.drawable.img_service_food,
                    icon = Icons.Default.Restaurant,
                    onClick = onNavigateToFood
                )
            }

            item {
                ServiceGridCard(
                    title = "Express Parcel",
                    subtitle = "Documents & gift deliveries",
                    imageRes = R.drawable.img_service_parcel,
                    icon = Icons.Default.LocalMall,
                    onClick = onNavigateToParcel
                )
            }
        }
    }
}

@Composable
private fun ServiceGridCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = YugoSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient scrim for contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = YugoWhite.copy(alpha = 0.9f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = YugoBlack, modifier = Modifier.size(18.dp))
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = YugoWhite,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = YugoWhite.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

