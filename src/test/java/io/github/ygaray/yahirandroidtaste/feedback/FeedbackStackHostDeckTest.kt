package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.center
import androidx.compose.ui.test.click
import androidx.compose.ui.test.down
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import kotlinx.coroutines.CoroutineScope
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
 * Robolectric gesture tests for the deck wiring of [FeedbackStackHost] (phase 26, plan 03): the
 * single-owner tap gesture, the peek-strip fan, long-hold suppression, the deck-wide pause hook and
 * the idle collapse. Every event is [SnackbarDuration.Indefinite] because the card timer runs on
 * the `show()` caller's dispatcher, not on the Compose clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackStackHostDeckTest {

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

    /** Shows targeted [messages] oldest first, recording each card's result by message. */
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

    private fun deck(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_DECK_TEST_TAG, useUnmergedTree = true)

    private fun bodyPoint(width: Int, height: Int) = Offset(width * 0.1f, height * 0.5f)

    private fun tapBody(message: String) {
        card(message).performTouchInput { click(bodyPoint(width, height)) }
        composeTestRule.waitForIdle()
    }

    /** Pitfall 3: a centre click lands on the front card, so click inside the 14dp strip. */
    private fun tapStrip(message: String) {
        card(message).performTouchInput { click(Offset(width * 0.5f, 4.dp.toPx())) }
        composeTestRule.waitForIdle()
    }

    private fun flatten(node: SemanticsNode): List<SemanticsNode> =
        listOf(node) + node.children.flatMap { flatten(it) }

    @Test
    fun peekTap_fansDeck_neverOpens_andPausesTimers() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        showAll(state, results, "First", "Second", "Third")
        assertFalse(state.pauseState.value.fanned)

        tapStrip("First")

        assertTrue(opened.isEmpty())
        assertTrue(results.isEmpty())
        assertTrue(state.pauseState.value.fanned)

        val first = card("First").getUnclippedBoundsInRoot()
        val second = card("Second").getUnclippedBoundsInRoot()
        val third = card("Third").getUnclippedBoundsInRoot()
        assertTrue((second.top - first.top) >= first.height)
        assertTrue((third.top - second.top) >= second.height)
        // The deck is bottom-anchored: the newest card stays on the deck's bottom edge when it grows.
        assertEquals(deck().getUnclippedBoundsInRoot().bottom.value, third.bottom.value, 1f)
        state.dismissAll()
    }

    @Test
    fun longHold_thenRelease_doesNotOpen_andDoesNotFan() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        showAll(state, results, "Back", "Front")

        var timeout = 0L
        card("Front").performTouchInput {
            timeout = viewConfiguration.longPressTimeoutMillis
            down(bodyPoint(width, height))
        }
        composeTestRule.mainClock.advanceTimeBy(timeout + 100)
        card("Front").performTouchInput { up() }
        composeTestRule.waitForIdle()

        assertTrue(opened.isEmpty())
        assertTrue(results.isEmpty())
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun pointerDown_pausesWhileHeld_longHoldIncluded() {
        val state = FeedbackStackState()
        setHost(state)
        showAll(state, mutableMapOf(), "Held")

        var timeout = 0L
        card("Held").performTouchInput {
            timeout = viewConfiguration.longPressTimeoutMillis
            down(bodyPoint(width, height))
        }
        composeTestRule.mainClock.advanceTimeBy(timeout + 100)
        composeTestRule.waitForIdle()
        assertTrue(state.pauseState.value.pointer)

        card("Held").performTouchInput { up() }
        composeTestRule.waitForIdle()
        assertFalse(state.pauseState.value.pointer)
        state.dismissAll()
    }

    @Test
    fun dragPastSlop_thenRelease_doesNotOpen() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        showAll(state, results, "Back", "Front")

        card("Front").performTouchInput {
            down(bodyPoint(width, height))
            moveBy(Offset(viewConfiguration.touchSlop * 4f, 0f))
            up()
        }
        composeTestRule.waitForIdle()

        assertTrue(opened.isEmpty())
        assertTrue(results.isEmpty())
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun dragOnPeekStrip_doesNotFan() {
        val state = FeedbackStackState()
        setHost(state)
        showAll(state, mutableMapOf(), "Back", "Front")

        card("Back").performTouchInput {
            down(Offset(width * 0.5f, 4.dp.toPx()))
            moveBy(Offset(viewConfiguration.touchSlop * 4f, 0f))
            up()
        }
        composeTestRule.waitForIdle()

        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun pointerDown_setsPause_releaseClears() {
        val state = FeedbackStackState()
        setHost(state)
        show(state, notify("Plain", withTarget = false), "Plain", mutableMapOf())
        assertFalse(state.pauseState.value.pointer)

        card("Plain").performTouchInput { down(bodyPoint(width, height)) }
        composeTestRule.waitForIdle()
        assertTrue(state.pauseState.value.pointer)

        card("Plain").performTouchInput { up() }
        composeTestRule.waitForIdle()
        assertFalse(state.pauseState.value.pointer)
        state.dismissAll()
    }

    @Test
    fun pointerDownOnUndo_setsPause() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        show(state, withUndo("Deleted"), "Deleted", results)

        val undo = composeTestRule.onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
        undo.performTouchInput { down(center) }
        composeTestRule.waitForIdle()
        assertTrue(state.pauseState.value.pointer)

        undo.performTouchInput { up() }
        composeTestRule.waitForIdle()
        assertFalse(state.pauseState.value.pointer)
        assertEquals(FeedbackResult.Undone, results["Deleted"])
    }

    @Test
    fun pointerDownOnPeekStrip_setsPause() {
        val state = FeedbackStackState()
        setHost(state)
        showAll(state, mutableMapOf(), "Back", "Front")

        card("Back").performTouchInput { down(Offset(width * 0.5f, 4.dp.toPx())) }
        composeTestRule.waitForIdle()
        assertTrue(state.pauseState.value.pointer)

        card("Back").performTouchInput { up() }
        composeTestRule.waitForIdle()
        assertFalse(state.pauseState.value.pointer)
        state.dismissAll()
    }

    @Test
    fun cardResolvedMidPress_pauseClearsOnRelease() {
        val state = FeedbackStackState()
        setHost(state)
        show(state, notify("Plain", withTarget = false), "Plain", mutableMapOf())

        card("Plain").performTouchInput { down(bodyPoint(width, height)) }
        composeTestRule.waitForIdle()
        state.dismissAll()
        composeTestRule.waitForIdle()
        assertTrue(state.pauseState.value.pointer)

        composeTestRule.onRoot().performTouchInput { up() }
        composeTestRule.waitForIdle()
        assertFalse(state.pauseState.value.pointer)
    }

    @Test
    fun bodyTap_onFrontCard_collapsed_opensOnce() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        showAll(state, results, "Back", "Front")

        card("Front").performTouchInput {
            click(bodyPoint(width, height))
            click(bodyPoint(width, height))
        }
        composeTestRule.waitForIdle()

        assertEquals(listOf(target), opened)
        assertEquals(FeedbackResult.Opened, results["Front"])
        assertNull(results["Back"])
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun cardWithoutTarget_tapDoesNothing() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        show(state, notify("Plain", withTarget = false), "Plain", results)

        tapBody("Plain")

        assertTrue(opened.isEmpty())
        assertTrue(results.isEmpty())
        card("Plain").assertExists()
        state.dismissAll()
    }

    @Test
    fun spentCard_tapDoesNothing() {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val opened = mutableListOf<FeedbackTarget>()
        setHost(state, opened)
        scope.launch { state.emitTrackedWithUndo(store, withUndo("Deleted")) }
        composeTestRule.waitForIdle()

        val entryId = store.entries.value.single().id
        scope.launch { store.attemptUndo(entryId) }
        composeTestRule.waitForIdle()
        tapBody("Deleted")

        assertTrue(opened.isEmpty())
        card("Deleted").assertExists()
        state.dismissAll()
    }

    @Test
    fun openLabel_isUsedVerbatim_withDiacriticsAndEmoji() {
        val state = FeedbackStackState()
        val label = "Öffnen 📂 Zürich"
        setHost(state)
        show(
            state,
            notify("Item saved").copy(target = FeedbackTarget("item-42", label)),
            "Item saved",
            mutableMapOf()
        )

        val onClickLabel = composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG)
            .fetchSemanticsNode().config[SemanticsActions.OnClick].label
        assertEquals(label, onClickLabel)

        val all = flatten(composeTestRule.onRoot(useUnmergedTree = true).fetchSemanticsNode())
        all.forEach { node ->
            val text = node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
            val description = node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
            (text + description).forEach { value ->
                assertFalse("node leaks the target key: $value", value.contains("item-42"))
            }
        }
        state.dismissAll()
    }

    @Test
    fun blankMessageAndBlankOpenLabel_doNotCrash() {
        val state = FeedbackStackState()
        setHost(state)
        show(
            state,
            notify("").copy(target = FeedbackTarget("item-42", "")),
            "blank",
            mutableMapOf()
        )

        val onClickLabel = composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG)
            .fetchSemanticsNode().config[SemanticsActions.OnClick].label
        assertEquals("", onClickLabel)
        state.dismissAll()
    }

    @Test
    fun semanticsClick_onCollapsedBackCard_opens_andDoesNotFan() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        showAll(state, results, "Back", "Front")

        composeTestRule.onNode(hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG) and hasText("Back"))
            .performSemanticsAction(SemanticsActions.OnClick)
        composeTestRule.waitForIdle()

        assertEquals(listOf(target), opened)
        assertEquals(FeedbackResult.Opened, results["Back"])
        assertNull(results["Front"])
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun fannedDeck_everyCardBodyOpens_andUndoOnAnyCardResolvesUndone() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        var undoRuns = 0
        setHost(state, opened)
        show(state, notify("A"), "A", results)
        show(state, withUndo("B") { undoRuns++ }, "B", results)
        show(state, notify("C"), "C", results)

        tapStrip("A")
        assertTrue(state.pauseState.value.fanned)
        tapBody("A")
        assertEquals(FeedbackResult.Opened, results["A"])
        assertEquals(listOf(target), opened)

        composeTestRule.onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Undone, results["B"])
        assertNull(results["C"])
        assertEquals(0, undoRuns)
        assertEquals(1, opened.size)
        state.dismissAll()
    }

    @Test
    fun singleCard_hasNoFanTarget() {
        val state = FeedbackStackState()
        setHost(state)
        show(state, notify("Solo", withTarget = false), "Solo", mutableMapOf())

        tapStrip("Solo")
        tapBody("Solo")

        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun arrivalWhileFanned_appendsNewestAtBottom() {
        val state = FeedbackStackState()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state)
        showAll(state, results, "A", "B", "C")
        tapStrip("A")
        assertTrue(state.pauseState.value.fanned)

        show(state, notify("D"), "D", results)

        assertEquals(FeedbackResult.Dismissed, results["A"])
        assertTrue(state.pauseState.value.fanned)
        val b = card("B").getUnclippedBoundsInRoot()
        val c = card("C").getUnclippedBoundsInRoot()
        val d = card("D").getUnclippedBoundsInRoot()
        assertTrue(b.top < c.top && c.top < d.top)
        assertEquals(deck().getUnclippedBoundsInRoot().bottom.value, d.bottom.value, 1f)
        state.dismissAll()
    }

    // ---- Idle collapse, auto-collapse and disposal cleanup (plan 26-03, task 3) ----

    /** Shows three cards without a target (so a body tap is inert), then fans the deck. */
    private fun fannedInertDeck(state: FeedbackStackState) {
        setHost(state)
        listOf("A", "B", "C").forEach { show(state, notify(it, withTarget = false), it, mutableMapOf()) }
        tapStrip("A")
        assertTrue(state.pauseState.value.fanned)
    }

    /** Advances the Compose clock by [millis] with no frame rounding, then runs one frame. */
    private fun advanceClock(millis: Long) {
        composeTestRule.mainClock.advanceTimeBy(millis, ignoreFrameDuration = true)
        composeTestRule.mainClock.advanceTimeByFrame()
    }

    private fun tops(vararg messages: String): List<Float> =
        messages.map { card(it).getUnclippedBoundsInRoot().top.value }

    @Test
    fun idleCollapse_reCollapsesAfterTheIdleDelay_notBefore() {
        val state = FeedbackStackState()
        fannedInertDeck(state)
        // A body tap on an inert card restarts the wait at a known instant (the fan spring is done).
        tapBody("B")
        assertTrue(state.pauseState.value.fanned)

        composeTestRule.mainClock.autoAdvance = false
        advanceClock(state.idleCollapseMillis - 500)
        assertTrue("collapsed before the idle delay", state.pauseState.value.fanned)

        advanceClock(1_000)
        assertFalse("still fanned after the idle delay", state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun pointerActivityWhileFanned_restartsTheIdleWait() {
        val state = FeedbackStackState()
        fannedInertDeck(state)
        tapBody("B")

        composeTestRule.mainClock.autoAdvance = false
        advanceClock(3_000)
        assertTrue(state.pauseState.value.fanned)

        composeTestRule.mainClock.autoAdvance = true
        tapBody("B")
        composeTestRule.mainClock.autoAdvance = false
        // 3000 + 3000 would have collapsed without the restart; only 3000 have passed since it.
        advanceClock(3_000)
        assertTrue("the activity did not restart the idle wait", state.pauseState.value.fanned)

        advanceClock(1_500)
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun noCollapseWhileAPointerIsDown() {
        val state = FeedbackStackState()
        fannedInertDeck(state)

        card("B").performTouchInput { down(bodyPoint(width, height)) }
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.autoAdvance = false
        advanceClock(state.idleCollapseMillis * 2)
        assertTrue("collapsed under a held finger", state.pauseState.value.fanned)
        assertTrue(state.pauseState.value.pointer)

        composeTestRule.mainClock.autoAdvance = true
        card("B").performTouchInput { up() }
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.autoAdvance = false
        advanceClock(state.idleCollapseMillis + 500)
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun fannedDeck_collapsesWhenOnlyOneCardRemains() {
        val state = FeedbackStackState()
        fannedInertDeck(state)

        composeTestRule.runOnIdle { state.resolve(state.items.last(), FeedbackResult.Dismissed) }
        composeTestRule.waitForIdle()
        assertEquals(2, state.items.size)
        assertTrue("two cards can still fan", state.pauseState.value.fanned)

        composeTestRule.runOnIdle { state.resolve(state.items.last(), FeedbackResult.Dismissed) }
        composeTestRule.waitForIdle()
        assertEquals(1, state.items.size)
        assertFalse(state.pauseState.value.fanned)
        state.dismissAll()
    }

    @Test
    fun hostLeavesComposition_whileFanned_clearsPause() {
        val state = FeedbackStackState()
        var showHost by mutableStateOf(true)
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            if (showHost) FeedbackStackHost(state = state, onOpenTarget = {})
        }
        listOf("A", "B", "C").forEach { show(state, notify(it, withTarget = false), it, mutableMapOf()) }
        tapStrip("A")
        assertTrue(state.pauseState.value.fanned)

        composeTestRule.runOnIdle { showHost = false }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackPauseState(), state.pauseState.value)
        state.dismissAll()
    }

    @Test
    fun hostLeavesComposition_midPress_clearsPause() {
        val state = FeedbackStackState()
        var showHost by mutableStateOf(true)
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            if (showHost) FeedbackStackHost(state = state, onOpenTarget = {})
        }
        show(state, notify("Plain", withTarget = false), "Plain", mutableMapOf())
        card("Plain").performTouchInput { down(bodyPoint(width, height)) }
        composeTestRule.waitForIdle()
        assertTrue(state.pauseState.value.pointer)

        composeTestRule.runOnIdle { showHost = false }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackPauseState(), state.pauseState.value)
        state.dismissAll()
    }

    @Test
    fun fannedOrder_isStableAcrossFanAndCollapse() {
        val state = FeedbackStackState()
        setHost(state)
        listOf("A", "B", "C").forEach { show(state, notify(it, withTarget = false), it, mutableMapOf()) }
        assertEquals(tops("A", "B", "C").sorted(), tops("A", "B", "C"))

        tapStrip("A")
        assertTrue(state.pauseState.value.fanned)
        assertEquals(tops("A", "B", "C").sorted(), tops("A", "B", "C"))
        val fannedHeight = deck().getUnclippedBoundsInRoot().height

        composeTestRule.mainClock.autoAdvance = false
        advanceClock(state.idleCollapseMillis + 1_000)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
        assertFalse(state.pauseState.value.fanned)
        assertTrue(deck().getUnclippedBoundsInRoot().height < fannedHeight)
        assertEquals(tops("A", "B", "C").sorted(), tops("A", "B", "C"))

        tapStrip("A")
        assertTrue(state.pauseState.value.fanned)
        assertEquals(tops("A", "B", "C").sorted(), tops("A", "B", "C"))
        state.dismissAll()
    }
}
