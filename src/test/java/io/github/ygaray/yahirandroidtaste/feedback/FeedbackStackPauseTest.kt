package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Pause contract of the card timers in [FeedbackStackState] (FSTK-06, brief D-05, phase decision
 * D-04 [pause-scope]): the whole deck pauses while a pointer is down or the deck is fanned, the
 * remainder resumes on release or collapse, and a pause that interrupts a running countdown floors
 * the remainder at 800 ms.
 *
 * Every test injects `testScheduler.timeSource` into the state before the first show, because the
 * timer measures elapsed time only through that seam. `advanceTimeBy(n)` runs tasks strictly before
 * the boundary, so every boundary check calls `runCurrent()` afterwards.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackStackPauseTest {

    private fun CoroutineScope.showAsync(
        state: FeedbackStackState,
        event: FeedbackEvent
    ): Deferred<FeedbackResult> = async { state.show(event) }

    private fun notify(duration: SnackbarDuration = SnackbarDuration.Short): FeedbackEvent.Notify =
        FeedbackEvent.Notify("message", duration)

    @Test
    fun pointerDown_pausesTimer_andReleaseResumesWithRemainder() = runTest {
        val state = FeedbackStackState()
        state.timeSource = testScheduler.timeSource
        val shown = showAsync(state, notify())
        runCurrent()

        advanceTimeBy(1000)
        runCurrent()
        state.setPointerDown(true)
        runCurrent()
        advanceTimeBy(2000)
        runCurrent()
        assertFalse(shown.isCompleted)

        state.setPointerDown(false)
        runCurrent()
        advanceTimeBy(2999)
        runCurrent()
        assertFalse(shown.isCompleted)
        advanceTimeBy(1)
        runCurrent()

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }
}
