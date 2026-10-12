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
 * Wave 0 characterization of the DEFAULT (flat) [UndoCenterScreen], captured from the unmodified
 * v2.5.1 screen BEFORE any Phase 23 edit to `UndoCenterScreen.kt` and never edited afterwards. It
 * pins the normalized unmerged semantics tree of four default states plus the row bounds, so the
 * group widening (VUNDO-02) is proven to leave a default call exactly as it was without any pixel
 * capture. The relative-time text of each row is normalized to a fixed placeholder (never dropped,
 * so the timestamp node still counts).
 *
 * Later tests reuse [UndoCenterScreenGoldens] by import instead of copying the constants.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class UndoCenterScreenDefaultParityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun assertGolden(name: String, golden: String, actual: String) {
        val paste = actual.lines().joinToString("\n") { "|$it" }
        assertEquals("$name drifted. Actual dump, ready to paste:\n$paste\n", golden, actual)
    }

    private fun assertNoGroupNodes(name: String, dump: String) {
        assertFalse("$name: no Undo all text allowed", dump.contains("Undo all"))
        assertFalse("$name: no undo-group tag allowed", dump.contains("undo-group"))
    }

    private fun dump(): String =
        SemanticsDump.allRoots(composeTestRule, UndoCenterScreenGoldens::normalizeTime)

    private fun show(entries: List<UndoHistoryEntry>) {
        composeTestRule.setContent {
            UndoCenterScreen(
                entries = entries,
                onNavigateBack = {},
                onUndo = {},
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `D1 three entries available undone and failed`() {
        show(UndoCenterScreenGoldens.threeEntries())
        val dump = dump()
        assertNoGroupNodes("D1", dump)
        assertGolden("D1", UndoCenterScreenGoldens.D1, dump)
    }

    @Test
    fun `D2 empty entries list shows the empty state`() {
        show(emptyList())
        val dump = dump()
        assertNoGroupNodes("D2", dump)
        assertFalse("D2: no Clear history on an empty list", dump.contains("Clear history"))
        assertGolden("D2", UndoCenterScreenGoldens.D2, dump)
    }

    @Test
    fun `D3 one available entry`() {
        show(UndoCenterScreenGoldens.availableEntries(1))
        val dump = dump()
        assertNoGroupNodes("D3", dump)
        assertFalse("D3: no Clear history without a spent entry", dump.contains("Clear history"))
        assertGolden("D3", UndoCenterScreenGoldens.D3, dump)
    }

    @Test
    fun `D4 two available entries`() {
        show(UndoCenterScreenGoldens.availableEntries(2))
        val dump = dump()
        assertNoGroupNodes("D4", dump)
        assertFalse("D4: no Clear history without a spent entry", dump.contains("Clear history"))
        assertGolden("D4", UndoCenterScreenGoldens.D4, dump)
    }

    @Test
    fun `row bounds and order are pinned for D1`() {
        show(UndoCenterScreenGoldens.threeEntries())
        val tags = listOf("u1", "u2", "u3").map { "$UNDO_ROW_TEST_TAG_PREFIX$it" }
        val actual = tags.joinToString("\n") { SemanticsDump.boundsLine(composeTestRule, it) }
        val tops = tags.map {
            composeTestRule.onNodeWithTag(it, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top
        }
        assertTrue("rows must be ordered u1, u2, u3 top to bottom: $tops", tops == tops.sorted())
        assertTrue("rows must not share a top: $tops", tops.toSet().size == 3)
        assertGolden("D1 bounds", UndoCenterScreenGoldens.D1_BOUNDS, actual)
    }
}

/** Goldens captured from the unmodified v2.5.1 screen (Wave 0). Reused by later tests, never edited. */
internal object UndoCenterScreenGoldens {
    const val TIME_PLACEHOLDER = "<RELATIVE_TIME>"

    private val RELATIVE_TIME = Regex(
        """^(In )?\d+ (second|seconds|minute|minutes|hour|hours|day|days|week|weeks)( ago)?$|^(Yesterday|Tomorrow)$""",
        RegexOption.IGNORE_CASE
    )

    /** Replaces a whole-string relative-time text by a fixed placeholder; other strings pass through. */
    fun normalizeTime(text: String): String =
        if (RELATIVE_TIME.matches(text)) TIME_PLACEHOLDER else text

    fun entry(id: String, message: String, status: UndoStatus): UndoHistoryEntry =
        UndoHistoryEntry(
            id = id,
            message = message,
            timestamp = System.currentTimeMillis(),
            undoAction = {},
            status = status
        )

    fun threeEntries(): List<UndoHistoryEntry> = listOf(
        entry("u1", "Tag deleted", UndoStatus.Available),
        entry("u2", "Card moved", UndoStatus.Undone),
        entry("u3", "Photo removed", UndoStatus.Failed)
    )

    fun availableEntries(count: Int): List<UndoHistoryEntry> =
        listOf(
            entry("u1", "Tag deleted", UndoStatus.Available),
            entry("u2", "Card moved", UndoStatus.Available)
        ).take(count)

    val D1 = """
|node
|  node
|    node
|      node TestTag=undo-row-u1
|        node
|          node Text=[Tag deleted]
|          node Text=[<RELATIVE_TIME>]
|          node Role=Button
|            node Text=[Undo]
|      node TestTag=undo-row-u2
|        node
|          node Text=[Card moved]
|          node Text=[<RELATIVE_TIME>]
|          node Text=[Undone]
|      node TestTag=undo-row-u3
|        node
|          node Text=[Photo removed]
|          node Text=[<RELATIVE_TIME>]
|          node Text=[Couldn't undo]
|    node
|      node
|        node Role=Button
|          node ContentDescription=[Back] Role=Image
|        node
|          node Text=[Undo History]
|        node Role=Button
|          node Text=[Clear history]
""".trimMargin()
    val D2 = """
|node
|  node
|    node Text=[No undo history yet]
|    node Text=[Undoable actions you take will appear here.]
|    node
|      node
|        node Role=Button
|          node ContentDescription=[Back] Role=Image
|        node
|          node Text=[Undo History]
""".trimMargin()
    val D3 = """
|node
|  node
|    node
|      node TestTag=undo-row-u1
|        node
|          node Text=[Tag deleted]
|          node Text=[<RELATIVE_TIME>]
|          node Role=Button
|            node Text=[Undo]
|    node
|      node
|        node Role=Button
|          node ContentDescription=[Back] Role=Image
|        node
|          node Text=[Undo History]
""".trimMargin()
    val D4 = """
|node
|  node
|    node
|      node TestTag=undo-row-u1
|        node
|          node Text=[Tag deleted]
|          node Text=[<RELATIVE_TIME>]
|          node Role=Button
|            node Text=[Undo]
|      node TestTag=undo-row-u2
|        node
|          node Text=[Card moved]
|          node Text=[<RELATIVE_TIME>]
|          node Role=Button
|            node Text=[Undo]
|    node
|      node
|        node Role=Button
|          node ContentDescription=[Back] Role=Image
|        node
|          node Text=[Undo History]
""".trimMargin()
    val D1_BOUNDS = """
|undo-row-u1 x=0 y=64 w=360 h=88
|undo-row-u2 x=0 y=152 w=360 h=88
|undo-row-u3 x=0 y=240 w=360 h=88
""".trimMargin()
}
