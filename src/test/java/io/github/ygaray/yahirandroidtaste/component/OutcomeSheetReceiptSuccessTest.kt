package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
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

    private fun pillText(handledBy: HandledByUiModel, expected: String) {
        host(OutcomeFixtures.success(handledBy = handledBy))

        composeTestRule.onNodeWithTag("outcome_sheet_handled_by").assertTextEquals(expected)
    }

    @Test
    fun `the pill appends approach provider and the escalation caption`() = pillText(
        OutcomeFixtures.handledBy(tier = "Cloud", provider = "Acme", escalationCount = 2),
        "Handled by Cloud · Acme · Escalations: 2"
    )

    @Test
    fun `a tier only pill has no trailing separator`() =
        pillText(OutcomeFixtures.handledBy(tier = "Cloud"), "Handled by Cloud")

    @Test
    fun `the pill honors a localized prefix label`() = pillText(
        OutcomeFixtures.handledBy(tier = "Lokal", provider = "offline", prefixLabel = "Bearbeitet von"),
        "Bearbeitet von Lokal · offline"
    )

    // ---- Tags and absent nodes (Y-14) -----------------------------------------------------------

    @Test
    fun `a full Success carries the Success tags and none of the bar or transcript tags`() {
        host(
            OutcomeFixtures.success(
                badge = OutcomeBadge.Success,
                titleActions = { Text("title action") },
                handledBy = OutcomeFixtures.handledBy(),
                startAction = OutcomeFixtures.sheetAction(label = "Start"),
                endAction = OutcomeFixtures.sheetAction(label = "End")
            )
        )

        tagged("outcome_sheet_title").assertTextEquals("Logged 2 of 3")
        tagged("outcome_sheet_badge").assertWidthIsEqualTo(32.dp).assertHeightIsEqualTo(32.dp)
        tagged("outcome_sheet_title_actions").assertExists()
        tagged("outcome_sheet_handled_by").assertExists()
        tagged("outcome_sheet_action_start").assertExists()
        tagged("outcome_sheet_action_end").assertExists()
        tagged("outcome_sheet_action_bar").assertDoesNotExist()
        tagged("outcome_sheet_action_bar_divider").assertDoesNotExist()
        tagged("outcome_sheet_transcript").assertDoesNotExist()
    }

    @Test
    fun `without a badge there is no badge node`() {
        host(OutcomeFixtures.success())

        tagged("outcome_sheet_badge").assertDoesNotExist()
    }

    @Test
    fun `the Reverted badge renders and uses the auto mirrored undo glyph`() {
        host(OutcomeFixtures.success(badge = OutcomeBadge.Reverted))

        tagged("outcome_sheet_badge").assertExists()
        assertSame(Icons.AutoMirrored.Filled.Undo, outcomeBadgeIcon(OutcomeBadge.Reverted))
    }

    // ---- Spacing (D-08) -------------------------------------------------------------------------

    private fun assertActionsGapAtLeast24dp(lastBlockBottom: Float) {
        val gap = topOf("outcome_sheet_action_start") - lastBlockBottom
        val minimum = 24f * composeTestRule.density.density
        assertTrue("gap $gap px must be at least $minimum px", gap >= minimum - 1f)
    }

    @Test
    fun `the actions row sits at least 24dp below the undo block`() {
        host(
            OutcomeFixtures.success(
                undo = OutcomeFixtures.undo(),
                startAction = OutcomeFixtures.sheetAction(label = "Start")
            )
        )

        val lastRow = composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row", useUnmergedTree = true)[1]
            .fetchSemanticsNode().boundsInRoot.bottom
        assertActionsGapAtLeast24dp(lastRow)
    }

    @Test
    fun `the actions row sits at least 24dp below the editable content`() {
        host(
            OutcomeFixtures.success(
                editableContent = OutcomeFixtures.editableProbe,
                startAction = OutcomeFixtures.sheetAction(label = "Start")
            )
        )

        val probe = composeTestRule.onNode(hasText("editable probe"), useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot.bottom
        assertActionsGapAtLeast24dp(probe)
    }

    // ---- OQ-1: editableContent while in flight --------------------------------------------------

    @Test
    fun `editable content is hidden while in flight`() {
        host(OutcomeFixtures.success(inFlight = true, editableContent = OutcomeFixtures.editableProbe))

        composeTestRule.onNode(hasText("editable probe"), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `editable content shows when not in flight`() {
        host(OutcomeFixtures.success(inFlight = false, editableContent = OutcomeFixtures.editableProbe))

        composeTestRule.onNode(hasText("editable probe"), useUnmergedTree = true).assertExists()
    }

    // ---- Shared batch and undo blocks (VXR-01, VXR-02) ------------------------------------------

    @Test
    fun `the shared batch and undo blocks render in Receipt`() {
        host(OutcomeFixtures.success(batchResults = OutcomeFixtures.batchResults(), undo = OutcomeFixtures.undo()))

        composeTestRule.onAllNodesWithTag("outcome_sheet_batch_row", useUnmergedTree = true).assertCountEquals(2)
        composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row", useUnmergedTree = true).assertCountEquals(2)
        tagged("outcome_sheet_undo_all").assertExists()
    }

    private fun liveRegionNodes() = composeTestRule.onAllNodes(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion),
        useUnmergedTree = true
    )

    @Test
    fun `the refused surface is the single assertive live region in Receipt Success`() {
        host(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        tagged("outcome_sheet_undo_refused")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive))
        liveRegionNodes().assertCountEquals(1)
    }

    @Test
    fun `no refusal means no live region in Receipt Success`() {
        host(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = null)))

        liveRegionNodes().assertCountEquals(0)
    }
}
