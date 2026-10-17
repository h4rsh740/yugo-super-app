package com.yugo.app.ui.components.map

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yugo.app.ui.theme.*
import kotlin.math.atan2

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveMapView(
    modifier: Modifier = Modifier,
    showRoute: Boolean = false,
    pickupLocationName: String? = "DLF Phase 2",
    destinationName: String? = "Cyber City",
    pickupLatitude: Double? = null,
    pickupLongitude: Double? = null,
    destinationLatitude: Double? = null,
    destinationLongitude: Double? = null,
    driverLatitude: Double? = null,
    driverLongitude: Double? = null,
    isDarkThemeMap: Boolean = false
) {
    var isRealMapMode by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // HTML / JS for real OpenStreetMap / CartoDB Voyager map + live OSRM routing
    val mapHtml = remember(showRoute, pickupLocationName, destinationName, pickupLatitude, pickupLongitude, destinationLatitude, destinationLongitude, isDarkThemeMap) {
        generateLeafletMapHtml(
            showRoute = showRoute,
            pickupName = pickupLocationName ?: "DLF Phase 2",
            dropName = destinationName ?: "Cyber City",
            startLat = pickupLatitude ?: 28.4820,
            startLng = pickupLongitude ?: 77.0835,
            endLat = destinationLatitude ?: 28.4960,
            endLng = destinationLongitude ?: 77.0910,
            isDark = isDarkThemeMap
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        if (isRealMapMode) {
            // Real Interactive Street Map (Leaflet + CartoDB Voyager/Positron)
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onReceivedError(
                                view: WebView?,
                                errorCode: Int,
                                description: String?,
                                failingUrl: String?
                            ) {
                                // Fallback to realistic vector map if network fails
                                isRealMapMode = false
                            }
                        }
                        loadDataWithBaseURL("https://yugo.app", mapHtml, "text/html", "UTF-8", null)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL("https://yugo.app", mapHtml, "text/html", "UTF-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // High-fidelity fallback vector cartography
            RealisticVectorMapFallback(
                showRoute = showRoute,
                pickupLocationName = pickupLocationName,
                destinationName = destinationName
            )
        }

        // Floating Map Controls (Zoom, Map Mode, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Toggle Real Map / Vector Map
            FloatingActionButton(
                onClick = { isRealMapMode = !isRealMapMode },
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                containerColor = YugoWhite,
                contentColor = if (isRealMapMode) YugoBlue else YugoBlack,
                elevation = FloatingActionButtonDefaults.elevation(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Map Style",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Recenter
            FloatingActionButton(
                onClick = {
                    webViewRef?.evaluateJavascript("if (window.recenterMap) { window.recenterMap(); }", null)
                },
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                containerColor = YugoWhite,
                contentColor = YugoBlack,
                elevation = FloatingActionButtonDefaults.elevation(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter Map",
                    modifier = Modifier.size(18.dp)
                )
            }

            // Zoom In / Out
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = YugoWhite,
                shadowElevation = 2.dp
            ) {
                Column {
                    IconButton(
                        onClick = {
                            webViewRef?.evaluateJavascript("if (window.map) { window.map.zoomIn(); }", null)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = YugoBlack)
                    }
                    HorizontalDivider(modifier = Modifier.width(36.dp), color = YugoDivider)
                    IconButton(
                        onClick = {
                            webViewRef?.evaluateJavascript("if (window.map) { window.map.zoomOut(); }", null)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = YugoBlack)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Real Leaflet + CartoDB Map Generator (HTML/JS)
// -------------------------------------------------------------

private fun generateLeafletMapHtml(
    showRoute: Boolean,
    pickupName: String,
    dropName: String,
    startLat: Double = 28.4820,
    startLng: Double = 77.0835,
    endLat: Double = 28.4960,
    endLng: Double = 77.0910,
    isDark: Boolean = false
): String {
    val tileUrl = if (isDark) {
        "https://{s}.basemaps.cartocdn.com/rastertiles/dark_all/{z}/{x}/{y}{r}.png"
    } else {
        "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8" />
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
          <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
          <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
          <style>
            html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #eef2f6; overflow: hidden; }
            .leaflet-control-attribution { display: none !important; }
            
            /* Custom Marker Styling */
            .pickup-marker {
              width: 18px;
              height: 18px;
              background: #16a34a;
              border: 3px solid #ffffff;
              border-radius: 50%;
              box-shadow: 0 4px 10px rgba(0,0,0,0.3);
            }
            .destination-marker {
              width: 18px;
              height: 18px;
              background: #0f172a;
              border: 3px solid #ffffff;
              border-radius: 4px;
              box-shadow: 0 4px 10px rgba(0,0,0,0.3);
            }
            .car-marker {
              width: 32px;
              height: 48px;
              display: flex;
              align-items: center;
              justify-content: center;
              transition: transform 0.2s linear;
            }
            .tooltip-label {
              background: #0f172a;
              color: #ffffff;
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
              font-size: 11px;
              font-weight: 700;
              padding: 4px 8px;
              border-radius: 6px;
              box-shadow: 0 4px 8px rgba(0,0,0,0.25);
              border: none;
            }
          </style>
        </head>
        <body>
          <div id="map"></div>
          <script>
            var startLat = $startLat, startLng = $startLng;
            var endLat = $endLat, endLng = $endLng;

            var centerLat = (startLat + endLat) / 2;
            var centerLng = (startLng + endLng) / 2;

            var map = L.map('map', {
              zoomControl: false,
              attributionControl: false
            }).setView([centerLat, centerLng], 14);

            window.map = map;

            L.tileLayer('$tileUrl', {
              maxZoom: 19,
              subdomains: 'abcd'
            }).addTo(map);

            window.recenterMap = function() {
              if ($showRoute) {
                map.fitBounds([
                  [startLat, startLng],
                  [endLat, endLng]
                ], { padding: [60, 60] });
              } else {
                map.setView([startLat, startLng], 15);
              }
            };

            // Custom Car SVG Icon
            var carSvg = '<svg width="26" height="42" viewBox="0 0 26 42" fill="none" xmlns="http://www.w3.org/2000/svg">' +
              '<rect x="2" y="3" width="22" height="36" rx="6" fill="#0f172a" opacity="0.25"/>' +
              '<rect x="1" y="1" width="24" height="38" rx="6" fill="#ffffff" stroke="#cbd5e1" stroke-width="1.5"/>' +
              '<rect x="4" y="8" width="18" height="8" rx="2" fill="#1e293b"/>' +
              '<rect x="4" y="24" width="18" height="6" rx="2" fill="#1e293b"/>' +
              '<circle cx="3" cy="35" r="2" fill="#dc2626"/>' +
              '<circle cx="23" cy="35" r="2" fill="#dc2626"/>' +
              '<circle cx="3" cy="4" r="2" fill="#fef08a"/>' +
              '<circle cx="23" cy="4" r="2" fill="#fef08a"/>' +
              '</svg>';

            var carIcon = L.divIcon({
              className: 'car-marker',
              html: carSvg,
              iconSize: [26, 42],
              iconAnchor: [13, 21]
            });

            if ($showRoute) {
              // Real-world Road Routing via OSRM (100% Free OpenStreetMap Router)
              var fallbackWaypoints = [
                [startLat, startLng],
                [startLat + (endLat - startLat) * 0.33, startLng + (endLng - startLng) * 0.33],
                [startLat + (endLat - startLat) * 0.66, startLng + (endLng - startLng) * 0.66],
                [endLat, endLng]
              ];

              function applyRoute(waypoints) {
                L.polyline(waypoints, {
                  color: '#1d4ed8',
                  weight: 12,
                  opacity: 0.25,
                  lineCap: 'round',
                  lineJoin: 'round'
                }).addTo(map);

                L.polyline(waypoints, {
                  color: '#2563eb',
                  weight: 6,
                  opacity: 1,
                  lineCap: 'round',
                  lineJoin: 'round'
                }).addTo(map);

                // Moving Simulated Driver along road coordinates
                var carMarker = L.marker([startLat, startLng], { icon: carIcon }).addTo(map);
                var progress = 0;
                setInterval(function() {
                  progress = (progress + 0.006) % 1.0;
                  var totalSeg = waypoints.length - 1;
                  var scaled = progress * totalSeg;
                  var idx = Math.min(Math.floor(scaled), totalSeg - 1);
                  var frac = scaled - idx;
                  var p1 = waypoints[idx];
                  var p2 = waypoints[idx + 1];

                  var curLat = p1[0] + (p2[0] - p1[0]) * frac;
                  var curLng = p1[1] + (p2[1] - p1[1]) * frac;
                  carMarker.setLatLng([curLat, curLng]);
                }, 100);

                map.fitBounds([
                  [startLat, startLng],
                  [endLat, endLng]
                ], { padding: [60, 60] });
              }

              var osrmUrl = 'https://router.project-osrm.org/route/v1/driving/' + startLng + ',' + startLat + ';' + endLng + ',' + endLat + '?overview=full&geometries=geojson';
              fetch(osrmUrl)
                .then(function(res) { return res.json(); })
                .then(function(data) {
                  if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
                    var coords = data.routes[0].geometry.coordinates.map(function(c) {
                      return [c[1], c[0]];
                    });
                    applyRoute(coords);
                  } else {
                    applyRoute(fallbackWaypoints);
                  }
                })
                .catch(function() {
                  applyRoute(fallbackWaypoints);
                });

              // Pickup Marker
              var pickupMarker = L.marker([startLat, startLng], {
                icon: L.divIcon({ className: 'pickup-marker', iconSize: [18, 18], iconAnchor: [9, 9] })
              }).addTo(map);
              pickupMarker.bindTooltip('$pickupName', { permanent: true, direction: 'top', className: 'tooltip-label', offset: [0, -10] });

              // Destination Marker
              var destMarker = L.marker([endLat, endLng], {
                icon: L.divIcon({ className: 'destination-marker', iconSize: [18, 18], iconAnchor: [9, 9] })
              }).addTo(map);
              destMarker.bindTooltip('$dropName', { permanent: true, direction: 'top', className: 'tooltip-label', offset: [0, -10] });
            } else {
              // Current User Location Marker with pulse
              var userIcon = L.divIcon({
                className: 'pickup-marker',
                iconSize: [20, 20],
                iconAnchor: [10, 10]
              });
              L.marker([startLat, startLng], { icon: userIcon }).addTo(map);

              // 3 Nearby moving driver cabs
              var cab1 = L.marker([startLat + 0.003, startLng + 0.002], { icon: carIcon }).addTo(map);
              var cab2 = L.marker([startLat - 0.002, startLng + 0.004], { icon: carIcon }).addTo(map);
              var cab3 = L.marker([startLat + 0.004, startLng - 0.003], { icon: carIcon }).addTo(map);
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}


// -------------------------------------------------------------
// High-Fidelity Realistic Fallback Vector Map
// -------------------------------------------------------------

@Composable
private fun RealisticVectorMapFallback(
    showRoute: Boolean,
    pickupLocationName: String?,
    destinationName: String?
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1.2f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
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

            // 1. City Blocks with building shadows
            val blockW = 130f * scale
            val blockH = 85f * scale
            val roadGap = 26f * scale
            for (i in -4..4) {
                for (j in -4..4) {
                    val left = center.x + i * (blockW + roadGap)
                    val top = center.y + j * (blockH + roadGap)
                    val isPark = (i == -2 && j == 1) || (i == 2 && j == -1)

                    if (isPark) {
                        drawRoundRect(
                            color = Color(0xFFDCFCE7),
                            topLeft = Offset(left, top),
                            size = Size(blockW, blockH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f * scale)
                        )
                    } else {
                        drawRoundRect(
                            color = Color(0xFFE2E8F0),
                            topLeft = Offset(left + 2f, top + 3f),
                            size = Size(blockW, blockH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale)
                        )
                        drawRoundRect(
                            color = Color(0xFFFFFFFF),
                            topLeft = Offset(left, top),
                            size = Size(blockW, blockH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale)
                        )
                    }
                }
            }

            // 2. Highway (NH-48 Express Highway)
            val hPath = Path().apply {
                moveTo(center.x - 700f * scale, center.y - 400f * scale)
                lineTo(center.x + 700f * scale, center.y + 400f * scale)
            }
            drawPath(path = hPath, color = Color(0xFFCBD5E1), style = Stroke(width = 38f * scale))
            drawPath(path = hPath, color = Color(0xFFFFFAF0), style = Stroke(width = 34f * scale))
            drawPath(
                path = hPath,
                color = Color(0xFFFBBF24),
                style = Stroke(width = 3f * scale, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f * scale, 14f * scale), 0f))
            )

            // 3. Arterial Boulevards with text
            val bPath = Path().apply {
                moveTo(center.x - 600f * scale, center.y + 20f * scale)
                lineTo(center.x + 600f * scale, center.y + 20f * scale)
            }
            drawPath(path = bPath, color = Color.White, style = Stroke(width = 24f * scale))

            drawIntoCanvas { canvas ->
                val p = Paint().apply {
                    color = android.graphics.Color.argb(140, 71, 85, 105)
                    textSize = 12f * scale
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText("DLF CENTRAL BOULEVARD", center.x - 220f * scale, center.y + 14f * scale, p)
                canvas.nativeCanvas.drawText("CYBER CITY EXPRESSWAY", center.x + 180f * scale, center.y + 160f * scale, p)
            }

            // 4. Route Line
            if (showRoute) {
                val p1 = Offset(center.x - 120f * scale, center.y + 110f * scale)
                val p2 = Offset(center.x - 60f * scale, center.y + 20f * scale)
                val p3 = Offset(center.x + 80f * scale, center.y + 20f * scale)
                val p4 = Offset(center.x + 130f * scale, center.y - 80f * scale)

                val route = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                }

                drawPath(path = route, color = Color(0x332563EB), style = Stroke(width = 16f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(path = route, color = Color(0xFF2563EB), style = Stroke(width = 7f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round))

                // Markers
                drawCircle(color = Color(0xFF16A34A), radius = 8f * scale, center = p1)
                drawCircle(color = Color.White, radius = 4f * scale, center = p1)

                drawRoundRect(color = Color(0xFF0F172A), topLeft = Offset(p4.x - 10f * scale, p4.y - 20f * scale), size = Size(20f * scale, 20f * scale), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale))
                drawRoundRect(color = Color.White, topLeft = Offset(p4.x - 5f * scale, p4.y - 15f * scale), size = Size(10f * scale, 10f * scale), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale))
            }
        }
    }
}
