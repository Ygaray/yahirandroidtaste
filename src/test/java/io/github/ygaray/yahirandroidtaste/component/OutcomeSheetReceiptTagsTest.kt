package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The Y-14 stable tag matrix across every Receipt state (VTAG-01, D-13, ROADMAP SC4). For each state
 * the exact node count (unmerged tree) is asserted for every applicable tag and zero for every other
 * member of the stable set. The two test-only hooks (`outcome_sheet_action_bar_divider`,
 * `outcome_sheet_handled_by_dot`) are not part of the stable set and are not asserted here. One
 * `setContent` per test method; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetReceiptTagsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val stableTags = listOf(
        "outcome_sheet_title",
        "outcome_sheet_badge",
        "outcome_sheet_title_actions",
        "outcome_sheet_transcript",
        "outcome_sheet_action_bar",
        "outcome_sheet_action_start",
        "outcome_sheet_action_end",
        "outcome_sheet_failure_surface",
        "outcome_sheet_failure_tip",
        "outcome_sheet_handled_by",
        "outcome_sheet_confirmation_cancel",
        "outcome_sheet_confirmation_confirm"
    )

    /** Other Y-14 tags that depend on the state and are asserted next to the stable set. */
    private val extraTags = listOf(
        "outcome_sheet_confirmation_item",
        "outcome_sheet_confirmation_item_remove",
        "outcome_sheet_confirmation_top_level_content",
        "outcome_sheet_batch_row",
        "outcome_sheet_undo_all",
        "outcome_sheet_undo_row"
    )

    /**
     * Renders [outcome] and asserts [present] (tag to exact count); every other tag of the stable set
     * and of [extraTags] must be absent.
     */
    private fun assertTags(outcome: VoiceOutcomeUiState, present: Map<String, Int>) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.height(900.dp)) { OutcomeSheetReceiptContent(outcome) }
            }
        }
        composeTestRule.waitForIdle()
        (stableTags + extraTags).forEach { tag ->
            composeTestRule.onAllNodesWithTag(tag, useUnmergedTree = true).assertCountEquals(present[tag] ?: 0)
        }
    }

    private fun allUndoneRows(): List<UndoRowUiModel> = listOf(
        UndoRowUiModel(id = "row-a", label = "Row A", state = UndoRowState.Undone),
        UndoRowUiModel(id = "row-b", label = "Row B", state = UndoRowState.Undone)
    )

    // ---- NeedsConfirmation ----------------------------------------------------------------------

    @Test
    fun `confirm single carries the title, bar and confirmation tags only`() {
        assertTags(
            OutcomeFixtures.needsConfirmation(
                items = listOf(ProposedItemUiModel(id = "only", title = "One item", onRemove = {})),
                titleActions = { Text("title action") }
            ),
            mapOf(
                "outcome_sheet_title" to 1,
                "outcome_sheet_title_actions" to 1,
                "outcome_sheet_transcript" to 1,
                "outcome_sheet_action_bar" to 1,
                "outcome_sheet_confirmation_cancel" to 1,
                "outcome_sheet_confirmation_confirm" to 1,
                "outcome_sheet_confirmation_item" to 1,
                "outcome_sheet_confirmation_item_remove" to 1
            )
        )
    }

    @Test
    fun `confirm batch carries three items and the top level content without a title`() {
        assertTags(
            OutcomeFixtures.needsConfirmation(
                title = null,
                items = OutcomeFixtures.proposedItems(3),
                topLevelContent = { Text("top level") }
            ),
            mapOf(
                "outcome_sheet_transcript" to 1,
                "outcome_sheet_action_bar" to 1,
                "outcome_sheet_confirmation_cancel" to 1,
                "outcome_sheet_confirmation_confirm" to 1,
                "outcome_sheet_confirmation_item" to 3,
                "outcome_sheet_confirmation_top_level_content" to 1
            )
        )
    }

    // ---- Success --------------------------------------------------------------------------------

    @Test
    fun `success single carries the badge, pill and both actions but no bar`() {
        assertTags(
            OutcomeFixtures.success(
                badge = OutcomeBadge.Success,
                titleActions = { Text("title action") },
                handledBy = OutcomeFixtures.handledBy(),
                startAction = OutcomeFixtures.sheetAction(label = "Start"),
                endAction = OutcomeFixtures.sheetAction(label = "End")
            ),
            mapOf(
                "outcome_sheet_title" to 1,
                "outcome_sheet_badge" to 1,
                "outcome_sheet_title_actions" to 1,
                "outcome_sheet_handled_by" to 1,
                "outcome_sheet_action_start" to 1,
                "outcome_sheet_action_end" to 1
            )
        )
    }

    @Test
    fun `success batch carries the batch rows and the undo block`() {
        assertTags(
            OutcomeFixtures.success(
                batchResults = OutcomeFixtures.batchResults(),
                undo = OutcomeFixtures.undo(onUndoAll = {})
            ),
            mapOf(
                "outcome_sheet_title" to 1,
                "outcome_sheet_batch_row" to 2,
                "outcome_sheet_undo_all" to 1,
                "outcome_sheet_undo_row" to 2
            )
        )
    }

    @Test
    fun `success all undone has the rows but no undo all`() {
        assertTags(
            OutcomeFixtures.success(
                undo = OutcomeFixtures.undo(rows = allUndoneRows(), onUndoAll = null)
            ),
            mapOf(
                "outcome_sheet_title" to 1,
                "outcome_sheet_undo_row" to 2
            )
        )
    }

    // ---- Failure --------------------------------------------------------------------------------

    private fun fullFailure(tone: FailureTone) = OutcomeFixtures.failure(
        title = "Couldn't log that",
        badge = OutcomeBadge.Error,
        transcript = "I ate two eggs",
        handledBy = OutcomeFixtures.handledBy(),
        tone = tone,
        tip = { Text("tip") },
        action = FailureActionUiModel(label = "Retry", onClick = {}),
        secondaryAction = OutcomeFixtures.sheetAction(label = "Close")
    )

    private val fullFailureTags = mapOf(
        "outcome_sheet_title" to 1,
        "outcome_sheet_badge" to 1,
        "outcome_sheet_transcript" to 1,
        "outcome_sheet_handled_by" to 1,
        "outcome_sheet_failure_surface" to 1,
        "outcome_sheet_failure_tip" to 1,
        "outcome_sheet_action_bar" to 1,
        "outcome_sheet_action_start" to 1,
        "outcome_sheet_action_end" to 1
    )

    @Test
    fun `failure error carries the card, tip and both bar actions`() {
        assertTags(fullFailure(FailureTone.Error), fullFailureTags)
    }

    @Test
    fun `failure warn carries the same tags as error`() {
        assertTags(fullFailure(FailureTone.Warn), fullFailureTags)
    }

    @Test
    fun `failure without actions has only the card`() {
        assertTags(
            OutcomeFixtures.failure(),
            mapOf("outcome_sheet_failure_surface" to 1)
        )
    }

    // ---- Classic is untouched -------------------------------------------------------------------

    @Test
    fun `Classic has no action bar tag for the same confirm single outcome`() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.height(900.dp)) {
                    OutcomeSheetContent(OutcomeFixtures.needsConfirmation(titleActions = { Text("title action") }))
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_action_bar", useUnmergedTree = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("outcome_sheet_confirmation_confirm", useUnmergedTree = true).assertCountEquals(1)
    }
}
