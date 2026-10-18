package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.click
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.ThemeMode
import io.github.ygaray.yahirandroidtaste.theme.YahirAndroidTasteTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Phase 29 gap plan 07 (Gate-1 SC3, D-03 [spanish-check], Robolectric half): a stacked card whose
 * content is taller than the space the deck gives it must stay inside the host and scroll its OWN
 * content, so the last message line is reachable. Spanish and German, 360dp wide, 360dp high host,
 * font scale 2.0. The earlier long-label tests ran at font scale 1.0 for the Undo and asserted only
 * horizontal bounds, so neither the vertical bound nor the shape under the text was ever checked.
 *
 * Heights and offsets are layout values read from the semantics nodes (size and position in the
 * root), never the clipped bounds, so a card that is cut by the deck's viewport is still seen as tall.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FeedbackCardTallContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Eintrag öffnen")
    private val germanMessage = "Eintrag Wocheneinkauf für das lange Wochenende wurde gelöscht"
    private val germanSecond = "Liste mit zwölf Einträgen, zuletzt gestern Abend geändert"
    private val germanUndo = "Löschung der gesamten Einkaufsliste rückgängig machen"
    private val spanishMessage = "La lista de la compra semanal se eliminó por completo del dispositivo"
    private val spanishSecond = "Lista con doce elementos, modificada por última vez ayer por la noche"
    private val spanishUndo = "Deshacer la eliminación de toda la lista"

    private lateinit var scope: CoroutineScope

    private class Fixture(val undo: String, val message: String, val second: String)

    private val spanish get() = Fixture(spanishUndo, spanishMessage, spanishSecond)
    private val german get() = Fixture(germanUndo, germanMessage, germanSecond)

    private fun setHost(
        state: FeedbackStackState,
        undoLabel: String,
        fontScale: Float = 2f,
        opened: MutableList<FeedbackTarget> = mutableListOf(),
        hostHeightDp: Int = HOST_HEIGHT_DP
    ) {
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    Box(Modifier.requiredHeight(hostHeightDp.dp)) {
                        FeedbackStackHost(
                            state = state,
                            onOpenTarget = { opened += it },
                            undoLabel = undoLabel
                        )
                    }
                }
            }
        }
    }

    private fun showTall(state: FeedbackStackState, f: Fixture, results: MutableMap<String, FeedbackResult>) {
        val event = FeedbackEvent.WithUndo(
            message = f.message,
            undoAction = {},
            duration = SnackbarDuration.Indefinite,
            target = target,
            supportingText = f.second
        )
        scope.launch { results[f.message] = state.show(event) }
        composeTestRule.waitForIdle()
    }

    private fun showNotify(state: FeedbackStackState, message: String) {
        val event = FeedbackEvent.Notify(message = message, duration = SnackbarDuration.Indefinite, target = null)
        scope.launch { state.show(event) }
        composeTestRule.waitForIdle()
    }

    private fun card(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG, useUnmergedTree = true)

    private fun text(value: String): SemanticsNodeInteraction =
        composeTestRule.onNodeWithText(value, useUnmergedTree = true)

    private val density get() = composeTestRule.density.density

    private fun SemanticsNodeInteraction.top(): Float = fetchSemanticsNode().positionInRoot.y

    private fun SemanticsNodeInteraction.height(): Float = fetchSemanticsNode().size.height.toFloat()

    private fun SemanticsNodeInteraction.bottom(): Float = top() + height()

    /** A point left of the middle and inside the visible part, clear of the Undo on the right. */
    private fun TouchInjectionScope.grip() = Offset(width * 0.3f, minOf(height * 0.5f, 120.dp.toPx()))

    private fun dragUp(fractionOfCard: Float = 0.8f) {
        card().performTouchInput {
            down(grip())
            val total = height * fractionOfCard
            repeat(STEPS) { moveBy(Offset(0f, -total / STEPS), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()
    }

    private fun flatten(node: SemanticsNode): List<SemanticsNode> =
        listOf(node) + node.children.flatMap { flatten(it) }

    private fun scrollRangesUnder(interaction: SemanticsNodeInteraction): List<Float> =
        flatten(interaction.fetchSemanticsNode()).mapNotNull {
            it.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange)?.maxValue?.invoke()
        }

    // ---- the clamp ----

    private fun assertClampedToTheHost(f: Fixture) {
        val state = FeedbackStackState()
        setHost(state, f.undo)
        showTall(state, f, mutableMapOf())

        val host = HOST_HEIGHT_DP * density
        assertTrue("card ${card().height()} px must not exceed the host $host px", card().height() <= host + 1f)
        assertTrue("card bottom ${card().bottom()} must be inside the host $host", card().bottom() <= host + 1f)
        assertTrue(
            "the content really is taller than the card: line bottom ${text(f.second).bottom()} card ${card().bottom()}",
            text(f.second).bottom() > card().bottom() + 1f
        )
        state.dismissAll()
    }

    @Test
    fun spanishTallCard_atFontScale2_isClampedToTheHostHeight() = assertClampedToTheHost(spanish)

    @Test
    fun germanTallCard_atFontScale2_isClampedToTheHostHeight() = assertClampedToTheHost(german)

    // ---- scrolling inside the card ----

    private fun assertLastLineScrollsIntoView(f: Fixture) {
        val state = FeedbackStackState()
        setHost(state, f.undo)
        showTall(state, f, mutableMapOf())
        val cardTop = card().top()
        val cardHeight = card().height()
        val host = HOST_HEIGHT_DP * density
        assertTrue(
            "the content must start taller than the card: line bottom ${text(f.second).bottom()} card ${card().bottom()}",
            text(f.second).bottom() > card().bottom() + 1f
        )

        var drags = 0
        while (text(f.second).bottom() > card().bottom() + 1f && drags < MAX_DRAGS) {
            dragUp()
            drags++
        }

        assertTrue(
            "last line bottom ${text(f.second).bottom()} must be inside the card bottom ${card().bottom()}",
            text(f.second).bottom() <= card().bottom() + 1f
        )
        assertTrue("last line top ${text(f.second).top()} must not leave the card top ${card().top()}", text(f.second).top() >= card().top() - 1f)
        assertTrue("last line ${text(f.second).bottom()} must be inside the host $host", text(f.second).bottom() <= host + 1f)
        assertEquals("the card did not move", cardTop, card().top(), 1f)
        assertEquals("the card did not change height", cardHeight, card().height(), 1f)
        state.dismissAll()
    }

    @Test
    fun spanishTallCard_verticalDrag_scrollsTheContentInsideTheCard_untilTheLastLineIsVisible() =
        assertLastLineScrollsIntoView(spanish)

    @Test
    fun germanTallCard_verticalDrag_scrollsTheContentInsideTheCard_untilTheLastLineIsVisible() =
        assertLastLineScrollsIntoView(german)

    // ---- gestures and semantics stay intact ----

    @Test
    fun tallCard_verticalDrag_doesNotDismissOrOpen() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, spanishUndo, opened = opened)
        showTall(state, spanish, results)

        dragUp(0.5f)

        assertTrue(results.isEmpty())
        assertTrue(opened.isEmpty())
        card().assertExists()
        state.dismissAll()
    }

    @Test
    fun tallCard_horizontalSwipe_stillDismisses() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, spanishUndo, opened = opened)
        showTall(state, spanish, results)

        card().performTouchInput {
            down(grip())
            repeat(50) { moveBy(Offset(-120.dp.toPx() / 50, 0f), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Dismissed, results[spanishMessage])
        assertTrue(opened.isEmpty())
    }

    @Test
    fun tallCard_tap_stillOpensTheTarget() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, spanishUndo, opened = opened)
        showTall(state, spanish, results)

        card().performTouchInput { click(grip()) }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Opened, results[spanishMessage])
        assertEquals(listOf(target), opened)
        state.dismissAll()
    }

    @Test
    fun tallCard_fingerDown_pausesTheDeck_andRelease_resumesIt() {
        val state = FeedbackStackState()
        setHost(state, spanishUndo)
        showTall(state, spanish, mutableMapOf())

        card().performTouchInput {
            down(grip())
            repeat(10) { moveBy(Offset(0f, -2.dp.toPx()), delayMillis = 16) }
        }
        composeTestRule.waitForIdle()
        assertTrue(state.pauseState.value.pointer)

        card().performTouchInput { up() }
        composeTestRule.waitForIdle()
        assertFalse(state.pauseState.value.pointer)
        state.dismissAll()
    }

    @Test
    fun tallCard_longHold_neitherOpensNorDismisses() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, spanishUndo, opened = opened)
        showTall(state, spanish, results)

        card().performTouchInput { down(grip()) }
        composeTestRule.mainClock.advanceTimeBy(1_000)
        card().performTouchInput { up() }
        composeTestRule.waitForIdle()

        assertTrue(results.isEmpty())
        assertTrue(opened.isEmpty())
        card().assertExists()
        state.dismissAll()
    }

    @Test
    fun tallCard_keepsItsSpokenNameAndActions() {
        val state = FeedbackStackState()
        setHost(state, spanishUndo)
        showTall(state, spanish, mutableMapOf())

        val config = composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).fetchSemanticsNode().config
        assertEquals(
            listOf("$spanishMessage, $spanishSecond"),
            config[SemanticsProperties.ContentDescription]
        )
        assertEquals(target.openLabel, config[SemanticsActions.OnClick].label)
        assertEquals(listOf(spanishUndo, "Dismiss"), config[SemanticsActions.CustomActions].map { it.label })
        assertNotNull(config.getOrNull(SemanticsActions.Dismiss))
        state.dismissAll()
    }

    // ---- the whole deck ----

    @Test
    fun threeCardDeck_withATallFront_fitsTheHost_andKeepsTheTwoPeekStrips() {
        val state = FeedbackStackState()
        setHost(state, spanishUndo)
        showNotify(state, "Alt A")
        showNotify(state, "Alt B")
        showTall(state, spanish, mutableMapOf())

        val nodes = composeTestRule.onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes().sortedBy { it.positionInRoot.y }
        assertEquals(3, nodes.size)
        val front = nodes.last()
        val limit = (HOST_HEIGHT_DP - 2 * PEEK_DP) * density
        assertTrue("front ${front.size.height} px must be at most $limit px", front.size.height <= limit + 1f)
        assertEquals(PEEK_DP * density, nodes[1].positionInRoot.y - nodes[0].positionInRoot.y, 1f)
        assertEquals(PEEK_DP * density, nodes[2].positionInRoot.y - nodes[1].positionInRoot.y, 1f)
        assertTrue(
            "the front content is taller than the front card",
            text(spanishSecond).bottom() > front.positionInRoot.y + front.size.height + 1f
        )
        state.dismissAll()
    }

    // ---- unchanged cases ----

    @Test
    fun shortCard_looksAsBefore_noScrollRange_andMinimumHeight() {
        val state = FeedbackStackState()
        setHost(state, "Undo", fontScale = 1f)
        showNotify(state, "Saved")

        assertEquals(56f * density, card().height(), 1f)
        val ranges = scrollRangesUnder(card())
        assertTrue("a short card exposes no scrollable range: $ranges", ranges.all { it == 0f })
        state.dismissAll()
    }

    @Test
    fun unboundedHost_tallCard_doesNotCrash_andIsNotClamped() {
        val state = FeedbackStackState()
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                YahirAndroidTasteTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        FeedbackStackHost(state = state, onOpenTarget = {}, undoLabel = spanishUndo)
                    }
                }
            }
        }
        showTall(state, spanish, mutableMapOf())

        assertTrue(
            "an unclamped card holds all its content: line ${text(spanishSecond).bottom()} card ${card().bottom()}",
            text(spanishSecond).bottom() <= card().bottom() + 1f
        )
        assertTrue("no scroll range sits under the card", scrollRangesUnder(card()).isEmpty())
        assertNull(card().fetchSemanticsNode().config.getOrNull(SemanticsProperties.VerticalScrollAxisRange))
        state.dismissAll()
    }

    private companion object {
        const val HOST_HEIGHT_DP = 360
        const val PEEK_DP = 14
        const val STEPS = 40
        const val MAX_DRAGS = 6
    }
}
