package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * FOPEN-03: through the second [UndoCenterScreen] overload (required `onOpenTarget`) an Available
 * entry that carries a target opens its item with one tap (D-04 [undo-center-tap]); entries that
 * are Undone or Failed, entries without a target and group headers never open, a hold shows the
 * preview and never opens (PD-11), and a stale row never opens after an Undo (Pitfall 5).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class UndoCenterScreenOpenTargetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Open item")
    private val opened = mutableListOf<FeedbackTarget>()
    private val undone = mutableListOf<String>()

    private fun rowTag(id: String) = "$UNDO_ROW_TEST_TAG_PREFIX$id"
    private fun chevronTag(id: String) = "$UNDO_OPEN_CHEVRON_TEST_TAG_PREFIX$id"

    private fun show(store: UndoHistoryStore, groupIds: List<String> = emptyList()) {
        composeTestRule.setContent {
            val entries by store.entries.collectAsState()
            val groups = remember(entries) {
                groupIds.map { id ->
                    UndoGroupRowUiModel(
                        groupId = id,
                        label = store.groupLabel(id).orEmpty(),
                        memberIds = store.group(id).map { it.id },
                        status = store.groupStatus(id)
                    )
                }
            }
            UndoCenterScreen(
                entries = entries,
                onNavigateBack = {},
                onUndo = { undone += it },
                groups = groups,
                onUndoGroup = {},
                onOpenTarget = { opened += it },
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()
    }

    /** The peek of an openable row sits inside its merged node, so it is only visible unmerged. */
    private fun peek() = composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG, useUnmergedTree = true)

    private fun tapLeftOf(tag: String) {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performTouchInput { click(Offset(10f, center.y)) }
        composeTestRule.waitForIdle()
    }

    private fun spend(store: UndoHistoryStore, id: String) {
        runBlocking { store.attemptUndo(id) }
        composeTestRule.waitForIdle()
    }

    // --- tracer ---------------------------------------------------------------------------

    @Test
    fun availableEntryWithTarget_tapOpensOnce() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { }
        show(store)

        tapLeftOf(rowTag(id))

        assertEquals(listOf(target), opened)
        assertTrue(undone.isEmpty())
        peek().assertDoesNotExist()
    }

    // --- inert cases ------------------------------------------------------------------------

    @Test
    fun undoneEntry_doesNotOpen_andHasNoChevronOrClick() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { }
        show(store)
        spend(store, id)

        tapLeftOf(rowTag(id))

        assertTrue(opened.isEmpty())
        composeTestRule.onNodeWithText("Undone").assertExists()
        composeTestRule.onNodeWithTag(chevronTag(id), useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(rowTag(id))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
    }

    @Test
    fun failedEntry_doesNotOpen() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { error("stale") }
        show(store)
        spend(store, id)

        tapLeftOf(rowTag(id))

        assertTrue(opened.isEmpty())
        composeTestRule.onNodeWithText("Couldn't undo").assertExists()
        composeTestRule.onNodeWithTag(chevronTag(id), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun entryWithoutTarget_doesNotOpen_andRendersTheV271Row() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", preview = UndoPreview.Tag("Work")) { }
        show(store)

        tapLeftOf(rowTag(id))
        assertTrue(opened.isEmpty())
        composeTestRule.onNodeWithTag(chevronTag(id), useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(rowTag(id))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))

        // The v2.7.1 row keeps its peek-on-press behaviour.
        composeTestRule.onNodeWithTag(rowTag(id)).performTouchInput { down(Offset(10f, center.y)) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(rowTag(id)).performTouchInput { up() }
        composeTestRule.waitForIdle()
        assertTrue(opened.isEmpty())
    }

    // --- edges: adjacency, ordering, empty -------------------------------------------------

    @Test
    fun groupHeader_neverOpens_membersDo() {
        val store = UndoHistoryStore()
        store.openGroup("g1", "Import batch") { }
        val m1 = store.append("First imported", groupId = "g1", target = target) { }
        store.append("Second imported", groupId = "g1", target = FeedbackTarget("item-43", "Open second")) { }
        show(store, listOf("g1"))

        tapLeftOf("$UNDO_GROUP_HEADER_TEST_TAG_PREFIX" + "g1")
        assertTrue("a group header must never open", opened.isEmpty())

        tapLeftOf(rowTag(m1))
        assertEquals(listOf(target), opened)
    }

    @Test
    fun availableRowNextToUndoneRowWithEqualTarget_onlyTheAvailableOpens() {
        val store = UndoHistoryStore()
        val spent = store.append("Older", target = target) { }
        val live = store.append("Newer", target = target) { }
        show(store)
        spend(store, spent)

        tapLeftOf(rowTag(spent))
        assertTrue(opened.isEmpty())
        tapLeftOf(rowTag(live))
        assertEquals(listOf(target), opened)
    }

    @Test
    fun equalTargetsOnTwoAvailableRows_eachOpensOnceOnItsOwnTap() {
        val store = UndoHistoryStore()
        val first = store.append("Older", target = target) { }
        val second = store.append("Newer", target = target) { }
        show(store)

        val topOfSecond = composeTestRule.onNodeWithTag(rowTag(second), useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot.top
        val topOfFirst = composeTestRule.onNodeWithTag(rowTag(first), useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue("caller order (newest first) must be kept", topOfSecond < topOfFirst)

        tapLeftOf(rowTag(first))
        assertEquals(1, opened.size)
        tapLeftOf(rowTag(second))
        assertEquals(listOf(target, target), opened)
    }

    @Test
    fun emptyEntries_withTheNewOverload_rendersTheEmptyState() {
        show(UndoHistoryStore())

        composeTestRule.onNodeWithText("No undo history yet").assertExists()
        assertTrue(opened.isEmpty())
    }

    // --- gestures ---------------------------------------------------------------------------

    @Test
    fun tappingUndo_runsUndoOnce_neitherOpensNorPeeks() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { }
        show(store)

        composeTestRule.onNodeWithText("Undo").performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(id), undone)
        assertTrue("tapping Undo must not open", opened.isEmpty())
        peek().assertDoesNotExist()
    }

    @Test
    fun holdingAnOpenableRow_showsThePeek_andReleaseNeitherOpensNorUndoes() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", preview = UndoPreview.Tag("Work"), target = target) { }
        show(store)

        var timeout = 0L
        composeTestRule.onNodeWithTag(rowTag(id)).performTouchInput {
            timeout = viewConfiguration.longPressTimeoutMillis
            down(Offset(10f, center.y))
        }
        composeTestRule.mainClock.advanceTimeBy(timeout + 100)
        composeTestRule.waitForIdle()
        peek().assertExists()

        composeTestRule.onNodeWithTag(rowTag(id)).performTouchInput { up() }
        composeTestRule.waitForIdle()

        peek().assertDoesNotExist()
        assertTrue("a hold must never open", opened.isEmpty())
        assertTrue("a hold must never undo", undone.isEmpty())
    }

    @Test
    fun tappingAnOpenableRow_neverComposesThePeek() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", preview = UndoPreview.Tag("Work"), target = target) { }
        show(store)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.onNodeWithTag(rowTag(id)).performTouchInput { down(Offset(10f, center.y)) }
        composeTestRule.mainClock.advanceTimeBy(50)
        peek().assertDoesNotExist()
        composeTestRule.onNodeWithTag(rowTag(id)).performTouchInput { up() }
        composeTestRule.mainClock.advanceTimeBy(50)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        assertEquals(listOf(target), opened)
        peek().assertDoesNotExist()
    }

    @Test
    fun dragOverAnOpenableRow_neverOpens() {
        val store = UndoHistoryStore()
        val ids = (1..20).map { store.append("Entry $it", target = FeedbackTarget("k$it", "Open $it")) { } }
        show(store)

        composeTestRule.onNodeWithTag(rowTag(ids.last())).performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()

        assertTrue("a scroll or fling must never open", opened.isEmpty())
        assertTrue(undone.isEmpty())
    }

    // --- stale-callback guard (Pitfall 5) ---------------------------------------------------

    @Test
    fun afterUndoViaTheStore_tappingTheRow_doesNotOpen() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { }
        show(store)
        composeTestRule.onNodeWithTag(chevronTag(id), useUnmergedTree = true).assertExists()

        spend(store, id)

        composeTestRule.onNodeWithTag(chevronTag(id), useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Undone").assertExists()
        composeTestRule.onNodeWithTag(rowTag(id))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsActions.OnClick))
        tapLeftOf(rowTag(id))
        assertEquals(0, opened.size)
    }

    // --- semantics --------------------------------------------------------------------------

    @Test
    fun blankOpenLabel_fallsBackToTheMessageAsClickLabel() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = FeedbackTarget("item-42", "  ")) { }
        show(store)

        val label = composeTestRule.onNodeWithTag(rowTag(id)).fetchSemanticsNode()
            .config[SemanticsActions.OnClick].label
        assertEquals("Tag deleted", label)
    }

    @Test
    fun semanticClick_opensOnce() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { }
        show(store)

        composeTestRule.onNodeWithTag(rowTag(id)).performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        assertEquals(listOf(target), opened)
        assertTrue(undone.isEmpty())
    }

    @Test
    fun rowIsOneMergedButton_andUndoIsItsOwnNode() {
        val store = UndoHistoryStore()
        val id = store.append("Tag deleted", target = target) { }
        show(store)

        val row = composeTestRule.onNodeWithTag(rowTag(id)).fetchSemanticsNode()
        assertEquals(Role.Button, row.config.getOrNull(SemanticsProperties.Role))
        assertEquals("Open item", row.config[SemanticsActions.OnClick].label)
        val rowText = row.config.getOrNull(SemanticsProperties.Text).orEmpty().joinToString { it.text }
        assertFalse("Undo is its own node, not part of the merged row text", rowText.contains("Undo"))
        // The message lives in the ListItem's own merged node, a child of the row.
        composeTestRule.onNodeWithText("Tag deleted").assertExists()

        val undo = composeTestRule.onNodeWithText("Undo").fetchSemanticsNode()
        assertTrue("Undo keeps a click action", undo.config.contains(SemanticsActions.OnClick))
        assertTrue(undo.id != row.id)
    }

    @Test
    fun chevronComesAfterUndo_andOnlyOnOpenableRows() {
        val store = UndoHistoryStore()
        val openable = store.append("Tag deleted", target = target) { }
        show(store)

        val chevron = composeTestRule.onNodeWithTag(chevronTag(openable), useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val undo = composeTestRule.onNodeWithText("Undo").fetchSemanticsNode().boundsInRoot
        assertTrue("chevron ${chevron.left} must sit at or right of Undo ${undo.right}", chevron.left >= undo.right)
    }

    @Test
    fun aRowWithoutATarget_hasNoChevron() {
        val store = UndoHistoryStore()
        val plain = store.append("Card moved") { }
        show(store)

        composeTestRule.onNodeWithTag(chevronTag(plain), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun opaqueKey_neverAppearsInTheSemanticsTree() {
        val store = UndoHistoryStore()
        store.append("Tag deleted", target = FeedbackTarget("KEY-DO-NOT-LEAK-31415", "Open item")) { }
        show(store)

        assertFalse(SemanticsDump.allRawRoots(composeTestRule).contains("KEY-DO-NOT-LEAK-31415"))
    }
}
