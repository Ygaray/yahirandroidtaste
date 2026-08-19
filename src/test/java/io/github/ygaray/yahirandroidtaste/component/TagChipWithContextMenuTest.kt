package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [TagChipWithContextMenu]'s additive `onDoubleClick` parameter (Phase 106-02,
 * TAG-02).
 *
 * **De-risking probe (step 3a, REVIEWS.md cycle-2 MEDIUM finding):** [longPress_opensMenu_editItemVisible]
 * is written and run ALONE first, against the pre-change signature (no `onDoubleClick` parameter),
 * to isolate harness reachability from this task's production edit. `TagChipWithContextMenu`
 * renders its context menu with Material3's `DropdownMenu`, which is `Popup`-backed — a second
 * composition window, the same cross-window family as the `Dialog` content plan 106-01 Task 2 had
 * to prove. No test anywhere in this hub asserts a node inside a dropdown menu or any other popup
 * window, so this is treated as an explicit checked step, not an assumption.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TagChipWithContextMenuTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun longPress_opensMenu_editItemVisible() {
        composeTestRule.setContent {
            TagChipWithContextMenu(
                label = "Work",
                isSelected = false,
                onClick = {},
                onEdit = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Edit").assertExists()
    }

    @Test
    fun `double-tap invokes the callback exactly once and does not open the dropdown menu`() {
        var doubleClickCount = 0

        composeTestRule.setContent {
            TagChipWithContextMenu(
                label = "Work",
                isSelected = false,
                onClick = {},
                onEdit = {},
                onDoubleClick = { doubleClickCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        assertEquals(
            "A double-tap must invoke onDoubleClick exactly once",
            1,
            doubleClickCount
        )
        // The menu's items are absent from the tree afterward — double-tap carries no hub-side
        // menu behavior (a straight passthrough, per D-02), unlike onLongClick which is wrapped
        // locally to open the menu.
        composeTestRule.onNodeWithText("Edit").assertDoesNotExist()
    }

    @Test
    fun `long press unregressed with onDoubleClick present - all three menu items show`() {
        composeTestRule.setContent {
            TagChipWithContextMenu(
                label = "Work",
                isSelected = true,
                onClick = {},
                onEdit = {},
                onRemoveFromContext = {},
                onDelete = {},
                removeLabel = "Remove from this card",
                onDoubleClick = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Edit").assertExists()
        composeTestRule.onNodeWithText("Remove from this card").assertExists()
        composeTestRule.onNodeWithText("Delete tag everywhere").assertExists()
    }

    @Test
    fun `long press unregressed with onDoubleClick omitted - Edit tap invokes onEdit once and closes`() {
        var editCount = 0

        composeTestRule.setContent {
            TagChipWithContextMenu(
                label = "Work",
                isSelected = false,
                onClick = {},
                onEdit = { editCount++ }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { longClick() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Edit").assertExists()

        composeTestRule.onNodeWithText("Edit").performClick()
        composeTestRule.waitForIdle()

        assertEquals("Tapping Edit must invoke onEdit exactly once", 1, editCount)
        composeTestRule.onNodeWithText("Edit").assertDoesNotExist()
    }

    @Test
    fun `menu composition unaffected by onDoubleClick - only delete supplied shows only delete`() {
        composeTestRule.setContent {
            TagChipWithContextMenu(
                label = "Work",
                isSelected = false,
                onClick = {},
                onDelete = {},
                onDoubleClick = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Work").performTouchInput { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Delete tag everywhere").assertExists()
        composeTestRule.onNodeWithText("Edit").assertDoesNotExist()
        composeTestRule.onAllNodesWithText("Remove", substring = true)
            .assertCountEquals(0)
    }
}
