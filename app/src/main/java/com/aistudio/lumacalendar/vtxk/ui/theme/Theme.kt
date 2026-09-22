package com.aistudio.lumacalendar.vtxk.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Material color scheme derived from the resolved appearance so any `colorScheme` read also
 * follows the selection (FR-012). Glass surface/border tokens have no Material equivalent and
 * are distributed through [LocalLumaAppearance] instead.
 */
@Composable
private fun lumaColorScheme(appearance: LumaAppearance): ColorScheme = darkColorScheme(
    primary = appearance.accentPrimary,
    onPrimary = Color.Black,
    primaryContainer = appearance.accentSecondary,
    onPrimaryContainer = Color.White,
    secondary = appearance.accentSecondary,
    onSecondary = Color.Black,
    tertiary = AccentDeepViolet,
    onTertiary = Color.White,
    background = appearance.canvasBase,
    onBackground = TextWhitePrimary,
    surface = appearance.canvasMid,
    onSurface = TextWhitePrimary,
    surfaceVariant = appearance.surfaceDefault,
    onSurfaceVariant = TextWhiteSecondary,
    outline = appearance.borderDefault,
    outlineVariant = appearance.borderSubtle
)

/**
 * App-wide theme. Resolves the two stored appearance values once and provides them to every
 * consumer; there is exactly one provider, so there is exactly one source of truth (FR-011).
 */
@Composable
fun LumaCalendarTheme(
    themeName: String,
    accentColorIndex: Int,
    content: @Composable () -> Unit
) {
    val appearance = remember(themeName, accentColorIndex) {
        resolveAppearance(themeName, accentColorIndex)
    }
    CompositionLocalProvider(LocalLumaAppearance provides appearance) {
        MaterialTheme(
            colorScheme = lumaColorScheme(appearance),
            typography = Typography,
            content = content
        )
    }
}
