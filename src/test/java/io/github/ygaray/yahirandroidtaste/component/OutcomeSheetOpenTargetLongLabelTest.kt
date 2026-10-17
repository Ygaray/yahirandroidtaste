package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Long German and Spanish row text at 360dp, at font scale 1.0 and 2.0, in both styles (the "test
 * localized long strings before signoff" lesson, FOPEN-01): the chevron stays fully inside the
 * window, the title and the detail end at or before the chevron, no line is ellipsized, and the row
 * sits below the heading. NATIVE graphics gives real font metrics; geometry is read UNCLIPPED; one
 * `setContent` per test method; the font scale is flipped through state inside that one composition.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OutcomeSheetOpenTargetLongLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class Copy(val summary: String, val title: String, val detail: String, val openLabel: String)

    private val german = Copy(
        summary = "Gespeichert",
        title = "Eintrag Wocheneinkauf für das lange Wochenende wurde gespeichert",
        detail = "Liste mit zwölf Einträgen, zuletzt gestern Abend geändert",
        openLabel = "Eintrag öffnen"
    )

    private val spanish = Copy(
        summary = "Guardado",
        title = "La lista de la compra semanal para el fin de semana largo se ha guardado",
        detail = "Lista con doce elementos, modificada por última vez ayer por la noche",
        openLabel = "Abrir elemento"
    )

    private fun successOf(copy: Copy) = VoiceOutcomeUiState.Success(
        summary = copy.summary,
        target = FeedbackTarget("item-42", copy.openLabel),
        targetLabel = copy.title,
        targetDetail = copy.detail
    )

    private var fontScale by mutableFloatStateOf(1f)

    private fun setContent(receipt: Boolean, copy: Copy, success: VoiceOutcomeUiState.Success = successOf(copy)) {
        composeTestRule.setContent {
            YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                CompositionLocalProvider(
                    LocalDensity provides Density(LocalDensity.current.density, fontScale = fontScale)
                ) {
                    Box(Modifier.width(360.dp).height(800.dp)) {
                        if (receipt) {
                            OutcomeSheetReceiptContent(success, onOpenTarget = {})
                        } else {
                            OutcomeSheetContentOpenTarget(success, onOpenTarget = {})
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun windowWidthPx(): Float = 360f * composeTestRule.density.density

    private fun tagged(tag: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun byText(text: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(text, useUnmergedTree = true)

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    private fun assertNoEllipsizedLine(label: String, node: SemanticsNodeInteraction) {
        val layout = node.textLayout()
        (0 until layout.lineCount).forEach { line ->
            assertFalse("$label line $line must not be ellipsized", layout.isLineEllipsized(line))
        }
    }

    /** Asserts the geometry at the current font scale and returns the row height in px. */
    private fun assertGeometry(receipt: Boolean, copy: Copy, scale: Float): Int {
        val chevron = tagged(OPEN_TARGET_CHEVRON_TEST_TAG).fetchSemanticsNode()
        val chevronLeft = chevron.positionInRoot.x
        val chevronRight = chevronLeft + chevron.size.width
        assertTrue("chevron has a visible width at x$scale", chevron.size.width > 0)
        assertTrue(
            "chevron $chevronLeft..$chevronRight must lie inside 0..${windowWidthPx()} at x$scale",
            chevronLeft >= -1f && chevronRight <= windowWidthPx() + 1f
        )

        listOf("title" to copy.title, "detail" to copy.detail).forEach { (label, text) ->
            val node = byText(text)
            val right = node.fetchSemanticsNode().let { it.positionInRoot.x + it.size.width }
            assertTrue("$label right edge $right must be at or left of the chevron $chevronLeft at x$scale", right <= chevronLeft + 1f)
            assertNoEllipsizedLine("$label at x$scale", node)
        }

        val row = tagged(OPEN_TARGET_ROW_TEST_TAG).fetchSemanticsNode()
        val headingTop = if (receipt) {
            tagged("outcome_sheet_title").fetchSemanticsNode().positionInRoot.y
        } else {
            byText(copy.summary).fetchSemanticsNode().positionInRoot.y
        }
        assertTrue("row top ${row.positionInRoot.y} must be below the heading top $headingTop", row.positionInRoot.y > headingTop)
        assertTrue("row must stay inside the window width at x$scale", row.positionInRoot.x + row.size.width <= windowWidthPx() + 1f)
        return row.size.height
    }

    private fun assertLongTextWraps(receipt: Boolean, copy: Copy) {
        fontScale = 1f
        setContent(receipt, copy)
        val normalHeight = assertGeometry(receipt, copy, 1f)

        fontScale = 2f
        composeTestRule.waitForIdle()
        val largeHeight = assertGeometry(receipt, copy, 2f)

        assertTrue("the row must grow taller at font scale 2.0 ($normalHeight -> $largeHeight)", largeHeight > normalHeight)
    }

    @Test
    fun receipt_germanTargetRow_wrapsAndKeepsTheChevronInside_atScale1and2() = assertLongTextWraps(true, german)

    @Test
    fun receipt_spanishTargetRow_wrapsAndKeepsTheChevronInside_atScale1and2() = assertLongTextWraps(true, spanish)

    @Test
    fun classic_germanTargetRow_wrapsAndKeepsTheChevronInside_atScale1and2() = assertLongTextWraps(false, german)

    @Test
    fun classic_spanishTargetRow_wrapsAndKeepsTheChevronInside_atScale1and2() = assertLongTextWraps(false, spanish)

    // --- FOPEN-02: a targeted, failed batch row with a long label and a long failure detail ---------------

    /** No single target: the batch row alone makes the outcome openable, so there is exactly one chevron. */
    private fun batchSuccessOf(copy: Copy) = VoiceOutcomeUiState.Success(
        summary = copy.summary,
        batchResults = listOf(
            BatchRowResultUiModel(
                label = copy.title,
                succeeded = false,
                detail = copy.detail,
                target = FeedbackTarget("item-42", copy.openLabel)
            )
        )
    )

    private fun assertBatchGeometry(copy: Copy, scale: Float): Int {
        val chevron = tagged(OPEN_TARGET_CHEVRON_TEST_TAG).fetchSemanticsNode()
        val chevronLeft = chevron.positionInRoot.x
        val chevronRight = chevronLeft + chevron.size.width
        assertTrue("chevron has a visible width at x$scale", chevron.size.width > 0)
        assertTrue(
            "chevron $chevronLeft..$chevronRight must lie inside 0..${windowWidthPx()} at x$scale",
            chevronLeft >= -1f && chevronRight <= windowWidthPx() + 1f
        )
        listOf("label" to copy.title, "detail" to copy.detail).forEach { (label, text) ->
            val node = byText(text)
            val right = node.fetchSemanticsNode().let { it.positionInRoot.x + it.size.width }
            assertTrue("$label right edge $right must be at or left of the chevron $chevronLeft at x$scale", right <= chevronLeft + 1f)
            assertNoEllipsizedLine("$label at x$scale", node)
        }
        val row = tagged("outcome_sheet_batch_row").fetchSemanticsNode()
        assertTrue("row must stay inside the window width at x$scale", row.positionInRoot.x + row.size.width <= windowWidthPx() + 1f)
        return row.size.height
    }

    private fun assertLongBatchRowWraps(receipt: Boolean, copy: Copy) {
        fontScale = 1f
        setContent(receipt, copy, batchSuccessOf(copy))
        val normalHeight = assertBatchGeometry(copy, 1f)

        fontScale = 2f
        composeTestRule.waitForIdle()
        val largeHeight = assertBatchGeometry(copy, 2f)

        assertTrue("the row must grow taller at font scale 2.0 ($normalHeight -> $largeHeight)", largeHeight > normalHeight)
    }

    @Test
    fun receipt_germanBatchRow_longLabelAndFailureDetail_wrapInsideTheWindow_chevronStaysInside() =
        assertLongBatchRowWraps(true, german)

    @Test
    fun receipt_spanishBatchRow_longLabelAndFailureDetail_wrapInsideTheWindow_chevronStaysInside() =
        assertLongBatchRowWraps(true, spanish)

    @Test
    fun classic_germanBatchRow_longLabelAndFailureDetail_wrapInsideTheWindow_chevronStaysInside() =
        assertLongBatchRowWraps(false, german)

    @Test
    fun classic_spanishBatchRow_longLabelAndFailureDetail_wrapInsideTheWindow_chevronStaysInside() =
        assertLongBatchRowWraps(false, spanish)
}
