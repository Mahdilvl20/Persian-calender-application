package com.aistudio.lumacalendar.vtxk.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.aistudio.lumacalendar.vtxk.notification.NotificationPreferences
import com.aistudio.lumacalendar.vtxk.ui.viewmodel.AccentPresets

/**
 * The two offered appearances. Labels and order live in Settings; this is how the stored
 * [NotificationPreferences.getThemeName] value resolves (VA-001: anything that is not an OLED
 * spelling resolves to the default Liquid Glass).
 */
enum class LumaThemeMode { LIQUID_GLASS, OLED_DEEP }

/**
 * The complete appearance the interface renders: accent pair plus every theme-dependent
 * glass token. Immutable and derived — never stored (contracts §2, VA-003).
 *
 * Tokens that are intentionally absent and must stay literal at their call sites:
 * `Category*` used as category markers, `CategorySpecial` used as the destructive/error
 * color, and all `TextWhite*` text colors (research Decision 4).
 */
data class LumaAppearance(
    val theme: LumaThemeMode,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val canvasBase: Color,
    val canvasMid: Color,
    val canvasSurface: Color,
    val surfaceUltraLight: Color,
    val surfaceDefault: Color,
    val surfaceHighlight: Color,
    val surfaceElevated: Color,
    val surfacePressed: Color,
    val borderSubtle: Color,
    val borderDefault: Color,
    val borderBright: Color,
    /** Primary ambient orb alpha. */
    val glowIntensity: Float,
    /** Multiplier applied to the secondary ambient orbs. */
    val glowScale: Float
)

// Liquid Glass == today's hardcoded values, byte for byte, so upgrading users who never
// touch the picker see no visual change (FR-013, SC-006, research Decision 3).
private val LiquidGlassTemplate = LumaAppearance(
    theme = LumaThemeMode.LIQUID_GLASS,
    accentPrimary = AccentElectricBlue,
    accentSecondary = AccentRoyalViolet,
    canvasBase = CanvasBlack,
    canvasMid = CanvasNavy,
    canvasSurface = CanvasSurface,
    surfaceUltraLight = GlassSurfaceUltraLight,
    surfaceDefault = GlassSurfaceDefault,
    surfaceHighlight = GlassSurfaceHighlight,
    surfaceElevated = GlassSurfaceElevated,
    surfacePressed = GlassSurfacePressed,
    borderSubtle = GlassBorderSubtle,
    borderDefault = GlassBorderDefault,
    borderBright = GlassBorderBright,
    glowIntensity = 0.35f,
    glowScale = 1.0f
)

// OLED Deep: near-black backdrop, surfaces one step lower in lift, borders subtler, weaker
// glow. Same token names, no new translucency vocabulary (constitution III).
private val OledDeepTemplate = LumaAppearance(
    theme = LumaThemeMode.OLED_DEEP,
    accentPrimary = AccentElectricBlue,
    accentSecondary = AccentRoyalViolet,
    canvasBase = Color.Black,
    canvasMid = Color.Black,
    canvasSurface = Color(0xFF050609),
    surfaceUltraLight = Color(0x0DFFFFFF), // 5%
    surfaceDefault = Color(0x12FFFFFF),    // 7%
    surfaceHighlight = Color(0x1FFFFFFF),  // 12%
    surfaceElevated = Color(0x26FFFFFF),   // 15%
    surfacePressed = Color(0x2EFFFFFF),    // 18%
    borderSubtle = Color(0x14FFFFFF),      // 8%
    borderDefault = Color(0x26FFFFFF),     // 15%
    borderBright = Color(0x40FFFFFF),      // 25%
    glowIntensity = 0.18f,
    glowScale = 0.5f
)

/**
 * Pure resolver: stored theme name + stored accent index -> the appearance to render.
 *
 * VA-001 unknown theme falls back to the default; VA-002 an out-of-range accent index is
 * constrained to a valid preset; VA-003 same inputs always yield an equal result and the
 * call never mutates anything it returned earlier.
 */
fun resolveAppearance(themeName: String, accentIndex: Int): LumaAppearance {
    val preset = AccentPresets[accentIndex.coerceIn(0, AccentPresets.lastIndex)]
    val template = if (themeName.contains("OLED", ignoreCase = true)) {
        OledDeepTemplate
    } else {
        LiquidGlassTemplate
    }
    return template.copy(
        accentPrimary = preset.primary,
        accentSecondary = preset.secondary
    )
}

/**
 * Single distribution point for appearance (contracts §3). Read during composition so a
 * change recomposes every consumer — no navigation or restart required.
 */
val LocalLumaAppearance = compositionLocalOf {
    resolveAppearance(NotificationPreferences.DEFAULT_THEME_NAME, 0)
}
