package com.aistudio.lumacalendar.vtxk

import com.aistudio.lumacalendar.vtxk.ui.theme.AccentElectricBlue
import com.aistudio.lumacalendar.vtxk.ui.theme.AccentRoyalViolet
import com.aistudio.lumacalendar.vtxk.ui.theme.CanvasBlack
import com.aistudio.lumacalendar.vtxk.ui.theme.CanvasNavy
import com.aistudio.lumacalendar.vtxk.ui.theme.CanvasSurface
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassBorderBright
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassBorderDefault
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassBorderSubtle
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassSurfaceDefault
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassSurfaceElevated
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassSurfaceHighlight
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassSurfacePressed
import com.aistudio.lumacalendar.vtxk.ui.theme.GlassSurfaceUltraLight
import com.aistudio.lumacalendar.vtxk.ui.theme.LumaThemeMode
import com.aistudio.lumacalendar.vtxk.ui.theme.resolveAppearance
import com.aistudio.lumacalendar.vtxk.ui.viewmodel.AccentPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec 010 SC-001/SC-002/SC-005 + contract §2: the resolver is what turns the two stored
 * values into the appearance the app renders. These tests are the executable form of
 * VA-001 (unknown theme → default), VA-002 (accent clamped), VA-003 (purity) and
 * Decision 2 (accent role mapping).
 */
class AppearanceTest {

    // --- SC-001: theme selection changes the effective theme ---

    @Test
    fun liquidGlassAndOledDeepProduceDifferentAppearances() {
        val liquid = resolveAppearance("Liquid Glass (Dark)", 0)
        val oled = resolveAppearance("OLED Deep", 0)

        assertEquals(LumaThemeMode.LIQUID_GLASS, liquid.theme)
        assertEquals(LumaThemeMode.OLED_DEEP, oled.theme)

        assertNotEquals("canvas must differ between themes", liquid.canvasBase, oled.canvasBase)
        assertNotEquals("mid canvas must differ between themes", liquid.canvasMid, oled.canvasMid)
        assertNotEquals("surfaces must differ between themes", liquid.surfaceDefault, oled.surfaceDefault)
        assertNotEquals("borders must differ between themes", liquid.borderDefault, oled.borderDefault)
        assertNotEquals("glow must differ between themes", liquid.glowIntensity, oled.glowIntensity)
        assertNotEquals("glow scale must differ between themes", liquid.glowScale, oled.glowScale)
    }

    @Test
    fun oledDeepIsDarkerThanLiquidGlass() {
        val liquid = resolveAppearance("Liquid Glass (Dark)", 0)
        val oled = resolveAppearance("OLED Deep", 0)

        assertTrue("OLED canvas must be at least as dark as Liquid Glass",
            oled.canvasBase.red <= liquid.canvasBase.red &&
                oled.canvasBase.green <= liquid.canvasBase.green &&
                oled.canvasBase.blue <= liquid.canvasBase.blue)
        assertTrue("OLED surfaces must not be more lifted than Liquid Glass",
            oled.surfaceDefault.alpha <= liquid.surfaceDefault.alpha)
        assertTrue("OLED glow must be weaker", oled.glowIntensity < liquid.glowIntensity)
    }

    // --- SC-002: accent selection changes the effective accent ---

    @Test
    fun everyAccentIndexResolvesToItsPresetPair() {
        AccentPresets.forEachIndexed { index, preset ->
            val appearance = resolveAppearance("Liquid Glass (Dark)", index)
            assertEquals("primary for accent $index", preset.primary, appearance.accentPrimary)
            assertEquals("secondary for accent $index", preset.secondary, appearance.accentSecondary)
        }
    }

    @Test
    fun everyAccentIndexProducesADistinctPrimary() {
        val primaries = AccentPresets.indices.map { resolveAppearance("Liquid Glass (Dark)", it).accentPrimary }
        assertEquals(AccentPresets.size, primaries.toSet().size)
    }

    @Test
    fun accentChangesRegardlessOfTheme() {
        val a = resolveAppearance("OLED Deep", 0)
        val b = resolveAppearance("OLED Deep", 4)
        assertNotEquals(a.accentPrimary, b.accentPrimary)
        assertEquals(LumaThemeMode.OLED_DEEP, a.theme)
        assertEquals(LumaThemeMode.OLED_DEEP, b.theme)
    }

    // --- VA-003: purity / non-mutation ---

    @Test
    fun resolutionIsDeterministic() {
        assertEquals(
            resolveAppearance("OLED Deep", 2),
            resolveAppearance("OLED Deep", 2)
        )
        assertEquals(
            resolveAppearance("Liquid Glass (Dark)", 0),
            resolveAppearance("Liquid Glass (Dark)", 0)
        )
    }

    @Test
    fun resolvingOneInputNeverAltersAPreviouslyResolvedValue() {
        val first = resolveAppearance("Liquid Glass (Dark)", 1)
        val snapshot = first.copy()
        resolveAppearance("OLED Deep", 4)
        resolveAppearance("Liquid Glass (Dark)", 3)
        assertEquals(snapshot, first)
    }

    // --- VA-001 / VA-002: fallbacks ---

    @Test
    fun unknownThemeFallsBackToDefault() {
        assertEquals(LumaThemeMode.LIQUID_GLASS, resolveAppearance("NotARealTheme", 0).theme)
        assertEquals(LumaThemeMode.LIQUID_GLASS, resolveAppearance("", 0).theme)
        // Existing stored spellings must both resolve to Liquid Glass
        assertEquals(LumaThemeMode.LIQUID_GLASS, resolveAppearance("Liquid Glass", 0).theme)
        assertEquals(LumaThemeMode.LIQUID_GLASS, resolveAppearance("Liquid Glass (Dark)", 0).theme)
        assertEquals(LumaThemeMode.OLED_DEEP, resolveAppearance("OLED Deep", 0).theme)
    }

    @Test
    fun outOfRangeAccentIndexIsConstrainedToAValidAccent() {
        val tooHigh = resolveAppearance("Liquid Glass (Dark)", 99)
        val tooLow = resolveAppearance("Liquid Glass (Dark)", -7)
        val maxIndex = AccentPresets.lastIndex

        assertEquals(AccentPresets[maxIndex].primary, tooHigh.accentPrimary)
        assertEquals(AccentPresets[0].primary, tooLow.accentPrimary)
    }

    // --- FR-013 / Decision 2: baseline stability ---

    @Test
    fun defaultSelectionReproducesTodayAppearanceExactly() {
        val baseline = resolveAppearance("Liquid Glass (Dark)", 0)

        // Accent pair must be the tokens the app hardcoded before this change
        assertEquals(AccentElectricBlue, baseline.accentPrimary)
        assertEquals(AccentRoyalViolet, baseline.accentSecondary)

        // Canvas
        assertEquals(CanvasBlack, baseline.canvasBase)
        assertEquals(CanvasNavy, baseline.canvasMid)
        assertEquals(CanvasSurface, baseline.canvasSurface)

        // Glass surfaces
        assertEquals(GlassSurfaceUltraLight, baseline.surfaceUltraLight)
        assertEquals(GlassSurfaceDefault, baseline.surfaceDefault)
        assertEquals(GlassSurfaceHighlight, baseline.surfaceHighlight)
        assertEquals(GlassSurfaceElevated, baseline.surfaceElevated)
        assertEquals(GlassSurfacePressed, baseline.surfacePressed)

        // Glass borders
        assertEquals(GlassBorderSubtle, baseline.borderSubtle)
        assertEquals(GlassBorderDefault, baseline.borderDefault)
        assertEquals(GlassBorderBright, baseline.borderBright)

        // Ambient glow
        assertEquals(0.35f, baseline.glowIntensity, 0.0001f)
        assertEquals(1.0f, baseline.glowScale, 0.0001f)
    }

    @Test
    fun oledGlowMatchesThePreExistingOledValue() {
        assertEquals(0.18f, resolveAppearance("OLED Deep", 0).glowIntensity, 0.0001f)
        assertEquals(0.5f, resolveAppearance("OLED Deep", 0).glowScale, 0.0001f)
    }
}
