package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import io.github.ygaray.yahirandroidtaste.component.OPEN_TARGET_CHEVRON_TEST_TAG
import io.github.ygaray.yahirandroidtaste.component.OPEN_TARGET_ROW_TEST_TAG
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertEquals
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

    private fun render() {
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

    private companion object {
        const val SLIDE_BUDGET_MS = 2_000L
    }
}
