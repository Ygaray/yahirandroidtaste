package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose UI tests for the group rows of [UndoCenterScreen] (VUNDO-02): header, count and status
 * text, Undo all, indent, the status rule, ordering, gestures and the real-store end to end.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class UndoCenterScreenGroupsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun headerTag(groupId: String) = "$UNDO_GROUP_HEADER_TEST_TAG_PREFIX$groupId"
    private fun undoAllTag(groupId: String) = "$UNDO_GROUP_UNDO_ALL_TEST_TAG_PREFIX$groupId"
    private fun rowTag(entryId: String) = "$UNDO_ROW_TEST_TAG_PREFIX$entryId"

    private fun entry(id: String, status: UndoStatus = UndoStatus.Available) =
        UndoCenterScreenGoldens.entry(id, "message $id", status)

    private fun top(tag: String): Float =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top

    private fun left(tag: String): Float =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.left

    /** Maps the store's groups to [UndoGroupRowUiModel] the way a consumer would. */
    private fun groupsOf(store: UndoHistoryStore, vararg groupIds: String): List<UndoGroupRowUiModel> =
        groupIds.map { id ->
            UndoGroupRowUiModel(
                groupId = id,
                label = store.groupLabel(id).orEmpty(),
                memberIds = store.group(id).map { it.id },
                status = store.groupStatus(id)
            )
        }

    @Composable
    private fun Harness(
        store: UndoHistoryStore,
        groupIds: List<String>,
        onUndo: (String) -> Unit = {},
        onUndoGroup: ((String) -> Unit)?
    ) {
        val entries by store.entries.collectAsState()
        val groups = remember(entries) { groupsOf(store, *groupIds.toTypedArray()) }
        UndoCenterScreen(
            entries = entries,
            onNavigateBack = {},
            onUndo = onUndo,
            groups = groups,
            onUndoGroup = onUndoGroup,
            onClear = {}
        )
    }

    private fun showStatic(
        entries: List<UndoHistoryEntry>,
        groups: List<UndoGroupRowUiModel>,
        onUndo: (String) -> Unit = {},
        onUndoGroup: ((String) -> Unit)? = {}
    ) {
        composeTestRule.setContent {
            UndoCenterScreen(
                entries = entries,
                onNavigateBack = {},
                onUndo = onUndo,
                groups = groups,
                onUndoGroup = onUndoGroup,
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()
    }

    private fun group(
        id: String,
        vararg members: String,
        status: UndoGroupStatus = UndoGroupStatus.Undoable
    ) = UndoGroupRowUiModel(groupId = id, label = "Batch $id", memberIds = members.toList(), status = status)

    // --- tracer ---------------------------------------------------------------------------

    @Test
    fun `a store group renders its header with label count status and a working Undo all`() {
        val store = UndoHistoryStore()
        store.openGroup("g1", "Import batch") { }
        val m1 = store.append("First imported", groupId = "g1") { }
        val m2 = store.append("Second imported", groupId = "g1") { }
        val solo = store.append("Tag deleted") { }
        val groupCalls = mutableListOf<String>()
        val undoCalls = mutableListOf<String>()

        composeTestRule.setContent {
            Harness(
                store = store,
                groupIds = listOf("g1"),
                onUndo = { undoCalls += it },
                onUndoGroup = { groupCalls += it }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(headerTag("g1")).assertExists()
        composeTestRule.onNodeWithText("Import batch").assertExists()
        composeTestRule.onNodeWithText("2 items · Undoable").assertExists()
        composeTestRule.onNodeWithTag(undoAllTag("g1")).assertTextEquals("Undo all")
        composeTestRule.onNodeWithTag(rowTag(m1)).assertExists()
        composeTestRule.onNodeWithTag(rowTag(m2)).assertExists()
        composeTestRule.onNodeWithTag(rowTag(solo)).assertExists()

        composeTestRule.onNodeWithTag(undoAllTag("g1")).performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf("g1"), groupCalls)
        assertEquals(emptyList<String>(), undoCalls)
    }

    @Test
    fun `with groups left at its default the screen renders the entries as today`() {
        val store = UndoHistoryStore()
        store.openGroup("g1", "Import batch") { }
        store.append("First imported", groupId = "g1") { }

        composeTestRule.setContent {
            val entries by store.entries.collectAsState()
            UndoCenterScreen(
                entries = entries,
                onNavigateBack = {},
                onUndo = {},
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(headerTag("g1")).assertDoesNotExist()
        composeTestRule.onNodeWithText("Undo all").assertDoesNotExist()
    }

    // --- explicit defaults equal the Wave 0 goldens ---------------------------------------

    @Test
    fun `explicit empty groups and a null callback equal the Wave 0 golden D1`() {
        showStatic(
            entries = UndoCenterScreenGoldens.threeEntries(),
            groups = emptyList(),
            onUndoGroup = null
        )
        val dump = SemanticsDump.allRoots(composeTestRule, UndoCenterScreenGoldens::normalizeTime)
        assertEquals(UndoCenterScreenGoldens.D1, dump)
    }

    @Test
    fun `explicit empty groups and a null callback equal the Wave 0 golden D2`() {
        showStatic(entries = emptyList(), groups = emptyList(), onUndoGroup = null)
        val dump = SemanticsDump.allRoots(composeTestRule, UndoCenterScreenGoldens::normalizeTime)
        assertEquals(UndoCenterScreenGoldens.D2, dump)
    }

    // --- status matrix --------------------------------------------------------------------

    @Test
    fun `a partly resolved header reads the status and keeps Undo all`() {
        showStatic(
            entries = listOf(entry("m1"), entry("m2", UndoStatus.Undone)),
            groups = listOf(group("g", "m1", "m2", status = UndoGroupStatus.PartiallyResolved))
        )
        composeTestRule.onNodeWithText("2 items · Partly resolved").assertExists()
        composeTestRule.onNodeWithTag(undoAllTag("g")).assertExists()
    }

    @Test
    fun `a fully resolved header reads Resolved and has no Undo all`() {
        showStatic(
            entries = listOf(entry("m1", UndoStatus.Undone), entry("m2", UndoStatus.Failed)),
            groups = listOf(group("g", "m1", "m2", status = UndoGroupStatus.FullyResolved))
        )
        composeTestRule.onNodeWithText("2 items · Resolved").assertExists()
        composeTestRule.onNodeWithTag(undoAllTag("g")).assertDoesNotExist()
        composeTestRule.onNodeWithText("Undo all").assertDoesNotExist()
    }

    @Test
    fun `an Empty status group with present members shows only the count and no Undo all`() {
        showStatic(
            entries = listOf(entry("m1"), entry("m2")),
            groups = listOf(group("g", "m1", "m2", status = UndoGroupStatus.Empty))
        )
        composeTestRule.onNodeWithText("2 items").assertExists()
        composeTestRule.onNodeWithTag(undoAllTag("g")).assertDoesNotExist()
    }

    @Test
    fun `a null callback hides Undo all for every status`() {
        var status by mutableStateOf<UndoGroupStatus>(UndoGroupStatus.Undoable)
        composeTestRule.setContent {
            UndoCenterScreen(
                entries = listOf(entry("m1"), entry("m2")),
                onNavigateBack = {},
                onUndo = {},
                groups = listOf(group("g", "m1", "m2", status = status)),
                onUndoGroup = null,
                onClear = {}
            )
        }
        listOf(
            UndoGroupStatus.Undoable,
            UndoGroupStatus.PartiallyResolved,
            UndoGroupStatus.FullyResolved,
            UndoGroupStatus.Empty
        ).forEach { next ->
            composeTestRule.runOnIdle { status = next }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithTag(headerTag("g")).assertExists()
            composeTestRule.onNodeWithTag(undoAllTag("g")).assertDoesNotExist()
        }
    }

    @Test
    fun `a custom undoAllLabel replaces the button text`() {
        showStatic(
            entries = listOf(entry("m1")),
            groups = listOf(group("g", "m1").copy(undoAllLabel = "Alle rückgängig machen"))
        )
        composeTestRule.onNodeWithTag(undoAllTag("g")).assertTextEquals("Alle rückgängig machen")
    }

    // --- ordering and edge cases ----------------------------------------------------------

    @Test
    fun `entries around a group render as a header followed by its members and ungrouped stay flat`() {
        showStatic(
            entries = listOf(entry("a"), entry("m1"), entry("b"), entry("m2")),
            groups = listOf(group("g", "m1", "m2"))
        )
        val order = listOf(rowTag("a"), headerTag("g"), rowTag("m1"), rowTag("m2"), rowTag("b")).map { top(it) }
        assertEquals("rows must follow a, header, m1, m2, b: $order", order.sorted(), order)
        assertEquals("every row has its own top: $order", 5, order.toSet().size)
    }

    @Test
    fun `two interleaved groups each render their own header once`() {
        showStatic(
            entries = listOf(entry("a1"), entry("b1"), entry("a2"), entry("b2")),
            groups = listOf(group("g1", "a1", "a2"), group("g2", "b1", "b2"))
        )
        composeTestRule.onNodeWithTag(headerTag("g1")).assertExists()
        composeTestRule.onNodeWithTag(headerTag("g2")).assertExists()
        val order = listOf(headerTag("g1"), rowTag("a1"), rowTag("a2"), headerTag("g2"), rowTag("b1"), rowTag("b2"))
            .map { top(it) }
        assertEquals(order.sorted(), order)
    }

    @Test
    fun `a group whose members are all absent renders no header`() {
        showStatic(
            entries = listOf(entry("x")),
            groups = listOf(group("ghost", "m1", "m2"))
        )
        composeTestRule.onNodeWithTag(headerTag("ghost")).assertDoesNotExist()
        composeTestRule.onNodeWithTag(rowTag("x")).assertExists()
    }

    @Test
    fun `the header counts only the members present in the entries`() {
        showStatic(
            entries = listOf(entry("m1"), entry("m2")),
            groups = listOf(group("g", "m1", "ghost", "m2"))
        )
        composeTestRule.onNodeWithText("2 items · Undoable").assertExists()
    }

    @Test
    fun `an id listed by two groups belongs to the first and does not collide`() {
        showStatic(
            entries = listOf(entry("m1"), entry("m2")),
            groups = listOf(group("g1", "m1"), group("g2", "m1", "m2"))
        )
        composeTestRule.onNodeWithTag(headerTag("g1")).assertExists()
        composeTestRule.onNodeWithTag(headerTag("g2")).assertExists()
        composeTestRule.onNodeWithTag(rowTag("m1")).assertExists()
        composeTestRule.onNodeWithTag(rowTag("m2")).assertExists()
    }

    @Test
    fun `a groupId equal to an entry id does not crash`() {
        showStatic(
            entries = listOf(entry("g"), entry("m1")),
            groups = listOf(group("g", "m1"))
        )
        composeTestRule.onNodeWithTag(headerTag("g")).assertExists()
        composeTestRule.onNodeWithTag(rowTag("g")).assertExists()
    }

    // --- indent ---------------------------------------------------------------------------

    @Test
    fun `members are inset by the horizontal padding and the header and ungrouped rows are not`() {
        var density = 1f
        composeTestRule.setContent {
            density = LocalDensity.current.density
            UndoCenterScreen(
                entries = listOf(entry("x"), entry("m1")),
                onNavigateBack = {},
                onUndo = {},
                groups = listOf(group("g", "m1")),
                onUndoGroup = {},
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()
        val inset = Dimens.HorizontalPadding.value * density
        val ungrouped = left(rowTag("x"))
        assertTrue(
            "member left ${left(rowTag("m1"))} must exceed the ungrouped left $ungrouped by at least $inset",
            left(rowTag("m1")) >= ungrouped + inset
        )
        assertEquals(ungrouped, left(headerTag("g")), 0.5f)
    }

    // --- gestures and per-row Undo --------------------------------------------------------

    @Test
    fun `holding the header shows no peek and holding a member shows it`() {
        showStatic(
            entries = listOf(entry("m1")),
            groups = listOf(group("g", "m1"))
        )
        val header = composeTestRule.onNodeWithTag(headerTag("g"))
        header.performTouchInput { down(Offset(10f, center.y)) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertDoesNotExist()
        header.performTouchInput { up() }
        composeTestRule.waitForIdle()

        val member = composeTestRule.onNodeWithTag(rowTag("m1"))
        member.performTouchInput { down(Offset(10f, center.y)) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(UNDO_PEEK_TEST_TAG).assertExists()
        member.performTouchInput { up() }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `tapping a member's own Undo invokes onUndo with its id once and never onUndoGroup`() {
        val undoCalls = mutableListOf<String>()
        val groupCalls = mutableListOf<String>()
        showStatic(
            entries = listOf(entry("m1"), entry("m2")),
            groups = listOf(group("g", "m1", "m2")),
            onUndo = { undoCalls += it },
            onUndoGroup = { groupCalls += it }
        )
        composeTestRule.onNode(hasText("Undo") and hasAnyAncestor(hasTestTag(rowTag("m1")))).performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf("m1"), undoCalls)
        assertEquals(emptyList<String>(), groupCalls)
    }

    // --- top bar --------------------------------------------------------------------------

    @Test
    fun `Clear history shows iff some entry is not Available regardless of groups`() {
        var entries by mutableStateOf(listOf(entry("m1"), entry("m2")))
        composeTestRule.setContent {
            UndoCenterScreen(
                entries = entries,
                onNavigateBack = {},
                onUndo = {},
                groups = listOf(group("g", "m1", "m2")),
                onUndoGroup = {},
                onClear = {}
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Clear history").assertDoesNotExist()

        composeTestRule.runOnIdle { entries = listOf(entry("m1", UndoStatus.Undone), entry("m2")) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Clear history").assertExists()
    }

    // --- real-store end to end ------------------------------------------------------------

    @Test
    fun `Undo all drives attemptUndoGroup, members read Undone, the header Resolved and Undo all goes`() {
        val store = UndoHistoryStore()
        var undoAllRuns = 0
        store.openGroup("g1", "Import batch") { undoAllRuns++ }
        val m1 = store.append("First imported", groupId = "g1") { }
        val m2 = store.append("Second imported", groupId = "g1") { }

        composeTestRule.setContent {
            Harness(
                store = store,
                groupIds = listOf("g1"),
                onUndoGroup = { id -> runBlocking { store.attemptUndoGroup(id) } }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("2 items · Undoable").assertExists()

        composeTestRule.onNodeWithTag(undoAllTag("g1")).performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, undoAllRuns)
        assertEquals(
            listOf(UndoStatus.Undone, UndoStatus.Undone),
            listOf(m1, m2).map { id -> store.entries.value.first { it.id == id }.status }
        )
        composeTestRule.onNodeWithText("2 items · Resolved").assertExists()
        composeTestRule.onNodeWithTag(undoAllTag("g1")).assertDoesNotExist()
        assertEquals(2, composeTestRule.onAllNodes(hasText("Undone")).fetchSemanticsNodes().size)
    }

    @Test
    fun `a refused group undo leaves the members Available and Undo all present for a retry`() {
        val store = UndoHistoryStore()
        store.openGroup("g1", "Import batch") { throw UndoGroupRefusedException("changed since") }
        val m1 = store.append("First imported", groupId = "g1") { }
        val m2 = store.append("Second imported", groupId = "g1") { }

        composeTestRule.setContent {
            Harness(
                store = store,
                groupIds = listOf("g1"),
                onUndoGroup = { id -> runBlocking { store.attemptUndoGroup(id) } }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(undoAllTag("g1")).performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(UndoStatus.Available, UndoStatus.Available),
            listOf(m1, m2).map { id -> store.entries.value.first { it.id == id }.status }
        )
        composeTestRule.onNodeWithText("2 items · Undoable").assertExists()
        composeTestRule.onNodeWithTag(undoAllTag("g1")).assertExists()
    }

    @Test
    fun `undoing one member first turns the header partly resolved and keeps Undo all`() {
        val store = UndoHistoryStore()
        store.openGroup("g1", "Import batch") { }
        val m1 = store.append("First imported", groupId = "g1") { }
        store.append("Second imported", groupId = "g1") { }

        composeTestRule.setContent {
            Harness(
                store = store,
                groupIds = listOf("g1"),
                onUndo = { id -> runBlocking { store.attemptUndo(id) } },
                onUndoGroup = { id -> runBlocking { store.attemptUndoGroup(id) } }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasText("Undo") and hasAnyAncestor(hasTestTag(rowTag(m1)))).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("2 items · Partly resolved").assertExists()
        composeTestRule.onNodeWithTag(undoAllTag("g1")).assertExists()
    }
}
