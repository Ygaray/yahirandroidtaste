package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * FOPEN-01: the single-target go-to-item row of a success sheet, in the Receipt and Classic styles,
 * through both public [OutcomeSheet] overloads. Robolectric tree assertions only; behavior on a real
 * device belongs to the Gate-1 tester.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OutcomeSheetOpenTargetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var state: SheetState
    private var dismissals = 0
    private val opened = mutableListOf<FeedbackTarget>()

    private val target = FeedbackTarget("item-42", "Open item")

    private fun targetSuccess() = VoiceOutcomeUiState.Success(
        summary = "Saved",
        target = target,
        targetLabel = "Weekend list",
        targetDetail = "Updated just now"
    )

    private fun noTargetSuccess() = VoiceOutcomeUiState.Success(
        summary = "Saved",
        targetLabel = "Weekend list",
        targetDetail = "Updated just now"
    )

    private fun hostHoisted(outcome: VoiceOutcomeUiState, style: OutcomeSheetStyle) {
        composeTestRule.setContent {
            state = rememberModalBottomSheetState()
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = { dismissals++ },
                style = style,
                sheetState = state,
                onOpenTarget = { opened += it }
            )
        }
        composeTestRule.waitForIdle()
    }

    private fun hostDefaultState(outcome: VoiceOutcomeUiState, style: OutcomeSheetStyle) {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = { dismissals++ },
                style = style,
                onOpenTarget = { opened += it }
            )
        }
        composeTestRule.waitForIdle()
    }

    private fun row(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(OPEN_TARGET_ROW_TEST_TAG, useUnmergedTree = true)

    private fun topOf(interaction: SemanticsNodeInteraction): Float =
        interaction.fetchSemanticsNode().positionInRoot.y

    private fun assertTapOpensOnce() {
        row().assertExists().performClick()
        composeTestRule.waitForIdle()
        assertEquals(listOf(target), opened)
        assertEquals("the library must never call the dismiss callback", 0, dismissals)
    }

    private fun assertButtonWithOpenLabel() {
        val config = row().assertExists().fetchSemanticsNode().config
        assertEquals("Open item", config[SemanticsActions.OnClick].label)
        assertEquals(Role.Button, config[SemanticsProperties.Role])
    }

    // --- tracer and the two overloads, both styles -------------------------------------------------

    @Test
    fun receipt_targetRow_tapOpensOnce_viaHoistedOverload() {
        hostHoisted(targetSuccess(), OutcomeSheetStyle.Receipt)
        assertButtonWithOpenLabel()
        assertTapOpensOnce()
        assertTrue("the library must never hide the sheet", state.isVisible)
    }

    @Test
    fun classic_targetRow_tapOpensOnce_viaHoistedOverload() {
        hostHoisted(targetSuccess(), OutcomeSheetStyle.Classic)
        assertButtonWithOpenLabel()
        assertTapOpensOnce()
        assertTrue("the library must never hide the sheet", state.isVisible)
    }

    @Test
    fun classic_targetRow_tapOpensOnce_viaDefaultStateOverload() {
        hostDefaultState(targetSuccess(), OutcomeSheetStyle.Classic)
        assertButtonWithOpenLabel()
        assertTapOpensOnce()
    }

    @Test
    fun receipt_targetRow_tapOpensOnce_viaDefaultStateOverload() {
        hostDefaultState(targetSuccess(), OutcomeSheetStyle.Receipt)
        assertButtonWithOpenLabel()
        assertTapOpensOnce()
    }

    // --- PD-2: toggling the target never re-creates the sheet window --------------------------------

    @Test
    fun swappingTargetOnOneCallSite_keepsTheSheetVisible() {
        var outcome: VoiceOutcomeUiState by mutableStateOf(targetSuccess())
        val visibility = mutableListOf<Boolean>()
        composeTestRule.setContent {
            state = rememberModalBottomSheetState()
            LaunchedEffect(state) { snapshotFlow { state.isVisible }.collect { visibility += it } }
            OutcomeSheet(
                outcome = outcome,
                onDismissRequest = { dismissals++ },
                style = OutcomeSheetStyle.Classic,
                sheetState = state,
                onOpenTarget = { opened += it }
            )
        }
        composeTestRule.waitForIdle()
        row().assertExists()

        outcome = VoiceOutcomeUiState.Success(summary = "Saved")
        composeTestRule.waitForIdle()
        row().assertDoesNotExist()
        composeTestRule.onNodeWithText("Saved").assertExists()

        outcome = targetSuccess()
        composeTestRule.waitForIdle()
        row().assertExists()

        assertTrue("the same hoisted state stays visible across the swaps", state.isVisible)
        val firstShown = visibility.indexOf(true)
        assertTrue("the sheet must have been shown ($visibility)", firstShown >= 0)
        assertFalse(
            "no hide/show cycle after the first show ($visibility)",
            visibility.drop(firstShown).contains(false)
        )
        assertEquals(0, dismissals)
    }

    // --- D-05: the library only calls the callback ---------------------------------------------------

    private fun assertLibraryNeverHides(style: OutcomeSheetStyle) {
        hostHoisted(targetSuccess(), style)
        row().performClick()
        composeTestRule.waitForIdle()
        row().performClick()
        composeTestRule.waitForIdle()
        assertTrue("the library must never hide the sheet", state.isVisible)
        assertEquals("the library must never call the dismiss callback", 0, dismissals)
        assertEquals("each tap reaches the handler, no debounce", 2, opened.size)
    }

    @Test
    fun libraryNeverHidesTheSheet_orCallsDismiss_afterATap_classic() =
        assertLibraryNeverHides(OutcomeSheetStyle.Classic)

    @Test
    fun libraryNeverHidesTheSheet_orCallsDismiss_afterATap_receipt() =
        assertLibraryNeverHides(OutcomeSheetStyle.Receipt)

    // --- parity: no target or no handler is the v2.7.1 tree (same code path) -----------------------

    /**
     * Renders [first] then [second] under ONE call site and returns both normalized trees, so equal
     * trees prove the same code path (a second `setContent` is not allowed in one test).
     */
    private fun treesOf(
        style: OutcomeSheetStyle,
        first: Pair<VoiceOutcomeUiState, ((FeedbackTarget) -> Unit)?>,
        second: Pair<VoiceOutcomeUiState, ((FeedbackTarget) -> Unit)?>
    ): Pair<String, String> {
        var current by mutableStateOf(first)
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = current.first,
                onDismissRequest = { dismissals++ },
                style = style,
                onOpenTarget = current.second
            )
        }
        composeTestRule.waitForIdle()
        val a = SemanticsDump.allRoots(composeTestRule)
        current = second
        composeTestRule.waitForIdle()
        return a to SemanticsDump.allRoots(composeTestRule)
    }

    private fun assertNoHandlerWithTargetEqualsNoTarget(style: OutcomeSheetStyle) {
        val (withTarget, withoutTarget) = treesOf(style, targetSuccess() to null, noTargetSuccess() to null)
        assertEquals(withoutTarget, withTarget)
        assertFalse(withTarget.contains(OPEN_TARGET_ROW_TEST_TAG))
    }

    private fun assertHandlerWithoutTargetEqualsNoHandler(style: OutcomeSheetStyle) {
        val handler: (FeedbackTarget) -> Unit = { }
        val (withHandler, withoutHandler) = treesOf(style, noTargetSuccess() to handler, noTargetSuccess() to null)
        assertEquals(withoutHandler, withHandler)
        assertFalse(withHandler.contains(OPEN_TARGET_ROW_TEST_TAG))
    }

    @Test
    fun noHandler_withTarget_rendersTheSameTreeAsNoTarget_classic() =
        assertNoHandlerWithTargetEqualsNoTarget(OutcomeSheetStyle.Classic)

    @Test
    fun noHandler_withTarget_rendersTheSameTreeAsNoTarget_receipt() =
        assertNoHandlerWithTargetEqualsNoTarget(OutcomeSheetStyle.Receipt)

    @Test
    fun handler_withoutTarget_rendersTheSameTreeAsNoHandler_classic() =
        assertHandlerWithoutTargetEqualsNoHandler(OutcomeSheetStyle.Classic)

    @Test
    fun handler_withoutTarget_rendersTheSameTreeAsNoHandler_receipt() =
        assertHandlerWithoutTargetEqualsNoHandler(OutcomeSheetStyle.Receipt)

    // --- D-02: placement ----------------------------------------------------------------------------

    private fun assertRowFollowsHandledByAndPrecedesEditable(style: OutcomeSheetStyle) {
        val outcome = targetSuccess().copy(
            handledBy = OutcomeFixtures.handledBy(),
            editableContent = OutcomeFixtures.editableProbe,
            batchResults = OutcomeFixtures.batchResults()
        )
        hostHoisted(outcome, style)

        val handledBy = topOf(composeTestRule.onNodeWithTag("outcome_sheet_handled_by", useUnmergedTree = true))
        val rowTop = topOf(row())
        val editable = topOf(composeTestRule.onNodeWithText("editable probe", useUnmergedTree = true))
        val batch = topOf(composeTestRule.onAllNodesWithTag("outcome_sheet_batch_row", useUnmergedTree = true)[0])
        assertTrue("row ($rowTop) must be below the handled-by indicator ($handledBy)", rowTop > handledBy)
        assertTrue("row ($rowTop) must be above the editable slot ($editable)", rowTop < editable)
        if (style == OutcomeSheetStyle.Classic) {
            assertTrue("Classic row ($rowTop) must be above the batch rows ($batch)", rowTop < batch)
        }
    }

    @Test
    fun targetRow_followsHandledBy_precedesEditable_inBothStyles() =
        assertRowFollowsHandledByAndPrecedesEditable(OutcomeSheetStyle.Receipt)

    @Test
    fun targetRow_followsHandledBy_precedesEditable_classic() =
        assertRowFollowsHandledByAndPrecedesEditable(OutcomeSheetStyle.Classic)

    // --- edge ordering --------------------------------------------------------------------------------

    private fun assertEqualSummaryAndLabelRendersHeadingThenRow(style: OutcomeSheetStyle) {
        hostHoisted(
            VoiceOutcomeUiState.Success(summary = "Weekend list", target = target, targetLabel = "Weekend list"),
            style
        )
        val nodes = composeTestRule.onAllNodesWithText("Weekend list", useUnmergedTree = true)
        assertEquals(
            "heading and row title are both rendered, never de-duplicated",
            2,
            nodes.fetchSemanticsNodes().size
        )
        assertTrue("heading first, row second", topOf(nodes[0]) < topOf(nodes[1]))
        assertTrue("the second text is inside the row", topOf(nodes[1]) >= topOf(row()))
    }

    @Test
    fun equalSummaryAndTargetLabel_rendersHeadingThenRow() =
        assertEqualSummaryAndLabelRendersHeadingThenRow(OutcomeSheetStyle.Receipt)

    @Test
    fun equalSummaryAndTargetLabel_rendersHeadingThenRow_classic() =
        assertEqualSummaryAndLabelRendersHeadingThenRow(OutcomeSheetStyle.Classic)

    // --- prohibition: the opaque key never reaches the tree -------------------------------------------

    private fun assertOpaqueKeyNeverAppears(style: OutcomeSheetStyle) {
        val secret = "KEY-DO-NOT-LEAK-31415"
        hostHoisted(
            VoiceOutcomeUiState.Success(
                summary = "Saved",
                target = FeedbackTarget(secret, "Open item"),
                targetLabel = "Weekend list"
            ),
            style
        )
        row().assertExists()
        val raw = SemanticsDump.allRawRoots(composeTestRule)
        for (fragment in listOf(secret, "DO-NOT-LEAK", "LEAK-31415", "31415")) {
            assertFalse("the opaque key fragment '$fragment' leaked into the tree", raw.contains(fragment))
        }
    }

    @Test
    fun opaqueKey_neverAppearsInTheSemanticsTree() = assertOpaqueKeyNeverAppears(OutcomeSheetStyle.Classic)

    @Test
    fun opaqueKey_neverAppearsInTheSemanticsTree_receipt() = assertOpaqueKeyNeverAppears(OutcomeSheetStyle.Receipt)
}
