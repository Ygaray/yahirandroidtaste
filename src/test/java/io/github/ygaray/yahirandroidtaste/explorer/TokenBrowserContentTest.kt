package io.github.ygaray.yahirandroidtaste.explorer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * EXPLORE-03 completeness gate: every hand-authored token table in [TokenSwatches.kt] must be
 * non-empty (with a vacuous-pass guard first) so an empty token category cannot ship silently.
 * `shapeEntries()` is intentionally NOT covered here — it is `@Composable` and needs a
 * composition context; its shape is left to Gate-1 device-based visual verification.
 */
class TokenBrowserContentTest {

    @Test
    fun colorSwatchesIsNotEmpty_vacuousPassGuard() {
        assertTrue(
            "colorSwatches is empty — completeness assertions below would vacuously pass " +
                "against an empty list. Failing loudly instead.",
            colorSwatches.isNotEmpty()
        )
    }

    @Test
    fun everyColorSwatch_hasNonBlankName() {
        val offenders = colorSwatches.filter { it.name.isBlank() }
        if (offenders.isNotEmpty()) {
            fail("colorSwatches has ${offenders.size} entries with a blank name.")
        }
    }

    @Test
    fun typeScaleEntries_hasExactlyFifteenEntries() {
        assertEquals(
            "typeScaleEntries must enumerate all 15 named M3 Typography styles.",
            15,
            typeScaleEntries.size
        )
    }

    @Test
    fun dimensEntriesIsNotEmpty() {
        assertTrue(
            "dimensEntries is empty — no Dimens tokens are authored.",
            dimensEntries.isNotEmpty()
        )
    }

    @Test
    fun elevationLevels_hasExactlySevenEntries() {
        assertEquals(
            "elevationLevels must enumerate all 7 SurfaceContainer*/SurfaceDim*/SurfaceBright* " +
                "ramp levels.",
            7,
            elevationLevels.size
        )
    }
}
