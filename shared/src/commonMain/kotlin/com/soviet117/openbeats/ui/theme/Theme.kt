package com.soviet117.openbeats.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Obsidian = Color(0xFF0B0812)
val SurfaceLow = Color(0xFF14101F)
val Surface = Color(0xFF191327)
val SurfaceHigh = Color(0xFF221A35)
val OutlineSoft = Color(0xFF2E2544)

val BrandViolet = Color(0xFF8B5CF6)
val BrandFuchsia = Color(0xFFEC4899)
val BrandSoft = Color(0xFFA78BFA)

val TextPrimary = Color(0xFFF6F2FC)
val TextSecondary = Color(0xFFA79CBE)
val TextMuted = Color(0xFF6F6486)

val BrandGradient = Brush.linearGradient(listOf(BrandViolet, BrandFuchsia))

private val Scheme = darkColorScheme(
    primary = BrandViolet,
    onPrimary = Color.White,
    primaryContainer = SurfaceHigh,
    onPrimaryContainer = TextPrimary,
    secondary = BrandFuchsia,
    onSecondary = Color.White,
    background = Obsidian,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceHigh,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSoft,
    outlineVariant = OutlineSoft,
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun OpenBeatsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = AppTypography,
        content = content,
    )
}
