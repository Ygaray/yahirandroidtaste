package io.github.ygaray.yahirandroidtaste.component

import io.github.ygaray.yahirandroidtaste.theme.LightColorScheme
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Fixture-test anchor for [relatednessTier] / [relatednessVisual] (VISUAL-01/02/03, keystone
 * Task 1, `91-UI-SPEC.md §1/§2/§4/§5`). Plain JUnit4 — no Robolectric, no Compose runtime — this
 * uses the real [LightColorScheme] (pure JVM math, mirrors [TertiaryContainerContrastTest]'s
 * established pattern) so role identities are the app's actual, distinct values.
 */
class RelatednessEncodingTest {

    private val scheme = LightColorScheme

    // -----------------------------------------------------------------------------------------
    // relatednessTier — bucket boundaries + clamping
    // -----------------------------------------------------------------------------------------

    @Test
    fun `tier boundary sweep matches pinned cut points`() {
        assertEquals(RelatednessTier.MINIMAL, relatednessTier(0.0f))
        assertEquals(RelatednessTier.MINIMAL, relatednessTier(0.119f))
        assertEquals(RelatednessTier.WEAK, relatednessTier(0.12f))
        assertEquals(RelatednessTier.WEAK, relatednessTier(0.349f))
        assertEquals(RelatednessTier.RELATED, relatednessTier(0.35f))
        assertEquals(RelatednessTier.RELATED, relatednessTier(0.649f))
        assertEquals(RelatednessTier.STRONG, relatednessTier(0.65f))
        assertEquals(RelatednessTier.STRONG, relatednessTier(1.0f))
    }

    @Test
    fun `out-of-range input is clamped, not thrown`() {
        assertEquals(RelatednessTier.MINIMAL, relatednessTier(-0.5f))
        assertEquals(RelatednessTier.STRONG, relatednessTier(2.0f))
    }

    @Test
    fun `NaN and Infinity inputs are clamped, not thrown`() {
        // Float.NaN.coerceIn(...) alone does NOT clamp — every IEEE-754 comparison against NaN is
        // false, so the un-guarded coerceIn would silently fall through to STRONG. Explicitly
        // pinning NaN to MINIMAL ("no signal") locks the contract this shared function's own KDoc
        // promises to Phase 94's mindmap consumer (WR-01).
        assertEquals(RelatednessTier.MINIMAL, relatednessTier(Float.NaN))
        assertEquals(RelatednessTier.STRONG, relatednessTier(Float.POSITIVE_INFINITY))
        assertEquals(RelatednessTier.MINIMAL, relatednessTier(Float.NEGATIVE_INFINITY))
    }

    // -----------------------------------------------------------------------------------------
    // relatednessVisual — jaccard-not-sharedCount fixture (VISUAL-03)
    // -----------------------------------------------------------------------------------------

    @Test
    fun `equal jaccard yields identical visual regardless of any sharedCount context`() {
        // relatednessVisual's only parameter is jaccard: Float — a count can never reach it.
        // Two hypothetical TagWithRelatedness rows with the same jaccard but different
        // sharedCount would call this function identically; simulate that directly.
        val jaccard = 0.42f
        val visualFromRowWithLowSharedCount = relatednessVisual(jaccard, scheme)
        val visualFromRowWithHighSharedCount = relatednessVisual(jaccard, scheme)

        assertEquals(visualFromRowWithLowSharedCount, visualFromRowWithHighSharedCount)
    }

    // -----------------------------------------------------------------------------------------
    // relatednessVisual — never the reserved isSelected role
    // -----------------------------------------------------------------------------------------

    @Test
    fun `containerColor never equals the reserved secondaryContainer pair across a sweep`() {
        val sweep = listOf(0.0f, 0.05f, 0.11f, 0.12f, 0.2f, 0.34f, 0.35f, 0.5f, 0.64f, 0.65f, 0.8f, 1.0f)
        sweep.forEach { j ->
            val visual = relatednessVisual(j, scheme)
            assertNotEquals(
                "containerColor collided with secondaryContainer at jaccard=$j",
                scheme.secondaryContainer,
                visual.containerColor
            )
            assertNotEquals(
                "contentColor collided with onSecondaryContainer at jaccard=$j",
                scheme.onSecondaryContainer,
                visual.contentColor
            )
        }
    }

    // -----------------------------------------------------------------------------------------
    // Redundant non-color channel — border width (4 distinct values) + font weight (low/high)
    // -----------------------------------------------------------------------------------------

    @Test
    fun `border width takes 4 distinct values across the tiers`() {
        val widths = listOf(
            relatednessVisual(0.0f, scheme).borderStroke.width,
            relatednessVisual(0.2f, scheme).borderStroke.width,
            relatednessVisual(0.5f, scheme).borderStroke.width,
            relatednessVisual(0.8f, scheme).borderStroke.width
        )
        assertEquals(4, widths.toSet().size)
    }

    @Test
    fun `font weight is Normal for Minimal and Weak, SemiBold for Related and Strong`() {
        assertEquals(FontWeight.Normal, relatednessVisual(0.0f, scheme).fontWeight) // Minimal
        assertEquals(FontWeight.Normal, relatednessVisual(0.2f, scheme).fontWeight) // Weak
        assertEquals(FontWeight.SemiBold, relatednessVisual(0.5f, scheme).fontWeight) // Related
        assertEquals(FontWeight.SemiBold, relatednessVisual(0.8f, scheme).fontWeight) // Strong
    }

    // -----------------------------------------------------------------------------------------
    // MINIMAL == AppChip's shipped unselected default (contrast-floor mitigation)
    // -----------------------------------------------------------------------------------------

    @Test
    fun `MINIMAL tier pairing equals AppChip's existing unselected default`() {
        val visual = relatednessVisual(0.0f, scheme)
        assertEquals(scheme.surface, visual.containerColor)
        assertEquals(scheme.onSurfaceVariant, visual.contentColor)
    }

    // -----------------------------------------------------------------------------------------
    // WEAK/RELATED/STRONG container-color ramp — pinned verbatim by 91-UI-SPEC.md §2 (VISUAL-01)
    // -----------------------------------------------------------------------------------------

    @Test
    fun `WEAK tier pairing is surfaceVariant on onSurfaceVariant`() {
        val visual = relatednessVisual(0.2f, scheme)
        assertEquals(scheme.surfaceVariant, visual.containerColor)
        assertEquals(scheme.onSurfaceVariant, visual.contentColor)
    }

    @Test
    fun `RELATED tier pairing is primaryContainer on onPrimaryContainer`() {
        val visual = relatednessVisual(0.5f, scheme)
        assertEquals(scheme.primaryContainer, visual.containerColor)
        assertEquals(scheme.onPrimaryContainer, visual.contentColor)
    }

    @Test
    fun `STRONG tier pairing is primary on onPrimary`() {
        val visual = relatednessVisual(0.8f, scheme)
        assertEquals(scheme.primary, visual.containerColor)
        assertEquals(scheme.onPrimary, visual.contentColor)
    }

    // -----------------------------------------------------------------------------------------
    // Border color is pinned to outline at every tier — only width varies
    // -----------------------------------------------------------------------------------------

    @Test
    fun `border color is outline at every tier`() {
        listOf(0.0f, 0.2f, 0.5f, 0.8f).forEach { j ->
            val brush = relatednessVisual(j, scheme).borderStroke.brush
            assertEquals(scheme.outline, (brush as SolidColor).value)
        }
    }
}
