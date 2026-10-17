package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.testsupport.SemanticsDump
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
import org.robolectric.annotation.GraphicsMode

/**
 * FOPEN-02: each batch result row opens its own item, in the Receipt and the Classic style. Robolectric
 * tree assertions only; behavior on a real device belongs to the Gate-1 tester. NATIVE graphics gives
 * real font metrics for the no-reserved-space width check. Receipt is hosted through
 * `OutcomeSheetReceiptContent`, Classic through the PUBLIC [OutcomeSheet] with a hoisted state, as a
 * consumer would; the both-styles tests flip the style inside the one composition each test is allowed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OutcomeSheetBatchOpenTargetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val opened = mutableListOf<FeedbackTarget>()

    private val targetA = FeedbackTarget("a-1", "Open item A")
    private val targetC = FeedbackTarget("c-3", "Open item C")

    private var style by mutableStateOf(OutcomeSheetStyle.Receipt)
    private var outcome: VoiceOutcomeUiState by mutableStateOf(VoiceOutcomeUiState.Success(summary = "Saved"))
    private var handler: ((FeedbackTarget) -> Unit)? by mutableStateOf({ opened += it })
    private lateinit var scheme: ColorScheme

    private fun mixedRows() = listOf(
        BatchRowResultUiModel(label = "Item A", succeeded = true, target = targetA),
        BatchRowResultUiModel(label = "Item B", succeeded = true),
        BatchRowResultUiModel(label = "Item C", succeeded = true, target = targetC)
    )

    private fun successOf(rows: List<BatchRowResultUiModel>, inFlight: Boolean = false) =
        VoiceOutcomeUiState.Success(summary = "Saved", batchResults = rows, inFlight = inFlight)

    private fun withoutTargets(rows: List<BatchRowResultUiModel>) = rows.map { it.copy(target = null) }

    private fun hostReceipt(success: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(Modifier.height(900.dp)) {
                    OutcomeSheetReceiptContent(success, onOpenTarget = { opened += it })
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    /** Hosts the Classic style through the PUBLIC sheet with a hoisted state, as a consumer would. */
    private fun hostClassic(success: VoiceOutcomeUiState) {
        composeTestRule.setContent {
            OutcomeSheet(
                outcome = success,
                onDismissRequest = {},
                style = OutcomeSheetStyle.Classic,
                sheetState = rememberModalBottomSheetState(),
                onOpenTarget = { opened += it }
            )
        }
        composeTestRule.waitForIdle()
    }

    /** Hosts the style, outcome and handler fields in ONE composition so a test can flip any of them. */
    private fun hostFlippable(initial: VoiceOutcomeUiState = outcome) {
        outcome = initial
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                scheme = MaterialTheme.colorScheme
                if (style == OutcomeSheetStyle.Receipt) {
                    Box(Modifier.height(900.dp)) { OutcomeSheetReceiptContent(outcome, onOpenTarget = handler) }
                } else {
                    OutcomeSheet(
                        outcome = outcome,
                        onDismissRequest = {},
                        style = OutcomeSheetStyle.Classic,
                        sheetState = rememberModalBottomSheetState(),
                        onOpenTarget = handler
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    /** Runs [block] once per style against the flippable host, with a clean `opened` list each time. */
    private fun forBothStyles(block: (OutcomeSheetStyle) -> Unit) {
        listOf(OutcomeSheetStyle.Receipt, OutcomeSheetStyle.Classic).forEach { s ->
            style = s
            opened.clear()
            composeTestRule.waitForIdle()
            block(s)
        }
    }

    private fun batchRows(): List<SemanticsNodeInteraction> {
        val nodes = composeTestRule.onAllNodesWithTag(BATCH_ROW_TAG, useUnmergedTree = true)
        return List(nodes.fetchSemanticsNodes().size) { nodes[it] }
    }

    private fun chevronCount(): Int =
        composeTestRule.onAllNodesWithTag(OPEN_TARGET_CHEVRON_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes().size

    private fun singleRowCount(): Int =
        composeTestRule.onAllNodesWithTag(OPEN_TARGET_ROW_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes().size

    private fun SemanticsNodeInteraction.hasClick(): Boolean =
        fetchSemanticsNode().config.contains(SemanticsActions.OnClick)

    private fun click(row: SemanticsNodeInteraction) {
        row.performClick()
        composeTestRule.waitForIdle()
    }

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    private fun textNodes(text: String): List<SemanticsNodeInteraction> {
        val nodes = composeTestRule.onAllNodesWithText(text, useUnmergedTree = true)
        return List(nodes.fetchSemanticsNodes().size) { nodes[it] }
    }

    // --- tracer and Classic ---------------------------------------------------------------------------

    @Test
    fun receipt_mixedBatch_eachTargetedRowOpensItsOwnItem_targetlessRowIsInert() {
        hostReceipt(successOf(mixedRows()))

        val rows = batchRows()
        assertEquals(3, rows.size)

        click(rows[0])
        assertEquals(listOf(targetA), opened)

        click(rows[2])
        assertEquals(listOf(targetA, targetC), opened)

        click(rows[1])
        assertEquals("a target-less row must not call the handler", listOf(targetA, targetC), opened)

        assertFalse("a target-less row exposes no click action", rows[1].hasClick())
        assertEquals("only the two targeted rows draw a chevron", 2, chevronCount())
        assertEquals("Open item A", rows[0].fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        assertTrue(rows[0].hasClick())
    }

    @Test
    fun classic_mixedBatch_eachTargetedRowOpensItsOwnItem_targetlessRowIsInert() {
        // No single Success.target: only the batch rows make the outcome openable.
        hostClassic(successOf(mixedRows()))

        val rows = batchRows()
        assertEquals(3, rows.size)

        click(rows[0])
        assertEquals(listOf(targetA), opened)

        click(rows[2])
        assertEquals(listOf(targetA, targetC), opened)

        click(rows[1])
        assertEquals("a target-less row must not call the handler", listOf(targetA, targetC), opened)

        assertFalse(rows[1].hasClick())
        assertEquals("only the two targeted rows draw a chevron", 2, chevronCount())
        assertEquals("Open item A", rows[0].fetchSemanticsNode().config[SemanticsActions.OnClick].label)
    }

    @Test
    fun classic_batchOnlyOutcome_reachesTheOpenPath() {
        hostClassic(successOf(mixedRows()))
        assertEquals("the open list draws a chevron, so the open body is active", 2, chevronCount())
        assertEquals("no single go-to-item row without a Success.target", 0, singleRowCount())
    }

    // --- edges: adjacency, empty, ordering --------------------------------------------------------------

    @Test
    fun equalTargetsOnTwoRows_eachCallsTheHandlerOnItsOwnTap() {
        hostFlippable(
            successOf(
                listOf(
                    BatchRowResultUiModel("One", true, target = targetA),
                    BatchRowResultUiModel("Two", true, target = targetA)
                )
            )
        )
        forBothStyles {
            val rows = batchRows()
            assertEquals("equal targets never merge the rows", 2, rows.size)
            click(rows[0])
            assertEquals(listOf(targetA), opened)
            click(rows[1])
            assertEquals(listOf(targetA, targetA), opened)
            click(rows[1])
            assertEquals(3, opened.size)
        }
    }

    @Test
    fun targetedRowBetweenTargetlessRows_leavesNeighboursInert() {
        hostFlippable(
            successOf(
                listOf(
                    BatchRowResultUiModel("Before", true),
                    BatchRowResultUiModel("Middle", true, target = targetA),
                    BatchRowResultUiModel("After", true)
                )
            )
        )
        forBothStyles {
            val rows = batchRows()
            assertEquals(3, rows.size)
            assertFalse(rows[0].hasClick())
            assertTrue(rows[1].hasClick())
            assertFalse(rows[2].hasClick())
            rows.forEach { click(it) }
            assertEquals("only the middle row opens", listOf(targetA), opened)
            assertEquals(1, chevronCount())
        }
    }

    @Test
    fun emptyBatch_rendersNoList() {
        hostFlippable(VoiceOutcomeUiState.Success(summary = "Saved", target = targetA))
        forBothStyles {
            assertEquals("an empty batch draws no row", 0, batchRows().size)
            assertEquals("only the single go-to-item row draws a chevron", 1, chevronCount())
        }
    }

    @Test
    fun allRowsWithoutTargets_rendersTheV271List() {
        val rows = OutcomeFixtures.batchResults()
        hostFlippable(successOf(rows))
        forBothStyles {
            handler = { opened += it }
            composeTestRule.waitForIdle()
            val withHandler = SemanticsDump.allRoots(composeTestRule)
            handler = null
            composeTestRule.waitForIdle()
            val withoutHandler = SemanticsDump.allRoots(composeTestRule)
            assertEquals("a handler with no targeted row renders the v2.7.1 list", withoutHandler, withHandler)
            assertEquals(rows.size, batchRows().size)
            assertEquals(0, chevronCount())
            assertTrue(batchRows().none { it.hasClick() })
        }
    }

    @Test
    fun singleRowBatch_withATarget_opens() {
        hostFlippable(successOf(listOf(BatchRowResultUiModel("Only", true, target = targetA))))
        forBothStyles {
            val rows = batchRows()
            assertEquals(1, rows.size)
            click(rows[0])
            assertEquals(listOf(targetA), opened)
        }
    }

    @Test
    fun equalLabels_keepListOrder_andEachOpensItsOwnTarget() {
        val first = FeedbackTarget("k1", "Open first")
        val second = FeedbackTarget("k2", "Open second")
        val third = FeedbackTarget("k3", "Open third")
        hostFlippable(
            successOf(
                listOf(
                    BatchRowResultUiModel("Same", true, target = first),
                    BatchRowResultUiModel("Same", true, target = second),
                    BatchRowResultUiModel("Same", true, target = third)
                )
            )
        )
        forBothStyles {
            val rows = batchRows()
            assertEquals("rows with equal labels are never de-duplicated", 3, rows.size)
            val tops = rows.map { it.fetchSemanticsNode().positionInRoot.y }
            assertEquals("rows keep their list order", tops.sorted(), tops)
            click(rows[1])
            click(rows[2])
            click(rows[0])
            assertEquals(listOf(second, third, first), opened)
        }
    }

    // --- parity with v2.7.1 -------------------------------------------------------------------------------

    @Test
    fun noHandler_withTargetedRows_rendersTheV271List_bothStyles() {
        val targeted = successOf(mixedRows())
        hostFlippable(targeted)
        handler = null
        forBothStyles {
            outcome = targeted
            composeTestRule.waitForIdle()
            val withTargets = SemanticsDump.allRoots(composeTestRule)
            outcome = successOf(withoutTargets(mixedRows()))
            composeTestRule.waitForIdle()
            val withoutTargetsTree = SemanticsDump.allRoots(composeTestRule)
            assertEquals("no handler means the v2.7.1 list", withoutTargetsTree, withTargets)
            assertEquals(0, chevronCount())
        }
    }

    // --- anatomy ---------------------------------------------------------------------------------------------

    @Test
    fun targetlessRow_reservesNoChevronSpace() {
        val label = "Wochenendeinkauf mit Gemüse, Brot, Käse und einer langen Notiz für alle"
        hostFlippable(
            successOf(
                listOf(
                    BatchRowResultUiModel(label, true),
                    BatchRowResultUiModel(label, true, target = targetA)
                )
            )
        )
        forBothStyles {
            val texts = textNodes(label)
            assertEquals(2, texts.size)
            val targetless = texts[0].textLayout()
            val targeted = texts[1].textLayout()
            assertTrue("the label wraps in the narrow list", targeted.lineCount > 1)
            assertTrue(
                "target-less width ${targetless.size.width} must exceed targeted width ${targeted.size.width}",
                targetless.size.width > targeted.size.width
            )
        }
    }

    @Test
    fun failedRowWithTarget_rendersErrorTextAndOpens() {
        hostFlippable(
            successOf(
                listOf(
                    BatchRowResultUiModel("Fine", true, target = targetA),
                    BatchRowResultUiModel("Broken", false, detail = "not found", target = targetC)
                )
            )
        )
        forBothStyles {
            assertEquals(scheme.onSurface, textNodes("Fine").single().textLayout().layoutInput.style.color)
            assertEquals(scheme.error, textNodes("Broken").single().textLayout().layoutInput.style.color)
            assertEquals(scheme.error, textNodes("not found").single().textLayout().layoutInput.style.color)
            click(batchRows()[1])
            assertEquals(listOf(targetC), opened)
        }
    }

    @Test
    fun failedRowWithoutTarget_showsDetailAndIsInert() {
        hostFlippable(successOf(OutcomeFixtures.batchResultsWithTargets()))
        forBothStyles {
            assertEquals("the failed row shows its detail", 1, textNodes("not found").size)
            val rows = batchRows()
            assertEquals(3, rows.size)
            assertFalse("the failed target-less row is inert", rows[1].hasClick())
            click(rows[1])
            assertTrue(opened.isEmpty())
            assertEquals(2, chevronCount())
        }
    }

    @Test
    fun succeededRowWithDetail_neverShowsTheDetail() {
        hostFlippable(successOf(listOf(BatchRowResultUiModel("Fine", true, detail = "hidden", target = targetA))))
        forBothStyles {
            assertEquals("the detail belongs to a failed row only", 0, textNodes("hidden").size)
        }
    }

    // --- PD-1: locked while in flight ---------------------------------------------------------------------

    @Test
    fun inFlight_locksTargetedRows_thenUnlocks() {
        hostFlippable(successOf(mixedRows(), inFlight = true))
        forBothStyles {
            outcome = successOf(mixedRows(), inFlight = true)
            composeTestRule.waitForIdle()
            val locked = batchRows()
            assertTrue("no row has a click action while in flight", locked.none { it.hasClick() })
            assertEquals("the chevrons stay while locked", 2, chevronCount())
            click(locked[0])
            assertTrue(opened.isEmpty())

            outcome = successOf(mixedRows(), inFlight = false)
            composeTestRule.waitForIdle()
            val unlocked = batchRows()
            assertTrue(unlocked[0].hasClick())
            assertFalse(unlocked[1].hasClick())
            assertTrue(unlocked[2].hasClick())
            click(unlocked[0])
            assertEquals(listOf(targetA), opened)
        }
    }

    // --- tags and privacy -----------------------------------------------------------------------------------

    @Test
    fun everyRowKeepsTheDocumentedBatchRowTag() {
        val rows = OutcomeFixtures.batchResultsWithTargets() + BatchRowResultUiModel("Item D", true)
        hostFlippable(successOf(rows))
        forBothStyles {
            assertEquals("one tagged node per row in the open list", rows.size, batchRows().size)
        }
    }

    @Test
    fun opaqueKeys_neverAppearInTheSemanticsTree() {
        val keys = listOf("ROW-KEY-ALPHA-111", "ROW-KEY-BETA-222")
        hostFlippable(
            successOf(
                listOf(
                    BatchRowResultUiModel("Item A", true, target = FeedbackTarget(keys[0], "Open item A")),
                    BatchRowResultUiModel("Item B", true, target = FeedbackTarget(keys[1], "Open item B"))
                )
            )
        )
        forBothStyles {
            val raw = SemanticsDump.allRawRoots(composeTestRule)
            assertEquals(2, chevronCount())
            keys.flatMap { listOf(it, it.substringBeforeLast('-')) }.forEach { fragment ->
                assertFalse("the opaque key fragment '$fragment' leaked into the tree", raw.contains(fragment))
            }
        }
    }

    private companion object {
        const val BATCH_ROW_TAG = "outcome_sheet_batch_row"
    }
}
