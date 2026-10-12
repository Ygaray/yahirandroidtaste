package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * G-15-1 regression: [SegmentedOptionSelector] segment labels too long for their half of the row
 * must stay INSIDE their segment (wrap to at most 2 lines, then ellipsize), including the selected
 * segment whose check icon M3 places in front of the label; both segments keep equal height.
 *
 * Same harness as [ApproachLadderCardLongLabelTest] (w360dp, GraphicsMode.NATIVE, the library
 * theme). Geometry is read UNCLIPPED (`positionInRoot` + `size`) -- `boundsInRoot` is clipped to
 * ancestors and would hide exactly the overflow this test exists to catch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SegmentedOptionSelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setSelector(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit = {}) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme {
                Box(Modifier.fillMaxWidth().padding(16.dp)) {
                    SegmentedOptionSelector(selectedIndex = selectedIndex, options = options, onSelect = onSelect)
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun label(text: String) = composeTestRule.onNodeWithText(text, useUnmergedTree = true)

    private fun segment(text: String, selected: Boolean) = composeTestRule.onNodeWithContentDescription(
        "$text, ${if (selected) "selected" else "not selected"}"
    )

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    /** Unclipped horizontal layout edges in root px: (left, right). */
    private fun SemanticsNodeInteraction.edges(): Pair<Float, Float> {
        val node = fetchSemanticsNode()
        val left = node.positionInRoot.x
        return left to left + node.size.width
    }

    @Test
    fun `long German labels stay inside their segment selected or not, on at most two lines`() {
        val options = listOf("Onlineverarbeitung", "Ausschließlich offline")
        setSelector(options, selectedIndex = 1)

        options.forEachIndexed { index, text ->
            val (labelLeft, labelRight) = label(text).edges()
            val (segmentLeft, segmentRight) = segment(text, selected = index == 1).edges()
            assertTrue(
                "\"$text\" right edge $labelRight must be <= its segment's right edge $segmentRight",
                labelRight <= segmentRight + EDGE_TOLERANCE_PX
            )
            assertTrue(
                "\"$text\" left edge $labelLeft must be >= its segment's left edge $segmentLeft",
                labelLeft >= segmentLeft
            )
            val lines = label(text).textLayout().lineCount
            assertTrue("\"$text\" must render on <= 2 lines, rendered on $lines", lines <= 2)
        }
    }

    @Test
    fun `segments keep equal height when only one label wraps`() {
        val short = "An"
        val long = "Ausschließlich offline verarbeiten lassen"
        setSelector(listOf(short, long), selectedIndex = 0)

        val shortHeight = segment(short, selected = true).fetchSemanticsNode().size.height
        val longHeight = segment(long, selected = false).fetchSemanticsNode().size.height
        assertTrue(
            "segment heights must match within 1px (short=$shortHeight, long=$longHeight)",
            kotlin.math.abs(shortHeight - longHeight) <= 1
        )
    }

    @Test
    fun `short labels stay single-line with unchanged descriptions and taps still emit onSelect`() {
        var selected: Int? = null
        setSelector(listOf("Online", "Offline only"), selectedIndex = 0, onSelect = { selected = it })

        assertEquals(1, label("Online").textLayout().lineCount)
        assertEquals(1, label("Offline only").textLayout().lineCount)
        segment("Online", selected = true).assertExists()
        segment("Offline only", selected = false).assertExists().performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, selected)
    }

    private companion object {
        const val EDGE_TOLERANCE_PX = 0.5f
    }
}
