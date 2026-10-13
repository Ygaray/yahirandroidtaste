package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The optional `handledBy` on [VoiceOutcomeUiState.NeedsConfirmation] (CalTracker 76-14, D-UAT-2):
 * the Receipt confirm body draws the existing handled-by pill directly under the title row when it
 * is set, nothing when it is `null` (the default), and the Classic style ignores it entirely.
 * Hosted through the scaffold-free content composables; one `setContent` per test method.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetReceiptConfirmHandledByTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val localGrammar = HandledByUiModel(
        tier = "Local grammar",
        provider = "offline",
        prefixLabel = "Handled by",
        onDevice = true
    )

    private fun confirmation(handledBy: HandledByUiModel?) = VoiceOutcomeUiState.NeedsConfirmation(
        reason = "You said: “two eggs”",
        items = OutcomeFixtures.proposedItems(1),
        title = "Log this?",
        confirmLabel = "Log it",
        handledBy = handledBy,
        onConfirm = {},
        onCancel = {}
    )

    private fun hostReceipt(outcome: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.height(900.dp)) { OutcomeSheetReceiptContent(outcome) }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun hostClassic(outcome: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.height(900.dp)) { OutcomeSheetContent(outcome) }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun pills() = composeTestRule.onAllNodesWithTag("outcome_sheet_handled_by", useUnmergedTree = true)

    private fun topOf(tag: String): Float =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot.top

    @Test
    fun `Receipt confirm with handledBy draws exactly one pill under the title row`() {
        hostReceipt(confirmation(localGrammar))

        pills().assertCountEquals(1)
        composeTestRule.onNodeWithTag("outcome_sheet_handled_by")
            .assertTextEquals("Handled by Local grammar · offline")

        val titleBottom = composeTestRule.onNodeWithTag("outcome_sheet_title", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot.bottom
        val pillTop = topOf("outcome_sheet_handled_by")
        assertTrue("pill top $pillTop must sit at or below the title bottom $titleBottom", pillTop >= titleBottom)

        // Drawn directly after the title row, so it precedes the transcript (the reason line).
        val transcriptTop = topOf("outcome_sheet_transcript")
        assertTrue("pill top $pillTop must precede the transcript top $transcriptTop", pillTop < transcriptTop)
    }

    @Test
    fun `Receipt confirm without handledBy draws no pill`() {
        hostReceipt(confirmation(handledBy = null))

        pills().assertCountEquals(0)
        composeTestRule.onNodeWithTag("outcome_sheet_title", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `handledBy defaults to null when a consumer omits it`() {
        val omitted = VoiceOutcomeUiState.NeedsConfirmation(
            reason = "r",
            items = emptyList(),
            onConfirm = {},
            onCancel = {}
        )
        assertNull(omitted.handledBy)
    }

    @Test
    fun `Classic confirm ignores handledBy`() {
        hostClassic(confirmation(localGrammar))

        pills().assertCountEquals(0)
        composeTestRule.onNodeWithTag("outcome_sheet_confirmation_confirm", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `toString still omits the handler and every free-text field`() {
        val text = confirmation(localGrammar).toString()
        assertFalse(text, text.contains("Local grammar"))
        assertFalse(text, text.contains("Log this?"))
        assertFalse(text, text.contains("two eggs"))
    }
}
