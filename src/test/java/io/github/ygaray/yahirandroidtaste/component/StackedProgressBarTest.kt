package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tests for [StackedProgressBar] and [clampSegmentFractions] (VBAR-01): the pure clamp rules (D-09),
 * the rendered per-segment widths on a 300dp track, the hostile-input cases (negative, zero, NaN),
 * the default and overridden height, the stable and overridden tag, and the clip-before-background
 * source contract.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StackedProgressBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val trackColor = Color(0xFFCCCCCC)
    private val first = Color(0xFF2255AA)
    private val second = Color(0xFFAA5522)

    private fun clamp(vararg raw: Float): List<Float> = clampSegmentFractions(raw.toList())

    private fun assertFractions(expected: List<Float>, actual: List<Float>) {
        assertEquals("size", expected.size, actual.size)
        expected.indices.forEach { assertEquals("index $it", expected[it], actual[it], 1e-6f) }
    }

    // ---- pure clamp ---------------------------------------------------------------------------

    @Test
    fun `clamp of an empty list is an empty list`() {
        assertTrue(clampSegmentFractions(emptyList()).isEmpty())
    }

    @Test
    fun `clamp leaves an in-range list unchanged`() {
        assertFractions(listOf(0.4f, 0.18f), clamp(0.4f, 0.18f))
    }

    @Test
    fun `clamp truncates the crossing segment instead of rescaling`() {
        assertFractions(listOf(0.7f, 0.3f), clamp(0.7f, 0.6f))
    }

    @Test
    fun `clamp caps a lone over-one fraction and an infinite one at the remaining room`() {
        assertFractions(listOf(1f), clamp(1.5f))
        assertFractions(listOf(1f), clamp(Float.POSITIVE_INFINITY))
        assertFractions(listOf(0.25f, 0.75f), clamp(0.25f, Float.POSITIVE_INFINITY))
    }

    @Test
    fun `clamp turns negative values into zero`() {
        assertFractions(listOf(0f, 0.5f), clamp(-0.2f, 0.5f))
        assertFractions(listOf(0f), clamp(Float.NEGATIVE_INFINITY))
    }

    @Test
    fun `clamp turns NaN into zero and leaves the next segment unaffected`() {
        assertFractions(listOf(0f, 0.5f), clamp(Float.NaN, 0.5f))
    }

    @Test
    fun `clamp gives later segments nothing once the track is full`() {
        assertFractions(listOf(0.5f, 0.5f, 0f), clamp(0.5f, 0.5f, 0.5f))
    }

    @Test
    fun `clamp keeps the size and never sums above one`() {
        val raw = listOf(0.3f, Float.NaN, -1f, 0.9f, 4f, Float.POSITIVE_INFINITY, 0.2f)
        val out = clampSegmentFractions(raw)
        assertEquals(raw.size, out.size)
        assertTrue("sum ${out.sum()} must not exceed 1", out.sum() <= 1f + 1e-6f)
        assertTrue("no negative or NaN fraction", out.all { it >= 0f && !it.isNaN() })
    }

    // ---- rendered widths ----------------------------------------------------------------------

    private fun render(
        segments: List<BarSegment>,
        modifier: Modifier = Modifier,
        height: Dp? = null
    ) {
        composeTestRule.setContent {
            Box(Modifier.width(300.dp)) {
                if (height == null) {
                    StackedProgressBar(segments = segments, trackColor = trackColor, modifier = modifier)
                } else {
                    StackedProgressBar(
                        segments = segments,
                        trackColor = trackColor,
                        modifier = modifier,
                        height = height
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    /** Segment nodes ordered left to right. */
    private fun segmentNodes(): List<SemanticsNodeInteraction> {
        val all = composeTestRule.onAllNodesWithTag("stacked_progress_bar_segment")
        val count = all.fetchSemanticsNodes().size
        return (0 until count).map { all[it] }.sortedBy { it.getUnclippedBoundsInRoot().left.value }
    }

    private fun widthsDp(): List<Float> = segmentNodes().map { it.getUnclippedBoundsInRoot().width.value }

    @Test
    fun `two segments 0_4 and 0_18 fill 58 percent of a 300dp track`() {
        render(listOf(BarSegment(0.4f, first), BarSegment(0.18f, second)))

        val nodes = segmentNodes()
        assertEquals(2, nodes.size)
        val a = nodes[0].getUnclippedBoundsInRoot()
        val b = nodes[1].getUnclippedBoundsInRoot()
        assertEquals(120f, a.width.value, 1f)
        assertEquals(54f, b.width.value, 1f)
        assertEquals("second segment starts where the first ends", a.right.value, b.left.value, 1f)
        assertEquals(174f, a.width.value + b.width.value, 1f)
    }

    @Test
    fun `a sum above one fills the whole track and no more`() {
        render(listOf(BarSegment(0.7f, first), BarSegment(0.6f, second)))

        val widths = widthsDp()
        assertEquals(2, widths.size)
        assertEquals(210f, widths[0], 1f)
        assertEquals(90f, widths[1], 1f)
        assertEquals(300f, widths.sum(), 1f)
    }

    @Test
    fun `a negative fraction draws no segment`() {
        render(listOf(BarSegment(-0.3f, first), BarSegment(0.5f, second)))

        val widths = widthsDp()
        assertEquals(1, widths.size)
        assertEquals(150f, widths[0], 1f)
    }

    @Test
    fun `a zero fraction renders no node and does not crash`() {
        render(listOf(BarSegment(0f, first), BarSegment(0.5f, second)))

        val widths = widthsDp()
        assertEquals(1, widths.size)
        assertEquals(150f, widths[0], 1f)
    }

    @Test
    fun `a NaN fraction renders only the positive segment and does not crash`() {
        render(listOf(BarSegment(Float.NaN, first), BarSegment(0.25f, second)))

        val widths = widthsDp()
        assertEquals(1, widths.size)
        assertEquals(75f, widths[0], 1f)
    }

    @Test
    fun `shrinking a mutable state list in place recomposes without crashing and redraws the new widths`() {
        val segments = mutableStateListOf(BarSegment(0.4f, first), BarSegment(0.3f, second))
        composeTestRule.setContent {
            Box(Modifier.width(300.dp)) {
                StackedProgressBar(segments = segments, trackColor = trackColor)
            }
        }
        composeTestRule.waitForIdle()
        assertEquals(2, widthsDp().size)

        composeTestRule.runOnIdle { segments.removeAt(1) }
        composeTestRule.waitForIdle()
        assertEquals(listOf(120f), widthsDp().map { Math.round(it).toFloat() })

        composeTestRule.runOnIdle { segments[0] = BarSegment(0.5f, first) }
        composeTestRule.waitForIdle()
        assertEquals(listOf(150f), widthsDp().map { Math.round(it).toFloat() })
    }

    @Test
    fun `an empty list renders the track with no segments`() {
        render(emptyList())

        composeTestRule.onNodeWithTag("stacked_progress_bar").assertExists()
        assertEquals(0, composeTestRule.onAllNodesWithTag("stacked_progress_bar_segment").fetchSemanticsNodes().size)
    }

    // ---- height and tag -----------------------------------------------------------------------

    @Test
    fun `default track height is 6dp`() {
        render(listOf(BarSegment(0.4f, first)))

        val bounds = composeTestRule.onNodeWithTag("stacked_progress_bar").getUnclippedBoundsInRoot()
        assertEquals(6f, bounds.height.value, 0.5f)
        assertEquals(300f, bounds.width.value, 1f)
    }

    @Test
    fun `an explicit height of 12dp is honoured`() {
        render(listOf(BarSegment(0.4f, first)), height = 12.dp)

        val bounds = composeTestRule.onNodeWithTag("stacked_progress_bar").getUnclippedBoundsInRoot()
        assertEquals(12f, bounds.height.value, 0.5f)
    }

    @Test
    fun `the default tag is stacked_progress_bar`() {
        render(listOf(BarSegment(0.4f, first)))

        composeTestRule.onNodeWithTag("stacked_progress_bar").assertExists()
    }

    @Test
    fun `a caller testTag overrides the library tag`() {
        render(listOf(BarSegment(0.4f, first)), modifier = Modifier.testTag("custom_bar"))

        composeTestRule.onNodeWithTag("custom_bar").assertExists()
        assertEquals(0, composeTestRule.onAllNodesWithTag("stacked_progress_bar").fetchSemanticsNodes().size)
    }

    // ---- source contract ----------------------------------------------------------------------

    @Test
    fun `the track clips to a circle before painting trackColor and reads the height token`() {
        val src = SourceContractTestSupport.stripComments(SourceContractTestSupport.source("StackedProgressBar.kt"))
        val body = SourceContractTestSupport.functionBody(src, "fun StackedProgressBar(")

        val clipAt = body.indexOf("clip(CircleShape)")
        val backgroundAt = body.indexOf("background(trackColor)")
        assertTrue("clip(CircleShape) must be present", clipAt >= 0)
        assertTrue("background(trackColor) must be present", backgroundAt >= 0)
        assertTrue("clip(CircleShape) must come BEFORE background(trackColor)", clipAt < backgroundAt)
        assertTrue(src.contains("Dimens.StackedProgressBar.Height"))
    }

    @Test
    fun `trackColor is a required parameter and the declaration shape matches the registry guard`() {
        val raw = SourceContractTestSupport.source("StackedProgressBar.kt")
        assertTrue(raw.contains("@Composable\nfun StackedProgressBar("))
        assertTrue(raw.contains("    trackColor: Color,\n"))
        assertTrue(!raw.contains("trackColor: Color ="))
    }
}
