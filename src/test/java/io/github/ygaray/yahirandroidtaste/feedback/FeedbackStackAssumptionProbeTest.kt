package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.click
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private enum class ProbeAnchor { Start, Settled, End }

/**
 * Wave 0 probes for phase 26: Compose platform behavior the deck relies on, pinned as
 * characterization tests and kept as a canary for a future BOM bump. These test Compose, not
 * library code. Plans 26-02 to 26-05 read the list below as their branch input.
 *
 * Recorded outcomes (Compose BOM 2026.04.01, Robolectric sdk 35, density 1.0, touch slop 16 px):
 *
 * - A1: CONFIRMED with a caveat. A `LaunchedEffect` delay and a `rememberCoroutineScope()` delay
 *   are both driven by `mainClock` (autoAdvance off). `advanceTimeBy(n)` rounds UP to whole frames
 *   (`advanceTimeBy(3999)` already fires a 4000 ms delay), so exact boundaries need
 *   `ignoreFrameDuration = true`. Plan 26-03 may test idle collapse on `mainClock`; no fallback needed.
 * - A2: CONFIRMED. `detectTapGestures(onLongPress, onTap)`: a hold past `longPressTimeoutMillis`
 *   gives one long press and no tap; a short press gives one tap. The probe advances
 *   `mainClock.advanceTimeBy` between `down` and `up` to let the long-press timeout elapse.
 * - A2b: FALSIFIED. A move beyond the touch slop inside the node does NOT cancel the tap when
 *   nothing consumes the move: `onTap` still fires. Fallback: the swipe's draggable consumes the
 *   drag, which is what cancels the tap (A2c); plans 26-03 and 26-04 must not rely on slop alone.
 * - A2c: CONFIRMED (new probe). With `anchoredDraggable` and `detectTapGestures` on one node, a
 *   drag past slop consumes the move and `onTap` and `onLongPress` stay silent (tested with the
 *   draggable ahead of the tap detector in the chain).
 * - A3: CONFIRMED. `AnchoredDraggableState(initial, DraggableAnchors { Settled at 0f })` has a
 *   non-NaN `offset` of 0 before composition; `updateAnchors` with three anchors keeps offset 0 and
 *   `currentValue` Settled.
 * - A3b: CONFIRMED with a caveat. The 72dp positional threshold works, measured on top of the
 *   touch slop: with slop 16 px a drag of 90 settles End (74 effective), a drag of 80 settles back
 *   (64 effective), 60 settles back. The threshold applies only when the release velocity is
 *   nonzero and below the velocity threshold; a release with exactly zero velocity picks the
 *   closest anchor (50 percent of the distance), so a test must keep the finger moving
 *   (16 ms move events) to exercise the 72dp rule.
 * - A3c: CONFIRMED. A fast flick (8 moves of 4dp at 16 ms, 32dp total, about 250dp/s) settles at
 *   the End anchor although it is far below the 72dp threshold. `swipeWithVelocity` over 30dp did
 *   NOT settle (harness limitation, release velocity not delivered): do not use it for flicks.
 * - Fling wiring: `val fling = AnchoredDraggableDefaults.flingBehavior(state = state,
 *   positionalThreshold = { 72.dp.toPx() })` (composable, build it in the card's composition,
 *   `toPx` via the current `LocalDensity`), passed as
 *   `Modifier.anchoredDraggable(state, Orientation.Horizontal, flingBehavior = fling)`.
 *   `anchoredDraggableFlingBehavior(...)` is internal to foundation and cannot be called.
 * - A5: CONFIRMED. Hit-testing follows `placeWithLayer`: a tap at y 7dp reaches only the back
 *   card whose layer is scaled 0.94 about its top center, a tap at y 30dp reaches only the front
 *   card (zIndex 1).
 * - A5b: CONFIRMED. A point inside the unscaled footprint but outside the scaled back card
 *   (x 2dp, y 7dp) reaches neither card, so no fallback is needed.
 * - A6: FALSIFIED. `onAllNodesWithTag("card")` lists children in zIndex order (lowest first), NOT
 *   composition order: the first-composed card with the higher zIndex is listed LAST. Fallback for
 *   plan 26-02: give each card `traversalIndex = index.toFloat()` from its first commit and edit
 *   the Phase 25 test `newestCardComesFirst` to compare the TraversalIndex property instead of
 *   list position (A11Y-01 forces that edit; say so in that commit).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackStackAssumptionProbeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ---- A1: LaunchedEffect / scope delay is driven by mainClock ----

    @Test
    fun probeA1_launchedEffectDelay_isDrivenByMainClock() {
        var flag by mutableStateOf(false)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            LaunchedEffect(Unit) {
                delay(4000)
                flag = true
            }
            Text(if (flag) "done" else "waiting", Modifier.testTag("flag"))
        }

        composeTestRule.mainClock.advanceTimeBy(3999, ignoreFrameDuration = true)
        assertFalse(flag)
        composeTestRule.mainClock.advanceTimeBy(1, ignoreFrameDuration = true)
        composeTestRule.mainClock.advanceTimeByFrame()
        assertTrue(flag)
    }

    @Test
    fun probeA1_scopeDelay_isDrivenByMainClock() {
        var flag by mutableStateOf(false)
        lateinit var scope: CoroutineScope
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            Text(if (flag) "done" else "waiting", Modifier.testTag("flag"))
        }
        composeTestRule.runOnUiThread {
            scope.launch {
                delay(4000)
                flag = true
            }
        }

        composeTestRule.mainClock.advanceTimeBy(3999, ignoreFrameDuration = true)
        assertFalse(flag)
        composeTestRule.mainClock.advanceTimeBy(1, ignoreFrameDuration = true)
        composeTestRule.mainClock.advanceTimeByFrame()
        assertTrue(flag)
    }

    // ---- A2: long press suppresses tap, drag past slop cancels tap ----

    private var taps = 0
    private var longPresses = 0

    private fun setTapBox() {
        taps = 0
        longPresses = 0
        composeTestRule.setContent {
            Box(
                Modifier
                    .size(200.dp)
                    .testTag("box")
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { longPresses++ }, onTap = { taps++ })
                    }
            )
        }
    }

    @Test
    fun probeA2_longPress_suppressesTap() {
        setTapBox()
        var longPressTimeout = 0L
        composeTestRule.onNodeWithTag("box").performTouchInput {
            longPressTimeout = viewConfiguration.longPressTimeoutMillis
            down(center)
        }
        composeTestRule.mainClock.advanceTimeBy(longPressTimeout + 100)
        composeTestRule.onNodeWithTag("box").performTouchInput { up() }
        composeTestRule.waitForIdle()
        assertEquals(1, longPresses)
        assertEquals(0, taps)

        composeTestRule.onNodeWithTag("box").performTouchInput { click(center) }
        composeTestRule.waitForIdle()
        assertEquals(1, taps)
        assertEquals(1, longPresses)
    }

    @Test
    fun probeA2b_dragPastSlopWithoutDragConsumer_stillTaps() {
        setTapBox()
        composeTestRule.onNodeWithTag("box").performTouchInput {
            down(center)
            moveBy(Offset(viewConfiguration.touchSlop * 4f, 0f))
            up()
        }
        composeTestRule.waitForIdle()
        assertEquals(1, taps)
        assertEquals(0, longPresses)
    }

    @Test
    fun probeA2c_dragConsumedByAnchoredDraggable_cancelsTap() {
        taps = 0
        longPresses = 0
        composeTestRule.setContent {
            val density = LocalDensity.current
            val widthPx = with(density) { 200.dp.toPx() }
            val state = remember {
                AnchoredDraggableState(
                    initialValue = ProbeAnchor.Settled,
                    anchors = DraggableAnchors {
                        ProbeAnchor.Start at -widthPx
                        ProbeAnchor.Settled at 0f
                        ProbeAnchor.End at widthPx
                    }
                )
            }
            Box(
                Modifier
                    .size(200.dp)
                    .testTag("box")
                    .anchoredDraggable(state, Orientation.Horizontal)
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { longPresses++ }, onTap = { taps++ })
                    }
            )
        }
        composeTestRule.onNodeWithTag("box").performTouchInput {
            down(center)
            moveBy(Offset(viewConfiguration.touchSlop * 4f, 0f))
            up()
        }
        composeTestRule.waitForIdle()
        assertEquals(0, taps)
        assertEquals(0, longPresses)
    }

    // ---- A3: seeded anchors give a non-NaN offset ----

    @Test
    fun probeA3_seededAnchors_giveNonNaNOffset() {
        val state = AnchoredDraggableState(
            initialValue = ProbeAnchor.Settled,
            anchors = DraggableAnchors { ProbeAnchor.Settled at 0f }
        )
        assertFalse(state.offset.isNaN())
        assertEquals(0f, state.offset, 0f)
        assertEquals(ProbeAnchor.Settled, state.settledValue)

        state.updateAnchors(
            DraggableAnchors {
                ProbeAnchor.Start at -300f
                ProbeAnchor.Settled at 0f
                ProbeAnchor.End at 300f
            }
        )
        assertEquals(0f, state.offset, 0f)
        assertEquals(ProbeAnchor.Settled, state.currentValue)
    }

    // ---- A3b / A3c: 72dp positional threshold and fling wiring ----

    private var draggable: AnchoredDraggableState<ProbeAnchor>? = null

    private fun setDraggableBox() {
        composeTestRule.setContent {
            val density = LocalDensity.current
            val widthPx = with(density) { 300.dp.toPx() }
            val state = remember {
                AnchoredDraggableState(
                    initialValue = ProbeAnchor.Settled,
                    anchors = DraggableAnchors {
                        ProbeAnchor.Start at -widthPx
                        ProbeAnchor.Settled at 0f
                        ProbeAnchor.End at widthPx
                    }
                )
            }
            draggable = state
            val fling = AnchoredDraggableDefaults.flingBehavior(
                state = state,
                positionalThreshold = { with(density) { 72.dp.toPx() } }
            )
            Box(
                Modifier
                    .size(300.dp)
                    .testTag("drag")
                    .anchoredDraggable(state, Orientation.Horizontal, flingBehavior = fling)
            )
        }
    }

    private fun slowDrag(dp: Float) {
        composeTestRule.onNodeWithTag("drag").performTouchInput {
            down(center)
            val steps = 100
            repeat(steps) { moveBy(Offset(dp.dp.toPx() / steps, 0f), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun probeA3b_positionalThreshold_settlesAt72dp() {
        setDraggableBox()
        slowDrag(60f)
        assertEquals(ProbeAnchor.Settled, draggable!!.settledValue)

        // 80dp is borderline: the touch slop (8dp) is consumed before the drag delta starts.
        slowDrag(80f)
        assertEquals(ProbeAnchor.Settled, draggable!!.settledValue)

        slowDrag(90f)
        assertEquals(ProbeAnchor.End, draggable!!.settledValue)
    }

    @Test
    fun probeA3c_fastFlickBelowThreshold_settlesAtEnd() {
        setDraggableBox()
        composeTestRule.onNodeWithTag("drag").performTouchInput {
            // 8 moves of 4dp every 16 ms: 32dp in total (below 72dp), about 250dp/s on release.
            down(center)
            repeat(8) { moveBy(Offset(4.dp.toPx(), 0f), delayMillis = 16) }
            up()
        }
        composeTestRule.waitForIdle()
        assertEquals(ProbeAnchor.End, draggable!!.settledValue)
    }

    // ---- A5: strip hit-testing under graphicsLayer ----

    private var backTaps = 0
    private var frontTaps = 0

    @Composable
    private fun TwoCardDeck(firstOnTop: Boolean = false, sharedTag: Boolean = false) {
        Layout(
            modifier = Modifier.size(300.dp, 80.dp).testTag("deck"),
            content = {
                Box(
                    Modifier
                        .testTag(if (sharedTag) "card" else "back")
                        .semantics { contentDescription = "back" }
                        .pointerInput(Unit) { detectTapGestures(onTap = { backTaps++ }) }
                )
                Box(
                    Modifier
                        .testTag(if (sharedTag) "card" else "front")
                        .semantics { contentDescription = "front" }
                        .pointerInput(Unit) { detectTapGestures(onTap = { frontTaps++ }) }
                )
            }
        ) { measurables, constraints ->
            val w = 300.dp.roundToPx()
            val h = 50.dp.roundToPx()
            val back = measurables[0].measure(Constraints.fixed(w, h))
            val front = measurables[1].measure(Constraints.fixed(w, h))
            layout(w, 80.dp.roundToPx().coerceIn(constraints.minHeight, constraints.maxHeight)) {
                back.placeWithLayer(0, 0, zIndex = if (firstOnTop) 1f else 0f) {
                    scaleX = 0.94f
                    scaleY = 0.94f
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
                front.placeWithLayer(0, 14.dp.roundToPx(), zIndex = if (firstOnTop) 0f else 1f)
            }
        }
    }

    private fun tapDeckAt(xDp: Float, yDp: Float) {
        composeTestRule.onNodeWithTag("deck").performTouchInput {
            click(Offset(xDp.dp.toPx(), yDp.dp.toPx()))
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun probeA5_peekStripTap_hitsBackCardNotFront() {
        backTaps = 0
        frontTaps = 0
        composeTestRule.setContent { TwoCardDeck() }

        tapDeckAt(150f, 7f)
        assertEquals(1, backTaps)
        assertEquals(0, frontTaps)

        tapDeckAt(150f, 30f)
        assertEquals(1, backTaps)
        assertEquals(1, frontTaps)
    }

    @Test
    fun probeA5b_pointOutsideScaledBackCard_isNotHit() {
        backTaps = 0
        frontTaps = 0
        composeTestRule.setContent { TwoCardDeck() }

        tapDeckAt(2f, 7f)
        assertEquals(0, backTaps)
        assertEquals(0, frontTaps)
    }

    // ---- A6: semantics child order under zIndex ----

    @Test
    fun probeA6_semanticsChildOrder_underZIndex() {
        composeTestRule.setContent { TwoCardDeck(firstOnTop = true, sharedTag = true) }
        val listed = composeTestRule.onAllNodesWithTag("card").fetchSemanticsNodes()
            .map { it.config[SemanticsProperties.ContentDescription].single() }
        // The first-composed card carries the HIGHER zIndex; the tree lists it LAST.
        assertEquals(listOf("front", "back"), listed)
    }
}
