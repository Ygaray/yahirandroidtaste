package io.github.ygaray.yahirandroidtaste.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JUnit tests for [TactileType]/[SpaceGroteskFamily] (Phase 123 Plan 02, DS-01; extended by
 * Phase 129 Plan 02, DS-02, D-03 to lock the additive fifth [TactileType.CardTitle] tier).
 *
 * Locks all five Space Grotesk ramp tiers' exact size/weight/lineHeight/letterSpacing values,
 * proves the four Display tiers' distinct-weights and strictly-decreasing-sizes invariants, and —
 * the automated backstop for D-01 and the phase's additive-only success criterion 4 — proves
 * `theme/Type.kt`'s shared Material3 [Typography] val still resolves to [FontFamily.Default] at
 * its original, unmodified scale.
 */
class TactileTypeTest {

    @Test
    fun `DisplayLarge matches the spec'd 34sp Bold ramp tier`() {
        val style = TactileType.DisplayLarge

        assertEquals(34.sp, style.fontSize)
        assertEquals(FontWeight.Bold, style.fontWeight)
        assertEquals(40.sp, style.lineHeight)
        assertEquals((-0.25).sp, style.letterSpacing)
    }

    @Test
    fun `DisplayMedium matches the spec'd 28sp SemiBold ramp tier`() {
        val style = TactileType.DisplayMedium

        assertEquals(28.sp, style.fontSize)
        assertEquals(FontWeight.SemiBold, style.fontWeight)
        assertEquals(34.sp, style.lineHeight)
        assertEquals(0.sp, style.letterSpacing)
    }

    @Test
    fun `DisplaySmall matches the spec'd 22sp Medium ramp tier`() {
        val style = TactileType.DisplaySmall

        assertEquals(22.sp, style.fontSize)
        assertEquals(FontWeight.Medium, style.fontWeight)
        assertEquals(28.sp, style.lineHeight)
        assertEquals(0.15.sp, style.letterSpacing)
    }

    @Test
    fun `DisplayXSmall matches the spec'd 18sp Normal ramp tier`() {
        val style = TactileType.DisplayXSmall

        assertEquals(18.sp, style.fontSize)
        assertEquals(FontWeight.Normal, style.fontWeight)
        assertEquals(24.sp, style.lineHeight)
        assertEquals(0.15.sp, style.letterSpacing)
    }

    @Test
    fun `CardTitle matches the canvas-derived 18sp SemiBold ramp tier`() {
        val style = TactileType.CardTitle

        assertEquals(18.sp, style.fontSize)
        assertEquals(FontWeight.SemiBold, style.fontWeight)
        assertEquals(24.sp, style.lineHeight)
        assertEquals(0.1.sp, style.letterSpacing)
    }

    @Test
    fun `all five tiers share the same SpaceGroteskFamily instance`() {
        assertEquals(SpaceGroteskFamily, TactileType.DisplayLarge.fontFamily)
        assertEquals(SpaceGroteskFamily, TactileType.DisplayMedium.fontFamily)
        assertEquals(SpaceGroteskFamily, TactileType.DisplaySmall.fontFamily)
        assertEquals(SpaceGroteskFamily, TactileType.DisplayXSmall.fontFamily)
        assertEquals(SpaceGroteskFamily, TactileType.CardTitle.fontFamily)
    }

    @Test
    fun `the four tiers' weights form a four-element distinct set`() {
        val weights = setOf(
            TactileType.DisplayLarge.fontWeight,
            TactileType.DisplayMedium.fontWeight,
            TactileType.DisplaySmall.fontWeight,
            TactileType.DisplayXSmall.fontWeight
        )

        assertEquals(4, weights.size)
    }

    @Test
    fun `the four tiers' sizes are strictly decreasing from DisplayLarge to DisplayXSmall`() {
        val large = TactileType.DisplayLarge.fontSize.value
        val medium = TactileType.DisplayMedium.fontSize.value
        val small = TactileType.DisplaySmall.fontSize.value
        val xSmall = TactileType.DisplayXSmall.fontSize.value

        assertTrue(large > medium)
        assertTrue(medium > small)
        assertTrue(small > xSmall)
    }

    // --- Regression group: the shared Material3 Typography val must stay byte-identical ---

    @Test
    fun `Typography displayLarge is untouched by the TactileType addition`() {
        assertEquals(FontFamily.Default, Typography.displayLarge.fontFamily)
        assertEquals(57.sp, Typography.displayLarge.fontSize)
        assertEquals(FontWeight.Normal, Typography.displayLarge.fontWeight)
    }

    @Test
    fun `Typography displayMedium and displaySmall sizes are untouched`() {
        assertEquals(45.sp, Typography.displayMedium.fontSize)
        assertEquals(36.sp, Typography.displaySmall.fontSize)
    }

    @Test
    fun `Typography titleLarge weight is untouched`() {
        assertEquals(FontWeight.SemiBold, Typography.titleLarge.fontWeight)
    }

    @Test
    fun `Typography bodyLarge fontFamily is untouched`() {
        assertEquals(FontFamily.Default, Typography.bodyLarge.fontFamily)
        assertNotEquals(SpaceGroteskFamily, Typography.bodyLarge.fontFamily)
    }
}
