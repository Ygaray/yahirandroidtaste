package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoAffordanceUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRefusedUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Compose tests for [OutcomeSheet] (Phase 11 Plan 01 Task 1, VOUT-01/02/03).
 *
 * Harness mirrors this module's established Robolectric+Compose harness for `SheetScaffold`-based
 * (`ModalBottomSheet`) components ([AlbumTitleConfirmSheetComposeTest]): `@RunWith
 * (RobolectricTestRunner::class)`, `@Config(sdk = [35])`, `createComposeRule()`, direct
 * instantiation inside `setContent`, node resolution works inside the sheet's separately-rooted
 * composition window.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── VOUT-01: Success renders its summary ────────────────────────────────

    @Test
    fun `Success renders its summary text`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(summary = "Logged 1 item"),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Logged 1 item").assertExists()
    }

    // ── VOUT-02: handled-by indicator ────────────────────────────────────────

    @Test
    fun `non-null handledBy renders the tier text`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Logged 1 item",
                    handledBy = HandledByUiModel(tier = "Local")
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_handled_by").assertExists()
        composeTestRule.onNodeWithText("Local").assertExists()
    }

    @Test
    fun `null handledBy renders no handled-by row`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(summary = "Logged 1 item", handledBy = null),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_handled_by").assertDoesNotExist()
    }

    // ── VOUT-03: loud failure surface + optional action slot ───────────────

    @Test
    fun `Failure renders on the failure surface tag with the reason text`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Failure(reason = "Could not reach the provider"),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_failure_surface").assertExists()
        composeTestRule.onNodeWithText("Could not reach the provider").assertExists()
    }

    @Test
    fun `non-null action renders exactly one action button and invokes onClick on tap`() {
        var clicked = false
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Failure(
                    reason = "API key rejected",
                    action = FailureActionUiModel(label = "Open Settings", onClick = { clicked = true })
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_action_button").assertCountEquals(1)
        composeTestRule.onNodeWithTag("outcome_sheet_action_button").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, clicked)
    }

    @Test
    fun `null action renders NO action at all -- no implicit default action ever renders`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Failure(reason = "Could not reach the provider", action = null),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_action_button").assertDoesNotExist()
    }

    // ── VUNDO-01: grouped undo affordance happy path (Phase 11 Plan 01 Task 1) ─────

    @Test
    fun `Success with a non-null undo renders undo-all and every row, and undo-all invokes its callback`() {
        var undoAllInvoked = false
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Logged 2 items",
                    undo = UndoAffordanceUiModel(
                        allLabel = "Undo all (2)",
                        rows = listOf(
                            UndoRowUiModel(id = "1", label = "Card deleted", state = UndoRowState.Available(onUndo = {})),
                            UndoRowUiModel(id = "2", label = "Tag removed", state = UndoRowState.Available(onUndo = {}))
                        ),
                        onUndoAll = { undoAllInvoked = true }
                    )
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_undo_all").assertCountEquals(1)
        composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row").assertCountEquals(2)

        composeTestRule.onNodeWithTag("outcome_sheet_undo_all").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, undoAllInvoked)
    }

    // ── VUNDO-01: edge hardening -- Unavailable/Refused rendering (Phase 11 Plan 01 Task 2) ──

    @Test
    fun `an Undone row renders muted trailing text and is never clickable`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Logged 1 item",
                    undo = UndoAffordanceUiModel(
                        allLabel = "Undo all (0)",
                        rows = listOf(
                            UndoRowUiModel(id = "1", label = "Card deleted", state = UndoRowState.Undone)
                        )
                    )
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        val rows = composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row")
        rows.assertCountEquals(1)
        rows[0].assert(hasText("Card deleted"))
        rows[0].assert(hasText("Undone"))
        rows[0].assertHasNoClickAction()
    }

    @Test
    fun `an Unavailable row renders its reason as non-clickable text while a sibling Available row still fires its own onUndo, and allLabel renders verbatim`() {
        var availableRowUndoInvoked = false
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Logged 1 item",
                    undo = UndoAffordanceUiModel(
                        allLabel = "Undo all (1)",
                        rows = listOf(
                            UndoRowUiModel(
                                id = "1",
                                label = "Card deleted",
                                state = UndoRowState.Available(onUndo = { availableRowUndoInvoked = true })
                            ),
                            UndoRowUiModel(
                                id = "2",
                                label = "Tag removed",
                                state = UndoRowState.Unavailable(reason = "Entangled")
                            )
                        ),
                        onUndoAll = {}
                    )
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        // The composable never recomputes the count -- allLabel renders exactly what it was given.
        composeTestRule.onNodeWithText("Undo all (1)").assertExists()
        composeTestRule.onNodeWithText("Entangled").assertExists()

        val rows = composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row")
        rows.assertCountEquals(2)
        rows[1].assertHasNoClickAction()

        rows[0].performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, availableRowUndoInvoked)
    }

    @Test
    fun `a null undo renders no undo_all or undo_row node at all`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(summary = "Logged 1 item", undo = null),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_undo_all").assertDoesNotExist()
        composeTestRule.onNodeWithTag("outcome_sheet_undo_row").assertDoesNotExist()
    }

    @Test
    fun `an undo with empty rows and a null onUndoAll also renders neither undo_all nor undo_row`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Logged 1 item",
                    undo = UndoAffordanceUiModel(allLabel = "Undo all (0)")
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_undo_all").assertDoesNotExist()
        composeTestRule.onNodeWithTag("outcome_sheet_undo_row").assertDoesNotExist()
    }

    @Test
    fun `undo rows render in the exact supplied list order, never resorted`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Logged 3 items",
                    undo = UndoAffordanceUiModel(
                        allLabel = "Undo all (3)",
                        rows = listOf(
                            UndoRowUiModel(id = "1", label = "Zebra card", state = UndoRowState.Available(onUndo = {})),
                            UndoRowUiModel(id = "2", label = "Apple tag", state = UndoRowState.Available(onUndo = {})),
                            UndoRowUiModel(id = "3", label = "Mango link", state = UndoRowState.Available(onUndo = {}))
                        )
                    )
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        val rows = composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row")
        rows.assertCountEquals(3)
        rows[0].assert(hasText("Zebra card"))
        rows[1].assert(hasText("Apple tag"))
        rows[2].assert(hasText("Mango link"))
    }

    @Test
    fun `a non-null refused renders the error-container surface with the reason and appends the changed-since suffix when changedItem is non-null`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Logged 1 item",
                    undo = UndoAffordanceUiModel(
                        allLabel = "Undo all (1)",
                        refused = UndoRefusedUiModel(reason = "Item changed", changedItem = "Card 1")
                    )
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_undo_refused").assertExists()
        composeTestRule.onNodeWithText("Couldn't undo: Item changed, Card 1 changed since").assertExists()
    }

    // ── CR-02: inFlight locks the undo affordance (regression) ─────────────────────

    @Test
    fun `inFlight true strips the click action from undo-all and every Available row`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Logging 2 items",
                    inFlight = true,
                    undo = UndoAffordanceUiModel(
                        allLabel = "Undo all (2)",
                        rows = listOf(
                            UndoRowUiModel(id = "1", label = "Card deleted", state = UndoRowState.Available(onUndo = {}))
                        ),
                        onUndoAll = {}
                    )
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        // Both controls still render (locked = disabled, never hidden, CR-02). "Undo all" is a
        // DynamicActionButton -- M3's own enabled=false (AlbumTitleConfirmSheetTest's established
        // assertIsNotEnabled convention). Row items carry no clickable Modifier at all while
        // locked (not a disabled-clickable) -- ApproachLadderCardTest's assertHasNoClickAction
        // convention for an absent callback applies instead.
        composeTestRule.onNodeWithTag("outcome_sheet_undo_all").assertExists()
        composeTestRule.onNodeWithTag("outcome_sheet_undo_all").assertIsNotEnabled()
        val rows = composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row")
        rows.assertCountEquals(1)
        rows[0].assertHasNoClickAction()
    }

    @Test
    fun `a non-null refused with a null changedItem renders the reason without the changed-since suffix`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Logged 1 item",
                    undo = UndoAffordanceUiModel(
                        allLabel = "Undo all (1)",
                        refused = UndoRefusedUiModel(reason = "Item changed", changedItem = null)
                    )
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_undo_refused").assertExists()
        composeTestRule.onNodeWithText("Couldn't undo: Item changed").assertExists()
    }

    // ── VOUT-04: NeedsConfirmation -- single-item confirm/cancel (Plan 01 Task 1) ──

    @Test
    fun `NeedsConfirmation with a single item renders its title, reason, reversibilityHint, and the item's title and subtitle`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "This will permanently remove the card and its history.",
                    title = "Delete card?",
                    items = listOf(ProposedItemUiModel(id = "card-1", title = "Grocery list", subtitle = "12 items")),
                    reversibilityHint = "Irreversible",
                    confirmLabel = "Delete",
                    onConfirm = {},
                    onCancel = {}
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Delete card?").assertExists()
        composeTestRule.onNodeWithText("This will permanently remove the card and its history.").assertExists()
        composeTestRule.onNodeWithText("Irreversible").assertExists()
        composeTestRule.onNodeWithText("Grocery list").assertExists()
        composeTestRule.onNodeWithText("12 items").assertExists()
    }

    @Test
    fun `tapping the Confirm button invokes onConfirm exactly once and does not invoke onCancel`() {
        var confirmed = false
        var cancelled = false
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "This will permanently remove the card and its history.",
                    title = "Delete card?",
                    items = listOf(ProposedItemUiModel(id = "card-1", title = "Grocery list")),
                    confirmLabel = "Delete",
                    onConfirm = { confirmed = true },
                    onCancel = { cancelled = true }
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, confirmed)
        assertEquals(false, cancelled)
    }

    @Test
    fun `tapping the Cancel button invokes onCancel exactly once and does not invoke onConfirm`() {
        var confirmed = false
        var cancelled = false
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "This will permanently remove the card and its history.",
                    title = "Delete card?",
                    items = listOf(ProposedItemUiModel(id = "card-1", title = "Grocery list")),
                    confirmLabel = "Delete",
                    onConfirm = { confirmed = true },
                    onCancel = { cancelled = true }
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_cancel").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, cancelled)
        assertEquals(false, confirmed)
    }

    @Test
    fun `ProposedItemUiModel toString never prints title, subtitle, or confidenceCue`() {
        val item = ProposedItemUiModel(
            id = "x",
            title = "Secret Name",
            subtitle = "Secret Sub",
            confidenceCue = "Weak match"
        )

        assertEquals("ProposedItemUiModel(id=x, amended=false)", item.toString())
    }

    // ── CR-01: NeedsConfirmation's own toString() must also stay privacy-safe (regression) ──

    @Test
    fun `NeedsConfirmation toString never prints title or reason`() {
        val confirmation = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "This will permanently remove 'My Secret Diary' and its history.",
            title = "Delete 'My Secret Diary'?",
            items = listOf(ProposedItemUiModel(id = "card-1", title = "My Secret Diary")),
            onConfirm = {},
            onCancel = {}
        )

        val representation = confirmation.toString()

        assertEquals(
            "NeedsConfirmation(items=1, selectionMode=AllOrNothing, severity=Neutral)",
            representation
        )
        assertEquals(false, representation.contains("My Secret Diary"))
        assertEquals(false, representation.contains(confirmation.reason))
        assertEquals(false, representation.contains(confirmation.title ?: ""))
    }

    // ── VOUT-04: NeedsConfirmation -- batch, topLevelContent, per-item remove, edge coverage (Plan 01 Task 2) ──

    @Test
    fun `a batch of 3 items renders exactly 3 rows in the exact supplied order, and topLevelContent renders exactly once`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "3 items parsed from your grocery run.",
                    items = listOf(
                        ProposedItemUiModel(id = "1", title = "Apple"),
                        ProposedItemUiModel(id = "2", title = "Banana"),
                        ProposedItemUiModel(id = "3", title = "Bread")
                    ),
                    topLevelContent = { Text("Logged for: Today") },
                    onConfirm = {},
                    onCancel = {}
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        val rows = composeTestRule.onAllNodesWithTag("outcome_sheet_confirmation_item")
        rows.assertCountEquals(3)
        rows[0].assert(hasText("Apple"))
        rows[1].assert(hasText("Banana"))
        rows[2].assert(hasText("Bread"))

        composeTestRule.onAllNodesWithTag("outcome_sheet_confirmation_top_level_content").assertCountEquals(1)
    }

    @Test
    fun `tapping a specific row's remove control invokes THAT row's own onRemove and no other row's`() {
        var row0Removed = false
        var row1Removed = false
        var row2Removed = false
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "3 items parsed from your grocery run.",
                    items = listOf(
                        ProposedItemUiModel(id = "1", title = "Apple", onRemove = { row0Removed = true }),
                        ProposedItemUiModel(id = "2", title = "Banana", onRemove = { row1Removed = true }),
                        ProposedItemUiModel(id = "3", title = "Bread", onRemove = { row2Removed = true })
                    ),
                    onConfirm = {},
                    onCancel = {}
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_confirmation_item_remove", useUnmergedTree = true)[1].performClick()
        composeTestRule.waitForIdle()

        assertEquals(false, row0Removed)
        assertEquals(true, row1Removed)
        assertEquals(false, row2Removed)
    }

    @Test
    fun `two items sharing the same id, and separately two sharing the same title, both render as 2 separate rows, never merged`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "Duplicate adjacency check.",
                    items = listOf(
                        ProposedItemUiModel(id = "dup", title = "Milk"),
                        ProposedItemUiModel(id = "dup", title = "Eggs"),
                        ProposedItemUiModel(id = "a", title = "Same Title"),
                        ProposedItemUiModel(id = "b", title = "Same Title")
                    ),
                    onConfirm = {},
                    onCancel = {}
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_confirmation_item").assertCountEquals(4)
    }

    @Test
    fun `items with duplicate titles still render in the exact supplied list order, never resorted`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "Ordering check.",
                    items = listOf(
                        ProposedItemUiModel(id = "1", title = "Zebra item", subtitle = "first"),
                        ProposedItemUiModel(id = "2", title = "Zebra item", subtitle = "second"),
                        ProposedItemUiModel(id = "3", title = "Zebra item", subtitle = "third")
                    ),
                    onConfirm = {},
                    onCancel = {}
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        val rows = composeTestRule.onAllNodesWithTag("outcome_sheet_confirmation_item")
        rows.assertCountEquals(3)
        rows[0].assert(hasText("first"))
        rows[1].assert(hasText("second"))
        rows[2].assert(hasText("third"))
    }

    @Test
    fun `an empty items list renders zero item rows without crashing, while reason and both buttons still render`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "Nothing left to confirm.",
                    items = emptyList(),
                    onConfirm = {},
                    onCancel = {}
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_confirmation_item").assertCountEquals(0)
        composeTestRule.onNodeWithText("Nothing left to confirm.").assertExists()
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").assertExists()
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_cancel").assertExists()
    }

    // ── WR-03: item.trailingContent has real rendering coverage ────────────────────

    @Test
    fun `a non-null trailingContent renders for its own row`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "Review this item before confirming.",
                    items = listOf(
                        ProposedItemUiModel(id = "tag-1", title = "Apple", trailingContent = { Text("tag") })
                    ),
                    onConfirm = {},
                    onCancel = {}
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("tag").assertExists()
    }

    @Test
    fun `severity Destructive still renders a clickable Confirm button whose tap invokes onConfirm`() {
        var confirmed = false
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = VoiceOutcomeUiState.NeedsConfirmation(
                    reason = "This will permanently remove the card and its history.",
                    items = listOf(ProposedItemUiModel(id = "card-1", title = "Grocery list")),
                    severity = ActionButtonDefaults.ActionButtonRole.Destructive,
                    confirmLabel = "Delete",
                    onConfirm = { confirmed = true },
                    onCancel = {}
                ),
                onDismissRequest = {}
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, confirmed)
    }

    // ── v2.4.1 regression (CT Phase 74) ──────────────────────────────────────
    // A NeedsConfirmation taller than its host must keep the Confirm/Cancel action row VISIBLE and
    // full-height — never starved to a ~10px sliver by the bounded sheet height. Hosts the internal
    // OutcomeSheetContent seam in a height-bounded Box (no ModalBottomSheet needed), mirroring CT's
    // shape (tall topLevelContent + several items). Goes RED if OutcomeSheetContent's scroll-body /
    // pinned-footer structure is reverted to a plain bounded Column (the trailing row starves).

    @Test
    fun `tall NeedsConfirmation keeps Confirm visible and full-height`() {
        composeTestRule.setContent {
            Box(Modifier.height(240.dp)) {
                OutcomeSheetContent(outcome = tallNeedsConfirmationFixture())
            }
        }
        composeTestRule.waitForIdle()

        // Pinned footer: both actions laid out, visible, and at least a full interactive height —
        // pre-fix these collapsed to ~10px with their labels absent from the tree (CT TESTER bounds).
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(40.dp)
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_cancel")
            .assertIsDisplayed()
            .assertHeightIsAtLeast(40.dp)
        // The label text actually reaches the rendered/semantics surface (CT found zero "Confirm"
        // matches pre-fix because the starved button never laid its Text out).
        composeTestRule.onNodeWithText("Confirm").assertExists()
    }

    /**
     * A CT-Phase-74-shaped [VoiceOutcomeUiState.NeedsConfirmation] whose content is deliberately
     * TALLER than its host Box: a tall [VoiceOutcomeUiState.NeedsConfirmation.topLevelContent] slot
     * plus several proposed items (each with its own trailing editor), so the trailing action row
     * is exactly the child a plain bounded [androidx.compose.foundation.layout.Column] would starve.
     */
    private fun tallNeedsConfirmationFixture() = VoiceOutcomeUiState.NeedsConfirmation(
        title = "Log these?",
        reason = "You said: “I ate two eggs and toast”",
        reversibilityHint = "You can undo this from the log.",
        severity = ActionButtonDefaults.ActionButtonRole.Save,
        confirmLabel = "Confirm",
        cancelLabel = "Cancel",
        topLevelContent = { Box(Modifier.height(400.dp)) { Text("date + correction controls") } },
        items = List(4) { i ->
            ProposedItemUiModel(
                id = "item-$i",
                title = "Proposed item $i",
                subtitle = "an amount / unit detail line",
                trailingContent = { Box(Modifier.height(80.dp)) { Text("editor $i") } }
            )
        },
        onConfirm = {},
        onCancel = {}
    )
}
