package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.KeyFieldState
import io.github.ygaray.yahirandroidtaste.model.ProviderKeyActions
import io.github.ygaray.yahirandroidtaste.model.SavedKeyUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Layer 1 of the scoped TalkBack sweep (A11Y-02, decision D-03 [talkback-method]): Robolectric
 * assertions on the semantics tree for the ReceiptRow, the refused-undo live region, the saved-key
 * summary and the target-row open label. Layer 2 (the real accessibility tree and the TalkBack
 * smoke on the tester device) is the Gate-1 plan 28-07; whatever is only audible stays parked.
 *
 * Every test carries a falsification control: an assertion that the same check would fail for the
 * wrong node (a sibling, the neighbouring form, the other state), so a vacuous pass is not possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class A11ySweepSemanticsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun SemanticsNodeInteraction.config() = fetchSemanticsNode().config

    // ---- item 1: ReceiptRow -----------------------------------------------------------------------

    @Test
    fun receiptRow_clickable_isOneButtonWithStateAndUndoIsAnotherStop() {
        var rowClicks = 0
        var undoClicks = 0
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                ReceiptRow(
                    title = "Item title",
                    value = "5",
                    onClick = { rowClicks++ },
                    onClickLabel = "Edit row",
                    trailingAction = { TextButton(onClick = { undoClicks++ }) { Text("Undo") } }
                )
            }
        }
        composeTestRule.waitForIdle()

        val summary = composeTestRule.onNodeWithTag("receipt_row_summary").config()
        assertEquals(Role.Button, summary.getOrNull(SemanticsProperties.Role))
        assertEquals("Edit row", summary[SemanticsActions.OnClick].label)
        assertEquals("Collapsed", summary.getOrNull(SemanticsProperties.StateDescription))

        // Control: the Undo node is a different stop, outside the summary, clickable on its own.
        val undo = composeTestRule.onNodeWithText("Undo")
        assertFalse(
            "Undo must not be inside the summary",
            hasAnyAncestor(hasTestTag("receipt_row_summary")).matches(undo.fetchSemanticsNode())
        )
        assertTrue(hasClickAction().matches(undo.fetchSemanticsNode()))
        assertNotEquals(
            composeTestRule.onNodeWithTag("receipt_row_summary").fetchSemanticsNode().id,
            undo.fetchSemanticsNode().id
        )
        undo.performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, undoClicks)
        assertEquals("Undo never toggles the row", 0, rowClicks)
        assertEquals(
            "Collapsed",
            composeTestRule.onNodeWithTag("receipt_row_summary").config().getOrNull(SemanticsProperties.StateDescription)
        )
    }

    @Test
    fun receiptRow_toggledTwice_returnsToCollapsed() {
        var expanded by mutableStateOf(false)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                ReceiptRow(title = "Item title", expanded = expanded, onClick = { expanded = !expanded })
            }
        }
        composeTestRule.waitForIdle()

        fun state() = composeTestRule.onNodeWithTag("receipt_row_summary").config()
            .getOrNull(SemanticsProperties.StateDescription)

        assertEquals("Collapsed", state())
        composeTestRule.onNodeWithTag("receipt_row_summary").performClick()
        composeTestRule.waitForIdle()
        assertEquals("Expanded", state())
        composeTestRule.onNodeWithTag("receipt_row_summary").performClick()
        composeTestRule.waitForIdle()
        assertEquals("Collapsed", state())
        assertEquals(
            "still exactly one summary node",
            1,
            composeTestRule.onAllNodesWithTag("receipt_row_summary", useUnmergedTree = true).fetchSemanticsNodes().size
        )
    }

    // ---- item 2: refused-undo live region -----------------------------------------------------------

    private var outcome: VoiceOutcomeUiState by mutableStateOf(VoiceOutcomeUiState.Success(summary = "x"))

    private fun hostRefused(initial: VoiceOutcomeUiState) {
        outcome = initial
        composeTestRule.setContent {
            Box(Modifier.height(800.dp)) { OutcomeSheetContent(outcome) }
        }
        composeTestRule.waitForIdle()
    }

    private fun liveRegionNodes() = composeTestRule.onAllNodes(
        SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion),
        useUnmergedTree = true
    )

    private fun assertRegionCarriesMessage(message: String) {
        val regions = liveRegionNodes().fetchSemanticsNodes()
        assertEquals("exactly one live region", 1, regions.size)
        val tagged = composeTestRule.onNodeWithTag("outcome_sheet_undo_refused", useUnmergedTree = true)
            .fetchSemanticsNode()
        assertEquals("the region is the tagged refused surface", tagged.id, regions.single().id)
        assertEquals(LiveRegionMode.Assertive, tagged.config[SemanticsProperties.LiveRegion])
        composeTestRule.onNode(
            hasText(message) and hasAnyAncestor(hasTestTag("outcome_sheet_undo_refused")),
            useUnmergedTree = true
        ).assertExists()
        // Control: a text that is NOT the message is not under the region.
        composeTestRule.onNode(
            hasText("Logged 2 of 3") and hasAnyAncestor(hasTestTag("outcome_sheet_undo_refused")),
            useUnmergedTree = true
        ).assertDoesNotExist()
    }

    @Test
    fun refusedSurface_isTheOnlyAssertiveRegion_andItsSubtreeCarriesTheMessage() {
        val message = "Couldn't undo: it was edited elsewhere"
        hostRefused(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))
        assertRegionCarriesMessage(message)

        // The Failure arm shows the same single region with the same message.
        outcome = OutcomeFixtures.failure(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused()))
        composeTestRule.waitForIdle()
        assertRegionCarriesMessage(message)

        // Control: without a refusal there is no region at all.
        outcome = OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = null))
        composeTestRule.waitForIdle()
        liveRegionNodes().fetchSemanticsNodes().let { assertEquals(0, it.size) }
    }

    @Test
    fun refusedSurface_withoutChangedItem_hasNoSuffix() {
        hostRefused(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))

        val texts = composeTestRule.onNodeWithTag("outcome_sheet_undo_refused", useUnmergedTree = true)
            .fetchSemanticsNode().children.flatMap { it.config.getOrNull(SemanticsProperties.Text).orEmpty() }
            .map { it.text }
        assertEquals(listOf("Couldn't undo: it was edited elsewhere"), texts)
        assertFalse("no dangling separator: $texts", texts.single().contains(", "))

        // Control: a changed item does add the suffix, so the check above can fail.
        outcome = OutcomeFixtures.success(
            undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused(changedItem = "Row A"))
        )
        composeTestRule.waitForIdle()
        val withItem = composeTestRule.onNodeWithTag("outcome_sheet_undo_refused", useUnmergedTree = true)
            .fetchSemanticsNode().children.flatMap { it.config.getOrNull(SemanticsProperties.Text).orEmpty() }
            .map { it.text }
        assertTrue("suffix present with a changed item: $withItem", withItem.single().contains(", Row A"))
    }

    @Test
    fun refusedSurface_recomposedWithEqualState_keepsExactlyOneRegion() {
        hostRefused(OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused())))
        assertEquals(1, liveRegionNodes().fetchSemanticsNodes().size)

        repeat(2) {
            outcome = OutcomeFixtures.success(undo = OutcomeFixtures.undo(refused = OutcomeFixtures.refused()))
            composeTestRule.waitForIdle()
            assertEquals(1, liveRegionNodes().fetchSemanticsNodes().size)
        }
        assertEquals(
            1,
            composeTestRule.onAllNodesWithTag("outcome_sheet_undo_refused", useUnmergedTree = true)
                .fetchSemanticsNodes().size
        )
    }

    // ---- item 3: saved-key summary ------------------------------------------------------------------

    private val fingerprint = "sk-ant-…abcd"

    @Composable
    private fun KeyCard(actions: ProviderKeyActions?) {
        ProviderKeyCard(
            providers = ProviderKeyCardGoldens.providers,
            selectedProviderId = "anthropic",
            onProviderSelected = {},
            keyValue = "",
            onKeyChange = {},
            keyState = KeyFieldState.Empty,
            keyLabel = "API key",
            savedKeys = mapOf("anthropic" to SavedKeyUiModel(fingerprint)),
            editingSavedKey = false,
            actions = actions
        )
    }

    private fun hostKeyCard(actions: ProviderKeyActions?) {
        composeTestRule.setContent { KeyCard(actions) }
        composeTestRule.waitForIdle()
    }

    private fun summaryTexts() = composeTestRule.onNodeWithTag("provider_key_saved_summary")
        .config()[SemanticsProperties.Text].map { it.text }

    private fun outsideSummary(tag: String): SemanticsNodeInteraction {
        val node = composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)
        assertFalse(
            "$tag must not sit inside the summary",
            hasAnyAncestor(hasTestTag("provider_key_saved_summary")).matches(node.fetchSemanticsNode())
        )
        return node
    }

    @Test
    fun savedKeySummary_isOneItem_andUpdateAndClearAreSeparateButtons() {
        hostKeyCard(ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = {}))

        assertEquals(listOf("Saved · $fingerprint"), summaryTexts())
        val summaryId = composeTestRule.onNodeWithTag("provider_key_saved_summary").fetchSemanticsNode().id
        val update = outsideSummary("provider_key_action_update")
        val clear = outsideSummary("provider_key_action_clear")
        listOf(update, clear).forEach { button ->
            assertEquals(Role.Button, button.config().getOrNull(SemanticsProperties.Role))
            assertTrue(button.config().contains(SemanticsActions.OnClick))
        }
        assertNotEquals(update.fetchSemanticsNode().id, clear.fetchSemanticsNode().id)
        // Control: the summary itself is not a button.
        assertNotEquals(update.fetchSemanticsNode().id, summaryId)
        assertFalse(composeTestRule.onNodeWithTag("provider_key_saved_summary").config().contains(SemanticsActions.OnClick))
        assertFalse(summaryTexts().any { it.contains("Update") || it.contains("Clear") })
    }

    @Test
    fun savedKeySummary_withoutActions_isStillOneItem_andHasNoButtons() {
        hostKeyCard(null)

        assertEquals(listOf("Saved · $fingerprint"), summaryTexts())
        composeTestRule.onNodeWithTag("provider_key_action_update", useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("provider_key_action_clear", useUnmergedTree = true).assertDoesNotExist()
        // Control: the summary node exists exactly once.
        assertEquals(
            1,
            composeTestRule.onAllNodesWithTag("provider_key_saved_summary", useUnmergedTree = true)
                .fetchSemanticsNodes().size
        )
    }

    @Test
    fun savedKeySummary_withOnlyUpdate_hasOnlyTheUpdateButton() {
        hostKeyCard(ProviderKeyActions(onSave = {}, onUpdate = {}, onClear = null))

        assertEquals(listOf("Saved · $fingerprint"), summaryTexts())
        val update = outsideSummary("provider_key_action_update")
        assertEquals(Role.Button, update.config().getOrNull(SemanticsProperties.Role))
        composeTestRule.onNodeWithTag("provider_key_action_clear", useUnmergedTree = true).assertDoesNotExist()
    }

    // ---- item 5a: sheet target rows ---------------------------------------------------------------------

    private var style by mutableStateOf(OutcomeSheetStyle.Receipt)

    private val composed = "Eintrag öffnen"
    private val decomposed = "Eintrag öffnen"

    private fun hostFlippable() {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                if (style == OutcomeSheetStyle.Receipt) {
                    Box(Modifier.height(900.dp)) { OutcomeSheetReceiptContent(outcome, onOpenTarget = {}) }
                } else {
                    OutcomeSheet(
                        outcome = outcome,
                        onDismissRequest = {},
                        style = OutcomeSheetStyle.Classic,
                        sheetState = rememberModalBottomSheetState(),
                        onOpenTarget = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun clickLabelsOf(tag: String): List<String?> =
        composeTestRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes()
            .map { it.config.getOrNull(SemanticsActions.OnClick)?.label }

    @Test
    fun sheetTargetRow_openLabel_passesThroughVerbatim_composedAndDecomposed() {
        assertNotEquals("the two Unicode forms are different strings", composed, decomposed)
        outcome = VoiceOutcomeUiState.Success(summary = "Saved")
        hostFlippable()

        listOf(OutcomeSheetStyle.Receipt, OutcomeSheetStyle.Classic).forEach { s ->
            style = s
            listOf(composed, decomposed).forEach { label ->
                // Single target row.
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Saved",
                    target = FeedbackTarget("k-1", label)
                )
                composeTestRule.waitForIdle()
                val single = clickLabelsOf(OPEN_TARGET_ROW_TEST_TAG)
                assertEquals("$s single row for ${label.length} chars", listOf<String?>(label), single)

                // Batch row.
                outcome = VoiceOutcomeUiState.Success(
                    summary = "Saved",
                    batchResults = listOf(
                        BatchRowResultUiModel("Item A", succeeded = true, target = FeedbackTarget("a-1", label)),
                        BatchRowResultUiModel("Item B", succeeded = true)
                    )
                )
                composeTestRule.waitForIdle()
                val batch = clickLabelsOf("outcome_sheet_batch_row")
                assertTrue("$s batch carries the label verbatim: $batch", batch.contains(label))

                // The label is announced, never drawn.
                assertEquals(
                    "$s: the open label is not drawn as text",
                    0,
                    composeTestRule.onAllNodesWithText(label, useUnmergedTree = true).fetchSemanticsNodes().size
                )
                // Control: the OTHER Unicode form is not what the node carries.
                val other = if (label == composed) decomposed else composed
                assertFalse("$s: no normalization to the other form", batch.contains(other))
            }
        }
        assertNotNull(style)
    }
}
