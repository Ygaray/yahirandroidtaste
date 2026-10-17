package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.click
import androidx.compose.ui.test.down
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric swipe tests for [FeedbackStackHost] (phase 26, plan 04, FSTK-05): a horizontal swipe
 * removes exactly the swiped card with [FeedbackResult.Dismissed] and never undoes anything.
 *
 * Drags are slow, 16 ms apart, because a release with exactly zero velocity picks the closest anchor
 * and ignores the 72dp threshold (probe A3b). Every event is Indefinite: the card timer runs on the
 * `show()` caller's dispatcher, not on the Compose clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackStackHostSwipeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Open item")

    private lateinit var scope: CoroutineScope

    private fun setHost(
        state: FeedbackStackState,
        opened: MutableList<FeedbackTarget> = mutableListOf()
    ) {
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            FeedbackStackHost(state = state, onOpenTarget = { opened += it })
        }
    }

    private fun notify(message: String, withTarget: Boolean = true) = FeedbackEvent.Notify(
        message = message,
        duration = SnackbarDuration.Indefinite,
        target = if (withTarget) target else null
    )

    private fun withUndo(message: String, undoAction: suspend () -> Unit = {}) = FeedbackEvent.WithUndo(
        message = message,
        undoAction = undoAction,
        duration = SnackbarDuration.Indefinite,
        target = target
    )

    private fun show(
        state: FeedbackStackState,
        event: FeedbackEvent,
        key: String,
        results: MutableMap<String, FeedbackResult>
    ) {
        scope.launch { results[key] = state.show(event) }
        composeTestRule.waitForIdle()
    }

    private fun showAll(
        state: FeedbackStackState,
        results: MutableMap<String, FeedbackResult>,
        vararg messages: String
    ) {
        messages.forEach { message -> show(state, notify(message), message, results) }
    }

    private fun card(message: String): SemanticsNodeInteraction = composeTestRule.onNode(
        hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG) and hasAnyDescendant(hasText(message)),
        useUnmergedTree = true
    )

    private fun cardCount(): Int =
        composeTestRule.onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes().size

    /** A point left of the middle, clear of the Undo button on the right of the row. */
    private fun TouchInjectionScope.grip() = Offset(width * 0.3f, height * 0.5f)

    /** Presses, moves [dxDp] in 16 ms steps (so the release carries a little velocity) and lifts. */
    private fun TouchInjectionScope.slowSwipe(dxDp: Float, steps: Int = 50) {
        down(grip())
        repeat(steps) { moveBy(Offset(dxDp.dp.toPx() / steps, 0f), delayMillis = 16) }
        up()
    }

    private fun swipe(message: String, dxDp: Float) {
        card(message).performTouchInput { slowSwipe(dxDp) }
        composeTestRule.waitForIdle()
    }

    /** Pitfall 3: a centre click lands on the front card, so click inside the 14dp strip. */
    private fun tapStrip(message: String) {
        card(message).performTouchInput { click(Offset(width * 0.5f, 4.dp.toPx())) }
        composeTestRule.waitForIdle()
    }

    private fun textX(message: String): Float =
        composeTestRule.onNodeWithText(message, useUnmergedTree = true)
            .fetchSemanticsNode().positionInRoot.x

    private fun textY(message: String): Float =
        composeTestRule.onNodeWithText(message, useUnmergedTree = true)
            .fetchSemanticsNode().positionInRoot.y

    // ---- tracer ----

    @Test
    fun swipeLeft_onFrontCard_dismissesOnlyThatCard() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        showAll(state, results, "First", "Second", "Third")
        assertEquals(3, cardCount())

        swipe("Third", -120f)

        assertEquals(FeedbackResult.Dismissed, results["Third"])
        assertNull(results["First"])
        assertNull(results["Second"])
        assertTrue(opened.isEmpty())
        assertEquals(2, cardCount())
        card("First").assertExists()
        card("Second").assertExists()
        assertEquals(listOf("Second", "First"), state.items.map { it.message })
        state.dismissAll()
    }

    // ---- direction, undo, enablement ----

    @Test
    fun swipeRight_dismissesToo() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "First", "Second", "Third")

        swipe("Third", 120f)

        assertEquals(FeedbackResult.Dismissed, results["Third"])
        assertNull(results["First"])
        assertNull(results["Second"])
        assertEquals(2, cardCount())
        state.dismissAll()
    }

    @Test
    fun swipe_neverRunsUndo_andTrackedEntryStaysAvailable() {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        var undoRuns = 0
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        scope.launch {
            results["Deleted"] = state.emitTrackedWithUndo(store, withUndo("Deleted") { undoRuns++ })
        }
        composeTestRule.waitForIdle()

        swipe("Deleted", -120f)

        assertEquals(FeedbackResult.Dismissed, results["Deleted"])
        assertEquals(0, undoRuns)
        assertEquals(UndoStatus.Available, store.entries.value.single().status)
    }

    @Test
    fun collapsedBackCardStrip_swipeDoesNothing() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "Back", "Front")

        card("Back").performTouchInput {
            down(Offset(width * 0.5f, 4.dp.toPx()))
            repeat(50) { moveBy(Offset(120.dp.toPx() / 50, 0f), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()

        assertTrue(results.isEmpty())
        assertEquals(2, cardCount())
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun fannedDeck_everyCardIsSwipeable() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "First", "Second", "Third")
        tapStrip("First")
        assertTrue(state.pauseState.value.fanned)

        swipe("First", -120f)

        assertEquals(FeedbackResult.Dismissed, results["First"])
        assertNull(results["Second"])
        assertNull(results["Third"])
        assertEquals(2, cardCount())
        state.dismissAll()
    }

    // ---- thresholds ----

    @Test
    fun belowThreshold_returnsToSettled_andCardStays() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "Only")
        val restX = textX("Only")

        card("Only").performTouchInput {
            down(grip())
            repeat(50) { moveBy(Offset(50.dp.toPx() / 50, 0f), delayMillis = 16) }
        }
        composeTestRule.waitForIdle()
        // Mid drag the content has really moved: the translation is on the card and not lost.
        assertTrue(textX("Only") > restX + 10f)
        card("Only").performTouchInput { up() }
        composeTestRule.waitForIdle()

        assertTrue(results.isEmpty())
        assertEquals(1, cardCount())
        assertEquals(restX, textX("Only"), 0.5f)
        state.dismissAll()
    }

    @Test
    fun aboveThreshold_slowDrag_dismisses() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "Only")

        swipe("Only", -90f)

        assertEquals(FeedbackResult.Dismissed, results["Only"])
    }

    @Test
    fun fastFlickBelowThreshold_dismisses() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "Only")

        // Probe A3c: 8 moves of 4dp every 16 ms is 32dp in total, far below 72dp, but fast enough.
        card("Only").performTouchInput {
            down(grip())
            repeat(8) { moveBy(Offset(-4.dp.toPx(), 0f), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Dismissed, results["Only"])
    }

    // ---- vertical drags and scrolling ----

    @Test
    fun verticalDrag_doesNotDismiss() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        showAll(state, results, "Back", "Front")

        card("Front").performTouchInput {
            down(grip())
            repeat(50) { moveBy(Offset(0f, -80.dp.toPx() / 50), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()

        assertTrue(results.isEmpty())
        assertTrue(opened.isEmpty())
        assertEquals(2, cardCount())
        state.dismissAll()
    }

    @Test
    fun fannedColumnScroll_doesNotDismiss_andHorizontalSwipeDoesNotScroll() {
        val state = FeedbackStackState()
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            // 150dp available gives the fanned deck a 90dp cap, so three 56dp cards must scroll.
            Box(Modifier.requiredHeight(150.dp)) {
                FeedbackStackHost(state = state, onOpenTarget = { })
            }
        }
        val results = mutableMapOf<String, FeedbackResult>()
        showAll(state, results, "First", "Second", "Third")
        tapStrip("First")
        assertTrue(state.pauseState.value.fanned)

        // A horizontal move on a card must not scroll the column: the card keeps its vertical place.
        val before = textY("Third")
        card("Third").performTouchInput {
            down(grip())
            repeat(20) { moveBy(Offset(20.dp.toPx() / 20, 0f), delayMillis = 16) }
        }
        composeTestRule.waitForIdle()
        assertEquals(before, textY("Third"), 0.5f)
        card("Third").performTouchInput { up() }
        composeTestRule.waitForIdle()
        assertTrue(results.isEmpty())

        // A vertical drag scrolls the column and dismisses nothing.
        card("Third").performTouchInput {
            down(grip())
            repeat(40) { moveBy(Offset(0f, -40.dp.toPx() / 40), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()
        assertTrue(results.isEmpty())
        assertEquals(3, cardCount())
        state.dismissAll()
    }

    // ---- concurrent changes ----

    @Test
    fun arrivalMidSwipe_swipeStaysOnTheSameCard() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "A", "B")

        card("B").performTouchInput {
            down(grip())
            repeat(25) { moveBy(Offset(-60.dp.toPx() / 25, 0f), delayMillis = 16) }
        }
        composeTestRule.waitForIdle()
        // A newer card arrives with the finger still down: B drops to the back of the collapsed deck.
        show(state, notify("C"), "C", results)
        card("B").performTouchInput {
            repeat(25) { moveBy(Offset(-60.dp.toPx() / 25, 0f), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Dismissed, results["B"])
        assertNull(results["A"])
        assertNull(results["C"])
        assertEquals(listOf("C", "A"), state.items.map { it.message })
        state.dismissAll()
    }

    @Test
    fun swipePausesTheDeck_andReleaseClears() {
        val state = FeedbackStackState()
        setHost(state)
        showAll(state, mutableMapOf(), "Only")

        card("Only").performTouchInput {
            down(grip())
            repeat(25) { moveBy(Offset(-60.dp.toPx() / 25, 0f), delayMillis = 16) }
        }
        composeTestRule.waitForIdle()
        assertTrue(state.pauseState.value.pointer)

        card("Only").performTouchInput {
            repeat(25) { moveBy(Offset(-60.dp.toPx() / 25, 0f), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()
        assertFalse(state.pauseState.value.pointer)
    }

    @Test
    fun swipedCard_leavesTheTree_afterSettle() {
        val state = FeedbackStackState()
        setHost(state)
        showAll(state, mutableMapOf(), "Gone", "Stays")

        swipe("Stays", 120f)

        card("Stays").assertDoesNotExist()
        card("Gone").assertExists()
        state.dismissAll()
    }

    @Test
    fun swipedCard_resolvesDismissedExactlyOnce_whenTheTimerRaces() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "Raced")

        card("Raced").performTouchInput {
            down(grip())
            repeat(50) { moveBy(Offset(-120.dp.toPx() / 50, 0f), delayMillis = 16) }
        }
        composeTestRule.waitForIdle()
        // The timer wins while the finger is still down, after the drag crossed the threshold.
        val item = composeTestRule.runOnIdle { state.items.single() }
        assertTrue(composeTestRule.runOnIdle { state.resolve(item, FeedbackResult.TimedOut) })
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().performTouchInput { up() }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.TimedOut, results["Raced"])
        assertEquals(0, cardCount())
        assertFalse(composeTestRule.runOnIdle { state.resolve(item, FeedbackResult.Dismissed) })
        assertFalse(state.pauseState.value.pointer)
    }

    @Test
    fun firstFrame_withUnmeasuredAnchors_neverThrows() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        val early = CoroutineScope(Dispatchers.Unconfined)
        try {
            early.launch { results["Early"] = state.show(notify("Early")) }
            assertEquals(1, state.items.size)

            composeTestRule.mainClock.autoAdvance = false
            composeTestRule.setContent {
                scope = rememberCoroutineScope()
                FeedbackStackHost(state = state, onOpenTarget = { })
            }
            repeat(5) { composeTestRule.mainClock.advanceTimeByFrame() }
            composeTestRule.mainClock.autoAdvance = true
            composeTestRule.waitForIdle()

            card("Early").assertExists()
            assertTrue(results.isEmpty())
        } finally {
            // Resolve the card and stop the scope even when an assertion above failed, so the
            // pending show() coroutine cannot leak into a later test.
            state.dismissAll()
            early.cancel()
        }
    }

    @Test
    fun secondFingerDown_doesNotBreakTheSwipe() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "First", "Second")

        card("Second").performTouchInput {
            down(0, grip())
            repeat(20) { moveBy(0, Offset(-5.dp.toPx(), 0f), delayMillis = 16) }
            down(1, Offset(width * 0.7f, height * 0.5f))
            repeat(30) { moveBy(0, Offset(-3.dp.toPx(), 0f), delayMillis = 16) }
            up(1)
            up(0)
        }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Dismissed, results["Second"])
        assertNull(results["First"])
        assertEquals(1, cardCount())
        state.dismissAll()
    }

    // ---- the fade helper ----

    @Test
    fun swipeAlpha_isOneAtRest() {
        assertEquals(1f, feedbackSwipeAlpha(0f, 300f), 0f)
    }

    @Test
    fun swipeAlpha_fallsWithDisplacement() {
        assertEquals(0.6f, feedbackSwipeAlpha(150f, 300f), 0.001f)
        assertEquals(0.6f, feedbackSwipeAlpha(-150f, 300f), 0.001f)
        assertTrue(feedbackSwipeAlpha(100f, 300f) > feedbackSwipeAlpha(200f, 300f))
    }

    @Test
    fun swipeAlpha_isFlooredAtTwoTenths() {
        assertEquals(0.2f, feedbackSwipeAlpha(300f, 300f), 0.001f)
        assertEquals(0.2f, feedbackSwipeAlpha(900f, 300f), 0.001f)
        assertEquals(0.2f, feedbackSwipeAlpha(-900f, 300f), 0.001f)
    }

    @Test
    fun swipeAlpha_ignoresNaNAndZeroWidth() {
        assertEquals(1f, feedbackSwipeAlpha(100f, 0f), 0f)
        assertEquals(1f, feedbackSwipeAlpha(100f, -5f), 0f)
        assertEquals(1f, feedbackSwipeAlpha(100f, Float.NaN), 0f)
        assertEquals(1f, feedbackSwipeAlpha(Float.NaN, 300f), 0f)
    }
}
