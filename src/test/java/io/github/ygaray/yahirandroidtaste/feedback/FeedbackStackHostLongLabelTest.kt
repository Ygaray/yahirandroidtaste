package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
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
 * Long German and Spanish strings on the feedback card at 360dp, with and without font scale 2.0
 * (the "test localized long strings before signoff" lesson): the message and second line wrap with
 * no line cap, a long Undo is capped and widened only to its longest word, and Undo and the chevron
 * never leave the card. Font scale 2.0 is set as in the segmented selector test, through
 * `LocalDensity provides Density(density.density, 2f)`. NATIVE graphics gives real font metrics.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FeedbackStackHostLongLabelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Eintrag öffnen")
    private val germanMessage = "Eintrag Wocheneinkauf für das lange Wochenende wurde gelöscht"
    private val germanSecond = "Liste mit zwölf Einträgen, zuletzt gestern Abend geändert"
    private val spanishUndo = "Deshacer la eliminación de toda la lista"
    private val spanishMessage = "La lista de la compra semanal se eliminó por completo del dispositivo"

    private lateinit var scope: CoroutineScope

    private fun setHost(
        state: FeedbackStackState,
        undoLabel: String = "Undo",
        fontScale: Float = 2f,
        direction: LayoutDirection = LayoutDirection.Ltr
    ) {
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale),
                LocalLayoutDirection provides direction
            ) {
                YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    FeedbackStackHost(
                        state = state,
                        onOpenTarget = {},
                        undoLabel = undoLabel
                    )
                }
            }
        }
    }

    private fun withUndo(
        message: String,
        withTarget: Boolean = true,
        supportingText: String? = null
    ) = FeedbackEvent.WithUndo(
        message = message,
        undoAction = {},
        duration = SnackbarDuration.Indefinite,
        target = if (withTarget) target else null,
        supportingText = supportingText
    )

    private fun show(state: FeedbackStackState, event: FeedbackEvent.WithUndo) {
        scope.launch { state.show(event) }
        composeTestRule.waitForIdle()
    }

    private fun tagged(tag: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun text(value: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(value, useUnmergedTree = true)

    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        val action = fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action
        checkNotNull(action) { "node has no GetTextLayoutResult action" }.invoke(results)
        return results.single()
    }

    private class Bounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        val width: Float get() = right - left
    }

    private fun SemanticsNodeInteraction.box(): Bounds {
        val node = fetchSemanticsNode()
        val left = node.positionInRoot.x
        val top = node.positionInRoot.y
        return Bounds(left, top, left + node.size.width, top + node.size.height)
    }

    private fun assertInside(inner: Bounds, outer: Bounds, label: String) {
        assertTrue(
            "$label ${inner.left}..${inner.right} must lie inside ${outer.left}..${outer.right}",
            inner.left >= outer.left - 1f && inner.right <= outer.right + 1f
        )
        assertTrue("$label must have a visible width", inner.width > 0f)
    }

    private fun windowWidthPx(): Float = 360f * composeTestRule.density.density

    @Test
    fun germanMessageAndSecondLine_wrapInsideCard_atFontScale2() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Rückgängig")
        show(state, withUndo(germanMessage, supportingText = germanSecond))

        val message = text(germanMessage).textLayout()
        val second = text(germanSecond).textLayout()
        assertTrue("message wrapped to ${message.lineCount} lines", message.lineCount >= 2)
        assertFalse(message.didOverflowHeight || message.didOverflowWidth)
        assertTrue("second line wrapped to ${second.lineCount} lines", second.lineCount >= 2)
        assertFalse(second.didOverflowHeight || second.didOverflowWidth)
        assertInside(tagged(FEEDBACK_STACK_ITEM_TEST_TAG).box(), Bounds(0f, 0f, windowWidthPx(), 0f), "card")
        state.dismissAll()
    }

    @Test
    fun germanUndoWord_doesNotBreakMidWord() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Rückgängig")
        show(state, withUndo(germanMessage, supportingText = germanSecond))

        assertEquals(1, text("Rückgängig").textLayout().lineCount)
        state.dismissAll()
    }

    @Test
    fun spanishLongUndoLabel_isCapped_andBodyKeepsTheMajority() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = spanishUndo, fontScale = 1f)
        show(state, withUndo(spanishMessage, withTarget = false))

        val card = tagged(FEEDBACK_STACK_ITEM_TEST_TAG).box()
        val undo = tagged(FEEDBACK_STACK_UNDO_TEST_TAG).box()
        val body = text(spanishMessage).box()
        assertTrue("Undo ${undo.width} must be at most 60% of ${card.width}", undo.width <= card.width * 0.6f + 1f)
        assertTrue("body ${body.width} must be wider than Undo ${undo.width}", body.width > undo.width)
        assertTrue("the long label must wrap", text(spanishUndo).textLayout().lineCount >= 2)
        state.dismissAll()
    }

    private fun assertUndoAndChevronInsideCard(undoLabel: String, message: String) {
        val state = FeedbackStackState()
        setHost(state, undoLabel = undoLabel)
        show(state, withUndo(message, supportingText = germanSecond))

        val card = tagged(FEEDBACK_STACK_ITEM_TEST_TAG).box()
        assertInside(tagged(FEEDBACK_STACK_UNDO_TEST_TAG).box(), card, "Undo '$undoLabel'")
        assertInside(tagged(FEEDBACK_STACK_CHEVRON_TEST_TAG).box(), card, "chevron '$undoLabel'")
        state.dismissAll()
    }

    @Test
    fun undoAndChevronStayInsideCard_german() =
        assertUndoAndChevronInsideCard("Rückgängig", germanMessage)

    @Test
    fun undoAndChevronStayInsideCard_spanish() =
        assertUndoAndChevronInsideCard(spanishUndo, spanishMessage)

    @Test
    fun rtlLayout_mirrorsRow_andKeepsEverythingInside() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "בטל", direction = LayoutDirection.Rtl)
        show(state, withUndo("הפריט נשמר בהצלחה ברשימת הקניות השבועית"))

        val card = tagged(FEEDBACK_STACK_ITEM_TEST_TAG).box()
        val chevron = tagged(FEEDBACK_STACK_CHEVRON_TEST_TAG).box()
        val undo = tagged(FEEDBACK_STACK_UNDO_TEST_TAG).box()
        val dot = tagged(FEEDBACK_STACK_DOT_TEST_TAG).box()
        assertTrue("chevron ${chevron.left} left of Undo ${undo.left}", chevron.left < undo.left)
        assertTrue("Undo ${undo.left} left of the dot ${dot.left}", undo.left < dot.left)
        assertInside(chevron, card, "chevron")
        assertInside(undo, card, "Undo")
        assertInside(dot, card, "dot")
        state.dismissAll()
    }

    @Test
    fun backCardKeepsFrontHeight_whenOlderCardIsLongerText() {
        val state = FeedbackStackState()
        setHost(state, undoLabel = "Rückgängig")
        show(state, withUndo(germanMessage, supportingText = germanSecond))
        show(state, withUndo("Kurz"))

        val heights = composeTestRule.onAllNodesWithTagHeights()
        assertEquals(2, heights.size)
        assertEquals("collapsed cards share the front card height: $heights", heights[0], heights[1])
        state.dismissAll()
    }

    private fun ComposeContentTestRule.onAllNodesWithTagHeights(): List<Int> =
        onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes().map { it.size.height }
}
