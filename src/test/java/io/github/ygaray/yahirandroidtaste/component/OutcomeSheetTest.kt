package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
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
}
