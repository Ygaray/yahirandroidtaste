package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pause contract of the card timers in [FeedbackStackState] (FSTK-06, brief D-05, phase decision
 * D-04 [pause-scope]): the whole deck pauses while a pointer is down or the deck is fanned, the
 * remainder resumes on release or collapse, and a pause that interrupts a running countdown floors
 * the remainder at 800 ms.
 *
 * Every test injects `testScheduler.timeSource` into the state before the first show, because the
 * timer measures elapsed time only through that seam. `advanceTimeBy(n)` runs tasks strictly before
 * the boundary, so [at] calls `runCurrent()` afterwards.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackStackPauseTest {

    private fun CoroutineScope.showAsync(
        state: FeedbackStackState,
        event: FeedbackEvent
    ): Deferred<FeedbackResult> = async { state.show(event) }

    private fun notify(duration: SnackbarDuration = SnackbarDuration.Short): FeedbackEvent.Notify =
        FeedbackEvent.Notify("message", duration)

    /** A state whose timers measure time on the test's virtual clock. */
    private fun TestScope.virtualState(maxVisible: Int = 3): FeedbackStackState =
        FeedbackStackState(maxVisible).also { it.timeSource = testScheduler.timeSource }

    /** Advance virtual time to the absolute instant [ms], running tasks due exactly at it. */
    private fun TestScope.at(ms: Long) {
        advanceTimeBy(ms - testScheduler.currentTime)
        runCurrent()
    }

    private fun TestScope.press(state: FeedbackStackState, down: Boolean) {
        state.setPointerDown(down)
        runCurrent()
    }

    private fun TestScope.fan(state: FeedbackStackState, fanned: Boolean) {
        state.setFanned(fanned)
        runCurrent()
    }

    private fun TestScope.show(
        state: FeedbackStackState,
        event: FeedbackEvent = notify()
    ): Deferred<FeedbackResult> = showAsync(state, event).also { runCurrent() }

    @Test
    fun pointerDown_pausesTimer_andReleaseResumesWithRemainder() = runTest {
        val state = virtualState()
        val shown = show(state)

        at(1000)
        press(state, true)
        at(3000)
        assertFalse(shown.isCompleted)

        press(state, false)
        at(5999)
        assertFalse(shown.isCompleted)
        at(6000)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun pauseFloor_resumesAtLeast800ms_afterLateTouch() = runTest {
        val state = virtualState()
        val shown = show(state)

        at(3900)
        press(state, true)
        at(5000)
        press(state, false)
        at(5799)
        assertFalse(shown.isCompleted)
        at(5800)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun floorAppliesPerCard_atPauseStart() = runTest {
        val state = virtualState()
        val a = show(state)
        at(3000)
        val b = show(state)

        at(3500)
        press(state, true)
        at(4000)
        press(state, false)

        at(4799)
        assertFalse(a.isCompleted)
        at(4800)
        assertEquals(FeedbackResult.TimedOut, a.await())
        at(7499)
        assertFalse(b.isCompleted)
        at(7500)
        assertEquals(FeedbackResult.TimedOut, b.await())
    }

    @Test
    fun untouchedCard_isUnaffectedByTheFloor() = runTest {
        val state = virtualState()
        press(state, true)
        at(1000)
        press(state, false)
        val shown = show(state)

        at(4999)
        assertFalse(shown.isCompleted)
        at(5000)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun secondPause_reducesRemainderFromTheResumePoint() = runTest {
        val state = virtualState()
        val shown = show(state)

        at(1000)
        press(state, true)
        at(1500)
        press(state, false)
        at(2500)
        press(state, true)
        at(3000)
        press(state, false)

        at(4999)
        assertFalse(shown.isCompleted)
        at(5000)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun fannedReason_pausesEveryCard_untilCollapsed() = runTest {
        val state = virtualState()
        val cards = List(3) { show(state) }

        at(1000)
        fan(state, true)
        at(2000)
        fan(state, false)

        at(4999)
        assertTrue(cards.none { it.isCompleted })
        at(5000)

        assertEquals(List(3) { FeedbackResult.TimedOut }, cards.awaitAll())
    }

    @Test
    fun bothReasons_keepPaused_untilBothClear() = runTest {
        val state = virtualState()
        val shown = show(state)

        fan(state, true)
        at(500)
        press(state, true)
        at(600)
        press(state, false)
        at(10_000)
        assertFalse(shown.isCompleted)
        at(10_600)
        fan(state, false)

        at(14_599)
        assertFalse(shown.isCompleted)
        at(14_600)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun cardEnqueuedWhilePaused_startsCountingAfterRelease() = runTest {
        val state = virtualState()
        press(state, true)
        at(100)
        val shown = show(state)

        at(2000)
        press(state, false)
        at(5999)
        assertFalse(shown.isCompleted)
        at(6000)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun pausingWithNoCards_isHarmless_andNextCardCountsNormally() = runTest {
        val state = virtualState()
        press(state, true)
        press(state, false)
        fan(state, true)
        fan(state, false)
        assertEquals(FeedbackPauseState(), state.pauseState.value)

        val shown = show(state)
        at(3999)
        assertFalse(shown.isCompleted)
        at(4000)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun pauseAtTheExpiryInstant_yieldsExactlyOneResult() = runTest {
        val state = virtualState()
        val shown = show(state)

        advanceTimeBy(4000)
        state.setPointerDown(true)
        runCurrent()
        state.setPointerDown(false)
        advanceTimeBy(800)
        runCurrent()

        assertTrue(shown.isCompleted)
        assertEquals(FeedbackResult.TimedOut, shown.await())
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun cardsWithEqualRemainder_allTimeOutOnceTogether() = runTest {
        val state = virtualState()
        val cards = List(3) { show(state) }

        at(1000)
        fan(state, true)
        at(2000)
        fan(state, false)

        at(4999)
        assertEquals(3, state.items.size)
        at(5000)

        assertEquals(List(3) { FeedbackResult.TimedOut }, cards.awaitAll())
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun cardResolvedWhilePaused_neverTimesOutLater_andCleansUp() = runTest {
        val state = virtualState()
        val shown = show(state)

        at(1000)
        press(state, true)
        assertTrue(state.resolve(state.items.single(), FeedbackResult.Opened))
        runCurrent()
        press(state, false)
        at(20_000)

        assertEquals(FeedbackResult.Opened, shown.await())
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun evictionWhilePaused_cancelsTheEvictedTimer() = runTest {
        val state = virtualState(maxVisible = 3)
        fan(state, true)
        val cards = List(4) { show(state) }

        assertTrue(cards[0].isCompleted)
        assertEquals(FeedbackResult.Dismissed, cards[0].await())
        assertEquals(3, state.items.size)

        at(1000)
        fan(state, false)
        at(4999)
        assertTrue(cards.drop(1).none { it.isCompleted })
        at(5000)

        assertEquals(List(3) { FeedbackResult.TimedOut }, cards.drop(1).awaitAll())
        assertEquals(FeedbackResult.Dismissed, cards[0].await())
    }

    @Test
    fun resetPause_clearsBothReasons() = runTest {
        val state = virtualState()
        state.setPointerDown(true)
        state.setFanned(true)
        assertEquals(FeedbackPauseState(pointer = true, fanned = true), state.pauseState.value)

        state.resetPause()
        assertEquals(FeedbackPauseState(), state.pauseState.value)

        val shown = show(state)
        at(3999)
        assertFalse(shown.isCompleted)
        at(4000)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun resetPause_releasesAWaitingCard() = runTest {
        val state = virtualState()
        val shown = show(state)
        press(state, true)
        fan(state, true)

        at(1000)
        state.resetPause()
        runCurrent()

        at(4999)
        assertFalse(shown.isCompleted)
        at(5000)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun concurrentReasonFlips_neverLoseAnUpdate() {
        val state = FeedbackStackState()
        runBlocking(Dispatchers.Default) {
            val pointer = async {
                repeat(10_000) {
                    state.setPointerDown(false)
                    state.setPointerDown(true)
                }
            }
            val fanned = async {
                repeat(10_000) {
                    state.setFanned(false)
                    state.setFanned(true)
                }
            }
            awaitAll(pointer, fanned)
        }

        assertEquals(FeedbackPauseState(pointer = true, fanned = true), state.pauseState.value)
    }

    @Test
    fun scaledTimeout_andPause_composeCorrectly() = runTest {
        val state = virtualState()
        state.timeoutScaler = { base, _ -> base * 2 }
        val shown = show(state)

        at(1000)
        press(state, true)
        at(2000)
        press(state, false)

        at(8999)
        assertFalse(shown.isCompleted)
        at(9000)

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun indefinite_neverTimesOut_acrossPauseCycles() = runTest {
        val state = virtualState()
        val shown = show(state, notify(SnackbarDuration.Indefinite))

        at(1000)
        press(state, true)
        at(2000)
        press(state, false)
        at(3000)
        fan(state, true)
        at(4000)
        fan(state, false)
        at(100_000)
        assertFalse(shown.isCompleted)
        assertEquals(1, state.items.size)

        state.dismissAll()
        assertEquals(FeedbackResult.Dismissed, shown.await())
    }
}
