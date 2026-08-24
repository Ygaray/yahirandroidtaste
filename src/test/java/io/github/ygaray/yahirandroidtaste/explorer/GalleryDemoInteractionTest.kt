package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Renders the [ComponentRegistry]'s OWN `TagChipEditorContent` `StateCell` content lambdas — the
 * exact render lambdas `ComponentDetailScreen` mounts on that entry's detail page, reached
 * through `ComponentRegistry.entries` the same way `ComponentStatesMatrixTest` reaches them — so
 * this test cannot drift from what a viewer of the `ExplorerActivity` gallery actually sees
 * (Phase 114-04 Task 1, cycle-1 review finding 7).
 *
 * **Behavioral proof (`<proof_scope>`).** This is the load-bearing evidence that the TAG-03
 * gallery demo (`DemoTagChipEditor` in `SheetsFamilyScreen.kt`) drives real, live state rather
 * than a no-op `onRemoveTag = {}` binding — a demo wired to a no-op fails every test below. The
 * per-cell isolation test additionally guards the state-scoping rule (cycle-1 review finding 8):
 * every `StateCell`'s mutable demo state must be `remember`-scoped to its own render lambda, never
 * shared with a sibling cell.
 *
 * **Explicit non-claim.** This class does NOT drive the EDIT-04 bottom-sheet demos
 * (`TextCardBottomSheet`/`ListCardBottomSheet`'s Edit-menu routing in `SheetsFamilyScreen.kt`).
 * Both sheets wrap `SheetScaffold`'s live `ModalBottomSheet`, which this module's Robolectric
 * harness cannot drive — the same limitation Plan 01 records in its own `<proof_scope>`, and the
 * reason both sheets' registry `StateCell`s are `N/A` (no render lambda) with recorded WR-01
 * rationale. The EDIT-04 gallery wiring is verified elsewhere by compilation plus a source
 * assertion (`grep -c onEditRequest`) and confirmed visually by running the gallery app — NOT by
 * a rendered test in this file. Do not read the absence of an EDIT-04 test here as a gap; it is a
 * recorded, deliberate scope boundary.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class GalleryDemoInteractionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun tagChipEditorEntry(): ComponentRegistry.Entry =
        ComponentRegistry.entries.first { it.name == "TagChipEditorContent" }

    private fun defaultCellRender(): @androidx.compose.runtime.Composable () -> Unit =
        tagChipEditorEntry().states.first { it.label == "Default" }.render
            ?: error("TagChipEditorContent's Default StateCell has no render lambda")

    private fun pressedSelectedCellRender(): @androidx.compose.runtime.Composable () -> Unit =
        tagChipEditorEntry().states.first { it.label == "Pressed / Selected" }.render
            ?: error("TagChipEditorContent's Pressed / Selected StateCell has no render lambda")

    @Test
    fun tagChipEditorContent_defaultCell_doubleTapRemovesChip_siblingsRemain() {
        composeTestRule.setContent {
            defaultCellRender().invoke()
        }

        // Default cell is seeded with 2 tags (ExplorerFakeData.tagChips.take(2)): Work, Personal.
        composeTestRule.onNodeWithContentDescription("Work tag").assertExists()
        composeTestRule.onNodeWithContentDescription("Personal tag").assertExists()

        composeTestRule.onNodeWithContentDescription("Work tag")
            .performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        // The double-tapped chip's node no longer exists; its sibling remains -- proves the demo
        // drives a real removal callback, not a no-op onRemoveTag = {}.
        composeTestRule.onNodeWithContentDescription("Work tag").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Personal tag").assertExists()
    }

    @Test
    fun tagChipEditorContent_defaultCell_removingDownToOne_showsDerivedLastTagWarning() {
        composeTestRule.setContent {
            defaultCellRender().invoke()
        }

        // With 2 tags present, isLastTag derives to false -- the warning is not shown.
        composeTestRule.onNodeWithText("Removing this tag will leave the card untagged")
            .assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Work tag")
            .performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        // One tag ("Personal") remains -- isLastTag derives to true (tags.size == 1) and the
        // warning appears, proving it is genuinely derived from live demo state, not hardcoded.
        composeTestRule.onNodeWithContentDescription("Personal tag").assertExists()
        composeTestRule.onNodeWithText("Removing this tag will leave the card untagged")
            .assertExists()
    }

    @Test
    fun tagChipEditorContent_defaultAndPressedSelectedCells_areStateIsolated() {
        // Each cell is wrapped in its own testTag'd Box so its own "Work tag" chip is
        // unambiguously addressable even though BOTH cells seed a chip named "Work" from the
        // same ExplorerFakeData.tagChips source (Default: Work+Personal; Pressed / Selected:
        // Work alone) -- this is the concrete rendered test of the state-scoping rule (cycle-1
        // review finding 8): each cell's `remember` must be its own instance, not shared.
        composeTestRule.setContent {
            Column {
                Box(Modifier.testTag("cell_default")) { defaultCellRender().invoke() }
                Box(Modifier.testTag("cell_pressed_selected")) { pressedSelectedCellRender().invoke() }
            }
        }

        val defaultCellWorkChip = composeTestRule.onNode(
            hasContentDescription("Work tag") and hasAnyAncestor(hasTestTag("cell_default"))
        )
        val pressedSelectedCellWorkChip = composeTestRule.onNode(
            hasContentDescription("Work tag") and
                hasAnyAncestor(hasTestTag("cell_pressed_selected"))
        )

        defaultCellWorkChip.assertExists()
        pressedSelectedCellWorkChip.assertExists()
        composeTestRule.onAllNodesWithContentDescription("Work tag").assertCountEquals(2)

        // Remove ONLY the Default cell's "Work" chip. If the two cells accidentally shared
        // mutable state (the failure this test guards against), this single removal would also
        // clear the Pressed / Selected cell's own "Work" chip.
        defaultCellWorkChip.performTouchInput { doubleClick() }
        composeTestRule.waitForIdle()

        defaultCellWorkChip.assertDoesNotExist()
        pressedSelectedCellWorkChip.assertExists()
        composeTestRule.onAllNodesWithContentDescription("Work tag").assertCountEquals(1)
    }
}
