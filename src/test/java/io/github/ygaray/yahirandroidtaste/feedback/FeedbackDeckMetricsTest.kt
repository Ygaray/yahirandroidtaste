package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.ui.unit.Constraints
import org.junit.Assert.assertEquals
import org.junit.Test

/** Plain-JVM pins for the pure deck geometry in `FeedbackDeckMetrics.kt`. */
class FeedbackDeckMetricsTest {

    // ---- Undo width cap (D-01 [card-layout]) ----

    @Test
    fun undoMaxWidth_shortLabelKeepsNaturalWidth() {
        assertEquals(80, feedbackUndoMaxWidth(rowWidth = 336, naturalWidth = 80, minIntrinsicWidth = 60))
    }

    @Test
    fun undoMaxWidth_longLabelCapsAtFortyPercent() {
        assertEquals(134, feedbackUndoMaxWidth(rowWidth = 336, naturalWidth = 300, minIntrinsicWidth = 60))
    }

    @Test
    fun undoMaxWidth_widensToTheLongestWord() {
        assertEquals(150, feedbackUndoMaxWidth(rowWidth = 336, naturalWidth = 300, minIntrinsicWidth = 150))
    }

    @Test
    fun undoMaxWidth_neverExceedsSixtyPercent() {
        assertEquals(201, feedbackUndoMaxWidth(rowWidth = 336, naturalWidth = 300, minIntrinsicWidth = 280))
    }

    // ---- Depth scale, alpha and collapsed height (D-01 Direction A) ----

    @Test
    fun depthScale_is094And088() {
        assertEquals(1f, feedbackDepthScale(0), 0.0001f)
        assertEquals(0.94f, feedbackDepthScale(1), 0.0001f)
        assertEquals(0.88f, feedbackDepthScale(2), 0.0001f)
    }

    @Test
    fun depthAlpha_isOneNinetyAndSeventyFive() {
        assertEquals(1f, feedbackDepthAlpha(0), 0.0001f)
        assertEquals(0.9f, feedbackDepthAlpha(1), 0.0001f)
        assertEquals(0.75f, feedbackDepthAlpha(2), 0.0001f)
        assertEquals(0.75f, feedbackDepthAlpha(5), 0.0001f)
    }

    @Test
    fun collapsedHeight_isFrontPlusPeeks() {
        assertEquals(88, feedbackCollapsedHeight(frontHeight = 60, count = 3, peekPx = 14))
        assertEquals(60, feedbackCollapsedHeight(frontHeight = 60, count = 1, peekPx = 14))
        assertEquals(0, feedbackCollapsedHeight(frontHeight = 60, count = 0, peekPx = 14))
    }

    // ---- Fanned geometry and cap (D-08 [fanned-overflow]) ----

    @Test
    fun fannedHeight_sumsHeightsAndGaps() {
        assertEquals(196, feedbackFannedHeight(listOf(60, 60, 60), gapPx = 8))
        assertEquals(60, feedbackFannedHeight(listOf(60), gapPx = 8))
        assertEquals(0, feedbackFannedHeight(emptyList(), gapPx = 8))
    }

    @Test
    fun fannedCap_isSixtyPercentOfAvailable() {
        assertEquals(600, feedbackFannedCap(available = 1000, imeBottom = 0))
    }

    @Test
    fun fannedCap_subtractsTheImeBottom() {
        assertEquals(500, feedbackFannedCap(available = 1000, imeBottom = 500))
        assertEquals(600, feedbackFannedCap(available = 1000, imeBottom = 0))
    }

    @Test
    fun fannedCap_ofUnboundedHeight_isUnbounded() {
        assertEquals(Int.MAX_VALUE, feedbackFannedCap(available = Constraints.Infinity, imeBottom = 300))
    }

    @Test
    fun fannedCap_neverNegative() {
        assertEquals(0, feedbackFannedCap(available = 100, imeBottom = 500))
        assertEquals(0, feedbackFannedCap(available = 0, imeBottom = 0))
    }

    @Test
    fun lerp_interpolatesBothEndsAndTheMiddle() {
        assertEquals(10, feedbackLerp(10, 30, 0f))
        assertEquals(30, feedbackLerp(10, 30, 1f))
        assertEquals(20, feedbackLerp(10, 30, 0.5f))
        assertEquals(0.9f, feedbackLerp(0.8f, 1f, 0.5f), 0.0001f)
    }
}
