package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Timing proof for [ReceiptRow]'s motion (Phase 21 VROW-01): the 200ms chevron rotation and the 200ms
 * dim fade are driven through the internal seams the real row calls, on a paused main clock, so the
 * mid-animation value is observable. The expanded-content toggle is proven through the real row.
 *
 * No text is measured here, so the default graphics mode is enough. No pixel capture. One
 * `setContent` per test method.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ReceiptRowMotionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var flag by mutableStateOf(false)
    private var seam: State<Float>? = null

    private fun driveFlag(value: Boolean) {
        composeTestRule.runOnUiThread {
            flag = value
            // The clock is paused, so push the write to the composition ourselves.
            Snapshot.sendApplyNotifications()
        }
    }

    private fun seamValue(): Float = checkNotNull(seam) { "seam was not composed" }.value

    /** Pauses the clock, composes the chevron seam with [flag] starting at [initial]. */
    private fun composeChevronSeam(initial: Boolean) {
        flag = initial
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            seam = rememberReceiptRowChevronRotation(flag)
        }
        composeTestRule.mainClock.advanceTimeByFrame()
    }

    /** Pauses the clock, composes the alpha seam with [flag] starting at [initial]. */
    private fun composeAlphaSeam(initial: Boolean) {
        flag = initial
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            seam = rememberReceiptRowAlpha(flag)
        }
        composeTestRule.mainClock.advanceTimeByFrame()
    }

    /** Flips the flag, lets the animation start, then samples at 100ms and after the full duration. */
    private fun flipAndSample(target: Boolean): Pair<Float, Float> {
        driveFlag(target)
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeBy(100)
        val mid = seamValue()
        composeTestRule.mainClock.advanceTimeBy(RECEIPT_ROW_MOTION_MS + 32L)
        return mid to seamValue()
    }

    // --- Chevron rotation --------------------------------------------------------------------

    @Test
    fun `tracer - the chevron rotates from 0 toward 180 over 200ms`() {
        composeChevronSeam(initial = false)
        assertEquals("collapsed at rest", 0f, seamValue(), 0.01f)

        val (mid, end) = flipAndSample(target = true)

        assertTrue("mid-animation value $mid must be strictly between 0 and 180", mid > 0f && mid < RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES)
        assertEquals("settled after the duration", RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES, end, 0.01f)
    }

    @Test
    fun `the chevron rotates back from 180 to 0 over 200ms`() {
        composeChevronSeam(initial = true)
        assertEquals("expanded at rest", RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES, seamValue(), 0.01f)

        val (mid, end) = flipAndSample(target = false)

        assertTrue("mid-animation value $mid must be strictly between 180 and 0", mid > 0f && mid < RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES)
        assertEquals("settled after the duration", 0f, end, 0.01f)
    }

    @Test
    fun `a chevron composed expanded reports 180 at once without animating`() {
        composeChevronSeam(initial = true)

        assertEquals(RECEIPT_ROW_CHEVRON_EXPANDED_DEGREES, seamValue(), 0.01f)
    }

    // --- Dim fade ----------------------------------------------------------------------------

    @Test
    fun `the dim alpha fades from 1 to 38 percent over 200ms`() {
        composeAlphaSeam(initial = false)
        assertEquals("not dimmed at rest", 1f, seamValue(), 0.01f)

        val (mid, end) = flipAndSample(target = true)

        assertTrue("mid-animation alpha $mid must be strictly between 1 and the dimmed alpha", mid < 1f && mid > RECEIPT_ROW_DIMMED_ALPHA)
        assertEquals("settled after the duration", RECEIPT_ROW_DIMMED_ALPHA, end, 0.01f)
        assertEquals("the dimmed alpha is 38 percent", 0.38f, RECEIPT_ROW_DIMMED_ALPHA, 0.0001f)
    }

    @Test
    fun `the dim alpha fades back from 38 percent to 1 over 200ms`() {
        composeAlphaSeam(initial = true)
        assertEquals("dimmed at rest", RECEIPT_ROW_DIMMED_ALPHA, seamValue(), 0.01f)

        val (mid, end) = flipAndSample(target = false)

        assertTrue("mid-animation alpha $mid must be strictly between the dimmed alpha and 1", mid < 1f && mid > RECEIPT_ROW_DIMMED_ALPHA)
        assertEquals("settled after the duration", 1f, end, 0.01f)
    }

    // --- Expanded content through the real row ------------------------------------------------

    @Test
    fun `toggling expanded through the real row shows and hides the expanded content`() {
        flag = false
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                ReceiptRow(
                    title = "Item title",
                    value = "1,250",
                    expanded = flag,
                    onClick = {},
                    expandedContent = { Text("detail") }
                )
            }
        }
        composeTestRule.waitForIdle()
        assertEquals(0, expandedContentCount())

        driveFlag(true)
        composeTestRule.waitForIdle()
        assertEquals(1, expandedContentCount())
        composeTestRule
            .onNode(hasText("detail") and hasAnyAncestor(hasTestTag("receipt_row_expanded_content")), useUnmergedTree = true)
            .assertExists()

        driveFlag(false)
        composeTestRule.waitForIdle()
        assertEquals(0, expandedContentCount())
    }

    private fun expandedContentCount(): Int =
        composeTestRule.onAllNodesWithTag("receipt_row_expanded_content", useUnmergedTree = true)
            .fetchSemanticsNodes().size
}
