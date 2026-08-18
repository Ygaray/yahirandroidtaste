package com.example.secondbrain.yahirandroidtaste.modifier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure [thresholdSide] helper — D-08 side-change haptic contract —
 * and the pure [installedAnchors] helper — D-15 delete-only anchor-gate contract.
 *
 * Both helpers are JVM-only (no Compose runtime), so these run as plain JUnit4 tests.
 *
 * [thresholdSide] regression (D-15 must not change this contract):
 *  1. Zero offset → None (no side, no haptic)
 *  2. Negative offset outside deadband → Left
 *  3. Positive offset outside deadband → Right
 *  4. Small positive offset inside deadband → None (no premature haptic)
 *  5. Crossing from Left to Right → different sides (fire-once-per-change proof)
 *
 * [installedAnchors] (new, D-15):
 *  6. hasEditAction = true → RevealedRight is installed alongside Resting/RevealedLeft
 *  7. hasEditAction = false → RevealedRight is omitted (delete-only mode); Resting/RevealedLeft
 *     still installed (delete is never optional)
 */
class SwipeThresholdTest {

    @Test
    fun zeroOffset_returnsNone() {
        assertEquals(ThresholdSide.None, thresholdSide(offsetPx = 0f, deadbandPx = 8f))
    }

    @Test
    fun negativeOffsetOutsideDeadband_returnsLeft() {
        assertEquals(ThresholdSide.Left, thresholdSide(offsetPx = -20f, deadbandPx = 8f))
    }

    @Test
    fun positiveOffsetOutsideDeadband_returnsRight() {
        assertEquals(ThresholdSide.Right, thresholdSide(offsetPx = 20f, deadbandPx = 8f))
    }

    @Test
    fun smallPositiveOffsetInsideDeadband_returnsNone() {
        // 5f < 8f deadband → no side detected, no haptic should fire
        assertEquals(ThresholdSide.None, thresholdSide(offsetPx = 5f, deadbandPx = 8f))
    }

    @Test
    fun crossingSides_produceDifferentResults() {
        // Proves that -20f and +20f produce different side values,
        // validating the fire-once-on-side-change haptic contract.
        val leftSide = thresholdSide(offsetPx = -20f, deadbandPx = 8f)
        val rightSide = thresholdSide(offsetPx = 20f, deadbandPx = 8f)
        assertNotEquals("Crossing from negative to positive should change sides", leftSide, rightSide)
    }

    @Test
    fun installedAnchors_hasEditAction_includesRevealedRight() {
        val anchors = installedAnchors(hasEditAction = true)
        assertTrue("RevealedRight must be installed when an edit handler is present", anchors.contains(SwipeAnchor.RevealedRight))
        assertTrue(anchors.contains(SwipeAnchor.Resting))
        assertTrue(anchors.contains(SwipeAnchor.RevealedLeft))
    }

    @Test
    fun installedAnchors_noEditAction_omitsRevealedRight() {
        val anchors = installedAnchors(hasEditAction = false)
        assertFalse("RevealedRight must be omitted in delete-only mode (D-15, Pitfall 4)", anchors.contains(SwipeAnchor.RevealedRight))
        // Delete is never optional on this primitive — Resting/RevealedLeft always installed.
        assertTrue(anchors.contains(SwipeAnchor.Resting))
        assertTrue(anchors.contains(SwipeAnchor.RevealedLeft))
    }
}
