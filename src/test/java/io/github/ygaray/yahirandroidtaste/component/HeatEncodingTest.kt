package io.github.ygaray.yahirandroidtaste.component

import io.github.ygaray.yahirandroidtaste.theme.DarkColorScheme
import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Fixture-test anchor for [heatTier] / [heatVisual] / [hubNodeVisual] (Phase 123 DS-01, D-03,
 * `123-UI-SPEC.md` § "Primitive Family 4"). Plain JUnit4 — no Robolectric, no Compose runtime —
 * mirrors [RelatednessEncodingTest]'s structure and idioms (real [LightColorScheme]/
 * [DarkColorScheme], section-comment banners, the `(brush as SolidColor).value` idiom). A NEW
 * file rather than an extension of `RelatednessEncodingTest.kt` — that keeps every pre-existing
 * Jaccard assertion byte-identical, which is the additive-only proof for this phase's
 * highest-risk file.
 */
class HeatEncodingTest {

    private val light = LightColorScheme
    private val dark = DarkColorScheme

    // -----------------------------------------------------------------------------------------
    // heatTier — bucket boundaries + clamping
    // -----------------------------------------------------------------------------------------

    @Test
    fun `tier boundary sweep matches pinned cut points`() {
        assertEquals(HeatTier.COOL, heatTier(0.0f))
        assertEquals(HeatTier.COOL, heatTier(0.079f))
        assertEquals(HeatTier.BRISK, heatTier(0.08f))
        assertEquals(HeatTier.BRISK, heatTier(0.179f))
        assertEquals(HeatTier.MILD, heatTier(0.18f))
        assertEquals(HeatTier.MILD, heatTier(0.299f))
        assertEquals(HeatTier.WARM, heatTier(0.30f))
        assertEquals(HeatTier.WARM, heatTier(0.449f))
        assertEquals(HeatTier.HOT, heatTier(0.45f))
        assertEquals(HeatTier.HOT, heatTier(0.649f))
        assertEquals(HeatTier.BLAZING, heatTier(0.65f))
        assertEquals(HeatTier.BLAZING, heatTier(1.0f))
    }

    @Test
    fun `out-of-range input is clamped, not thrown`() {
        assertEquals(HeatTier.COOL, heatTier(-0.5f))
        assertEquals(HeatTier.BLAZING, heatTier(2.0f))
    }

    @Test
    fun `NaN and Infinity inputs are clamped, not thrown`() {
        // Float.NaN.coerceIn(...) alone does NOT clamp — every IEEE-754 comparison against NaN is
        // false, so the un-guarded coerceIn would silently fall through to the top tier. Explicitly
        // pinning NaN to COOL ("no signal") locks the contract heatTier's own KDoc promises,
        // mirroring relatednessTier's identical discipline.
        assertEquals(HeatTier.COOL, heatTier(Float.NaN))
        assertEquals(HeatTier.BLAZING, heatTier(Float.POSITIVE_INFINITY))
        assertEquals(HeatTier.COOL, heatTier(Float.NEGATIVE_INFINITY))
    }

    @Test
    fun `HeatTier entries has exactly six values in declaration order`() {
        assertEquals(
            listOf(
                HeatTier.COOL,
                HeatTier.BRISK,
                HeatTier.MILD,
                HeatTier.WARM,
                HeatTier.HOT,
                HeatTier.BLAZING
            ),
            HeatTier.entries
        )
    }

    @Test
    fun `hub-fill determinism guard — heatTier(1_0f) resolves to the top tier`() {
        // The consumer's fixed hub-fill lookup (heatVisual(1.0f, ...)) needs zero special-casing:
        // jaccard=1.0 must always resolve to whichever tier is highest, with no new branch.
        assertEquals(HeatTier.BLAZING, heatTier(1.0f))
    }

    // -----------------------------------------------------------------------------------------
    // heatVisual — distinct values across tiers (color + redundant non-color channels)
    // -----------------------------------------------------------------------------------------

    // One sample comfortably inside each of the six locked bands.
    private val sixTierSamples = listOf(0.04f, 0.12f, 0.24f, 0.37f, 0.55f, 0.80f)

    @Test
    fun `heatVisual yields six distinct nodeFillColor values across the tiers`() {
        val colors = sixTierSamples.map { heatVisual(it, light).nodeFillColor }
        assertEquals(6, colors.toSet().size)
    }

    @Test
    fun `heatVisual yields six distinct edgeStrokeWidth values matching the pinned scale`() {
        val widths = sixTierSamples.map { heatVisual(it, light).edgeStrokeWidth }
        assertEquals(listOf(1.dp, 1.3.dp, 1.6.dp, 1.9.dp, 2.2.dp, 2.5.dp), widths)
    }

    @Test
    fun `heatVisual yields six strictly increasing nodeRadius values matching the pinned scale, capped at 20dp`() {
        val radii = sixTierSamples.map { heatVisual(it, light).nodeRadius }
        assertEquals(listOf(8.dp, 10.dp, 13.dp, 15.dp, 18.dp, 20.dp), radii)
        for (i in 1 until radii.size) {
            assert(radii[i].value > radii[i - 1].value) { "nodeRadius not strictly increasing at index $i" }
        }
        // Radius ceiling guard: the consumer's fixed 28dp hub node must stay the largest on screen.
        assertEquals(20.dp, radii.max())
    }

    // -----------------------------------------------------------------------------------------
    // heatVisual — theme-aware, pinned per-theme hex values
    // -----------------------------------------------------------------------------------------

    @Test
    fun `heatVisual nodeFillColor differs between light and dark theme at every tier`() {
        sixTierSamples.forEach { j ->
            assertNotEquals(
                "nodeFillColor did not differ by theme at jaccard=$j",
                heatVisual(j, light).nodeFillColor,
                heatVisual(j, dark).nodeFillColor
            )
        }
    }

    @Test
    fun `heatVisual nodeFillColor matches the pinned hex per tier per theme`() {
        assertEquals(Color(0xFF60A5FA), heatVisual(0.04f, light).nodeFillColor) // COOL light
        assertEquals(Color(0xFF3B82F6), heatVisual(0.04f, dark).nodeFillColor) // COOL dark
        assertEquals(Color(0xFF38BDF8), heatVisual(0.12f, light).nodeFillColor) // BRISK light
        assertEquals(Color(0xFF0EA5E9), heatVisual(0.12f, dark).nodeFillColor) // BRISK dark
        assertEquals(Color(0xFFFBBF24), heatVisual(0.24f, light).nodeFillColor) // MILD light
        assertEquals(Color(0xFFF59E0B), heatVisual(0.24f, dark).nodeFillColor) // MILD dark
        assertEquals(Color(0xFFFB923C), heatVisual(0.37f, light).nodeFillColor) // WARM light
        assertEquals(Color(0xFFF97316), heatVisual(0.37f, dark).nodeFillColor) // WARM dark
        assertEquals(Color(0xFFEF4444), heatVisual(0.55f, light).nodeFillColor) // HOT light
        assertEquals(Color(0xFFDC2626), heatVisual(0.55f, dark).nodeFillColor) // HOT dark
        assertEquals(Color(0xFFDC2626), heatVisual(0.80f, light).nodeFillColor) // BLAZING light
        assertEquals(Color(0xFFB91C1C), heatVisual(0.80f, dark).nodeFillColor) // BLAZING dark
    }

    @Test
    fun `heatVisual edgeColor matches the pinned light-theme hex per tier`() {
        assertEquals(Color(0xFFBFDBFE), heatVisual(0.04f, light).edgeColor) // COOL
        assertEquals(Color(0xFFBAE6FD), heatVisual(0.12f, light).edgeColor) // BRISK
        assertEquals(Color(0xFFFDE68A), heatVisual(0.24f, light).edgeColor) // MILD
        assertEquals(Color(0xFFFED7AA), heatVisual(0.37f, light).edgeColor) // WARM
        assertEquals(Color(0xFFFCA5A5), heatVisual(0.55f, light).edgeColor) // HOT
        assertEquals(Color(0xFFFECACA), heatVisual(0.80f, light).edgeColor) // BLAZING
    }

    @Test
    fun `heatVisual dark edgeColor equals that same tier's light nodeFillColor, for all six tiers`() {
        // Asserted as a rule over all six samples (not six independent hex literals) so a future
        // tier addition inherits the check automatically.
        sixTierSamples.forEach { j ->
            assertEquals(
                "dark edgeColor did not derive from this tier's own light nodeFillColor at jaccard=$j",
                heatVisual(j, light).nodeFillColor,
                heatVisual(j, dark).edgeColor
            )
        }
    }

    // -----------------------------------------------------------------------------------------
    // hubNodeVisual — orthogonal ring, live theme role
    // -----------------------------------------------------------------------------------------

    @Test
    fun `hubNodeVisual width is 2dp and color resolves to scheme primary in both themes`() {
        listOf(light, dark).forEach { scheme ->
            val stroke = hubNodeVisual(scheme)
            assertEquals(2.dp, stroke.width)
            assertEquals(scheme.primary, (stroke.brush as SolidColor).value)
        }
    }

    // -----------------------------------------------------------------------------------------
    // Coexistence backstop — the Jaccard ramp was never rewired
    // -----------------------------------------------------------------------------------------

    @Test
    fun `relatednessTier still buckets independently of the new Heat ramp`() {
        assertEquals(RelatednessTier.WEAK, relatednessTier(0.12f))
        assertEquals(RelatednessTier.MINIMAL, relatednessTier(Float.NaN))
    }
}
