package com.airvana.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airvana.intercom.ChannelPlace
import com.airvana.intercom.IntercomConnection
import com.airvana.intercom.IntercomSession
import com.airvana.location.PlaceReporter
import com.airvana.theme.AirvanaPalette
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun MapScreen(
    session: IntercomSession,
    reporter: PlaceReporter,
) {
    val intercom = session.state.value
    val reading = reporter.reading.value
    val self = intercom.places.firstOrNull { it.self }
    val others = intercom.places.filter { !it.self }
    val onChannel = intercom.connection == IntercomConnection.Connecting ||
        intercom.connection == IntercomConnection.Connected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 12.dp),
    ) {
        SectionLabel("Map")
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Where you are",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 32.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "GPS on this phone, shared with the channel. It keeps working when there is no internet.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(22.dp))
        AirvanaCard {
            PlaceMap(self = self, others = others)
            Spacer(Modifier.height(12.dp))
            Text(
                text = reading.detail ?: "Waiting for GPS.",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!reading.allowed || !reading.gpsOn) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        if (!reading.allowed) reporter.requestPermission() else reporter.openLocationSettings()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AirvanaPalette.Line),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AirvanaPalette.White),
                ) {
                    Text(
                        text = if (!reading.allowed) "Allow location" else "Turn on location",
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        AirvanaCard {
            Text(
                text = when {
                    !onChannel -> "Not on a channel"
                    others.isEmpty() -> "No other phone yet"
                    else -> "${others.size + 1} on the map"
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    !onChannel -> "Join a channel to see the other phones. Your own GPS fix still shows here."
                    others.isEmpty() -> "The other phones share their GPS over this channel, including Bluetooth and hotspot."
                    else -> "Distances are from you."
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            if (intercom.places.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                intercom.places.forEach { place ->
                    PlaceRow(place = place, self = self)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PlaceMap(
    self: ChannelPlace?,
    others: List<ChannelPlace>,
) {
    val ring = ringMeters(self, others)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.38f
        drawCircle(
            color = AirvanaPalette.Line,
            radius = radius,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )
        drawCircle(
            color = AirvanaPalette.Line,
            radius = radius / 2f,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )
        if (self == null) return@Canvas
        val metersPerPixel = ring / radius
        others.forEach { place ->
            val east = eastMeters(self, place)
            val north = northMeters(self, place)
            val point = Offset(
                x = center.x + (east / metersPerPixel).toFloat(),
                y = center.y - (north / metersPerPixel).toFloat(),
            )
            drawCircle(color = AirvanaPalette.White, radius = 6.dp.toPx(), center = point)
        }
        drawCircle(color = AirvanaPalette.White, radius = 7.dp.toPx(), center = center)
        drawCircle(color = AirvanaPalette.Black, radius = 3.dp.toPx(), center = center)
    }
    Text(
        text = "Outer ring ${formatDistance(ring)}",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun PlaceRow(place: ChannelPlace, self: ChannelPlace?) {
    val distance = if (self == null || place.self) {
        if (place.self) "This phone" else ""
    } else {
        formatDistance(metersBetween(self, place))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = place.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "%.5f, %.5f".format(place.latitude, place.longitude),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (distance.isNotEmpty()) {
            Spacer(Modifier.size(12.dp))
            Text(text = distance, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun ringMeters(self: ChannelPlace?, others: List<ChannelPlace>): Double {
    val farthest = if (self == null) 100.0 else {
        others.maxOfOrNull { metersBetween(self, it) } ?: 100.0
    }
    val steps = doubleArrayOf(50.0, 100.0, 250.0, 500.0, 1000.0, 2500.0, 5000.0, 10000.0, 25000.0)
    return steps.firstOrNull { it >= farthest.coerceAtLeast(40.0) } ?: farthest
}

private fun metersBetween(from: ChannelPlace, to: ChannelPlace): Double {
    val earth = 6_371_000.0
    val lat1 = kotlin.math.PI * from.latitude / 180.0
    val lat2 = kotlin.math.PI * to.latitude / 180.0
    val dLat = lat2 - lat1
    val dLng = kotlin.math.PI * (to.longitude - from.longitude) / 180.0
    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
    return 2 * earth * asin(sqrt(a))
}

private fun northMeters(self: ChannelPlace, place: ChannelPlace): Double {
    return kotlin.math.PI * (place.latitude - self.latitude) / 180.0 * 6_371_000.0
}

private fun eastMeters(self: ChannelPlace, place: ChannelPlace): Double {
    val mid = kotlin.math.PI * ((self.latitude + place.latitude) / 2.0) / 180.0
    return kotlin.math.PI * (place.longitude - self.longitude) / 180.0 * 6_371_000.0 * cos(mid)
}

private fun formatDistance(meters: Double): String {
    return if (meters < 1000) {
        "${meters.roundToInt()} m"
    } else {
        val km = meters / 1000.0
        val rounded = (km * 10).roundToInt() / 10.0
        "$rounded km"
    }
}
