package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.FailureActionUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Receipt Failure (VRCPT-04, D-09, D-11): the section order, the tone card with the reason and the
 * body slot, the tip and the shared undo block under the card, the pinned bar with the secondary
 * action at the start and the role-hued action at the end, both tones, the semanticsPrefix merge and
 * the live region. Hosted through `OutcomeSheetReceiptContent`; one `setContent` per test method;
 * geometry is read from the unmerged tree; no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetReceiptFailureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun host(
        outcome: VoiceOutcomeUiState,
        colors: OutcomeSheetColors? = null,
        hostHeightDp: Int = 900
    ) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.height(hostHeightDp.dp)) {
                    if (colors == null) {
                        OutcomeSheetReceiptContent(outcome)
                    } else {
                        OutcomeSheetReceiptContent(outcome, colors = colors)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun tagged(tag: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun count(tag: String) = composeTestRule.onAllNodesWithTag(tag, useUnmergedTree = true)

    private fun topOf(tag: String): Float = tagged(tag).fetchSemanticsNode().boundsInRoot.top

    private val bodyProbe: @Composable () -> Unit = { Text("body probe") }
    private val tipProbe: @Composable () -> Unit = { Text("tip probe") }

    private var actionClicks = 0
    private var secondaryClicks = 0

    private fun fullFailure(tone: FailureTone = FailureTone.Error) = OutcomeFixtures.failure(
        title = "Couldn't log that",
        badge = OutcomeBadge.Error,
        transcript = "I ate two eggs",
        handledBy = OutcomeFixtures.handledBy(),
        tone = tone,
        body = bodyProbe,
        tip = tipProbe,
        undo = OutcomeFixtures.undo(),
        action = FailureActionUiModel(label = "Retry", onClick = { actionClicks++ }, icon = Icons.Filled.Check),
        secondaryAction = OutcomeFixtures.sheetAction(label = "Close", onClick = { secondaryClicks++ })
    )

    // ---- Order and containment (D-09, D-11) -----------------------------------------------------

    @Test
    fun `Failure sections render in the D-09 order`() {
        host(fullFailure())

        val order = listOf(
            "outcome_sheet_title",
            "outcome_sheet_transcript",
            "outcome_sheet_handled_by",
            "outcome_sheet_failure_surface",
            "outcome_sheet_failure_tip",
            "outcome_sheet_undo_all",
            "outcome_sheet_action_bar"
        )
        val tops = order.map { topOf(it) }
        order.indices.drop(1).forEach { i ->
            assertTrue("${order[i - 1]} ${tops[i - 1]} must be above ${order[i]} ${tops[i]}", tops[i - 1] < tops[i])
        }
    }

    @Test
    fun `the card holds the reason and the body and nothing else`() {
        host(fullFailure())

        val inCard = hasAnyAncestor(hasTestTag("outcome_sheet_failure_surface"))
        composeTestRule.onNode(hasText("Network down") and inCard, useUnmergedTree = true).assertExists()
        composeTestRule.onNode(hasText("body probe") and inCard, useUnmergedTree = true).assertExists()
        composeTestRule.onNode(hasText("tip probe") and inCard, useUnmergedTree = true).assertDoesNotExist()
        tagged("outcome_sheet_title").assert(!inCard)
        tagged("outcome_sheet_failure_tip").assert(!inCard)
        tagged("outcome_sheet_undo_all").assert(!inCard)
        tagged("outcome_sheet_action_end").assert(!inCard)
        tagged("outcome_sheet_action_start").assert(!inCard)
    }

    @Test
    fun `the actions sit in the bar with the secondary left of the action`() {
        host(fullFailure())

        val inBar = hasAnyAncestor(hasTestTag("outcome_sheet_action_bar"))
        tagged("outcome_sheet_action_end").assert(inBar)
        tagged("outcome_sheet_action_start").assert(inBar)
        val startLeft = tagged("outcome_sheet_action_start").fetchSemanticsNode().boundsInRoot.left
        val endLeft = tagged("outcome_sheet_action_end").fetchSemanticsNode().boundsInRoot.left
        assertTrue("secondary left $startLeft must be left of the action left $endLeft", startLeft < endLeft)
    }

    @Test
    fun `the caller tag wins over tone_card`() {
        host(fullFailure())

        tagged("outcome_sheet_failure_surface").assertExists()
        tagged("tone_card").assertDoesNotExist()
    }

    @Test
    fun `the reason is bodyMedium semibold onSurface`() {
        var expectedSize = androidx.compose.ui.unit.TextUnit.Unspecified
        var expectedColor = Color.Unspecified
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                expectedSize = MaterialTheme.typography.bodyMedium.fontSize
                expectedColor = MaterialTheme.colorScheme.onSurface
                Box(Modifier.height(900.dp)) { OutcomeSheetReceiptContent(OutcomeFixtures.failure()) }
            }
        }
        composeTestRule.waitForIdle()

        val results = mutableListOf<TextLayoutResult>()
        val node = composeTestRule.onNode(hasText("Network down"), useUnmergedTree = true).fetchSemanticsNode()
        checkNotNull(node.config[SemanticsActions.GetTextLayoutResult].action).invoke(results)
        val style = results.single().layoutInput.style
        assertEquals("reason font size", expectedSize, style.fontSize)
        assertEquals("reason font weight", FontWeight.SemiBold, style.fontWeight)
        assertEquals("reason color", expectedColor, style.color)
    }

    // ---- Clicks ---------------------------------------------------------------------------------

    @Test
    fun `the action and the secondary each fire once and a disabled secondary is not enabled`() {
        host(fullFailure())

        tagged("outcome_sheet_action_end").assertIsEnabled().performClick()
        tagged("outcome_sheet_action_start").assertIsEnabled().performClick()

        assertEquals(1, actionClicks)
        assertEquals(1, secondaryClicks)
    }

    @Test
    fun `a secondary built with enabled false is not enabled`() {
        host(
            OutcomeFixtures.failure(
                secondaryAction = OutcomeFixtures.sheetAction(label = "Close", enabled = false, onClick = { secondaryClicks++ })
            )
        )

        tagged("outcome_sheet_action_start").assertIsNotEnabled().performClick()

        assertEquals(0, secondaryClicks)
    }

    // ---- Tone (D-09) ----------------------------------------------------------------------------

    private val warnTone = ToneColors(container = Color(0xFF112233), content = Color(0xFFAABBCC))
    private val errorTone = ToneColors(container = Color(0xFF332211), content = Color(0xFFCCBBAA))
    private val customColors = OutcomeSheetColors(warnTone = warnTone, errorTone = errorTone)

    @Test
    fun `failureToneColors maps Warn to warnTone and Error to errorTone`() {
        assertEquals(warnTone, failureToneColors(FailureTone.Warn, customColors))
        assertEquals(errorTone, failureToneColors(FailureTone.Error, customColors))
    }

    private fun capturedContentColor(tone: FailureTone): Color {
        var captured = Color.Unspecified
        host(
            OutcomeFixtures.failure(tone = tone, body = { captured = LocalContentColor.current }),
            colors = customColors
        )
        return captured
    }

    @Test
    fun `a Warn failure paints its body with the warn tone content color`() {
        assertEquals(warnTone.content, capturedContentColor(FailureTone.Warn))
    }

    @Test
    fun `an Error failure paints its body with the error tone content color`() {
        assertEquals(errorTone.content, capturedContentColor(FailureTone.Error))
    }

    // ---- Bar presence (OQ-3) --------------------------------------------------------------------

    @Test
    fun `a Failure with no actions renders no bar and no divider`() {
        host(OutcomeFixtures.failure())

        count("outcome_sheet_action_bar").assertCountEquals(0)
        count("outcome_sheet_action_bar_divider").assertCountEquals(0)
        count("outcome_sheet_action_start").assertCountEquals(0)
        count("outcome_sheet_action_end").assertCountEquals(0)
        tagged("outcome_sheet_failure_surface").assertExists()
    }

    @Test
    fun `only a secondary action renders only the start node`() {
        host(OutcomeFixtures.failure(secondaryAction = OutcomeFixtures.sheetAction(label = "Close")))

        count("outcome_sheet_action_bar").assertCountEquals(1)
        count("outcome_sheet_action_start").assertCountEquals(1)
        count("outcome_sheet_action_end").assertCountEquals(0)
    }

    @Test
    fun `only an action renders only the end node`() {
        host(OutcomeFixtures.failure(action = FailureActionUiModel(label = "Retry", onClick = {})))

        count("outcome_sheet_action_bar").assertCountEquals(1)
        count("outcome_sheet_action_start").assertCountEquals(0)
        count("outcome_sheet_action_end").assertCountEquals(1)
    }

    @Test
    fun `an over-long body never starves the bar buttons in a 240dp host`() {
        host(
            OutcomeFixtures.failure(
                body = { Box(Modifier.height(600.dp)) { Text("a very tall body") } },
                action = FailureActionUiModel(label = "Retry", onClick = {}),
                secondaryAction = OutcomeFixtures.sheetAction(label = "Close")
            ),
            hostHeightDp = 240
        )

        composeTestRule.onNodeWithTag("outcome_sheet_action_end").assertIsDisplayed().assertHeightIsAtLeast(40.dp)
        composeTestRule.onNodeWithTag("outcome_sheet_action_start").assertIsDisplayed().assertHeightIsAtLeast(40.dp)
    }

    // ---- semanticsPrefix (VFAIL-03) -------------------------------------------------------------

    @Test
    fun `a non-blank prefix gives the card a combined description and the action stays its own node`() {
        host(
            OutcomeFixtures.failure(
                semanticsPrefix = "Error:",
                action = FailureActionUiModel(label = "Retry", onClick = { actionClicks++ })
            )
        )

        composeTestRule.onNodeWithTag("outcome_sheet_failure_surface").assertContentDescriptionEquals("Error: Network down")
        composeTestRule.onNode(hasText("Retry") and hasClickAction()).assertIsEnabled().performClick()
        assertEquals(1, actionClicks)
    }

    @Test
    fun `a blank prefix adds no combined description`() {
        host(OutcomeFixtures.failure(semanticsPrefix = "   "))

        val config = composeTestRule.onNodeWithTag("outcome_sheet_failure_surface").fetchSemanticsNode().config
        assertTrue("no content description expected", SemanticsProperties.ContentDescription !in config)
    }

    // ---- Undo and the live region (D-11, VXR-01, VXR-02) ----------------------------------------

    private fun liveRegionNodes() = composeTestRule.onAllNodes(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion),
        useUnmergedTree = true
    )

    @Test
    fun `undo all fires once and the rows render in list order`() {
        var undoAll = 0
        host(OutcomeFixtures.failure(undo = OutcomeFixtures.undo(onUndoAll = { undoAll++ })))

        tagged("outcome_sheet_undo_all").performClick()
        assertEquals(1, undoAll)
        val rows = composeTestRule.onAllNodesWithTag("outcome_sheet_undo_row", useUnmergedTree = true)
        rows.assertCountEquals(2)
        assertTrue(rows[0].fetchSemanticsNode().boundsInRoot.top < rows[1].fetchSemanticsNode().boundsInRoot.top)
    }

    @Test
    fun `the refused surface is the single assertive live region in Receipt Failure`() {
        host(OutcomeFixtures.failure(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        tagged("outcome_sheet_undo_refused")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive))
        liveRegionNodes().assertCountEquals(1)
    }

    @Test
    fun `no refusal means no live region in Receipt Failure`() {
        host(OutcomeFixtures.failure(undo = OutcomeFixtures.undo(refused = null)))

        liveRegionNodes().assertCountEquals(0)
    }
}
