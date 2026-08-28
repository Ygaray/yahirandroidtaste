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
        assertEquals(HeatTier.BRISK, heatTier(0.119f))
        assertEquals(HeatTier.MILD, heatTier(0.12f))
        assertEquals(HeatTier.MILD, heatTier(0.349f))
        assertEquals(HeatTier.WARM, heatTier(0.35f))
        assertEquals(HeatTier.WARM, heatTier(0.649f))
        assertEquals(HeatTier.HOT, heatTier(0.65f))
        assertEquals(HeatTier.HOT, heatTier(1.0f))
    }

    @Test
    fun `out-of-range input is clamped, not thrown`() {
        assertEquals(HeatTier.COOL, heatTier(-0.5f))
        assertEquals(HeatTier.HOT, heatTier(2.0f))
    }

    @Test
    fun `NaN and Infinity inputs are clamped, not thrown`() {
        // Float.NaN.coerceIn(...) alone does NOT clamp — every IEEE-754 comparison against NaN is
        // false, so the un-guarded coerceIn would silently fall through to HOT. Explicitly
        // pinning NaN to COOL ("no signal") locks the contract heatTier's own KDoc promises,
        // mirroring relatednessTier's identical discipline.
        assertEquals(HeatTier.COOL, heatTier(Float.NaN))
        assertEquals(HeatTier.HOT, heatTier(Float.POSITIVE_INFINITY))
        assertEquals(HeatTier.COOL, heatTier(Float.NEGATIVE_INFINITY))
    }

    @Test
    fun `HeatTier entries has exactly five values in declaration order`() {
        assertEquals(
            listOf(HeatTier.COOL, HeatTier.BRISK, HeatTier.MILD, HeatTier.WARM, HeatTier.HOT),
            HeatTier.entries
        )
    }

    @Test
    fun `heatVisual BRISK sample matches the pinned hex, stroke and radius per theme`() {
        // 0.10f, not 0.12f: this task's cut point is `j < 0.12f -> BRISK`, so 0.12f itself lands
        // on the boundary and falls through to MILD (MILD's cut point is untouched this task).
        // 0.10f is comfortably inside BRISK's actual 0.08-0.12 band, consistent with the boundary
        // sweep test's own `heatTier(0.119f)` BRISK assertion above.
        assertEquals(Color(0xFF38BDF8), heatVisual(0.10f, light).nodeFillColor)
        assertEquals(Color(0xFF0EA5E9), heatVisual(0.10f, dark).nodeFillColor)
        assertEquals(Color(0xFFBAE6FD), heatVisual(0.10f, light).edgeColor)
        // BRISK's dark edge derives from BRISK's own LIGHT node fill (the hub's documented rule).
        assertEquals(Color(0xFF38BDF8), heatVisual(0.10f, dark).edgeColor)
        assertEquals(1.3.dp, heatVisual(0.10f, light).edgeStrokeWidth)
        assertEquals(10.dp, heatVisual(0.10f, light).nodeRadius)
    }

    // -----------------------------------------------------------------------------------------
    // heatVisual — distinct values across tiers (color + redundant non-color channels)
    // -----------------------------------------------------------------------------------------

    @Test
    fun `heatVisual yields four distinct nodeFillColor values across the tiers`() {
        val colors = listOf(0.05f, 0.2f, 0.5f, 0.8f).map { heatVisual(it, light).nodeFillColor }
        assertEquals(4, colors.toSet().size)
    }

    @Test
    fun `heatVisual yields four distinct edgeStrokeWidth values matching the pinned scale`() {
        val widths = listOf(0.05f, 0.2f, 0.5f, 0.8f).map { heatVisual(it, light).edgeStrokeWidth }
        assertEquals(listOf(1.dp, 1.5.dp, 2.dp, 2.5.dp), widths)
    }

    @Test
    fun `heatVisual yields four strictly increasing nodeRadius values matching the pinned scale`() {
        val radii = listOf(0.05f, 0.2f, 0.5f, 0.8f).map { heatVisual(it, light).nodeRadius }
        assertEquals(listOf(8.dp, 12.dp, 16.dp, 20.dp), radii)
        for (i in 1 until radii.size) {
            assert(radii[i].value > radii[i - 1].value) { "nodeRadius not strictly increasing at index $i" }
        }
    }

    // -----------------------------------------------------------------------------------------
    // heatVisual — theme-aware, pinned per-theme hex values
    // -----------------------------------------------------------------------------------------

    @Test
    fun `heatVisual nodeFillColor differs between light and dark theme at every tier`() {
        listOf(0.05f, 0.2f, 0.5f, 0.8f).forEach { j ->
            assertNotEquals(
                "nodeFillColor did not differ by theme at jaccard=$j",
                heatVisual(j, light).nodeFillColor,
                heatVisual(j, dark).nodeFillColor
            )
        }
    }

    @Test
    fun `heatVisual nodeFillColor matches the pinned hex per tier per theme`() {
        assertEquals(Color(0xFF60A5FA), heatVisual(0.05f, light).nodeFillColor) // COOL light
        assertEquals(Color(0xFF3B82F6), heatVisual(0.05f, dark).nodeFillColor) // COOL dark
        assertEquals(Color(0xFFFBBF24), heatVisual(0.2f, light).nodeFillColor) // MILD light
        assertEquals(Color(0xFFF59E0B), heatVisual(0.2f, dark).nodeFillColor) // MILD dark
        assertEquals(Color(0xFFFB923C), heatVisual(0.5f, light).nodeFillColor) // WARM light
        assertEquals(Color(0xFFF97316), heatVisual(0.5f, dark).nodeFillColor) // WARM dark
        assertEquals(Color(0xFFEF4444), heatVisual(0.8f, light).nodeFillColor) // HOT light
        assertEquals(Color(0xFFDC2626), heatVisual(0.8f, dark).nodeFillColor) // HOT dark
    }

    @Test
    fun `heatVisual edgeColor matches the pinned light-theme hex per tier`() {
        assertEquals(Color(0xFFBFDBFE), heatVisual(0.05f, light).edgeColor) // COOL
        assertEquals(Color(0xFFFDE68A), heatVisual(0.2f, light).edgeColor) // MILD
        assertEquals(Color(0xFFFED7AA), heatVisual(0.5f, light).edgeColor) // WARM
        assertEquals(Color(0xFFFCA5A5), heatVisual(0.8f, light).edgeColor) // HOT
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
