package io.github.ygaray.yahirandroidtaste

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheet
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheetContent
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheetDefaults
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheetReceiptContent
import io.github.ygaray.yahirandroidtaste.component.OutcomeSheetStyle
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Call-site canary for the post-v2.6.0 voice-sheet additions (CalTracker 76-14): the optional
 * `NeedsConfirmation.handledBy` field and the hoisted-state `OutcomeSheet(..., sheetState)` overload.
 * VERBATIM v2.6.0-shaped call sites (named arguments, as both live consumers write them) must still
 * compile at HEAD, keep their v2.6.0 defaults and render with no handled-by pill in either style.
 *
 * Provenance: `SB <file>:<line>` is SecondBrain (`app/src/main/java/com/example/secondbrain/...`,
 * pinned v2.5.1), `CT <file>:<line>` is CalTracker (`app/src/main/java/com/caltracker/app/ui/...`,
 * pinned v2.6.0). Consumer-only identifiers are replaced by neutral holders; the argument shape is
 * kept as written. Composable `OutcomeSheet` calls live in non-invoked lambdas: they only have to
 * COMPILE. The model shapes are rendered through the scaffold-free content composables.
 *
 * Positional `NeedsConfirmation` construction, positional `copy` and destructuring past
 * `titleActions` are the accepted, documented break (API.md section 11, INTEGRATION.md 5b): neither
 * consumer writes them.
 *
 * Deliberately NO class-level `@OptIn(ExperimentalMaterial3Api::class)`: SecondBrain's
 * `VoiceSheetHost.kt` calls `OutcomeSheet` without that opt-in, so the shapes that do not pass
 * `sheetState` must compile without it (an experimental type in the called signature would demand
 * it). Only the hoisted-state shape opts in, exactly as a consumer that hoists the state must.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class V260CallSiteCanaryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** CT voice/VoiceOutcomeMapper.kt:661: the v2.6.0 single-confirm mapper, every argument named. */
    private fun ctV260Shape() = VoiceOutcomeUiState.NeedsConfirmation(
        reason = "You said: “two eggs”",
        items = emptyList(),
        title = "Log this?",
        severity = ActionButtonDefaults.ActionButtonRole.Save,
        confirmLabel = "Log it",
        cancelLabel = "Cancel",
        topLevelContent = { Column { Text("receipt row") } },
        confirmEnabled = true,
        titleActions = { Text("mic") },
        onConfirm = {},
        onCancel = {},
    )

    /** SB feature/voicecommand/VoiceConfirmSheetModels.kt:29: the v2.5.1 named set, in its order. */
    private fun sbShape() = VoiceOutcomeUiState.NeedsConfirmation(
        reason = "reason",
        items = listOf(ProposedItemUiModel(id = "1", title = "Card")),
        title = "title",
        severity = ActionButtonDefaults.ActionButtonRole.Destructive,
        reversibilityHint = "hint",
        confirmLabel = "Delete",
        cancelLabel = "Cancel",
        onConfirm = {},
        onCancel = {},
    )

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun outcomeSheet_v260ConsumerShapes_compile() {
        val outcome: VoiceOutcomeUiState = ctV260Shape()
        val onDismiss: () -> Unit = {}

        // CT voice/VoiceResultSheet.kt:177: named outcome, onDismissRequest, modifier, style, colors.
        val ctNamed: @Composable () -> Unit = {
            val colors = OutcomeSheetDefaults.colors()
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = onDismiss,
                modifier = Modifier,
                style = OutcomeSheetStyle.Receipt,
                colors = colors,
            )
        }
        // SB feature/voicecommand/VoiceSheetHost.kt:196: named outcome and onDismissRequest only.
        val sbNamed: @Composable () -> Unit = {
            OutcomeSheet(outcome = outcome, onDismissRequest = { onDismiss() })
        }
        // v2.6.0 positional prefix (outcome, onDismissRequest, modifier, style) still binds.
        val positional: @Composable () -> Unit = {
            OutcomeSheet(outcome, onDismiss, Modifier, OutcomeSheetStyle.Classic)
        }
    }

    /** CT 76-20 shape: the v2.6.0 named call plus a hoisted `sheetState`, under the consumer's opt-in. */
    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("UNUSED_VARIABLE")
    @Test
    fun outcomeSheet_hoistedStateShape_compiles() {
        val outcome: VoiceOutcomeUiState = ctV260Shape()
        val ctHoisted: @Composable () -> Unit = {
            val outcomeSheetState = rememberModalBottomSheetState()
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = {},
                modifier = Modifier,
                style = OutcomeSheetStyle.Receipt,
                colors = OutcomeSheetDefaults.colors(),
                sheetState = outcomeSheetState,
            )
        }
    }

    @Test
    fun needsConfirmation_v260Shapes_keepTheirDefaults() {
        val ct = ctV260Shape()
        val sb = sbShape()
        assertNull(ct.handledBy)
        assertNull(sb.handledBy)
        assertTrue(sb.confirmEnabled)
        assertNull(sb.titleActions)
        assertEquals("Log it", ct.confirmLabel)
        assertEquals("Delete", sb.confirmLabel)
    }

    @Test
    fun needsConfirmation_v260Shapes_renderWithoutAPill_inBothStyles() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column {
                    Box(Modifier.height(400.dp)) { OutcomeSheetReceiptContent(ctV260Shape()) }
                    Box(Modifier.height(400.dp)) { OutcomeSheetReceiptContent(sbShape()) }
                    Box(Modifier.height(400.dp)) { OutcomeSheetContent(ctV260Shape()) }
                    Box(Modifier.height(400.dp)) { OutcomeSheetContent(sbShape()) }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("outcome_sheet_handled_by", useUnmergedTree = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("outcome_sheet_confirmation_confirm", useUnmergedTree = true)
            .assertCountEquals(4)
    }
}
