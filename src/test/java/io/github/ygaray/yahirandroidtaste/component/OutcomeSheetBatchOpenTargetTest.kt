package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * FOPEN-02: each batch result row opens its own item. Robolectric tree assertions only; behavior on
 * a real device belongs to the Gate-1 tester.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetBatchOpenTargetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val opened = mutableListOf<FeedbackTarget>()

    private val targetA = FeedbackTarget("a-1", "Open item A")
    private val targetC = FeedbackTarget("c-3", "Open item C")

    private fun mixedRows() = listOf(
        BatchRowResultUiModel(label = "Item A", succeeded = true, target = targetA),
        BatchRowResultUiModel(label = "Item B", succeeded = true),
        BatchRowResultUiModel(label = "Item C", succeeded = true, target = targetC)
    )

    private fun hostReceipt(outcome: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.height(900.dp)) {
                    OutcomeSheetReceiptContent(outcome, onOpenTarget = { opened += it })
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    /** Hosts the Classic style through the PUBLIC sheet with a hoisted state, as a consumer would. */
    private fun hostClassic(outcome: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = {},
                style = OutcomeSheetStyle.Classic,
                sheetState = rememberModalBottomSheetState(),
                onOpenTarget = { opened += it }
            )
        }
        composeTestRule.waitForIdle()
    }

    private fun batchRows(): List<SemanticsNodeInteraction> {
        val nodes = composeTestRule.onAllNodesWithTag(BATCH_ROW_TAG, useUnmergedTree = true)
        return List(nodes.fetchSemanticsNodes().size) { nodes[it] }
    }

    private fun chevronCount(): Int =
        composeTestRule.onAllNodesWithTag(OPEN_TARGET_CHEVRON_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes().size

    // --- tracer ---------------------------------------------------------------------------------------

    @Test
    fun receipt_mixedBatch_eachTargetedRowOpensItsOwnItem_targetlessRowIsInert() {
        hostReceipt(VoiceOutcomeUiState.Success(summary = "Saved", batchResults = mixedRows()))

        val rows = batchRows()
        assertEquals(3, rows.size)

        rows[0].performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(targetA), opened)

        rows[2].performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(targetA, targetC), opened)

        rows[1].performClick()
        composeTestRule.waitForIdle()
        assertEquals("a target-less row must not call the handler", listOf(targetA, targetC), opened)

        assertFalse(
            "a target-less row exposes no click action",
            rows[1].fetchSemanticsNode().config.contains(SemanticsActions.OnClick)
        )
        assertEquals("only the two targeted rows draw a chevron", 2, chevronCount())
        assertEquals(
            "Open item A",
            rows[0].fetchSemanticsNode().config[SemanticsActions.OnClick].label
        )
        assertTrue(rows[0].fetchSemanticsNode().config.contains(SemanticsActions.OnClick))
    }

    // --- Classic ----------------------------------------------------------------------------------------

    @Test
    fun classic_mixedBatch_eachTargetedRowOpensItsOwnItem_targetlessRowIsInert() {
        // No single Success.target: only the batch rows make the outcome openable.
        hostClassic(VoiceOutcomeUiState.Success(summary = "Saved", batchResults = mixedRows()))

        val rows = batchRows()
        assertEquals(3, rows.size)

        rows[0].performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(targetA), opened)

        rows[2].performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(targetA, targetC), opened)

        rows[1].performClick()
        composeTestRule.waitForIdle()
        assertEquals("a target-less row must not call the handler", listOf(targetA, targetC), opened)

        assertFalse(rows[1].fetchSemanticsNode().config.contains(SemanticsActions.OnClick))
        assertEquals("only the two targeted rows draw a chevron", 2, chevronCount())
        assertEquals("Open item A", rows[0].fetchSemanticsNode().config[SemanticsActions.OnClick].label)
    }

    @Test
    fun classic_batchOnlyOutcome_reachesTheOpenPath() {
        hostClassic(VoiceOutcomeUiState.Success(summary = "Saved", batchResults = mixedRows()))
        assertEquals(
            "the open list draws a chevron, so the open body is active",
            2,
            chevronCount()
        )
        assertEquals("no single go-to-item row without a Success.target", 0, singleRowCount())
    }

    private fun singleRowCount(): Int =
        composeTestRule.onAllNodesWithTag(OPEN_TARGET_ROW_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes().size

    private companion object {
        const val BATCH_ROW_TAG = "outcome_sheet_batch_row"
    }
}
