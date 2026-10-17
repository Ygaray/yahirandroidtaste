package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.github.ygaray.yahirandroidtaste.theme.DarkColorScheme
import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/**
 * POL-02 (D-07 [dot-contrast]): the warn dot keeps its color, and a pure decision adds a contrast ring
 * only when the dot fill has less than 3:1 against the pill (`surfaceContainerHighest`). Proven on the
 * hub light and dark schemes, synthetic schemes and hand-written samples that stand in for dynamic
 * (wallpaper-derived) schemes, which a JVM test cannot generate. A translucent fill is treated as
 * opaque, because luminance ignores alpha (FA-7; documented, not tested).
 */
class HandledByDotContrastTest {

    private fun ColorScheme.pill(): Color = surfaceContainerHighest

    private fun grey(level: Int): Color = Color(level, level, level)

    private fun assertRingPasses(fill: Color, pill: Color, scheme: ColorScheme) {
        val ring = handledByDotRing(fill, pill, scheme)
        assertNotNull("fill $fill on pill $pill needs a ring", ring)
        val contrast = wcagContrast(checkNotNull(ring), pill)
        assertTrue("ring $ring has contrast $contrast against $pill, expected at least 3", contrast >= 3f)
    }

    // ---- Hub schemes ----------------------------------------------------------------------------

    @Test
    fun hubLightAndDarkDefaults_needNoRing() {
        listOf(LightColorScheme, DarkColorScheme).forEach { scheme ->
            val fill = scheme.onTertiaryContainer
            val contrast = wcagContrast(fill, scheme.pill())
            assertNull("hub fill $fill has $contrast against the pill, no ring expected", handledByDotRing(fill, scheme.pill(), scheme))
        }
    }

    // ---- Synthetic schemes ----------------------------------------------------------------------

    @Test
    fun syntheticWhiteOnLightPill_getsARing_withContrastAtLeast3() {
        val pill = Color(0xFFDCE3E5)
        val scheme = lightColorScheme(surfaceContainerHighest = pill)
        assertTrue("the fixture must fail 3:1", wcagContrast(Color.White, pill) < 3f)
        assertRingPasses(Color.White, pill, scheme)
    }

    @Test
    fun syntheticDarkOnDarkPill_getsARing() {
        val pill = Color(0xFF2A2C2E)
        val scheme = darkColorScheme(surfaceContainerHighest = pill)
        assertTrue("the fixture must fail 3:1", wcagContrast(Color(0xFF1A1C1E), pill) < 3f)
        assertRingPasses(Color(0xFF1A1C1E), pill, scheme)
    }

    @Test
    fun dynamicSamples_eitherPassOrGetAPassingRing() {
        val samples = listOf(
            // pale teal wallpaper, dark amber-brown content: passes without a ring
            lightColorScheme(
                surfaceContainerHighest = Color(0xFFDDE4E3),
                onSurface = Color(0xFF161D1D),
                tertiaryContainer = Color(0xFFD5E3FF),
                onTertiaryContainer = Color(0xFF3E2D00)
            ),
            // lavender wallpaper, mid-tone content: low contrast
            lightColorScheme(
                surfaceContainerHighest = Color(0xFFE6DEF3),
                onSurface = Color(0xFF1D1A22),
                tertiaryContainer = Color(0xFFD0BCFF),
                onTertiaryContainer = Color(0xFFB59DE6)
            ),
            // deep blue dark wallpaper, light content: passes
            darkColorScheme(
                surfaceContainerHighest = Color(0xFF33363D),
                onSurface = Color(0xFFE2E2E9),
                tertiaryContainer = Color(0xFF4B3A00),
                onTertiaryContainer = Color(0xFFFFE08C)
            ),
            // dark wallpaper, dark mid-tone content: low contrast
            darkColorScheme(
                surfaceContainerHighest = Color(0xFF3B3342),
                onSurface = Color(0xFFE8DFEE),
                tertiaryContainer = Color(0xFF5B4A00),
                onTertiaryContainer = Color(0xFF5E4F73)
            )
        )
        var withRing = 0
        samples.forEach { scheme ->
            val fill = scheme.onTertiaryContainer
            val pill = scheme.pill()
            val ring = handledByDotRing(fill, pill, scheme)
            if (ring != null) withRing++
            assertTrue(
                "fill $fill on $pill: pass or a passing ring",
                wcagContrast(fill, pill) >= 3f || (ring != null && wcagContrast(ring, pill) >= 3f)
            )
        }
        assertTrue("at least one sample must be low-contrast and need a ring", withRing >= 1)
    }

    // ---- The manual edge list (FA-7) -----------------------------------------------------------

    @Test
    fun ringIsTotal_overAGreyRamp() {
        // The repo's luminance-threshold black-or-white helper can return a color below 3:1 on a
        // mid-tone pill; the ring must not (PD-17).
        val scheme = lightColorScheme()
        (0..255).forEach { level ->
            val pill = grey(level)
            assertRingPasses(fill = pill, pill = pill, scheme = scheme)
        }
    }

    @Test
    fun threeToOneBoundary_flipsBetweenAdjacentGreys() {
        val white = Color.White
        val scheme = lightColorScheme(surfaceContainerHighest = white)
        val lightestPassing = (0..255).last { wcagContrast(grey(it), white) >= 3f }
        assertTrue("the boundary must be inside the ramp", lightestPassing in 1..254)
        assertNull(handledByDotRing(grey(lightestPassing), white, scheme))
        assertNotNull(handledByDotRing(grey(lightestPassing + 1), white, scheme))
    }

    @Test
    fun fillEqualToPill_getsARing() {
        val pill = Color(0xFFE0E0E0)
        val scheme = lightColorScheme(surfaceContainerHighest = pill)
        assertEquals(1f, wcagContrast(pill, pill), 1e-6f)
        assertRingPasses(pill, pill, scheme)
    }

    @Test
    fun darkSchemeWorks() {
        val scheme = darkColorScheme()
        val pill = scheme.pill()
        assertNull(handledByDotRing(Color.White, pill, scheme))
        assertRingPasses(pill, pill, scheme)
    }

    @Test
    fun ringPrefersOnSurface_whenItPasses_elseBlackOrWhite() {
        val pill = Color(0xFF808080)
        val onPillFailing = lightColorScheme(surfaceContainerHighest = pill, onSurface = Color(0xFF8A8A8A))
        val fallback = checkNotNull(handledByDotRing(pill, pill, onPillFailing))
        assertTrue("falls back to black or white", fallback == Color.Black || fallback == Color.White)
        val onPillPassing = lightColorScheme(surfaceContainerHighest = pill, onSurface = Color(0xFF101010))
        assertEquals(Color(0xFF101010), handledByDotRing(pill, pill, onPillPassing))
    }

    @Test
    fun mainContrastFormula_equalsTheTestSideFormula() {
        val random = Random(20271027L)
        repeat(200) {
            val a = Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
            val b = Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
            assertEquals("pair $a $b", contrastRatio(a, b), wcagContrast(a, b), 1e-6f)
        }
    }

    // ---- The dot color itself is untouched ------------------------------------------------------

    @Test
    fun handledByDotColor_isUnchanged() {
        val scheme = lightColorScheme()
        val colors = OutcomeSheetColors(
            warnTone = ToneColors(Color(0xFFFFE0B2), Color(0xFFE65100)),
            errorTone = ToneColors(Color.Red, Color.White)
        )
        assertEquals(colors.warnTone.content, handledByDotColor(true, colors, scheme))
        assertEquals(scheme.primary, handledByDotColor(false, colors, scheme))
    }

    // ---- Wiring contracts -----------------------------------------------------------------------

    private fun String.compact(): String = filterNot { it.isWhitespace() }

    private val receiptSource =
        SourceContractTestSupport.stripComments(SourceContractTestSupport.source("OutcomeSheetReceipt.kt"))

    @Test
    fun pillDotAppliesTheRingBeforeItsBackground() {
        val body = SourceContractTestSupport.functionBody(receiptSource, "fun ReceiptHandledByPill(").compact()
        assertTrue("the ring decision is computed", body.contains("handledByDotRing("))
        val ring = body.indexOf("handledByDotRingBehind(")
        val background = body.indexOf(".background(handledByDotColor(")
        assertTrue("ring modifier missing", ring >= 0)
        assertTrue("background missing", background >= 0)
        assertTrue("the ring ($ring) must precede the dot background ($background)", ring < background)
        assertTrue(body.contains("Modifier.size(Dimens.OutcomeReceipt.DotSize)"))
        assertTrue(body.contains("\"outcome_sheet_handled_by_dot\""))
    }

    @Test
    fun handledByDotColorSourceIsByteStable() {
        val expected = "internalfunhandledByDotColor(onDevice:Boolean,colors:OutcomeSheetColors," +
            "scheme:ColorScheme):Color=if(onDevice)colors.warnTone.contentelsescheme.primary"
        assertTrue(receiptSource.compact().contains(expected))
    }
}
