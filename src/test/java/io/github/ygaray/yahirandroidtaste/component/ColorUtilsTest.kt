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
    private val accentSweep: List<Color> = listOf(
        Color(ACCENT_COLORS[0].light),  // Red
        Color(ACCENT_COLORS[2].light),  // Purple
        Color(ACCENT_COLORS[5].light),  // Blue
        Color(ACCENT_COLORS[7].light),  // Green
        Color(ACCENT_COLORS[18].light), // Charcoal
        Color(ACCENT_COLORS[19].light)  // Gold
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
    fun `accentTint default alpha is 0_08 in light theme and 0_16 in dark theme`() {
        accentSweep.forEach { accent ->
            assertEquals(
                accentTint(accent, LightColorScheme, 0.08f),
                accentTint(accent, LightColorScheme)
            )
            assertEquals(
                accentTint(accent, DarkColorScheme, 0.16f),
                accentTint(accent, DarkColorScheme)
            )
        }
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
