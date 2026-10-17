package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Layer 1 of the scoped TalkBack sweep (A11Y-02, D-03 [talkback-method]) for the Undo Center: the
 * group header is a heading (the one assertion that did not exist) and the openable row is ONE
 * merged button that carries its message, with Undo as its own control (candidate C-2, carried over
 * from 27-03 FA-7). The real accessibility tree on the tester device is Gate-1, plan 28-07.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class A11ySweepUndoCenterSemanticsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Open item")

    private fun headerTag(groupId: String) = "$UNDO_GROUP_HEADER_TEST_TAG_PREFIX$groupId"
    private fun undoAllTag(groupId: String) = "$UNDO_GROUP_UNDO_ALL_TEST_TAG_PREFIX$groupId"
    private fun rowTag(id: String) = "$UNDO_ROW_TEST_TAG_PREFIX$id"

    private fun showGroup(label: String, onUndoGroup: ((String) -> Unit)?) {
        val store = UndoHistoryStore()
        store.openGroup("g1", label) { }
        store.append("First imported", groupId = "g1") { }
        store.append("Second imported", groupId = "g1") { }
        composeTestRule.setContent {
            val entries by store.entries.collectAsState()
            val groups = remember(entries) {
                listOf(
                    UndoGroupRowUiModel(
                        groupId = "g1",
                        label = label,
                        memberIds = store.group("g1").map { it.id },
                        status = store.groupStatus("g1")
                    )
                )
            }
            UndoCenterScreen(
                entries = entries,
                onNavigateBack = {},
                onUndo = {},
                groups = groups,
                onUndoGroup = onUndoGroup,
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()
    }

    private fun inHeader(text: String) = composeTestRule.onNode(
        hasText(text) and hasAnyAncestor(hasTestTag(headerTag("g1"))),
        useUnmergedTree = true
    )

    private fun SemanticsNode.depthFirst(): List<SemanticsNode> =
        listOf(this) + children.flatMap { it.depthFirst() }

    private fun isHeading(node: SemanticsNode) = node.config.contains(SemanticsProperties.Heading)

    // ---- item 4: the group header is a heading --------------------------------------------------------

    @Test
    fun groupHeaderLabel_isAHeading() {
        showGroup("Import batch", onUndoGroup = {})

        val label = inHeader("Import batch").fetchSemanticsNode()
        assertTrue("the group label must be a heading", isHeading(label))
        // Control: the subtitle sits in the same header and is not a heading.
        assertFalse(isHeading(inHeader("2 items · Undoable").fetchSemanticsNode()))
        // Control: exactly one heading in the header.
        val header = composeTestRule.onNodeWithTag(headerTag("g1"), useUnmergedTree = true).fetchSemanticsNode()
        assertEquals(1, header.depthFirst().count { isHeading(it) })
    }

    @Test
    fun groupHeaderSubtitleAndUndoAll_areNotHeadings() {
        showGroup("Import batch", onUndoGroup = {})

        val subtitle = inHeader("2 items · Undoable").fetchSemanticsNode()
        val undoAll = composeTestRule.onNodeWithTag(undoAllTag("g1"), useUnmergedTree = true).fetchSemanticsNode()
        assertFalse(isHeading(subtitle))
        assertFalse(isHeading(undoAll))
        assertEquals(Role.Button, undoAll.config.getOrNull(SemanticsProperties.Role))
        assertTrue("Undo all keeps a click action", undoAll.config.contains(SemanticsActions.OnClick))
        // Control: the label node, by contrast, is one.
        assertTrue(isHeading(inHeader("Import batch").fetchSemanticsNode()))
    }

    @Test
    fun groupHeaderLabel_precedesUndoAllInChildOrder() {
        showGroup("Import batch", onUndoGroup = {})

        val header = composeTestRule.onNodeWithTag(headerTag("g1"), useUnmergedTree = true).fetchSemanticsNode()
        val order = header.depthFirst().map { it.id }
        val labelIndex = order.indexOf(inHeader("Import batch").fetchSemanticsNode().id)
        val undoAllIndex = order.indexOf(
            composeTestRule.onNodeWithTag(undoAllTag("g1"), useUnmergedTree = true).fetchSemanticsNode().id
        )
        assertTrue("label must be found in the header", labelIndex >= 0)
        assertTrue("Undo all must be found in the header", undoAllIndex >= 0)
        assertTrue("heading $labelIndex must come before Undo all $undoAllIndex", labelIndex < undoAllIndex)
    }

    @Test
    fun groupHeaderWithoutUndoAllHandler_keepsTheHeading_andHasNoButton() {
        showGroup("Import batch", onUndoGroup = null)

        assertTrue(isHeading(inHeader("Import batch").fetchSemanticsNode()))
        composeTestRule.onNodeWithTag(undoAllTag("g1"), useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Undo all").assertDoesNotExist()
    }

    @Test
    fun germanGroupLabel_isTheHeadingTextVerbatim() {
        val composed = "Übersicht"
        val decomposed = "Übersicht"
        assertNotEquals("the two forms are different strings", composed, decomposed)

        showGroup(decomposed, onUndoGroup = {})

        val node = inHeader(decomposed).fetchSemanticsNode()
        assertTrue(isHeading(node))
        val text = node.config[SemanticsProperties.Text].single().text
        assertEquals("no normalization", decomposed, text)
        // Control: the composed form is NOT what the heading carries.
        assertNotEquals(composed, text)
        composeTestRule.onAllNodesWithText(composed, useUnmergedTree = true).assertCountEqualsZero()
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteractionCollection.assertCountEqualsZero() {
        assertEquals(0, fetchSemanticsNodes().size)
    }

    // ---- item 5b: the openable row (candidate C-2) --------------------------------------------------------

    private fun showStore(store: UndoHistoryStore) {
        composeTestRule.setContent {
            val entries by store.entries.collectAsState()
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

    private fun mergedTexts(node: SemanticsNode): List<String> =
        node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }

    @Test
    fun openableRow_isOneStopWithTheMessageAndTheOpenLabel() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { }
        showStore(store)

        val row = composeTestRule.onNodeWithTag(rowTag(id)).fetchSemanticsNode()
        assertEquals(Role.Button, row.config.getOrNull(SemanticsProperties.Role))
        assertEquals("Open item", row.config[SemanticsActions.OnClick].label)
        val texts = mergedTexts(row)
        assertTrue("the row node carries the message: $texts", texts.contains("Tag deleted"))
        assertTrue("the row node carries the timestamp too: $texts", texts.size >= 2)
        assertFalse("Undo is its own stop: $texts", texts.contains("Undo"))
    }

    @Test
    fun openableRow_hasNoSeparateMessageNode() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { }
        showStore(store)

        val withMessage = composeTestRule.onAllNodesWithText("Tag deleted").fetchSemanticsNodes()
        assertEquals("exactly one merged node carries the message", 1, withMessage.size)
        val row = composeTestRule.onNodeWithTag(rowTag(id)).fetchSemanticsNode()
        assertEquals("and it is the row node", row.id, withMessage.single().id)
    }

    @Test
    fun openableRow_undoStaysItsOwnButton() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { }
        showStore(store)

        val row = composeTestRule.onNodeWithTag(rowTag(id)).fetchSemanticsNode()
        val undo = composeTestRule.onNodeWithText("Undo").fetchSemanticsNode()
        assertNotEquals(row.id, undo.id)
        assertEquals(Role.Button, undo.config.getOrNull(SemanticsProperties.Role))
        assertTrue(undo.config.contains(SemanticsActions.OnClick))
        // Control: the Undo click label is not the row's open label.
        assertNotEquals(
            row.config[SemanticsActions.OnClick].label,
            undo.config[SemanticsActions.OnClick].label
        )
        assertFalse(mergedTexts(row).contains("Undo"))
    }

    @Test
    fun openableRow_openLabel_passesThroughVerbatim_composedAndDecomposed() {
        val composed = "Eintrag öffnen"
        val decomposed = "Eintrag öffnen"
        assertNotEquals(composed, decomposed)
        val store = UndoHistoryStore()
        val a = store.append("Erster", target = FeedbackTarget("k-a", composed)) { }
        val b = store.append("Zweiter", target = FeedbackTarget("k-b", decomposed)) { }
        showStore(store)

        val labelA = composeTestRule.onNodeWithTag(rowTag(a)).fetchSemanticsNode().config[SemanticsActions.OnClick].label
        val labelB = composeTestRule.onNodeWithTag(rowTag(b)).fetchSemanticsNode().config[SemanticsActions.OnClick].label
        assertEquals(composed, labelA)
        assertEquals(decomposed, labelB)
        assertNotEquals("the two forms stay different (no normalization)", labelA, labelB)
    }

    @Test
    fun rowWithoutATarget_keepsTheV271Structure() {
        val store = UndoHistoryStore()
        val id = store.append("Card moved") { }
        showStore(store)

        val row = composeTestRule.onNodeWithTag(rowTag(id)).fetchSemanticsNode()
        assertFalse(row.config.contains(SemanticsProperties.Role))
        assertFalse(row.config.contains(SemanticsActions.OnClick))
        // The message stays in the ListItem's own node, a child of the row; Undo is separate.
        val message = composeTestRule.onNodeWithText("Card moved").fetchSemanticsNode()
        assertNotEquals(row.id, message.id)
        assertFalse(message.config.contains(SemanticsActions.OnClick))
        assertTrue(composeTestRule.onNodeWithText("Undo").fetchSemanticsNode().config.contains(SemanticsActions.OnClick))
    }
}
