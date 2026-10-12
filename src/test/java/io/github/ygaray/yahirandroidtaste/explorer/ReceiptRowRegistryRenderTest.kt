package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextDecoration
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the `ReceiptRow` registry entry the way the gallery does (Phase 21 VROW-01, D-09): every
 * non-null state cell and the Variants under both themes, plus the single-open accordion whose open
 * index lives in the demo. NATIVE graphics so text decoration is real; the window is tall so every
 * accordion row is on screen (geometry is still read unclipped). No pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReceiptRowRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val canonicalLabels = listOf("Default", "Pressed / Selected", "Disabled", "Focused")

    private fun entry(): ComponentRegistry.Entry =
        ComponentRegistry.entries.first { it.name == "ReceiptRow" }

    private fun renderCell(label: String) {
        val render = checkNotNull(entry().states.first { it.label == label }.render)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) { Column { render() } }
        }
        composeTestRule.waitForIdle()
    }

    /** Renders each non-null state cell and the content lambda once per theme mode (one composition). */
    private fun renderEverything(entry: ComponentRegistry.Entry) {
        val renders = entry.states.mapNotNull { it.render } + listOfNotNull(entry.content)
        composeTestRule.setContent {
            Column {
                listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                    renders.forEach { render ->
                        YahirAndroidTasteTheme(themeMode = mode, dynamicColor = false) { render() }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun renderVariants() {
        val content = checkNotNull(entry().content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) { Column { content() } }
        }
        composeTestRule.waitForIdle()
    }

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    private fun count(tag: String): Int =
        composeTestRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().size

    private fun detailCount(): Int =
        composeTestRule.onAllNodesWithText("Accordion detail", substring = true, useUnmergedTree = true)
            .fetchSemanticsNodes().size

    private fun accordionRow(number: Int): SemanticsNodeInteraction =
        composeTestRule.onNode(hasTestTag("receipt_row_summary") and hasText("Accordion row $number"))

    private fun SemanticsNodeInteraction.top(): Float = fetchSemanticsNode().positionInRoot.y

    private fun SemanticsNodeInteraction.bottom(): Float {
        val node = fetchSemanticsNode()
        return node.positionInRoot.y + node.size.height
    }

    // --- Registry entry ---------------------------------------------------------------------

    @Test
    fun `ReceiptRow is a Cards pattern with canonical states and renders in both themes`() {
        val row = entry()
        assertEquals(ExplorerFamilies.CARDS, row.family)
        assertEquals(ComponentRegistry.Tier.PATTERN, row.tier)
        assertEquals(canonicalLabels, row.states.map { it.label })
        listOf("Default", "Pressed / Selected", "Disabled").forEach { label ->
            assertNotNull("$label cell", row.states.first { it.label == label }.render)
        }
        assertNull("Focused cell", row.states.first { it.label == "Focused" }.render)
        assertNotNull(row.content)

        renderEverything(row)

        val summaries = count("receipt_row_summary")
        assertTrue("expected summaries from cells and Variants in both themes, found $summaries", summaries >= 2 * 4)
    }

    @Test
    fun `the Default cell shows one chevron`() {
        renderCell("Default")

        assertEquals(1, count("receipt_row_chevron"))
    }

    @Test
    fun `the Pressed or Selected cell shows its expanded content once`() {
        renderCell("Pressed / Selected")

        assertEquals(1, count("receipt_row_expanded_content"))
    }

    @Test
    fun `the Disabled cell strikes through its title`() {
        renderCell("Disabled")

        val decoration = composeTestRule.onNodeWithText("Item title", useUnmergedTree = true)
            .textLayout().layoutInput.style.textDecoration
        assertTrue("title must carry LineThrough", decoration?.contains(TextDecoration.LineThrough) == true)
    }

    // --- Single-open accordion --------------------------------------------------------------

    @Test
    fun `the accordion keeps one row open at a time with the state held in the demo`() {
        renderVariants()
        assertEquals("nothing is open at first", 0, detailCount())
        assertEquals("no expanded content at first", 0, count("receipt_row_expanded_content"))

        accordionRow(2).performClick()
        composeTestRule.waitForIdle()

        assertEquals("exactly one expanded content after opening row 2", 1, count("receipt_row_expanded_content"))
        assertEquals("detail 2 shown once", 1, composeTestRule.onAllNodesWithText("Accordion detail 2", useUnmergedTree = true).fetchSemanticsNodes().size)
        val detail = composeTestRule.onNodeWithTag("receipt_row_expanded_content", useUnmergedTree = true)
        assertTrue("detail 2 sits below row 2: ${detail.top()} vs ${accordionRow(2).bottom()}", detail.top() >= accordionRow(2).bottom() - 1f)
        assertTrue("detail 2 sits above row 3: ${detail.bottom()} vs ${accordionRow(3).top()}", detail.bottom() <= accordionRow(3).top() + 1f)

        accordionRow(3).performClick()
        composeTestRule.waitForIdle()

        assertEquals("detail 3 shown once", 1, composeTestRule.onAllNodesWithText("Accordion detail 3", useUnmergedTree = true).fetchSemanticsNodes().size)
        assertEquals("detail 2 is closed by opening row 3", 0, composeTestRule.onAllNodesWithText("Accordion detail 2", useUnmergedTree = true).fetchSemanticsNodes().size)
        assertEquals("still exactly one open", 1, count("receipt_row_expanded_content"))

        accordionRow(3).performClick()
        composeTestRule.waitForIdle()

        assertEquals("tapping the open row closes everything", 0, detailCount())
    }
}
