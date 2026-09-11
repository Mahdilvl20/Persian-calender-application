package com.aistudio.lumacalendar.vtxk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LumaDarkColorScheme = darkColorScheme(
    primary = AccentElectricBlue,
    onPrimary = Color.Black,
    primaryContainer = AccentRoyalViolet,
    onPrimaryContainer = Color.White,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    tertiary = AccentDeepViolet,
    onTertiary = Color.White,
    background = CanvasBlack,
    onBackground = TextWhitePrimary,
    surface = CanvasNavy,
    onSurface = TextWhitePrimary,
    surfaceVariant = GlassSurfaceDefault,
    onSurfaceVariant = TextWhiteSecondary,
    outline = GlassBorderDefault,
    outlineVariant = GlassBorderSubtle
)

@Composable
fun LumaCalendarTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LumaDarkColorScheme,
        typography = Typography,
        content = content
    )
}

