package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Virtual-time timer contract of [FeedbackStackState] (D-06, brief D-04): Short 4000 ms, Long
 * 10000 ms, Indefinite never; the timer starts at enqueue and the scaler seam is read once.
 *
 * `advanceTimeBy(n)` runs tasks strictly before the boundary, so every boundary check calls
 * `runCurrent()` afterwards to run a task due exactly at it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackStackStateTimeoutTest {

    private val target = FeedbackTarget(key = "item-42", openLabel = "Open item")

    private fun CoroutineScope.showAsync(
        state: FeedbackStackState,
        event: FeedbackEvent
    ): Deferred<FeedbackResult> = async { state.show(event) }

    private fun withUndo(duration: SnackbarDuration): FeedbackEvent.WithUndo =
        FeedbackEvent.WithUndo("Deleted", undoAction = { }, duration = duration)

    // ---- (a) timer boundaries ----

    @Test
    fun short_isPendingAt3999_andTimedOutAfterOneMoreMillisecond() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, FeedbackEvent.Notify("m", SnackbarDuration.Short))
        runCurrent()

        advanceTimeBy(3999)
        runCurrent()
        assertFalse(shown.isCompleted)
        advanceTimeBy(1)
        runCurrent()

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun long_isPendingAt9999_andTimedOutAt10000() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, withUndo(SnackbarDuration.Long))
        runCurrent()

        advanceTimeBy(9999)
        runCurrent()
        assertFalse(shown.isCompleted)
        advanceTimeBy(1)
        runCurrent()

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun dismissScreen_timesOutAtShortDuration() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, FeedbackEvent.DismissScreen("Saved"))
        runCurrent()

        advanceTimeBy(3999)
        runCurrent()
        assertFalse(shown.isCompleted)
        advanceTimeBy(1)
        runCurrent()

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun indefinite_neverTimesOut_untilDismissAll() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, FeedbackEvent.Notify("m", SnackbarDuration.Indefinite))
        runCurrent()

        advanceTimeBy(600_000)
        runCurrent()
        assertFalse(shown.isCompleted)
        assertEquals(1, state.items.size)

        state.dismissAll()
        assertEquals(FeedbackResult.Dismissed, shown.await())
    }

    @Test
    fun eachCardsTimer_startsAtItsOwnEnqueue() = runTest {
        val state = FeedbackStackState()
        val a = showAsync(state, FeedbackEvent.Notify("a", SnackbarDuration.Short))
        runCurrent()
        advanceTimeBy(1000)
        runCurrent()
        val b = showAsync(state, FeedbackEvent.Notify("b", SnackbarDuration.Short))
        runCurrent()

        advanceTimeBy(3000)
        runCurrent()
        assertEquals(FeedbackResult.TimedOut, a.await())
        assertFalse(b.isCompleted)

        advanceTimeBy(1000)
        runCurrent()
        assertEquals(FeedbackResult.TimedOut, b.await())
    }

    @Test
    fun timedOutCard_leavesTheList_andFreesItsSlot() = runTest {
        val state = FeedbackStackState(maxVisible = 1)
        val a = showAsync(state, FeedbackEvent.Notify("a", SnackbarDuration.Short))
        runCurrent()
        assertEquals(1, state.items.size)

        advanceTimeBy(4000)
        runCurrent()
        assertEquals(FeedbackResult.TimedOut, a.await())
        assertTrue(state.items.isEmpty())

        val b = showAsync(state, FeedbackEvent.Notify("b", SnackbarDuration.Indefinite))
        runCurrent()
        assertEquals(1, state.items.size)
        assertFalse(b.isCompleted)
        state.dismissAll()
        assertEquals(FeedbackResult.Dismissed, b.await())
    }

    // ---- (b) the scaler seam ----

    /** Shows [event] with a recording scaler, returns every (base, containsControls) it saw. */
    private suspend fun TestScope.scalerCalls(
        event: FeedbackEvent
    ): List<Pair<Long, Boolean>> {
        val state = FeedbackStackState()
        val calls = mutableListOf<Pair<Long, Boolean>>()
        state.timeoutScaler = { base, controls ->
            calls += base to controls
            base
        }
        val shown = showAsync(state, event)
        runCurrent()
        state.dismissAll()
        shown.await()
        return calls
    }

    @Test
    fun scaler_notifyWithoutTarget_receives4000AndNoControls() = runTest {
        assertEquals(
            listOf(4_000L to false),
            scalerCalls(FeedbackEvent.Notify("m", SnackbarDuration.Short))
        )
    }

    @Test
    fun scaler_notifyWithTarget_receives4000AndControls() = runTest {
        assertEquals(
            listOf(4_000L to true),
            scalerCalls(FeedbackEvent.Notify("m", SnackbarDuration.Short, target = target))
        )
    }

    @Test
    fun scaler_withUndo_receives10000AndControls() = runTest {
        assertEquals(listOf(10_000L to true), scalerCalls(withUndo(SnackbarDuration.Long)))
    }

    @Test
    fun scaler_dismissScreen_receives4000AndNoControls() = runTest {
        assertEquals(listOf(4_000L to false), scalerCalls(FeedbackEvent.DismissScreen("Saved")))
    }

    @Test
    fun scaler_isNeverInvokedForIndefinite() = runTest {
        assertTrue(scalerCalls(FeedbackEvent.Notify("m", SnackbarDuration.Indefinite)).isEmpty())
    }

    @Test
    fun scalerDoublingBase_makesShortPendingAt7999_andTimedOutAt8000() = runTest {
        val state = FeedbackStackState()
        state.timeoutScaler = { base, _ -> base * 2 }
        val shown = showAsync(state, FeedbackEvent.Notify("m", SnackbarDuration.Short))
        runCurrent()

        advanceTimeBy(7999)
        runCurrent()
        assertFalse(shown.isCompleted)
        advanceTimeBy(1)
        runCurrent()

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun replacingScalerAfterEnqueue_doesNotChangeAnExistingTimer() = runTest {
        val state = FeedbackStackState()
        state.timeoutScaler = { base, _ -> base * 2 }
        val shown = showAsync(state, FeedbackEvent.Notify("m", SnackbarDuration.Short))
        runCurrent()

        state.timeoutScaler = { base, _ -> base * 10 }
        advanceTimeBy(7999)
        runCurrent()
        assertFalse(shown.isCompleted)
        advanceTimeBy(1)
        runCurrent()

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }

    @Test
    fun withNoScaler_theBaseValueIsUsed() = runTest {
        val state = FeedbackStackState()
        assertEquals(null, state.timeoutScaler)
        val shown = showAsync(state, FeedbackEvent.Notify("m", SnackbarDuration.Short))
        runCurrent()

        advanceTimeBy(4000)
        runCurrent()

        assertEquals(FeedbackResult.TimedOut, shown.await())
    }
}
