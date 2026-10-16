package com.yugo.app.ui.components.map

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yugo.app.ui.theme.*
import kotlin.math.*

data class NavigationManeuver(
    val instruction: String,
    val distanceText: String,
    val streetName: String,
    val nextStep: String,
    val isTurnRight: Boolean = true
)

@Composable
fun ProfessionalNavigationMap(
    modifier: Modifier = Modifier,
    pickupName: String = "DLF Phase 2",
    destinationName: String = "Cyber City Phase 3",
    driverName: String = "Rajesh Sharma",
    onManeuverUpdate: (NavigationManeuver) -> Unit = {}
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1.15f) }
    var is3DMode by remember { mutableStateOf(false) }
    var showTraffic by remember { mutableStateOf(true) }

    // Infinite animation driving the vehicle smoothly along the route
    val transition = rememberInfiniteTransition(label = "CarMovement")
    val carProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CarProgress"
    )

    // Pulse animation for beacon
    val beaconPulse by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BeaconPulse"
    )

    // Chevron flow animation on the route
    val chevronFlow by transition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ChevronFlow"
    )

    // Waypoints for the route
    val waypoints = remember {
        listOf(
            Offset(-140f, 130f),  // Start: Pickup DLF Phase 2
            Offset(-80f, 130f),   // Straight along DLF Avenue
            Offset(-80f, 30f),    // Turn onto Central Boulevard
            Offset(40f, 30f),     // Turn onto Highway Link
            Offset(40f, -80f),    // Approaching Cyber City
            Offset(140f, -80f)    // Destination: Cyber City Tower B
        )
    }

    // Calculate vehicle position and heading along waypoints
    val (carOffset, carHeading, currentSegmentIndex) = remember(carProgress, waypoints) {
        val totalSegments = waypoints.size - 1
        val scaledProgress = (carProgress * totalSegments).coerceIn(0f, totalSegments.toFloat())
        val index = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
        val segmentFraction = scaledProgress - index

        val p1 = waypoints[index]
        val p2 = waypoints[index + 1]

        val currentPos = Offset(
            x = p1.x + (p2.x - p1.x) * segmentFraction,
            y = p1.y + (p2.y - p1.y) * segmentFraction
        )

        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val angleDegrees = (atan2(dy.toDouble(), dx.toDouble()) * 180.0 / Math.PI).toFloat() + 90f

        Triple(currentPos, angleDegrees, index)
    }

    // Dynamic maneuver instructions based on current segment
    LaunchedEffect(currentSegmentIndex, carProgress) {
        val maneuver = when (currentSegmentIndex) {
            0 -> NavigationManeuver(
                instruction = "In 200 m",
                distanceText = "200 m",
                streetName = "Turn Left onto DLF Central Blvd",
                nextStep = "Then continue 350 m",
                isTurnRight = false
            )
            1 -> NavigationManeuver(
                instruction = "In 150 m",
                distanceText = "150 m",
                streetName = "Turn Right onto Cyber City Connector",
                nextStep = "Then head towards DLF Cyber City",
                isTurnRight = true
            )
            2 -> NavigationManeuver(
                instruction = "In 300 m",
                distanceText = "300 m",
                streetName = "Keep Straight on Expressway Link",
                nextStep = "Then merge onto Cyber City Blvd",
                isTurnRight = false
            )
            3 -> NavigationManeuver(
                instruction = "In 100 m",
                distanceText = "100 m",
                streetName = "Turn Right onto Tower B Access Rd",
                nextStep = "Destination will be on the left",
                isTurnRight = true
            )
            else -> NavigationManeuver(
                instruction = "Arriving",
                distanceText = "40 m",
                streetName = "Arriving at DLF Cyber City Tower B",
                nextStep = "Your driver has arrived",
                isTurnRight = true
            )
        }
        onManeuverUpdate(maneuver)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FA)) // Sleek Uber Map Land
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.8f, 2.5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + offsetX, size.height / 2 + offsetY)

            // 1. Draw Professional Urban City Grid
            drawUrbanCity(center, scale)

            // 2. Draw Major Highways & Arterial Roads with Street Names
            drawHighwaysAndAvenues(center, scale)

            // 3. Draw Waterway (River)
            drawWaterway(center, scale)

            // 4. Draw Professional Uber Navigation Route Polyline
            drawNavigationRoute(
                center = center,
                waypoints = waypoints,
                scale = scale,
                chevronOffset = chevronFlow,
                showTraffic = showTraffic
            )

            // 5. Draw Pickup Marker with Ripple
            val pickupScreenPos = Offset(center.x + waypoints.first().x * scale, center.y + waypoints.first().y * scale)
            drawPickupBeacon(pickupScreenPos, scale, beaconPulse)

            // 6. Draw Destination Flag Marker
            val dropScreenPos = Offset(center.x + waypoints.last().x * scale, center.y + waypoints.last().y * scale)
            drawDestinationPin(dropScreenPos, scale, destinationName)

            // 7. Draw 3D Top-Down Moving Vehicle with Headlights
            val carScreenPos = Offset(center.x + carOffset.x * scale, center.y + carOffset.y * scale)
            draw3DVehicle(carScreenPos, carHeading, scale)
        }

        // Floating Map Controls (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Compass / 3D Toggle
            FloatingActionButton(
                onClick = { is3DMode = !is3DMode },
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                containerColor = YugoWhite,
                contentColor = if (is3DMode) YugoBlue else YugoBlack,
                elevation = FloatingActionButtonDefaults.elevation(3.dp)
            ) {
                Icon(Icons.Default.Explore, contentDescription = "3D Mode", modifier = Modifier.size(20.dp))
            }

            // Traffic Toggle
            FloatingActionButton(
                onClick = { showTraffic = !showTraffic },
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                containerColor = YugoWhite,
                contentColor = if (showTraffic) YugoGreen else YugoTextSecondary,
                elevation = FloatingActionButtonDefaults.elevation(3.dp)
            ) {
                Icon(Icons.Default.Traffic, contentDescription = "Traffic Layer", modifier = Modifier.size(20.dp))
            }

            // Recenter Camera on Car
            FloatingActionButton(
                onClick = {
                    offsetX = -carOffset.x * scale
                    offsetY = -carOffset.y * scale
                    scale = 1.25f
                },
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                containerColor = YugoWhite,
                contentColor = YugoBlack,
                elevation = FloatingActionButtonDefaults.elevation(3.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter on Car", modifier = Modifier.size(20.dp))
            }

            // Zoom Controls
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = YugoWhite,
                shadowElevation = 3.dp
            ) {
                Column {
                    IconButton(
                        onClick = { scale = (scale * 1.2f).coerceAtMost(2.5f) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = YugoBlack)
                    }
                    HorizontalDivider(modifier = Modifier.width(38.dp), color = YugoDivider)
                    IconButton(
                        onClick = { scale = (scale / 1.2f).coerceAtLeast(0.8f) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = YugoBlack)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Cartography & Map Rendering Helpers
// -------------------------------------------------------------

private fun DrawScope.drawUrbanCity(center: Offset, scale: Float) {
    val blockW = 120f * scale
    val blockH = 80f * scale
    val roadGap = 28f * scale

    val buildingColor = Color(0xFFFFFFFF)
    val buildingShadow = Color(0xFFE2E8F0)
    val parkColor = Color(0xFFE8F5E9)

    for (i in -4..4) {
        for (j in -4..4) {
            val left = center.x + i * (blockW + roadGap)
            val top = center.y + j * (blockH + roadGap)

            val isPark = (i == -2 && j == 1) || (i == 2 && j == -1) || (i == 0 && j == -3)

            if (isPark) {
                drawRoundRect(
                    color = parkColor,
                    topLeft = Offset(left, top),
                    size = Size(blockW, blockH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * scale)
                )
            } else {
                // Building Drop Shadow
                drawRoundRect(
                    color = buildingShadow,
                    topLeft = Offset(left + 3f * scale, top + 3f * scale),
                    size = Size(blockW, blockH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale)
                )
                // Building Footprint
                drawRoundRect(
                    color = buildingColor,
                    topLeft = Offset(left, top),
                    size = Size(blockW, blockH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale)
                )
            }
        }
    }
}

private fun DrawScope.drawHighwaysAndAvenues(center: Offset, scale: Float) {
    val highwayColor = Color(0xFFFFFAF0)
    val highwayBorder = Color(0xFFCBD5E1)
    val highwayWidth = 36f * scale

    // Diagonal Express Highway (NH-48)
    val hPath = Path().apply {
        moveTo(center.x - 700f * scale, center.y - 450f * scale)
        lineTo(center.x + 700f * scale, center.y + 450f * scale)
    }

    // Outer road casing
    drawPath(path = hPath, color = highwayBorder, style = Stroke(width = highwayWidth + 4f))
    // Road surface
    drawPath(path = hPath, color = highwayColor, style = Stroke(width = highwayWidth))
    // Center divider dash
    drawPath(
        path = hPath,
        color = Color(0xFFFBBF24), // Golden expressway divider
        style = Stroke(
            width = 3f * scale,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f * scale, 16f * scale), 0f)
        )
    )

    // Arterial Boulevards (DLF Central Avenue & Cyber City Blvd)
    val bPath1 = Path().apply {
        moveTo(center.x - 600f * scale, center.y + 30f * scale)
        lineTo(center.x + 600f * scale, center.y + 30f * scale)
    }
    drawPath(path = bPath1, color = Color(0xFFFFFFFF), style = Stroke(width = 24f * scale))

    val bPath2 = Path().apply {
        moveTo(center.x + 40f * scale, center.y - 500f * scale)
        lineTo(center.x + 40f * scale, center.y + 500f * scale)
    }
    drawPath(path = bPath2, color = Color(0xFFFFFFFF), style = Stroke(width = 24f * scale))

    // Street Names Text
    drawIntoCanvas { canvas ->
        val textPaint = Paint().apply {
            color = android.graphics.Color.argb(160, 71, 85, 105)
            textSize = 12f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.nativeCanvas.drawText("CYBER CITY BLVD", center.x + 55f * scale, center.y - 200f * scale, textPaint)
        canvas.nativeCanvas.drawText("DLF CENTRAL AVE", center.x - 280f * scale, center.y + 24f * scale, textPaint)
        canvas.nativeCanvas.drawText("NH-48 EXPRESSWAY", center.x + 220f * scale, center.y + 200f * scale, textPaint)
    }
}

private fun DrawScope.drawWaterway(center: Offset, scale: Float) {
    val riverPath = Path().apply {
        moveTo(center.x - 600f * scale, center.y + 380f * scale)
        cubicTo(
            center.x - 250f * scale, center.y + 200f * scale,
            center.x + 150f * scale, center.y + 440f * scale,
            center.x + 600f * scale, center.y + 240f * scale
        )
    }
    drawPath(
        path = riverPath,
        color = Color(0xFFC7D2FE).copy(alpha = 0.6f),
        style = Stroke(width = 46f * scale)
    )
}

private fun DrawScope.drawNavigationRoute(
    center: Offset,
    waypoints: List<Offset>,
    scale: Float,
    chevronOffset: Float,
    showTraffic: Boolean
) {
    val routePath = Path()
    waypoints.forEachIndexed { i, pt ->
        val screenPt = Offset(center.x + pt.x * scale, center.y + pt.y * scale)
        if (i == 0) routePath.moveTo(screenPt.x, screenPt.y)
        else routePath.lineTo(screenPt.x, screenPt.y)
    }

    // 1. Soft Ambient Route Glow / Shadow
    drawPath(
        path = routePath,
        color = Color(0x332563EB),
        style = Stroke(width = 18f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 2. High-Visibility Navigation Route Line (Uber Electric Blue)
    drawPath(
        path = routePath,
        color = Color(0xFF2563EB),
        style = Stroke(width = 8f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 3. Traffic Speed Colors (Green flow & slight Amber traffic zone)
    if (showTraffic) {
        val p2 = Offset(center.x + waypoints[2].x * scale, center.y + waypoints[2].y * scale)
        val p3 = Offset(center.x + waypoints[3].x * scale, center.y + waypoints[3].y * scale)
        val trafficPath = Path().apply {
            moveTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
        }
        drawPath(
            path = trafficPath,
            color = Color(0xFFF59E0B), // Moderate traffic zone
            style = Stroke(width = 5f * scale, cap = StrokeCap.Round)
        )
    }

    // 4. Directional Chevron Flow Effect
    drawPath(
        path = routePath,
        color = Color(0xFFFFFFFF),
        style = Stroke(
            width = 3f * scale,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(6f * scale, 18f * scale),
                chevronOffset
            )
        )
    )
}

private fun DrawScope.drawPickupBeacon(pos: Offset, scale: Float, pulse: Float) {
    // Expanding green radar ripple
    drawCircle(
        color = Color(0xFF16A34A).copy(alpha = (0.4f / pulse).coerceIn(0f, 0.4f)),
        radius = 24f * scale * pulse,
        center = pos
    )
    // White ring
    drawCircle(
        color = Color.White,
        radius = 9f * scale,
        center = pos
    )
    // Solid green dot
    drawCircle(
        color = Color(0xFF16A34A),
        radius = 6f * scale,
        center = pos
    )
}

private fun DrawScope.drawDestinationPin(pos: Offset, scale: Float, destinationName: String) {
    val pinW = 20f * scale
    val pinH = 26f * scale

    // Drop shadow
    drawOval(
        color = Color(0x33000000),
        topLeft = Offset(pos.x - 10f * scale, pos.y + 2f * scale),
        size = Size(20f * scale, 8f * scale)
    )

    // Pin Head (Black Rounded Card with White Center)
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(pos.x - pinW / 2, pos.y - pinH),
        size = Size(pinW, pinH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale)
    )

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(pos.x - pinW / 4, pos.y - pinH + 4f * scale),
        size = Size(pinW / 2, pinH / 2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale)
    )

    // Destination Floating Tooltip Pill
    drawIntoCanvas { canvas ->
        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 10f * scale
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        val bgPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            isAntiAlias = true
        }
        canvas.nativeCanvas.drawRoundRect(
            pos.x - 45f * scale, pos.y - pinH - 24f * scale,
            pos.x + 45f * scale, pos.y - pinH - 4f * scale,
            12f * scale, 12f * scale, bgPaint
        )
        canvas.nativeCanvas.drawText(
            destinationName.take(12),
            pos.x - 36f * scale,
            pos.y - pinH - 10f * scale,
            textPaint
        )
    }
}

private fun DrawScope.draw3DVehicle(pos: Offset, heading: Float, scale: Float) {
    rotate(degrees = heading, pivot = pos) {
        val carW = 22f * scale
        val carL = 38f * scale

        // Headlights beam shining forward
        val beamPath = Path().apply {
            moveTo(pos.x - carW * 0.4f, pos.y - carL / 2)
            lineTo(pos.x - carW * 1.5f, pos.y - carL * 1.6f)
            lineTo(pos.x + carW * 1.5f, pos.y - carL * 1.6f)
            lineTo(pos.x + carW * 0.4f, pos.y - carL / 2)
            close()
        }
        drawPath(
            path = beamPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x55FEF08A), Color.Transparent),
                startY = pos.y - carL / 2,
                endY = pos.y - carL * 1.6f
            )
        )

        // Vehicle drop shadow
        drawRoundRect(
            color = Color(0x40000000),
            topLeft = Offset(pos.x - carW / 2 + 2f, pos.y - carL / 2 + 3f),
            size = Size(carW, carL),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale)
        )

        // Car Body (Glossy White Sedan)
        drawRoundRect(
            color = Color(0xFFFFFFFF),
            topLeft = Offset(pos.x - carW / 2, pos.y - carL / 2),
            size = Size(carW, carL),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale)
        )

        // Side mirrors
        drawRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(pos.x - carW / 2 - 3f * scale, pos.y - carL * 0.15f),
            size = Size(3f * scale, 6f * scale)
        )
        drawRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(pos.x + carW / 2, pos.y - carL * 0.15f),
            size = Size(3f * scale, 6f * scale)
        )

        // Windshield (Tinted glass)
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(pos.x - carW * 0.35f, pos.y - carL * 0.25f),
            size = Size(carW * 0.7f, carL * 0.2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale)
        )

        // Sunroof
        drawRect(
            color = Color(0xFF334155),
            topLeft = Offset(pos.x - carW * 0.25f, pos.y - carL * 0.02f),
            size = Size(carW * 0.5f, carL * 0.22f)
        )

        // Rear window
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(pos.x - carW * 0.35f, pos.y + carL * 0.25f),
            size = Size(carW * 0.7f, carL * 0.12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale)
        )

        // Tail lights (red LED glow)
        drawCircle(color = Color(0xFFDC2626), radius = 2f * scale, center = Offset(pos.x - carW * 0.35f, pos.y + carL / 2))
        drawCircle(color = Color(0xFFDC2626), radius = 2f * scale, center = Offset(pos.x + carW * 0.35f, pos.y + carL / 2))
    }
}
