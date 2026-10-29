package com.airvana.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ChannelIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val bars = listOf(0.38f, 0.72f, 1f, 0.56f)
        val gap = 3.2.dp.toPx()
        val barWidth = 3.dp.toPx()
        val total = bars.size * barWidth + (bars.size - 1) * gap
        var x = (size.width - total) / 2f
        bars.forEach { scale ->
            val height = size.height * 0.72f * scale
            val top = (size.height - height) / 2f
            drawRoundRect(
                color = tint,
                topLeft = Offset(x, top),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
            )
            x += barWidth + gap
        }
    }
}

@Composable
fun EarphoneIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = tint,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(4.dp.toPx(), 3.dp.toPx()),
            size = Size(16.dp.toPx(), 14.dp.toPx()),
            style = stroke,
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(3.5.dp.toPx(), 11.dp.toPx()),
            size = Size(4.6.dp.toPx(), 7.2.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(15.9.dp.toPx(), 11.dp.toPx()),
            size = Size(4.6.dp.toPx(), 7.2.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
        )
    }
}

@Composable
fun MapIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val center = Offset(size.width / 2f, 8.5.dp.toPx())
        drawCircle(color = tint, radius = 4.2.dp.toPx(), center = center, style = stroke)
        drawCircle(color = tint, radius = 1.4.dp.toPx(), center = center)
        drawLine(
            color = tint,
            start = Offset(size.width / 2f, 12.6.dp.toPx()),
            end = Offset(size.width / 2f, 20.dp.toPx()),
            strokeWidth = 1.7.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun SettingsIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val left = 4.dp.toPx()
        val right = size.width - 4.dp.toPx()
        val knobCenters = listOf(0.62f, 0.36f, 0.74f)
        listOf(7.5.dp, 12.dp, 16.5.dp).forEachIndexed { index, yDp ->
            val y = yDp.toPx()
            drawLine(
                color = tint,
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 1.7.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = tint,
                radius = 2.5.dp.toPx(),
                center = Offset(size.width * knobCenters[index], y),
            )
        }
    }
}
