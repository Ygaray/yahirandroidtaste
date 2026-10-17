package io.github.ygaray.yahirandroidtaste.feedback

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
}
