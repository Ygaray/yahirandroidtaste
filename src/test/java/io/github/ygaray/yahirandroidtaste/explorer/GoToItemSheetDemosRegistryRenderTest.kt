package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.ygaray.yahirandroidtaste.component.OPEN_TARGET_CHEVRON_TEST_TAG
import io.github.ygaray.yahirandroidtaste.component.OPEN_TARGET_ROW_TEST_TAG
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import androidx.compose.ui.text.TextLayoutResult
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
 * Renders the `OutcomeSheet` registry entry's Variants the way the gallery does and drives the
 * go-to-item sheet demos added in Phase 28 (DEMO-03): a Success with one target and a mixed batch,
 * in the Receipt and the Classic style, in English and in German. The library never hides the sheet
 * and never navigates, so the demo slides the sheet down with the hoisted state's `hide()` and reads
 * the key only after the slide finished. Trigger tags are asserted as literals. NATIVE graphics, one
 * `setContent` per method, no pixel capture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h3000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class GoToItemSheetDemosRegistryRenderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val readoutTag = "demo_goto_opened"

    private var rendered = false

    /** One `setContent` per method: a second call in the same test keeps the page, and the sheet swaps. */
    private fun render() {
        if (rendered) return
        rendered = true
        val content = checkNotNull(ComponentRegistry.entries.first { it.name == "OutcomeSheet" }.content)
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) { content() }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun open(trigger: String) {
        composeTestRule.onNode(hasTestTag(trigger)).performScrollTo().performClick()
        composeTestRule.waitForIdle()
    }

    private fun count(tag: String): Int =
        composeTestRule.onAllNodes(hasTestTag(tag), useUnmergedTree = true).fetchSemanticsNodes().size

    private fun textCount(text: String): Int =
        composeTestRule.onAllNodes(hasText(text), useUnmergedTree = true).fetchSemanticsNodes().size

    private fun readout(): SemanticsNodeInteraction = composeTestRule.onNode(hasTestTag(readoutTag))

    private fun openRow(): SemanticsNodeInteraction =
        composeTestRule.onNode(hasTestTag(OPEN_TARGET_ROW_TEST_TAG), useUnmergedTree = true)

    private fun clickLabelOf(node: SemanticsNodeInteraction): String? =
        node.fetchSemanticsNode().config.getOrNull(SemanticsActions.OnClick)?.label

    /** Lets the slide-down finish: the manual clock idiom of `OutcomeSheetHoistedStateTest`. */
    private fun settleSlide() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.mainClock.advanceTimeBy(SLIDE_BUDGET_MS)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
    }

    private fun tap(node: SemanticsNodeInteraction) {
        node.performClick()
        composeTestRule.waitForIdle()
        settleSlide()
    }

    // --- Tracer: Receipt Success with one target --------------------------------------------------

    @Test
    fun readoutStartsNeutral() {
        render()
        readout().assertTextEquals("Opened: none")
    }

    @Test
    fun singleReceipt_showsTheRowAndChevron_andTheOpenLabelIsNotDrawn() {
        render()
        open("demo_goto_single_receipt")

        assertEquals("one go-to-item row", 1, count(OPEN_TARGET_ROW_TEST_TAG))
        assertEquals("one chevron", 1, count(OPEN_TARGET_CHEVRON_TEST_TAG))
        assertEquals(1, textCount("Item A"))
        assertEquals(1, textCount("List, 3 items"))
        assertEquals("the open label is never drawn", 0, textCount("Open item"))
        assertEquals("Open item", clickLabelOf(openRow()))
    }

    @Test
    fun singleReceipt_tapSlidesTheSheetDown_thenShowsTheKey() {
        render()
        open("demo_goto_single_receipt")
        assertEquals(1, count("outcome_sheet_title"))

        tap(openRow())

        readout().assertTextEquals("Opened: demo-item-1")
        assertEquals("the sheet is gone after the slide", 0, count("outcome_sheet_title"))
        assertEquals(0, count(OPEN_TARGET_ROW_TEST_TAG))
    }

    // --- Mixed batch, Classic style, German ---------------------------------------------------------

    private val batchRowTag = "outcome_sheet_batch_row"
    private val germanItemC = "Eintrag C mit einer sehr langen Bezeichnung für die Prüfung"

    private fun batchRows(): List<SemanticsNode> = composeTestRule
        .onAllNodes(hasTestTag(batchRowTag), useUnmergedTree = true).fetchSemanticsNodes()

    /** The batch row whose subtree shows [label]; the node carries the click action when targeted. */
    private fun batchRow(label: String): SemanticsNodeInteraction = composeTestRule.onNode(
        hasTestTag(batchRowTag) and hasAnyDescendant(hasText(label)),
        useUnmergedTree = true
    )

    private fun hasClick(node: SemanticsNode): Boolean = node.config.contains(SemanticsActions.OnClick)

    private fun assertRowsKeepTheirOrder(trigger: String, labels: List<String>) {
        render()
        open(trigger)
        val tops = labels.map { batchRow(it).fetchSemanticsNode().boundsInRoot.top }
        assertEquals("four rows", 4, batchRows().size)
        assertEquals("rows keep the caller's order top to bottom", tops.sorted(), tops)
        assertEquals("rows are distinct", 4, tops.toSet().size)
    }

    @Test
    fun batchReceipt_rowsKeepTheirInputOrder() =
        assertRowsKeepTheirOrder("demo_goto_batch_receipt", listOf("Item A", "Item B", "Item C", "Item D"))

    @Test
    fun batchClassic_rowsKeepTheirInputOrder() =
        assertRowsKeepTheirOrder("demo_goto_batch_classic", listOf("Item A", "Item B", "Item C", "Item D"))

    @Test
    fun onlyTheTargetedRowsOfTheMixedBatchAreClickable() {
        listOf("demo_goto_batch_receipt", "demo_goto_batch_classic").forEach { trigger ->
            render()
            open(trigger)
            val rows = batchRows()
            assertEquals("$trigger: four rows", 4, rows.size)
            assertEquals("$trigger: exactly two rows are clickable", 2, rows.count(::hasClick))
            assertEquals("$trigger: two chevrons", 2, count(OPEN_TARGET_CHEVRON_TEST_TAG))
            assertTrue("$trigger: A is clickable", hasClick(batchRow("Item A").fetchSemanticsNode()))
            assertTrue("$trigger: C is clickable", hasClick(batchRow("Item C").fetchSemanticsNode()))
        }
    }

    @Test
    fun batchRowsWithoutATarget_haveNoChevronAndNoClickAction() {
        render()
        open("demo_goto_batch_receipt")
        listOf("Item B", "Item D").forEach { label ->
            val node = batchRow(label).fetchSemanticsNode()
            assertFalse("$label has no click action", hasClick(node))
            val chevrons = composeTestRule.onAllNodes(
                hasTestTag(OPEN_TARGET_CHEVRON_TEST_TAG) and hasAnyAncestor(
                    hasTestTag(batchRowTag) and hasAnyDescendant(hasText(label))
                ),
                useUnmergedTree = true
            ).fetchSemanticsNodes()
            assertEquals("$label has no chevron", 0, chevrons.size)
        }
        tap(batchRow("Item B"))
        tap(batchRow("Item D"))
        readout().assertTextEquals("Opened: none")
        assertEquals("the sheet stays up", 4, batchRows().size)
    }

    private fun assertTappingOpensItsOwnKey(trigger: String) {
        render()
        open(trigger)
        tap(batchRow("Item A"))
        readout().assertTextEquals("Opened: demo-batch-a")
        assertEquals("the sheet is gone", 0, count(batchRowTag))

        open(trigger)
        tap(batchRow("Item C"))
        readout().assertTextEquals("Opened: demo-batch-c")
    }

    @Test
    fun batchReceipt_tappingATargetedRow_showsItsOwnKey() = assertTappingOpensItsOwnKey("demo_goto_batch_receipt")

    @Test
    fun batchClassic_tappingATargetedRow_showsItsOwnKey() = assertTappingOpensItsOwnKey("demo_goto_batch_classic")

    @Test
    fun singleClassic_showsTheRowAndOpensIt() {
        render()
        open("demo_goto_single_classic")
        assertEquals(1, count(OPEN_TARGET_ROW_TEST_TAG))
        assertEquals(1, count(OPEN_TARGET_CHEVRON_TEST_TAG))
        assertEquals(1, textCount("Item A"))
        assertEquals(0, textCount("Open item"))
        assertEquals("Open item", clickLabelOf(openRow()))

        tap(openRow())

        readout().assertTextEquals("Opened: demo-item-1")
        assertEquals(0, count(OPEN_TARGET_ROW_TEST_TAG))
    }

    @Test
    fun germanSingle_usesTheGermanOpenLabel() {
        listOf("demo_goto_single_receipt_german", "demo_goto_single_classic_german").forEach { trigger ->
            render()
            open(trigger)
            assertEquals("$trigger: title", 1, textCount("Eintrag A"))
            assertEquals("$trigger: detail", 1, textCount("Liste, 3 Einträge"))
            assertEquals("$trigger: label not drawn", 0, textCount("Eintrag öffnen"))
            assertEquals("Eintrag öffnen", clickLabelOf(openRow()))
            tap(openRow())
            readout().assertTextEquals("Geöffnet: demo-item-1")
        }
    }

    @Test
    fun germanBatch_tappingAnItem_showsTheGermanReadout() {
        render()
        open("demo_goto_batch_classic_german")
        assertEquals("Eintrag A öffnen", clickLabelOf(batchRow("Eintrag A")))
        tap(batchRow("Eintrag A"))
        readout().assertTextEquals("Geöffnet: demo-batch-a")

        assertRowsKeepTheirOrder(
            "demo_goto_batch_receipt_german",
            listOf("Eintrag A", "Eintrag B", germanItemC, "Eintrag D")
        )
    }

    @Test
    fun germanSheets_stayInsideA360dpWindow() {
        listOf(
            "demo_goto_single_receipt_german",
            "demo_goto_single_classic_german",
            "demo_goto_batch_receipt_german",
            "demo_goto_batch_classic_german"
        ).forEach { trigger ->
            render()
            open(trigger)
            val nodes = composeTestRule.onAllNodes(
                hasTestTag(OPEN_TARGET_ROW_TEST_TAG) or hasTestTag(batchRowTag) or
                    hasTestTag(OPEN_TARGET_CHEVRON_TEST_TAG),
                useUnmergedTree = true
            )
            val count = nodes.fetchSemanticsNodes().size
            assertTrue("$trigger: rows and chevrons exist ($count)", count >= 2)
            for (i in 0 until count) {
                val bounds = nodes[i].getUnclippedBoundsInRoot()
                assertTrue("$trigger node $i left ${bounds.left}", bounds.left.value >= 0f)
                assertTrue("$trigger node $i right ${bounds.right} must be <= 360dp", bounds.right.value <= 360f)
            }
            assertNoTextOverflowsItsHeight(trigger)
        }
    }

    private fun assertNoTextOverflowsItsHeight(trigger: String) {
        val texts = composeTestRule.onAllNodes(
            hasAnyAncestor(hasTestTag(OPEN_TARGET_ROW_TEST_TAG) or hasTestTag(batchRowTag)),
            useUnmergedTree = true
        ).fetchSemanticsNodes()
        var laidOut = 0
        texts.forEach { semantics ->
            val action = semantics.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action
                ?: return@forEach
            val results = mutableListOf<TextLayoutResult>()
            action.invoke(results)
            results.forEach { assertFalse("$trigger text overflows its height", it.didOverflowHeight) }
            laidOut++
        }
        assertTrue("$trigger: row texts were laid out ($laidOut)", laidOut >= 1)
    }

    @Test
    fun everyGoToItemTriggerExistsOnce() {
        render()
        assertEquals(8, GoToItemDemo.entries.size)
        GoToItemDemo.entries.forEach { demo ->
            assertEquals("exactly one ${demo.tag}", 1, count(demo.tag))
        }
        assertEquals("the readout exists once", 1, count(readoutTag))
    }

    private companion object {
        const val SLIDE_BUDGET_MS = 2_000L
    }
}
