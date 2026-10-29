package com.airvana.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AirvanaPalette {
    val Black = Color(0xFF070708)
    val Surface = Color(0xFF121214)
    val SurfaceHigh = Color(0xFF1C1C20)
    val Line = Color(0xFF2A2A30)
    val White = Color(0xFFF5F5F7)
    val Muted = Color(0xFFA1A1AA)
    val Dim = Color(0xFF6F6F78)
}

private val AirvanaColors = darkColorScheme(
    primary = AirvanaPalette.White,
    onPrimary = AirvanaPalette.Black,
    background = AirvanaPalette.Black,
    onBackground = AirvanaPalette.White,
    surface = AirvanaPalette.Surface,
    onSurface = AirvanaPalette.White,
    surfaceVariant = AirvanaPalette.SurfaceHigh,
    onSurfaceVariant = AirvanaPalette.Muted,
    outline = AirvanaPalette.Line,
)

private val AirvanaTypography = Typography(
    headlineLarge = TextStyle(
        fontSize = 40.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-1).sp,
        color = AirvanaPalette.White,
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = AirvanaPalette.White,
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = AirvanaPalette.Muted,
    ),
    labelLarge = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp,
    ),
)

@Composable
fun AirvanaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AirvanaColors,
        typography = AirvanaTypography,
        content = content,
    )
}
