package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
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
 * Receipt Success (VRCPT-03, VRCPT-05, D-08, D-10): the section order, the in-flight action rule,
 * the one-string handled-by pill and its dot. Hosted through `OutcomeSheetReceiptContent` in a
 * height-bounded box so no `ModalBottomSheet` is driven; one `setContent` per test method; geometry
 * is read from `boundsInRoot` against the unmerged tree; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetReceiptSuccessTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun host(outcome: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.height(900.dp)) { OutcomeSheetReceiptContent(outcome) }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun tagged(tag: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun topOf(tag: String): Float = tagged(tag).fetchSemanticsNode().boundsInRoot.top

    // ---- Order (D-08, ROADMAP SC3) -------------------------------------------------------------

    @Test
    fun `Success sections render in the D-08 order`() {
        host(
            OutcomeFixtures.success(
                handledBy = OutcomeFixtures.handledBy(tier = "Local grammar", provider = "offline", onDevice = true),
                editableContent = OutcomeFixtures.editableProbe,
                batchResults = OutcomeFixtures.batchResults(),
                undo = OutcomeFixtures.undo(),
                startAction = OutcomeFixtures.sheetAction(label = "Start"),
                endAction = OutcomeFixtures.sheetAction(label = "End")
            )
        )

        val title = topOf("outcome_sheet_title")
        val pill = topOf("outcome_sheet_handled_by")
        val probe = composeTestRule.onNode(hasText("editable probe"), useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot.top
        val batch = composeTestRule.onAllNodesWithTag("outcome_sheet_batch_row", useUnmergedTree = true)[0]
            .fetchSemanticsNode().boundsInRoot.top
        val undoAll = topOf("outcome_sheet_undo_all")
        val action = topOf("outcome_sheet_action_start")

        assertTrue("title $title < pill $pill", title < pill)
        assertTrue("pill $pill < editable $probe", pill < probe)
        assertTrue("editable $probe < batch $batch", probe < batch)
        assertTrue("batch $batch < undo all $undoAll", batch < undoAll)
        assertTrue("undo all $undoAll < actions $action", undoAll < action)
    }

    @Test
    fun `Success without actions renders no action nodes`() {
        host(OutcomeFixtures.success())

        composeTestRule.onAllNodesWithTag("outcome_sheet_action_start", useUnmergedTree = true).assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("outcome_sheet_action_end", useUnmergedTree = true).assertCountEquals(0)
    }

    // ---- In-flight rule (T-22-15) ---------------------------------------------------------------

    private var startClicks = 0
    private var endClicks = 0

    private fun hostActions(
        inFlight: Boolean,
        startEnabled: Boolean = true,
        startActive: Boolean = true
    ) {
        host(
            OutcomeFixtures.success(
                inFlight = inFlight,
                startAction = OutcomeFixtures.sheetAction(
                    label = "Cancel",
                    emphasis = ActionButtonEmphasis.Text,
                    activeWhileInFlight = startActive,
                    enabled = startEnabled,
                    onClick = { startClicks++ }
                ),
                endAction = OutcomeFixtures.sheetAction(label = "Done", onClick = { endClicks++ })
            )
        )
    }

    @Test
    fun `in flight the exempt start action fires and the normal end action never does`() {
        hostActions(inFlight = true)

        tagged("outcome_sheet_action_start").assertIsEnabled().performClick()
        tagged("outcome_sheet_action_end").assertIsNotEnabled().performClick()

        assertEquals("exempt start fired once", 1, startClicks)
        assertEquals("normal end never fires in flight", 0, endClicks)
    }

    @Test
    fun `not in flight both actions are enabled and fire once`() {
        hostActions(inFlight = false)

        tagged("outcome_sheet_action_start").assertIsEnabled().performClick()
        tagged("outcome_sheet_action_end").assertIsEnabled().performClick()

        assertEquals(1, startClicks)
        assertEquals(1, endClicks)
    }

    @Test
    fun `an exempt action the caller disabled stays disabled in flight`() {
        hostActions(inFlight = true, startEnabled = false)

        tagged("outcome_sheet_action_start").assertIsNotEnabled().performClick()

        assertEquals("caller enabled = false beats activeWhileInFlight", 0, startClicks)
    }

    @Test
    fun `actionEnabled follows enabled and not inFlight or activeWhileInFlight`() {
        val exempt = OutcomeFixtures.sheetAction(activeWhileInFlight = true)
        val normal = OutcomeFixtures.sheetAction(activeWhileInFlight = false)
        val disabledExempt = OutcomeFixtures.sheetAction(activeWhileInFlight = true, enabled = false)

        assertTrue(actionEnabled(exempt, inFlight = true))
        assertFalse(actionEnabled(normal, inFlight = true))
        assertTrue(actionEnabled(normal, inFlight = false))
        assertFalse(actionEnabled(disabledExempt, inFlight = true))
        assertFalse(actionEnabled(disabledExempt, inFlight = false))
    }

    @Test
    fun `in flight the shared undo all stays locked`() {
        host(OutcomeFixtures.success(inFlight = true, undo = OutcomeFixtures.undo()))

        tagged("outcome_sheet_undo_all").assertIsNotEnabled()
    }

    // ---- Handled-by pill (D-10, VRCPT-05) -------------------------------------------------------

    @Test
    fun `the pill merges into one string and ends with the provider`() {
        host(
            OutcomeFixtures.success(
                handledBy = OutcomeFixtures.handledBy(tier = "Local grammar", provider = "offline", onDevice = true)
            )
        )

        composeTestRule.onNodeWithTag("outcome_sheet_handled_by")
            .assertTextEquals("Handled by Local grammar · offline")
        tagged("outcome_sheet_handled_by_dot").assertExists()
    }

    @Test
    fun `the dot is the warn content color on device and the primary role otherwise`() {
        val scheme = lightColorScheme()
        val colors = OutcomeSheetColors(
            warnTone = ToneColors(Color(0xFFFFE0B2), Color(0xFFE65100)),
            errorTone = ToneColors(Color.Red, Color.White)
        )

        assertEquals(colors.warnTone.content, handledByDotColor(onDevice = true, colors = colors, scheme = scheme))
        assertEquals(scheme.primary, handledByDotColor(onDevice = false, colors = colors, scheme = scheme))
    }
}
