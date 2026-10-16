package com.yugo.app.ui.components.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.R
import com.yugo.app.data.model.RideCategory
import com.yugo.app.ui.theme.*

@Composable
fun VehicleImageThumbnail(
    category: RideCategory,
    modifier: Modifier = Modifier
) {
    val imageRes = when (category) {
        RideCategory.AUTO -> R.drawable.img_auto
        RideCategory.MOTO -> R.drawable.img_moto
        RideCategory.GO -> R.drawable.img_cab_go
        RideCategory.PREMIER -> R.drawable.img_cab_premier
        RideCategory.XL -> R.drawable.img_cab_xl
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = category.name,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun LocationImageThumbnail(
    title: String,
    modifier: Modifier = Modifier
) {
    val imageRes = when {
        title.contains("Cyber", ignoreCase = true) -> R.drawable.img_cyber_city
        title.contains("Airport", ignoreCase = true) -> R.drawable.img_airport
        else -> R.drawable.img_cyber_city
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = title,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ParcelHeaderBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_parcel_banner),
                contentDescription = "Parcel Express",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = YugoGreen
                ) {
                    Text(
                        text = "EXPRESS HYPERLOCAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = YugoWhite,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "45-Minute Doorstep Delivery",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = YugoWhite,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "Documents, gifts, clothes & essentials delivered across town",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = YugoWhite.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RestaurantHeaderBanner(
    restaurantName: String,
    cuisine: String,
    modifier: Modifier = Modifier
) {
    val imageRes = when {
        restaurantName.contains("Biryani", ignoreCase = true) -> R.drawable.img_biryani
        restaurantName.contains("Pizzeria", ignoreCase = true) -> R.drawable.img_pizza
        restaurantName.contains("South", ignoreCase = true) -> R.drawable.img_south_indian
        else -> R.drawable.img_north_indian
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = restaurantName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = YugoGreen
                ) {
                    Text(
                        text = "TOP RATED",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = YugoWhite,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = restaurantName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = YugoWhite,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = cuisine,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = YugoWhite.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun DriverAvatarBadge(
    driverName: String,
    rating: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_driver_avatar),
            contentDescription = driverName,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = YugoGreen,
            modifier = Modifier.offset(x = 4.dp, y = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = YugoWhite, modifier = Modifier.size(10.dp))
                Text(
                    text = "$rating",
                    color = YugoWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun DishItemImageThumbnail(
    dishName: String,
    imageRes: Int = 0,
    modifier: Modifier = Modifier
) {
    val resId = if (imageRes != 0) {
        imageRes
    } else {
        when {
            dishName.contains("Chicken", ignoreCase = true) && dishName.contains("Biryani", ignoreCase = true) -> R.drawable.img_chicken_biryani
            dishName.contains("Mutton", ignoreCase = true) -> R.drawable.img_mutton_biryani
            dishName.contains("Paneer", ignoreCase = true) && dishName.contains("Biryani", ignoreCase = true) -> R.drawable.img_paneer_biryani
            dishName.contains("Kebab", ignoreCase = true) -> R.drawable.img_galouti_kebab
            dishName.contains("Garlic", ignoreCase = true) && dishName.contains("Naan", ignoreCase = true) -> R.drawable.img_garlic_naan
            dishName.contains("Naan", ignoreCase = true) -> R.drawable.img_butter_naan
            dishName.contains("Dal", ignoreCase = true) -> R.drawable.img_dal_makhani
            dishName.contains("Paneer", ignoreCase = true) -> R.drawable.img_paneer_masala
            dishName.contains("Thali", ignoreCase = true) -> R.drawable.img_veg_thali
            dishName.contains("Pepperoni", ignoreCase = true) -> R.drawable.img_pepperoni_pizza
            dishName.contains("Margherita", ignoreCase = true) -> R.drawable.img_margherita_pizza
            dishName.contains("Pasta", ignoreCase = true) -> R.drawable.img_truffle_pasta
            dishName.contains("Garlic", ignoreCase = true) || dishName.contains("Sticks", ignoreCase = true) -> R.drawable.img_garlic_bread
            dishName.contains("Dosa", ignoreCase = true) -> R.drawable.img_masala_dosa
            dishName.contains("Idli", ignoreCase = true) || dishName.contains("Vada", ignoreCase = true) -> R.drawable.img_idli_vada
            dishName.contains("Coffee", ignoreCase = true) -> R.drawable.img_filter_coffee
            dishName.contains("Biryani", ignoreCase = true) -> R.drawable.img_biryani
            dishName.contains("Pizza", ignoreCase = true) -> R.drawable.img_pizza
            else -> R.drawable.img_north_indian
        }
    }

    Image(
        painter = painterResource(id = resId),
        contentDescription = dishName,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ServiceThumbnail(
    serviceType: String,
    modifier: Modifier = Modifier
) {
    val imageRes = when (serviceType.uppercase()) {
        "CAB" -> R.drawable.img_service_cab
        "BIKE", "MOTO" -> R.drawable.img_service_bike
        "FOOD" -> R.drawable.img_service_food
        "PARCEL" -> R.drawable.img_service_parcel
        else -> R.drawable.img_service_cab
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = serviceType,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}

