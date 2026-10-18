package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.CappedPillShape
import io.github.ygaray.yahirandroidtaste.component.SourceContractTestSupport
import io.github.ygaray.yahirandroidtaste.component.rememberTextButtonCappedShape
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
 * Phase 29 gap plan 07 (Gate-1 SC3, D-03 [spanish-check]): the stacked card's Undo is a text button
 * whose fully rounded ends cut a wrapped label (Spanish, 360dp, font scale 2.0). Its shape keeps the
 * handled-by pill rule: a capsule on one line, a capped radius once it wraps.
 *
 * The semantics tree cannot show a shape, so the geometry is checked on the outline the production
 * accessor builds against the measured text box, with a CircleShape control that must FAIL the same
 * corner check, and a source contract proves the card uses the internal capped button while the
 * public button keeps its default shape.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FeedbackCardUndoShapeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val spanishUndo = "Deshacer la eliminación de toda la lista"
    private val spanishMessage = "La lista de la compra semanal se eliminó por completo del dispositivo"
    private val germanUndo = "Löschung der gesamten Einkaufsliste rückgängig machen"
    private val germanMessage = "Eintrag Wocheneinkauf für das lange Wochenende wurde gelöscht"

    private lateinit var scope: CoroutineScope

    private class Measured(
        val shape: Shape,
        val density: Density,
        val button: Bounds,
        val label: Bounds,
        val card: Bounds,
        val labelLines: Int
    ) {
        fun labelCorners(): List<Offset> {
            val left = label.left - button.left
            val top = label.top - button.top
            val right = left + label.width
            val bottom = top + label.height
            return listOf(Offset(left, top), Offset(right, top), Offset(left, bottom), Offset(right, bottom))
        }
    }

    private class Bounds(val left: Float, val top: Float, val width: Float, val height: Float) {
        val right: Float get() = left + width
        val bottom: Float get() = top + height
    }

    private fun SemanticsNodeInteraction.bounds(): Bounds {
        val node = fetchSemanticsNode()
        return Bounds(node.positionInRoot.x, node.positionInRoot.y, node.size.width.toFloat(), node.size.height.toFloat())
    }

    private fun SemanticsNodeInteraction.lineCount(): Int {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single().lineCount
    }

    private fun measure(undo: String, message: String, fontScale: Float): Measured {
        var shape: Shape? = null
        var scaled: Density? = null
        val state = FeedbackStackState()
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            val base = LocalDensity.current
            val density = Density(base.density, fontScale)
            CompositionLocalProvider(LocalDensity provides density) {
                YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    shape = rememberTextButtonCappedShape()
                    scaled = density
                    FeedbackStackHost(state = state, onOpenTarget = {}, undoLabel = undo)
                }
            }
        }
        val event = FeedbackEvent.WithUndo(
            message = message,
            undoAction = {},
            duration = SnackbarDuration.Indefinite,
            target = FeedbackTarget("k", "Open"),
            supportingText = null
        )
        scope.launch { state.show(event) }
        composeTestRule.waitForIdle()

        val label = composeTestRule.onNodeWithText(undo, useUnmergedTree = true)
        val measured = Measured(
            shape = checkNotNull(shape),
            density = checkNotNull(scaled),
            button = composeTestRule.onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true).bounds(),
            label = label.bounds(),
            card = composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG, useUnmergedTree = true).bounds(),
            labelLines = label.lineCount()
        )
        state.dismissAll()
        return measured
    }

    private fun Outline.requireRounded() = this as? Outline.Rounded
        ?: error("expected Outline.Rounded but was $this")

    private fun Measured.size() = Size(button.width, button.height)

    private fun assertWrappedAndTallEnough(m: Measured) {
        assertTrue("the label must wrap, had ${m.labelLines} lines", m.labelLines >= 2)
        val minTouch = 48f * m.density.density
        assertTrue("button ${m.button.height} must be at least 48dp ($minTouch px) so the minimum touch size does not inflate it", m.button.height >= minTouch)
    }

    private fun assertCornersInsideProductionShape(m: Measured) {
        assertWrappedAndTallEnough(m)
        val outline = m.shape.createOutline(m.size(), LayoutDirection.Ltr, m.density).requireRounded()
        val outside = m.labelCorners().filterNot { outline.roundRect.contains(it) }
        assertTrue("every text corner must lie inside the painted shape, outside: $outside", outside.isEmpty())
    }

    private fun assertCircleControlLosesACorner(m: Measured) {
        assertWrappedAndTallEnough(m)
        val outline = CircleShape.createOutline(m.size(), LayoutDirection.Ltr, m.density).requireRounded()
        val outside = m.labelCorners().filterNot { outline.roundRect.contains(it) }
        assertTrue("the CircleShape control must lose at least one corner, so the check is not vacuous", outside.isNotEmpty())
    }

    private fun assertLabelInsideButtonInsideCard(m: Measured) {
        assertTrue(
            "label ${m.label.left}..${m.label.right} x ${m.label.top}..${m.label.bottom} inside button " +
                "${m.button.left}..${m.button.right} x ${m.button.top}..${m.button.bottom}",
            m.label.left >= m.button.left - 1f && m.label.right <= m.button.right + 1f &&
                m.label.top >= m.button.top - 1f && m.label.bottom <= m.button.bottom + 1f
        )
        assertTrue(
            "button inside the card",
            m.button.left >= m.card.left - 1f && m.button.right <= m.card.right + 1f &&
                m.button.top >= m.card.top - 1f && m.button.bottom <= m.card.bottom + 1f
        )
    }

    @Test
    fun wrappedSpanishUndo_atFontScale2_keepsEveryTextCornerInsideTheShape() =
        assertCornersInsideProductionShape(measure(spanishUndo, spanishMessage, 2f))

    @Test
    fun wrappedGermanUndo_atFontScale2_keepsEveryTextCornerInsideTheShape() =
        assertCornersInsideProductionShape(measure(germanUndo, germanMessage, 2f))

    @Test
    fun controlCircleShape_failsTheSameCornerCheck_inSpanish() =
        assertCircleControlLosesACorner(measure(spanishUndo, spanishMessage, 2f))

    @Test
    fun controlCircleShape_failsTheSameCornerCheck_inGerman() =
        assertCircleControlLosesACorner(measure(germanUndo, germanMessage, 2f))

    @Test
    fun spanishUndoText_fitsInsideTheButtonBounds_atFontScale2() =
        assertLabelInsideButtonInsideCard(measure(spanishUndo, spanishMessage, 2f))

    @Test
    fun germanUndoText_fitsInsideTheButtonBounds_atFontScale2() =
        assertLabelInsideButtonInsideCard(measure(germanUndo, germanMessage, 2f))

    private fun assertSingleLineIsACapsule(fontScale: Float) {
        val m = measure("Undo", "Saved", fontScale)
        assertEquals("a short Undo renders on one line", 1, m.labelLines)
        val density = m.density
        val painted = maxOf(with(density) { 40.dp.toPx() }, m.label.height + 2 * with(density) { 8.dp.toPx() })
        val size = Size(m.button.width, painted)
        val capped = m.shape.createOutline(size, LayoutDirection.Ltr, density).requireRounded()
        val circle = CircleShape.createOutline(size, LayoutDirection.Ltr, density).requireRounded()
        assertEquals(painted / 2f, capped.roundRect.topLeftCornerRadius.x, 0.5f)
        assertEquals(circle.roundRect.topLeftCornerRadius.x, capped.roundRect.topLeftCornerRadius.x, 0.5f)
        assertTrue("the card uses the capped shape", m.shape is CappedPillShape)
    }

    @Test
    fun singleLineUndo_isStillAPill_atFontScale1() = assertSingleLineIsACapsule(1f)

    @Test
    fun singleLineUndo_isStillAPill_atFontScale1_5() = assertSingleLineIsACapsule(1.5f)

    @Test
    fun singleLineUndo_isStillAPill_atFontScale2() = assertSingleLineIsACapsule(2f)

    @Test
    fun feedbackCardUndoUsesTheCappedPillButton_andThePublicButtonKeepsItsDefaultShape() {
        fun strip(path: String) = SourceContractTestSupport.stripComments(File(path).readText())
        val component = "src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt"
        val card = strip("src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt")
        val button = strip(component)
        val row = SourceContractTestSupport.functionBody(card, "fun FeedbackCardRow(").filterNot { it.isWhitespace() }
        assertTrue("the card Undo must use CappedPillTextActionButton", row.contains("CappedPillTextActionButton("))
        assertFalse("the card must not call the public DynamicActionButton", row.contains("DynamicActionButton("))
        val capped = SourceContractTestSupport.functionBody(button, "fun CappedPillTextActionButton(")
            .filterNot { it.isWhitespace() }
        assertTrue("the capped button must take the capped shape", capped.contains("rememberTextButtonCappedShape()"))
        val public = SourceContractTestSupport.functionBody(button, "fun DynamicActionButton(")
            .filterNot { it.isWhitespace() }
        assertFalse(public.contains("CappedPill"))
        assertFalse(public.contains("rememberTextButtonCappedShape"))
    }

    /**
     * The measured tests take the shape from their own `rememberTextButtonCappedShape()` call, so they
     * cannot see whether the card's button really paints it. This pins the two hand-offs that make the
     * fix work: the capped button passes the capped shape into `EmphasisActionButton`, and that function
     * forwards `shape` into the `TextButton` of its Text branch (not back to `ButtonDefaults.textShape`).
     */
    @Test
    fun theCappedShapeReachesTheTextButton_throughEmphasisActionButton() {
        val button = SourceContractTestSupport.stripComments(
            File("src/main/java/io/github/ygaray/yahirandroidtaste/component/DynamicActionButton.kt").readText()
        )
        val capped = SourceContractTestSupport.functionBody(button, "fun CappedPillTextActionButton(")
            .filterNot { it.isWhitespace() }
        assertTrue("the capped button must call EmphasisActionButton", capped.contains("EmphasisActionButton("))
        assertTrue(
            "the capped button must hand the capped shape to EmphasisActionButton",
            capped.contains("shape=rememberTextButtonCappedShape()")
        )
        assertTrue("the capped button is the Text emphasis", capped.contains("emphasis=ActionButtonEmphasis.Text,"))
        val emphasis = SourceContractTestSupport.functionBody(button, "fun EmphasisActionButton(")
            .filterNot { it.isWhitespace() }
        val textBranchStart = emphasis.indexOf("ActionButtonEmphasis.Text->TextButton(")
        val textSmallStart = emphasis.indexOf("ActionButtonEmphasis.TextSmall->")
        assertTrue("the Text branch must exist before the TextSmall branch", textBranchStart in 0 until textSmallStart)
        val textBranch = emphasis.substring(textBranchStart, textSmallStart)
        assertTrue(
            "the Text branch must forward its shape to the TextButton",
            textBranch.contains("shape=shape?:ButtonDefaults.textShape")
        )
        val card = SourceContractTestSupport.stripComments(
            File("src/main/java/io/github/ygaray/yahirandroidtaste/feedback/FeedbackCard.kt").readText()
        )
        assertTrue(
            "the card Undo is the capped variant",
            SourceContractTestSupport.functionBody(card, "fun FeedbackCardRow(").contains("CappedPillTextActionButton(")
        )
    }
}
