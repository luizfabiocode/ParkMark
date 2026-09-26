package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParkingSpot
import com.example.location.LocationClient
import com.example.location.UserLocationData
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.RadarGrid
import com.example.ui.theme.RadarSweep
import com.example.ui.theme.SafetyOrange
import com.example.ui.theme.SuccessGreen
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ParkingRadarMap(
    savedSpot: ParkingSpot,
    currentLocation: UserLocationData?,
    distanceMeters: Float?,
    bearingDegrees: Float?,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing animation for the user's location
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadiusFraction by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    // Radar sweep rotation
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RadarSweep"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, DarkCardBorder, RoundedCornerShape(24.dp))
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomScale = (zoomScale * zoom).coerceIn(0.4f, 4.0f)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
            .testTag("parking_radar_map")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f + panOffsetX, size.height / 2f + panOffsetY)
            val maxRadius = min(size.width, size.height) * 0.44f * zoomScale

            // Draw Background Radar Grids
            drawRadarGrid(center, maxRadius, distanceMeters ?: 50f)

            // Draw dynamic distance range labels
            val currentDist = distanceMeters ?: 50f
            val baseScale = max(currentDist * 1.3f, 25f)
            val ringDistances = listOf(baseScale * 0.25f, baseScale * 0.5f, baseScale * 0.75f, baseScale)

            for (i in 1..4) {
                val r = (maxRadius / 4f) * i
                drawCircle(
                    color = RadarGrid,
                    radius = r,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = if (i % 2 == 1) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                    )
                )

                val distLabel = LocationClient.formatDistance(ringDistances[i - 1])
                val textLayoutResult = textMeasurer.measure(
                    text = distLabel,
                    style = TextStyle(
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(center.x + 8f, center.y - r - 14f)
                )
            }

            // Calculate relative offset of saved spot from user
            val spotOffset: Offset = if (currentLocation != null) {
                val latDiff = savedSpot.latitude - currentLocation.latitude
                val lngDiff = savedSpot.longitude - currentLocation.longitude
                // Approx meters: 1 deg lat = 111,320m, 1 deg lng = 111,320m * cos(lat)
                val latMeters = (latDiff * 111320.0).toFloat()
                val lngMeters = (lngDiff * 111320.0 * cos(Math.toRadians(currentLocation.latitude))).toFloat()

                val scalePixelsPerMeter = maxRadius / baseScale
                Offset(
                    x = center.x + (lngMeters * scalePixelsPerMeter),
                    y = center.y - (latMeters * scalePixelsPerMeter) // y inverted in screen space
                )
            } else {
                // Default spot preview if user location not ready yet
                Offset(center.x, center.y - (maxRadius * 0.55f))
            }

            // Draw connection dashed guide line between user and car
            drawLine(
                color = ElectricBluePrimary.copy(alpha = 0.7f),
                start = center,
                end = spotOffset,
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
            )

            // Draw User Location (Center dot + Pulsing ripples)
            drawCircle(
                color = PulseBlue.copy(alpha = pulseAlpha * 0.4f),
                radius = 36.dp.toPx() * pulseRadiusFraction,
                center = center
            )
            drawCircle(
                color = ElectricBluePrimary.copy(alpha = 0.3f),
                radius = 18.dp.toPx(),
                center = center
            )
            drawCircle(
                color = ElectricBluePrimary,
                radius = 7.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = center
            )

            // Draw Saved Car Location Marker Pin
            drawCircle(
                color = SafetyOrange.copy(alpha = 0.25f),
                radius = 24.dp.toPx(),
                center = spotOffset
            )
            drawCircle(
                color = SafetyOrange,
                radius = 14.dp.toPx(),
                center = spotOffset
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = spotOffset
            )

            // Car Label Tag
            val carText = "CARRO (${distanceMeters?.let { LocationClient.formatDistance(it) } ?: "AQUI"})"
            val carLabel = textMeasurer.measure(
                text = carText,
                style = TextStyle(
                    color = SafetyOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(
                textLayoutResult = carLabel,
                topLeft = Offset(
                    spotOffset.x - (carLabel.size.width / 2f),
                    spotOffset.y + 18.dp.toPx()
                )
            )
        }

        // Live Compass / Direction Pointer Header
        if (bearingDegrees != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(20.dp),
                color = DarkCardSurface.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Direção até o Carro",
                        tint = SafetyOrange,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(bearingDegrees)
                    )
                    Text(
                        text = "Rumo: ${bearingDegrees.toInt()}° (${getCardinalDirection(bearingDegrees)})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Radar Zoom & Re-Center Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = DarkCardSurface.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(4.0f) },
                    modifier = Modifier.size(38.dp).testTag("zoom_in_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar Zoom",
                        tint = ElectricBluePrimary
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = DarkCardSurface.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.4f) },
                    modifier = Modifier.size(38.dp).testTag("zoom_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Diminuir Zoom",
                        tint = ElectricBluePrimary
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = DarkCardSurface.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = {
                        panOffsetX = 0f
                        panOffsetY = 0f
                        zoomScale = 1f
                    },
                    modifier = Modifier.size(38.dp).testTag("recenter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Centralizar Visão",
                        tint = ElectricBluePrimary
                    )
                }
            }
        }

        // Live Radar Legend Pill at Bottom-Left
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            color = DarkCardSurface.copy(alpha = 0.88f),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(ElectricBluePrimary, CircleShape)
                    )
                    Text("Você", style = TextStyle(color = Color(0xFF94A3B8), fontSize = 10.sp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(SafetyOrange, CircleShape)
                    )
                    Text("Carro", style = TextStyle(color = Color(0xFF94A3B8), fontSize = 10.sp))
                }
            }
        }
    }
}

private fun DrawScope.drawRadarGrid(center: Offset, maxRadius: Float, distance: Float) {
    // Crosshair axes
    drawLine(
        color = RadarGrid,
        start = Offset(center.x - maxRadius, center.y),
        end = Offset(center.x + maxRadius, center.y),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = RadarGrid,
        start = Offset(center.x, center.y - maxRadius),
        end = Offset(center.x, center.y + maxRadius),
        strokeWidth = 1.dp.toPx()
    )

    // Diagonal guidelines
    val diagOffset = maxRadius * 0.7071f
    drawLine(
        color = RadarGrid.copy(alpha = 0.5f),
        start = Offset(center.x - diagOffset, center.y - diagOffset),
        end = Offset(center.x + diagOffset, center.y + diagOffset),
        strokeWidth = 0.8.dp.toPx()
    )
    drawLine(
        color = RadarGrid.copy(alpha = 0.5f),
        start = Offset(center.x - diagOffset, center.y + diagOffset),
        end = Offset(center.x + diagOffset, center.y - diagOffset),
        strokeWidth = 0.8.dp.toPx()
    )
}

private fun getCardinalDirection(bearing: Float): String {
    val directions = arrayOf("N", "NE", "L", "SE", "S", "SO", "O", "NO")
    val index = (((bearing + 22.5f) % 360) / 45).toInt()
    return directions[index.coerceIn(0, 7)]
}
