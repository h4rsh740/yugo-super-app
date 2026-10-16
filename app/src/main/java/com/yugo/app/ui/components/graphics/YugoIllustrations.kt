package com.yugo.app.ui.components.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.data.model.RideCategory
import com.yugo.app.ui.theme.*

@Composable
fun VehicleVectorGraphic(
    category: RideCategory,
    modifier: Modifier = Modifier,
    tint: Color = YugoBlack
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (category) {
            RideCategory.AUTO -> {
                // Auto Rickshaw Vector Drawing
                val bodyPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.7f)
                    lineTo(w * 0.15f, h * 0.45f)
                    lineTo(w * 0.4f, h * 0.25f)
                    lineTo(w * 0.75f, h * 0.25f)
                    lineTo(w * 0.85f, h * 0.45f)
                    lineTo(w * 0.85f, h * 0.7f)
                    close()
                }
                drawPath(bodyPath, tint)
                // Yellow Canopy Top Accent
                drawRoundRect(
                    color = YugoAmber,
                    topLeft = Offset(w * 0.35f, h * 0.22f),
                    size = Size(w * 0.45f, h * 0.12f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Wheels
                drawCircle(color = YugoBlack, radius = w * 0.12f, center = Offset(w * 0.3f, h * 0.75f))
                drawCircle(color = YugoWhite, radius = w * 0.05f, center = Offset(w * 0.3f, h * 0.75f))
                drawCircle(color = YugoBlack, radius = w * 0.12f, center = Offset(w * 0.7f, h * 0.75f))
                drawCircle(color = YugoWhite, radius = w * 0.05f, center = Offset(w * 0.7f, h * 0.75f))
            }
            RideCategory.MOTO -> {
                // Motorcycle Vector Drawing
                drawCircle(color = tint, radius = w * 0.16f, center = Offset(w * 0.25f, h * 0.65f))
                drawCircle(color = YugoWhite, radius = w * 0.07f, center = Offset(w * 0.25f, h * 0.65f))
                drawCircle(color = tint, radius = w * 0.16f, center = Offset(w * 0.75f, h * 0.65f))
                drawCircle(color = YugoWhite, radius = w * 0.07f, center = Offset(w * 0.75f, h * 0.65f))

                // Frame line
                drawLine(tint, Offset(w * 0.25f, h * 0.65f), Offset(w * 0.45f, h * 0.4f), strokeWidth = 8f)
                drawLine(tint, Offset(w * 0.45f, h * 0.4f), Offset(w * 0.75f, h * 0.65f), strokeWidth = 8f)
                // Fuel tank
                drawRoundRect(tint, Offset(w * 0.38f, h * 0.35f), Size(w * 0.3f, h * 0.18f), CornerRadius(8f, 8f))
            }
            RideCategory.GO -> {
                // Hatchback Car
                drawRoundRect(tint, Offset(w * 0.1f, h * 0.45f), Size(w * 0.8f, h * 0.28f), CornerRadius(12f, 12f))
                drawRoundRect(tint, Offset(w * 0.25f, h * 0.25f), Size(w * 0.48f, h * 0.25f), CornerRadius(10f, 10f))

                // Windows
                drawRoundRect(YugoWhite, Offset(w * 0.28f, h * 0.28f), Size(w * 0.2f, h * 0.18f), CornerRadius(4f, 4f))
                drawRoundRect(YugoWhite, Offset(w * 0.5f, h * 0.28f), Size(w * 0.2f, h * 0.18f), CornerRadius(4f, 4f))

                // Wheels
                drawCircle(YugoBlack, w * 0.11f, Offset(w * 0.28f, h * 0.72f))
                drawCircle(YugoWhite, w * 0.05f, Offset(w * 0.28f, h * 0.72f))
                drawCircle(YugoBlack, w * 0.11f, Offset(w * 0.72f, h * 0.72f))
                drawCircle(YugoWhite, w * 0.05f, Offset(w * 0.72f, h * 0.72f))
            }
            RideCategory.PREMIER -> {
                // Sedan Car
                drawRoundRect(tint, Offset(w * 0.05f, h * 0.45f), Size(w * 0.9f, h * 0.28f), CornerRadius(12f, 12f))
                drawRoundRect(tint, Offset(w * 0.22f, h * 0.22f), Size(w * 0.52f, h * 0.28f), CornerRadius(12f, 12f))

                // Windows
                drawRoundRect(YugoWhite, Offset(w * 0.25f, h * 0.26f), Size(w * 0.22f, h * 0.2f), CornerRadius(4f, 4f))
                drawRoundRect(YugoWhite, Offset(w * 0.49f, h * 0.26f), Size(w * 0.22f, h * 0.2f), CornerRadius(4f, 4f))

                // Wheels
                drawCircle(YugoBlack, w * 0.11f, Offset(w * 0.25f, h * 0.72f))
                drawCircle(YugoWhite, w * 0.05f, Offset(w * 0.25f, h * 0.72f))
                drawCircle(YugoBlack, w * 0.11f, Offset(w * 0.75f, h * 0.72f))
                drawCircle(YugoWhite, w * 0.05f, Offset(w * 0.75f, h * 0.72f))
            }
            RideCategory.XL -> {
                // SUV Car
                drawRoundRect(tint, Offset(w * 0.05f, h * 0.38f), Size(w * 0.9f, h * 0.35f), CornerRadius(14f, 14f))
                drawRoundRect(tint, Offset(w * 0.18f, h * 0.18f), Size(w * 0.62f, h * 0.28f), CornerRadius(12f, 12f))

                // Windows
                drawRoundRect(YugoWhite, Offset(w * 0.21f, h * 0.22f), Size(w * 0.25f, h * 0.2f), CornerRadius(4f, 4f))
                drawRoundRect(YugoWhite, Offset(w * 0.48f, h * 0.22f), Size(w * 0.28f, h * 0.2f), CornerRadius(4f, 4f))

                // Wheels
                drawCircle(YugoBlack, w * 0.13f, Offset(w * 0.25f, h * 0.73f))
                drawCircle(YugoWhite, w * 0.06f, Offset(w * 0.25f, h * 0.73f))
                drawCircle(YugoBlack, w * 0.13f, Offset(w * 0.75f, h * 0.73f))
                drawCircle(YugoWhite, w * 0.06f, Offset(w * 0.75f, h * 0.73f))
            }
        }
    }
}

@Composable
fun FoodDishGraphic(
    category: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(YugoSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            when {
                category.contains("Biryani", ignoreCase = true) -> {
                    // Handi Bowl
                    drawCircle(Color(0xFFEA580C), radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.52f))
                    drawCircle(YugoAmber, radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.5f))
                    // Rice specks
                    drawCircle(YugoWhite, radius = 6f, center = Offset(w * 0.45f, h * 0.48f))
                    drawCircle(YugoWhite, radius = 8f, center = Offset(w * 0.55f, h * 0.52f))
                    drawCircle(YugoWhite, radius = 6f, center = Offset(w * 0.48f, h * 0.54f))
                }
                category.contains("Pizza", ignoreCase = true) -> {
                    // Pizza Triangle Slice
                    val slice = Path().apply {
                        moveTo(w * 0.5f, h * 0.2f)
                        lineTo(w * 0.2f, h * 0.78f)
                        lineTo(w * 0.8f, h * 0.78f)
                        close()
                    }
                    drawPath(slice, YugoAmber)
                    // Crust
                    drawRoundRect(Color(0xFFD97706), Offset(w * 0.18f, h * 0.74f), Size(w * 0.64f, h * 0.08f), CornerRadius(6f, 6f))
                    // Pepperoni
                    drawCircle(YugoRed, radius = w * 0.08f, center = Offset(w * 0.45f, h * 0.55f))
                    drawCircle(YugoRed, radius = w * 0.07f, center = Offset(w * 0.55f, h * 0.68f))
                }
                else -> {
                    // Thali / Plate
                    drawCircle(YugoBorder, radius = w * 0.38f, center = Offset(w * 0.5f, h * 0.5f))
                    drawCircle(YugoWhite, radius = w * 0.34f, center = Offset(w * 0.5f, h * 0.5f))
                    drawCircle(YugoGreen, radius = w * 0.1f, center = Offset(w * 0.38f, h * 0.42f))
                    drawCircle(YugoAmber, radius = w * 0.1f, center = Offset(w * 0.62f, h * 0.42f))
                    drawCircle(YugoRed, radius = w * 0.1f, center = Offset(w * 0.5f, h * 0.62f))
                }
            }
        }
    }
}

@Composable
fun PromoBannerGraphic(
    title: String,
    subtitle: String,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = YugoBlack
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = YugoGreenLight
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = YugoGreen,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = YugoWhite,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = YugoTextMuted,
                        fontSize = 13.sp
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(YugoWhite.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "YUGO",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = YugoWhite
                )
            }
        }
    }
}
