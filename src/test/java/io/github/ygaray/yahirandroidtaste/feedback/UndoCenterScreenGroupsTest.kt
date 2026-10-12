package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose UI tests for the group rows of [UndoCenterScreen] (VUNDO-02): header, count and status
 * text, Undo all, indent, the status rule and the real-store end to end.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UndoCenterScreenGroupsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

        composeTestRule.onNodeWithTag("${UNDO_GROUP_HEADER_TEST_TAG_PREFIX}g1").assertExists()
        composeTestRule.onNodeWithText("Import batch").assertExists()
        composeTestRule.onNodeWithText("2 items · Undoable").assertExists()
        composeTestRule.onNodeWithTag("${UNDO_GROUP_UNDO_ALL_TEST_TAG_PREFIX}g1").assertTextEquals("Undo all")
        composeTestRule.onNodeWithTag("$UNDO_ROW_TEST_TAG_PREFIX$m1").assertExists()
        composeTestRule.onNodeWithTag("$UNDO_ROW_TEST_TAG_PREFIX$m2").assertExists()
        composeTestRule.onNodeWithTag("$UNDO_ROW_TEST_TAG_PREFIX$solo").assertExists()

        composeTestRule.onNodeWithTag("${UNDO_GROUP_UNDO_ALL_TEST_TAG_PREFIX}g1").performClick()
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

        composeTestRule.onNodeWithTag("${UNDO_GROUP_HEADER_TEST_TAG_PREFIX}g1").assertDoesNotExist()
        composeTestRule.onNodeWithText("Undo all").assertDoesNotExist()
    }
}
