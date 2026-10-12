package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
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
 * Tracer proof for the Receipt NeedsConfirmation path (VRCPT-01, VRCPT-02): the public
 * [OutcomeSheet] entry with `style = Receipt` renders the title, the reason as the transcript, one
 * row per item and the pinned Cancel / Confirm bar, and the clicks reach the right callbacks.
 * Hosts the public entry (as OutcomeSheetTest does) so the real dispatch and `SheetScaffold(shape)`
 * are exercised.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetReceiptNeedsConfirmationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `receipt confirmation shows title transcript items and the pinned bar`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = OutcomeFixtures.needsConfirmation(),
                onDismissRequest = {},
                style = OutcomeSheetStyle.Receipt
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_title").assertExists()
        composeTestRule.onNodeWithText("Log these?").assertExists()
        composeTestRule.onNodeWithTag("outcome_sheet_transcript").assertExists()
        composeTestRule.onAllNodesWithTag("outcome_sheet_confirmation_item").assertCountEquals(2)
        composeTestRule.onNodeWithTag("outcome_sheet_action_bar").assertExists()
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_cancel").assertExists()
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").assertExists()
    }

    @Test
    fun `receipt confirm fires onConfirm once and not onCancel`() {
        var confirms = 0
        var cancels = 0
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = OutcomeFixtures.needsConfirmation(onConfirm = { confirms++ }, onCancel = { cancels++ }),
                onDismissRequest = {},
                style = OutcomeSheetStyle.Receipt
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, confirms)
        assertEquals(0, cancels)
    }

    @Test
    fun `receipt cancel fires onCancel once and not onConfirm`() {
        var confirms = 0
        var cancels = 0
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = OutcomeFixtures.needsConfirmation(onConfirm = { confirms++ }, onCancel = { cancels++ }),
                onDismissRequest = {},
                style = OutcomeSheetStyle.Receipt
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_cancel").performClick()
        composeTestRule.waitForIdle()

        assertEquals(0, confirms)
        assertEquals(1, cancels)
    }

    @Test
    fun `disabled confirm never fires onConfirm while cancel still fires`() {
        var confirms = 0
        var cancels = 0
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = OutcomeFixtures.needsConfirmation(
                    confirmEnabled = false,
                    onConfirm = { confirms++ },
                    onCancel = { cancels++ }
                ),
                onDismissRequest = {},
                style = OutcomeSheetStyle.Receipt
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").assertIsNotEnabled().performClick()
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_cancel").assertIsEnabled().performClick()
        composeTestRule.waitForIdle()

        assertEquals(0, confirms)
        assertEquals(1, cancels)
    }

    @Test
    fun `classic style has no action bar and no transcript`() {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = OutcomeFixtures.needsConfirmation(),
                onDismissRequest = {},
                style = OutcomeSheetStyle.Classic
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_action_bar").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("outcome_sheet_transcript").assertCountEquals(0)
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm").assertExists()
    }

    @Test
    fun `three argument call renders classic with no action bar and no transcript`() {
        composeTestRule.setContent {
            OutcomeSheet(OutcomeFixtures.needsConfirmation(), {}, Modifier)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_action_bar").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("outcome_sheet_transcript").assertCountEquals(0)
    }
}
