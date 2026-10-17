package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The second [UndoCenterScreen] overload must render exactly like the v2.7.1 one whenever no row is
 * openable: it reproduces the default-parity goldens D1 to D4 and the D1 row bounds. The goldens
 * are reused by import from [UndoCenterScreenGoldens], never copied.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class UndoCenterScreenOpenParityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun assertGolden(name: String, golden: String, actual: String) {
        val paste = actual.lines().joinToString("\n") { "|$it" }
        assertEquals("$name drifted. Actual dump, ready to paste:\n$paste\n", golden, actual)
    }

    private fun dump(): String =
        SemanticsDump.allRoots(composeTestRule, UndoCenterScreenGoldens::normalizeTime)

    private fun show(entries: List<UndoHistoryEntry>) {
        composeTestRule.setContent {
            UndoCenterScreen(
                entries = entries,
                onNavigateBack = {},
                onUndo = {},
                onOpenTarget = {},
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `D1 three entries available undone and failed through the new overload`() {
        show(UndoCenterScreenGoldens.threeEntries())
        assertGolden("D1", UndoCenterScreenGoldens.D1, dump())
    }

    @Test
    fun `D2 empty entries list shows the empty state through the new overload`() {
        show(emptyList())
        val dump = dump()
        assertFalse("D2: no Clear history on an empty list", dump.contains("Clear history"))
        assertGolden("D2", UndoCenterScreenGoldens.D2, dump)
    }

    @Test
    fun `D3 one available entry through the new overload`() {
        show(UndoCenterScreenGoldens.availableEntries(1))
        assertGolden("D3", UndoCenterScreenGoldens.D3, dump())
    }

    @Test
    fun `D4 two available entries through the new overload`() {
        show(UndoCenterScreenGoldens.availableEntries(2))
        assertGolden("D4", UndoCenterScreenGoldens.D4, dump())
    }

    @Test
    fun `row bounds and order are pinned for D1 through the new overload`() {
        show(UndoCenterScreenGoldens.threeEntries())
        val tags = listOf("u1", "u2", "u3").map { "$UNDO_ROW_TEST_TAG_PREFIX$it" }
        val actual = tags.joinToString("\n") { SemanticsDump.boundsLine(composeTestRule, it) }
        val tops = tags.map {
            composeTestRule.onNodeWithTag(it, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top
        }
        assertTrue("rows must be ordered u1, u2, u3 top to bottom: $tops", tops == tops.sorted())
        assertGolden("D1 bounds", UndoCenterScreenGoldens.D1_BOUNDS, actual)
    }

    @Test
    fun `spent entries carrying targets match the untargeted goldens`() {
        val target = FeedbackTarget("item-42", "Open item")
        val spent = UndoCenterScreenGoldens.threeEntries()
            .filter { it.status != UndoStatus.Available }
            .map { it.copy(target = target) }
        show(spent)

        // D1 minus its Available row u1: the Undone and Failed rows and the top bar stay as they were.
        val lines = UndoCenterScreenGoldens.D1.lines()
        val from = lines.indexOfFirst { it.contains("TestTag=undo-row-u1") }
        val to = lines.indexOfFirst { it.contains("TestTag=undo-row-u2") }
        val expected = (lines.take(from) + lines.drop(to)).joinToString("\n")
        assertGolden("spent entries with targets", expected, dump())
    }

    @Test
    fun `an available entry with a target adds exactly the open anatomy to its row`() {
        val target = FeedbackTarget("item-42", "Open item")
        val entries = UndoCenterScreenGoldens.availableEntries(1).map { it.copy(target = target) }
        show(entries)

        val dump = dump()
        assertTrue("the row is a button", dump.contains("TestTag=undo-row-u1"))
        assertTrue("the chevron is tagged", dump.contains(UNDO_OPEN_CHEVRON_TEST_TAG_PREFIX))
    }
}
