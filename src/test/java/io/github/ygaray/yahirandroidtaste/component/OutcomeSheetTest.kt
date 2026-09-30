package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
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
}
