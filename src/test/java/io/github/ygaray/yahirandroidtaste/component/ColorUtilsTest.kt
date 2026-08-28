package io.github.ygaray.yahirandroidtaste.component

import io.github.ygaray.yahirandroidtaste.theme.DarkColorScheme
import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fixture-test anchor for [accentGradientStops] / [accentGradient] / [accentTint] (Phase 123
 * DS-01, D-02, `123-UI-SPEC.md` § "Primitive Family 3 — Gradient-Accent Surface Helpers"). Plain
 * JUnit4 — no Robolectric, no Compose runtime — mirrors [RelatednessEncodingTest]'s shape,
 * driving the real [LightColorScheme] / [DarkColorScheme] from `theme/Color.kt` so role identities
 * are the library's actual values.
 */
class ColorUtilsTest {

    // Six-sample accent sweep drawn from the existing public ACCENT_COLORS palette
    // (component/AccentColorPicker.kt) rather than invented literals.
    //
    // Includes BOTH the `.light` and `.dark` hex variant of each sampled accent (123 code-review
    // WR-01): the `.light` values alone gave zero regression signal for the "sheen is never
    // darker than the base" contract in dark theme, because they were never the color values a
    // real dark-themed consumer screen actually passes in — per AccentColorPicker.kt's
    // `if (isDark) accentColor.dark else accentColor.light`, dark theme renders the `.dark` hex,
    // not `.light`. The `.dark` variants (brighter, near-`onSurface`-luminance colors) are what
    // actually stress the invariant's margin, which is thin for bright accents like Gold.
    private val accentSweep: List<Color> = listOf(
        Color(ACCENT_COLORS[0].light),  // Red (light)
        Color(ACCENT_COLORS[0].dark),   // Red (dark)
        Color(ACCENT_COLORS[2].light),  // Purple (light)
        Color(ACCENT_COLORS[2].dark),   // Purple (dark)
        Color(ACCENT_COLORS[5].light),  // Blue (light)
        Color(ACCENT_COLORS[5].dark),   // Blue (dark)
        Color(ACCENT_COLORS[7].light),  // Green (light)
        Color(ACCENT_COLORS[7].dark),   // Green (dark)
        Color(ACCENT_COLORS[18].light), // Charcoal (light)
        Color(ACCENT_COLORS[18].dark),  // Charcoal (dark)
        Color(ACCENT_COLORS[19].light), // Gold (light)
        Color(ACCENT_COLORS[19].dark)   // Gold (dark)
    )

    private val schemes = listOf(LightColorScheme, DarkColorScheme)

    // -----------------------------------------------------------------------------------------
    // accentGradientStops
    // -----------------------------------------------------------------------------------------

    @Test
    fun `accentGradientStops returns exactly two colors with the first being the accent unchanged`() {
        schemes.forEach { scheme ->
            accentSweep.forEach { accent ->
                val stops = accentGradientStops(accent, scheme)
                assertEquals(2, stops.size)
                assertEquals(accent, stops[0])
            }
        }
    }

    @Test
    fun `accentGradientStops second stop is never darker than the first across the sweep in both schemes`() {
        schemes.forEach { scheme ->
            accentSweep.forEach { accent ->
                val stops = accentGradientStops(accent, scheme)
                assertTrue(
                    "sheen darkened for accent=$accent scheme=$scheme " +
                        "(stop0.luminance=${stops[0].luminance()}, stop1.luminance=${stops[1].luminance()})",
                    stops[1].luminance() >= stops[0].luminance()
                )
            }
        }
    }

    @Test
    fun `accentGradientStops second stop matches the AlbumAccent canvas specimen within tolerance`() {
        // Canvas specimen: .planning/design/v2.1-card-faces/AlbumAccent.dc.html line 54 —
        // `background: linear-gradient(135deg, #8E4585 0%, #BB8BB6 100%);` — the one gradient on
        // this canvas explicitly labelled "accentGradient from the album tag colour (plum)",
        // rather than one of the decorative photo-mosaic tile gradients on the same page.
        // Phase 129 DS-02, D-03 cross-check: this locks the confirmed 0.4f ratio against a real
        // canvas value so a future retune that breaks the canvas match goes red.
        val plumAccent = Color(0xFF8E4585)
        val canvasSecondStop = Color(0xFFBB8BB6)

        val stops = accentGradientStops(plumAccent, LightColorScheme)

        val tolerance = 0.02f
        assertTrue(
            "red channel drifted from canvas specimen: ${stops[1].red} vs ${canvasSecondStop.red}",
            kotlin.math.abs(stops[1].red - canvasSecondStop.red) <= tolerance
        )
        assertTrue(
            "green channel drifted from canvas specimen: ${stops[1].green} vs ${canvasSecondStop.green}",
            kotlin.math.abs(stops[1].green - canvasSecondStop.green) <= tolerance
        )
        assertTrue(
            "blue channel drifted from canvas specimen: ${stops[1].blue} vs ${canvasSecondStop.blue}",
            kotlin.math.abs(stops[1].blue - canvasSecondStop.blue) <= tolerance
        )
    }

    // -----------------------------------------------------------------------------------------
    // accentGradient
    // -----------------------------------------------------------------------------------------

    @Test
    fun `accentGradient returns a non-null Brush for every accent in the sweep in both schemes`() {
        schemes.forEach { scheme ->
            accentSweep.forEach { accent ->
                assertNotNull(accentGradient(accent, scheme))
            }
        }
    }

    // -----------------------------------------------------------------------------------------
    // accentTint
    // -----------------------------------------------------------------------------------------

    @Test
    fun `accentTint at alpha 0 equals scheme surface exactly`() {
        schemes.forEach { scheme ->
            accentSweep.forEach { accent ->
                assertEquals(scheme.surface, accentTint(accent, scheme, 0f))
            }
        }
    }

    @Test
    fun `accentTint at alpha 1 equals the accent exactly`() {
        schemes.forEach { scheme ->
            accentSweep.forEach { accent ->
                assertEquals(accent, accentTint(accent, scheme, 1f))
            }
        }
    }

    @Test
    fun `accentTint default alpha is 0_13 in light theme and 0_26 in dark theme`() {
        accentSweep.forEach { accent ->
            assertEquals(
                accentTint(accent, LightColorScheme, 0.13f),
                accentTint(accent, LightColorScheme)
            )
            assertEquals(
                accentTint(accent, DarkColorScheme, 0.26f),
                accentTint(accent, DarkColorScheme)
            )
        }
    }

    @Test
    fun `accentTint dark default alpha is strictly greater than the light default alpha`() {
        // Locks the documented "a dark surface needs a stronger tint to read" relationship
        // structurally, not just via the two magic numbers in the test above (Phase 129 DS-02,
        // D-03): the default alpha actually baked into each scheme's result is derived back out
        // of the returned Color by inverting the lerp, rather than re-asserting 0.26f > 0.13f.
        accentSweep.forEach { accent ->
            val lightAlpha = impliedDefaultAlpha(accent, LightColorScheme)
            val darkAlpha = impliedDefaultAlpha(accent, DarkColorScheme)
            assertTrue(
                "dark default alpha ($darkAlpha) must exceed light default alpha ($lightAlpha) " +
                    "for accent=$accent",
                darkAlpha > lightAlpha
            )
        }
    }

    /**
     * Inverts `accentTint`'s `lerp(surface, accent, alpha)` to recover the default [alpha] that
     * was actually applied, using whichever RGB channel has the largest surface/accent
     * separation (avoiding a near-zero denominator on any one channel).
     */
    private fun impliedDefaultAlpha(
        accent: Color,
        scheme: androidx.compose.material3.ColorScheme
    ): Float {
        val default = accentTint(accent, scheme)
        val surface = scheme.surface
        val channels = listOf(
            Triple(default.red, accent.red, surface.red),
            Triple(default.green, accent.green, surface.green),
            Triple(default.blue, accent.blue, surface.blue)
        )
        val (result, accentC, surfaceC) = channels.maxByOrNull { (_, a, s) -> kotlin.math.abs(a - s) }!!
        return (result - surfaceC) / (accentC - surfaceC)
    }

    @Test
    fun `accentTint at alpha 0 derives from the passed scheme, not a hardcoded constant`() {
        accentSweep.forEach { accent ->
            assertNotEquals(
                accentTint(accent, LightColorScheme, 0f),
                accentTint(accent, DarkColorScheme, 0f)
            )
        }
    }

    // -----------------------------------------------------------------------------------------
    // contrastingForeground regression — unchanged by this phase's additive extension
    // -----------------------------------------------------------------------------------------

    @Test
    fun `contrastingForeground regression is unchanged by this phase's additive extension`() {
        assertEquals(Color.Black, contrastingForeground(Color.White))
        assertEquals(Color.White, contrastingForeground(Color.Black))
    }
}
