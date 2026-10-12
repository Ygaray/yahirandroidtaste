package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.SheetActionUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Classic parity proof (VRCPT-01, ROADMAP SC1, T-22-12): with the defaulted call, each
 * [VoiceOutcomeUiState] arm built from v2.5.1 arguments only renders EXACTLY the tag and ordered
 * text inventory that v2.5.1's `OutcomeSheet.kt` produces, and setting every Receipt-only field
 * changes neither inventory (D-06) nor adds a Receipt-only tag.
 *
 * The expected constants below were derived by reading v2.5.1's Classic code (the `testTag`
 * literals and the `Text` calls of `SuccessBody`, `HandledByRow`, `BatchResultsList`,
 * `UndoAffordanceBody`, `UndoRowItem`, `FailureBody`, `NeedsConfirmationBody`,
 * `NeedsConfirmationActions`, `ProposedItemRow`), NOT by pasting what HEAD prints. The text
 * inventory is the `Text` semantics value of every node in the UNMERGED tree, in tree order.
 *
 * Hosts the internal `OutcomeSheetContent` with its defaults (`OutcomeSheetContent(outcome)` is the
 * v2.5.1 call shape). One `setContent` per test method.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetClassicParityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun host(outcome: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            Box(Modifier.height(800.dp)) { OutcomeSheetContent(outcome) }
        }
        composeTestRule.waitForIdle()
    }

    private fun tagInventory(): Map<String, Int> =
        composeTestRule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.TestTag),
            useUnmergedTree = true
        ).fetchSemanticsNodes()
            .map { it.config[SemanticsProperties.TestTag] }
            .groupingBy { it }
            .eachCount()

    private fun textInventory(): List<String> =
        composeTestRule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.Text),
            useUnmergedTree = true
        ).fetchSemanticsNodes()
            .map { node -> node.config[SemanticsProperties.Text].joinToString("") { it.text } }

    private fun assertNoReceiptOnlyTag() {
        val leaked = tagInventory().keys.filter { it in RECEIPT_ONLY_TAGS }
        assertTrue("Classic must not render Receipt-only tags, found $leaked", leaked.isEmpty())
    }

    // -- Success ---------------------------------------------------------------------------------

    private fun successV251(): VoiceOutcomeUiState.Success = OutcomeFixtures.success(
        handledBy = OutcomeFixtures.handledBy(tier = "Local", provider = "OpenAI", escalationCount = 2),
        editableContent = { Text("EDITOR") },
        batchResults = OutcomeFixtures.batchResults(),
        undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())
    )

    private fun successWithReceiptFields(): VoiceOutcomeUiState.Success = successV251().copy(
        badge = OutcomeBadge.Success,
        titleActions = { Text("TITLE-ACTIONS") },
        startAction = SheetActionUiModel("Start", {}, ActionButtonEmphasis.Text),
        endAction = SheetActionUiModel("End", {}, ActionButtonEmphasis.Filled)
    )

    @Test
    fun `Success built from v251 arguments renders the v251 inventories`() {
        host(successV251())

        assertEquals(SUCCESS_TAGS, tagInventory())
        assertEquals(SUCCESS_TEXTS, textInventory())
    }

    @Test
    fun `Success with every Receipt only field set renders the same inventories`() {
        host(successWithReceiptFields())

        assertEquals(SUCCESS_TAGS, tagInventory())
        assertEquals(SUCCESS_TEXTS, textInventory())
        assertNoReceiptOnlyTag()
    }

    // -- Failure ---------------------------------------------------------------------------------

    private fun failureV251(): VoiceOutcomeUiState.Failure = OutcomeFixtures.failure(
        handledBy = OutcomeFixtures.handledBy(tier = "Local", model = "gpt-4"),
        action = FailureActionUiModel("Retry", {}, ActionButtonDefaults.ActionButtonRole.Save),
        body = { Text("BODYTEXT") },
        semanticsPrefix = "Error:"
    )

    private fun failureWithReceiptFields(): VoiceOutcomeUiState.Failure = failureV251().copy(
        title = "Receipt title",
        transcript = "I ate two eggs",
        tone = FailureTone.Error,
        tip = { Text("TIP-TEXT") },
        secondaryAction = SheetActionUiModel("Dismiss", {}, ActionButtonEmphasis.Text),
        badge = OutcomeBadge.Error,
        undo = null
    )

    @Test
    fun `Failure built from v251 arguments renders the v251 inventories`() {
        host(failureV251())

        assertEquals(FAILURE_TAGS, tagInventory())
        assertEquals(FAILURE_TEXTS, textInventory())
    }

    @Test
    fun `Failure with every Receipt only field set renders the same inventories`() {
        host(failureWithReceiptFields())

        assertEquals(FAILURE_TAGS, tagInventory())
        assertEquals(FAILURE_TEXTS, textInventory())
        assertNoReceiptOnlyTag()
    }

    // -- NeedsConfirmation -----------------------------------------------------------------------

    private fun confirmationItems() = listOf(
        ProposedItemUiModel(id = "item-0", title = "Proposed item 0", subtitle = "detail 0"),
        ProposedItemUiModel(id = "item-1", title = "Proposed item 1", subtitle = "detail 1", onRemove = {}),
        ProposedItemUiModel(id = "item-2", title = "Proposed item 2", subtitle = "detail 2")
    )

    private fun confirmationV251() = OutcomeFixtures.needsConfirmation(
        reason = "You said: eggs",
        title = "Log these?",
        items = confirmationItems(),
        topLevelContent = { Text("TOP") },
        reversibilityHint = "Undoable"
    )

    private fun confirmationWithReceiptFields() = confirmationV251().copy(
        titleActions = { Text("TITLE-ACTIONS") }
    )

    @Test
    fun `NeedsConfirmation built from v251 arguments renders the v251 inventories`() {
        host(confirmationV251())

        assertEquals(CONFIRMATION_TAGS, tagInventory())
        assertEquals(CONFIRMATION_TEXTS, textInventory())
    }

    @Test
    fun `NeedsConfirmation with titleActions set renders the same inventories`() {
        host(confirmationWithReceiptFields())

        assertEquals(CONFIRMATION_TAGS, tagInventory())
        assertEquals(CONFIRMATION_TEXTS, textInventory())
        assertNoReceiptOnlyTag()
        assertTrue("outcome_sheet_title_actions" !in tagInventory().keys)
    }

    // -- HandledByRow ----------------------------------------------------------------------------

    @Test
    fun `HandledByRow default model renders the tier and the provider caption`() {
        host(OutcomeFixtures.success(handledBy = OutcomeFixtures.handledBy(tier = "Local", provider = "OpenAI")))

        assertEquals(listOf("Logged 2 of 3", "Local", "OpenAI"), textInventory())
    }

    @Test
    fun `HandledByRow ignores prefixLabel and onDevice`() {
        host(
            OutcomeFixtures.success(
                handledBy = OutcomeFixtures.handledBy(
                    tier = "Local",
                    provider = "OpenAI",
                    prefixLabel = "Bearbeitet von",
                    onDevice = true
                )
            )
        )

        assertEquals(listOf("Logged 2 of 3", "Local", "OpenAI"), textInventory())
        composeTestRule.onAllNodesWithText("Bearbeitet von", substring = true, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    // -- Public entry ----------------------------------------------------------------------------

    @Test
    fun `positional three argument OutcomeSheet call renders the Success summary`() {
        composeTestRule.setContent {
            OutcomeSheet(OutcomeFixtures.success(), {}, Modifier)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Logged 2 of 3").assertExists()
    }

    private companion object {
        /** Tags no Classic render may carry (D-06); the Receipt tags of 22-02, 22-04 and 22-05. */
        val RECEIPT_ONLY_TAGS = setOf(
            "outcome_sheet_title",
            "outcome_sheet_badge",
            "outcome_sheet_title_actions",
            "outcome_sheet_transcript",
            "outcome_sheet_action_bar",
            "outcome_sheet_action_bar_divider",
            "outcome_sheet_action_start",
            "outcome_sheet_action_end",
            "outcome_sheet_failure_tip"
        )

        // v2.5.1 SuccessBody: summary; HandledByRow (tier, then "provider · label count"); two
        // batch rows (a failed row adds its detail); the editor slot; UndoAffordanceBody (undo-all
        // button label, rows in list order with the Undone label trailing, then the refused text).
        val SUCCESS_TAGS = mapOf(
            "outcome_sheet_handled_by" to 1,
            "outcome_sheet_batch_row" to 2,
            "outcome_sheet_undo_all" to 1,
            "outcome_sheet_undo_row" to 2,
            "outcome_sheet_undo_refused" to 1
        )
        val SUCCESS_TEXTS = listOf(
            "Logged 2 of 3",
            "Local",
            "OpenAI · Escalations: 2",
            "Oats",
            "Milk",
            "not found",
            "EDITOR",
            "Undo all (1)",
            "Row A",
            "Row B",
            "Undone",
            "Couldn't undo: it was edited elsewhere"
        )

        // v2.5.1 FailureBody: reason; HandledByRow; the body slot; the in-card action label.
        val FAILURE_TAGS = mapOf(
            "outcome_sheet_failure_surface" to 1,
            "outcome_sheet_handled_by" to 1,
            "outcome_sheet_action_button" to 1
        )
        val FAILURE_TEXTS = listOf("Network down", "Local", "gpt-4", "BODYTEXT", "Retry")

        // v2.5.1 NeedsConfirmationBody (title, reason, hint, top-level slot, one merged row per
        // item with title and subtitle, a remove IconButton with a content description only) and
        // the pinned NeedsConfirmationActions (Cancel then Confirm).
        val CONFIRMATION_TAGS = mapOf(
            "outcome_sheet_confirmation_top_level_content" to 1,
            "outcome_sheet_confirmation_item" to 3,
            "outcome_sheet_confirmation_item_remove" to 1,
            "outcome_sheet_confirmation_cancel" to 1,
            "outcome_sheet_confirmation_confirm" to 1
        )
        val CONFIRMATION_TEXTS = listOf(
            "Log these?",
            "You said: eggs",
            "Undoable",
            "TOP",
            "Proposed item 0",
            "detail 0",
            "Proposed item 1",
            "detail 1",
            "Proposed item 2",
            "detail 2",
            "Cancel",
            "Confirm"
        )
    }
}
