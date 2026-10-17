package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Plain-JVM contract for [FeedbackStackState] (FSTK-01): no composition, virtual time only.
 *
 * Every test ends with its `show()` calls resolved or cancelled, because `runTest` waits for
 * its children and an unresolved Indefinite card would time the test out.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackStackStateTest {

    private val target = FeedbackTarget(key = "item-42", openLabel = "Open item")

    private fun CoroutineScope.showAsync(
        state: FeedbackStackState,
        event: FeedbackEvent
    ): Deferred<FeedbackResult> = async { state.show(event) }

    private fun indefinite(message: String, withTarget: Boolean = false): FeedbackEvent.Notify =
        FeedbackEvent.Notify(
            message = message,
            duration = SnackbarDuration.Indefinite,
            target = if (withTarget) target else null
        )

    // ---- (a) exactly one result per show() ----

    @Test
    fun showThenResolveOpened_returnsOpenedAndRemovesItem() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, indefinite("m", withTarget = true))
        runCurrent()

        assertEquals(1, state.items.size)
        val item = state.items.single()
        assertTrue(state.resolve(item, FeedbackResult.Opened))
        assertEquals(FeedbackResult.Opened, shown.await())
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun secondResolveOnSameItem_returnsFalseAndKeepsFirstResult() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, indefinite("m"))
        runCurrent()

        val item = state.items.single()
        assertTrue(state.resolve(item, FeedbackResult.Opened))
        assertFalse(state.resolve(item, FeedbackResult.Undone))
        assertEquals(FeedbackResult.Opened, shown.await())
    }

    @Test
    fun eachResolvedResult_isExactlyWhatShowReturns() = runTest {
        for (result in listOf(FeedbackResult.Opened, FeedbackResult.Undone, FeedbackResult.Dismissed)) {
            val state = FeedbackStackState()
            val shown = showAsync(state, indefinite("m"))
            runCurrent()
            assertTrue(state.items.isNotEmpty())
            assertTrue(state.resolve(state.items.single(), result))
            assertEquals(result, shown.await())
        }
    }

    @Test
    fun parallelResolvers_produceExactlyOneWinner() = runTest {
        val proposals = listOf(FeedbackResult.Opened, FeedbackResult.Undone, FeedbackResult.Dismissed)
        repeat(50) {
            val state = FeedbackStackState()
            val shown = showAsync(state, indefinite("m", withTarget = true))
            runCurrent()
            assertTrue(state.items.isNotEmpty())
            val item = state.items.single()

            val attempts = (0 until 8).map { i ->
                val proposal = proposals[i % proposals.size]
                async(Dispatchers.Default) { proposal to state.resolve(item, proposal) }
            }.awaitAll()

            val winners = attempts.filter { it.second }
            assertEquals(1, winners.size)
            assertEquals(winners.single().first, shown.await())
            assertTrue(state.items.isEmpty())
        }
    }

    // ---- (b) dismissAll ----

    @Test
    fun dismissAll_resolvesEveryVisibleCardAsDismissed_andLaterResolveIsFalse() = runTest {
        val state = FeedbackStackState()
        val a = showAsync(state, indefinite("a"))
        val b = showAsync(state, indefinite("b", withTarget = true))
        runCurrent()
        assertEquals(2, state.items.size)
        val snapshot = state.items.toList()

        state.dismissAll()

        assertEquals(FeedbackResult.Dismissed, a.await())
        assertEquals(FeedbackResult.Dismissed, b.await())
        assertTrue(state.items.isEmpty())
        snapshot.forEach { assertFalse(state.resolve(it, FeedbackResult.Opened)) }
    }

    @Test
    fun dismissAll_onEmptyState_isANoOp() = runTest {
        val state = FeedbackStackState()
        state.dismissAll()
        assertTrue(state.items.isEmpty())
    }

    // ---- (c) cap and eviction ----

    @Test
    fun fourthArrival_evictsOldestAsDismissed_itemsNewestFirst() = runTest {
        val state = FeedbackStackState(maxVisible = 3)
        val first = showAsync(state, indefinite("1"))
        runCurrent()
        val second = showAsync(state, indefinite("2"))
        val third = showAsync(state, indefinite("3"))
        val fourth = showAsync(state, indefinite("4"))
        runCurrent()

        assertEquals(FeedbackResult.Dismissed, first.await())
        assertFalse(second.isCompleted)
        assertEquals(listOf("4", "3", "2"), state.items.map { it.message })

        state.dismissAll()
        listOf(second, third, fourth).forEach { assertEquals(FeedbackResult.Dismissed, it.await()) }
    }

    @Test
    fun maxVisibleOne_holdsOnlyTheNewestCard() = runTest {
        val state = FeedbackStackState(maxVisible = 1)
        val a = showAsync(state, indefinite("a"))
        runCurrent()
        val b = showAsync(state, indefinite("b"))
        runCurrent()

        assertEquals(1, state.items.size)
        assertEquals("b", state.items.single().message)
        assertEquals(FeedbackResult.Dismissed, a.await())
        state.dismissAll()
        assertEquals(FeedbackResult.Dismissed, b.await())
    }

    @Test
    fun maxVisibleBelowOne_readsBackAsOne_andNeverThrows() = runTest {
        assertEquals(1, FeedbackStackState(maxVisible = 0).maxVisible)
        assertEquals(1, FeedbackStackState(maxVisible = -5).maxVisible)
        assertEquals(3, FeedbackStackState().maxVisible)
        assertEquals(7, FeedbackStackState(maxVisible = 7).maxVisible)
    }

    @Test
    fun resolvedCard_freesItsSlot_soNextArrivalEvictsNobody() = runTest {
        val state = FeedbackStackState(maxVisible = 3)
        val a = showAsync(state, indefinite("a"))
        val b = showAsync(state, indefinite("b"))
        val c = showAsync(state, indefinite("c"))
        runCurrent()
        assertEquals(3, state.items.size)
        val oldest = state.items.last()
        val middle = state.items[1]
        assertTrue(state.resolve(middle, FeedbackResult.Undone))

        val d = showAsync(state, indefinite("d"))
        runCurrent()

        assertEquals(3, state.items.size)
        assertFalse(oldest.result.isCompleted)
        assertEquals(listOf("d", "c", "a"), state.items.map { it.message })
        state.dismissAll()
        listOf(a, b, c, d).forEach { it.await() }
    }

    // ---- (d) racing pairs at one virtual instant ----

    @Test
    fun tapThenTimer_resultStaysOpened() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, FeedbackEvent.Notify("m", SnackbarDuration.Short, target = target))
        runCurrent()
        val item = state.items.single()

        advanceTimeBy(3999)
        runCurrent()
        assertTrue(state.resolve(item, FeedbackResult.Opened))
        advanceTimeBy(1)
        runCurrent()

        assertEquals(FeedbackResult.Opened, shown.await())
    }

    @Test
    fun timerThenTap_tapLosesAndResultIsTimedOut() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, FeedbackEvent.Notify("m", SnackbarDuration.Short, target = target))
        runCurrent()
        val item = state.items.single()

        advanceTimeBy(4000)
        runCurrent()

        assertEquals(FeedbackResult.TimedOut, shown.await())
        assertFalse(state.resolve(item, FeedbackResult.Opened))
    }

    @Test
    fun evictionThenUndo_undoLosesAndResultStaysDismissed() = runTest {
        val state = FeedbackStackState(maxVisible = 3)
        val first = showAsync(state, FeedbackEvent.WithUndo("1", undoAction = { }, duration = SnackbarDuration.Indefinite))
        runCurrent()
        val evicted = state.items.single()
        val others = (2..4).map { showAsync(state, indefinite("$it")) }
        runCurrent()

        assertFalse(state.resolve(evicted, FeedbackResult.Undone))
        assertEquals(FeedbackResult.Dismissed, first.await())
        state.dismissAll()
        others.forEach { it.await() }
    }

    @Test
    fun undoThenEviction_resolvedCardIsGone_soFourthArrivalEvictsNothingExtra() = runTest {
        val state = FeedbackStackState(maxVisible = 3)
        val a = showAsync(state, indefinite("a"))
        val b = showAsync(
            state,
            FeedbackEvent.WithUndo("b", undoAction = { }, duration = SnackbarDuration.Indefinite)
        )
        val c = showAsync(state, indefinite("c"))
        runCurrent()
        val oldest = state.items.last()
        val undoCard = state.items.first { it.message == "b" }
        assertTrue(state.resolve(undoCard, FeedbackResult.Undone))
        assertEquals(FeedbackResult.Undone, b.await())

        val d = showAsync(state, indefinite("d"))
        runCurrent()

        assertFalse(oldest.result.isCompleted)
        assertEquals(listOf("d", "c", "a"), state.items.map { it.message })
        state.dismissAll()
        listOf(a, c, d).forEach { it.await() }
    }

    @Test
    fun dismissAllThenTap_tapLoses() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, indefinite("m", withTarget = true))
        runCurrent()
        val item = state.items.single()

        state.dismissAll()

        assertFalse(state.resolve(item, FeedbackResult.Opened))
        assertEquals(FeedbackResult.Dismissed, shown.await())
    }

    // ---- (e) cancellation ----

    @Test
    fun cancelledShow_removesItem_freesSlot_andDeliversCancellationExceptionWithNoResult() = runTest {
        val state = FeedbackStackState(maxVisible = 1)
        var caught: CancellationException? = null
        var returned: FeedbackResult? = null
        val job = launch {
            try {
                returned = state.show(indefinite("m", withTarget = true))
            } catch (e: CancellationException) {
                caught = e
                throw e
            }
        }
        runCurrent()
        assertEquals(1, state.items.size)
        val item = state.items.single()

        job.cancel()
        runCurrent()

        assertNotNull(caught)
        assertNull(returned)
        assertTrue(state.items.isEmpty())
        assertFalse(state.resolve(item, FeedbackResult.Opened))

        // The slot is free: a new arrival is not evicted by a ghost.
        val next = showAsync(state, indefinite("next"))
        runCurrent()
        assertEquals(1, state.items.size)
        assertFalse(next.isCompleted)
        state.dismissAll()
        assertEquals(FeedbackResult.Dismissed, next.await())
    }

    @Test
    fun cancelledAsyncShow_throwsCancellationExceptionFromAwait() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, indefinite("m"))
        runCurrent()
        assertEquals(1, state.items.size)

        shown.cancel()
        runCurrent()

        var thrown: Throwable? = null
        try {
            shown.await()
        } catch (e: CancellationException) {
            thrown = e
        }
        assertNotNull(thrown)
        assertTrue(state.items.isEmpty())
    }

    // ---- (f) event mapping ----

    @Test
    fun notifyMapping_copiesFields_andHasNoUndo() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(
            state,
            FeedbackEvent.Notify(
                message = "Saved",
                duration = SnackbarDuration.Indefinite,
                target = target,
                supportingText = "Groceries"
            )
        )
        runCurrent()

        assertTrue(state.items.isNotEmpty())
        val item = state.items.single()
        assertEquals("Saved", item.message)
        assertEquals("Groceries", item.supportingText)
        assertEquals(target, item.target)
        assertFalse(item.hasUndo)
        assertTrue(item.hasControls)
        assertNull(item.baseMillis)
        assertNull(item.entryId)
        state.dismissAll()
        shown.await()
    }

    @Test
    fun shortNotifyMapping_hasFourSecondBase_andNoControlsWithoutTarget() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, FeedbackEvent.Notify("Saved"))
        runCurrent()

        assertTrue(state.items.isNotEmpty())
        val item = state.items.single()
        assertEquals(4_000L, item.baseMillis)
        assertFalse(item.hasControls)
        assertNull(item.target)
        assertNull(item.supportingText)
        state.dismissAll()
        shown.await()
    }

    @Test
    fun withUndoMapping_setsHasUndoAndLongMillis() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, FeedbackEvent.WithUndo("Deleted", undoAction = { }))
        runCurrent()

        assertTrue(state.items.isNotEmpty())
        val item = state.items.single()
        assertTrue(item.hasUndo)
        assertTrue(item.hasControls)
        assertEquals(10_000L, item.baseMillis)
        state.dismissAll()
        shown.await()
    }

    @Test
    fun dismissScreenMapping_isMessageOnly_shortDuration() = runTest {
        val state = FeedbackStackState()
        val shown = showAsync(state, FeedbackEvent.DismissScreen("Saved"))
        runCurrent()

        assertTrue(state.items.isNotEmpty())
        val item = state.items.single()
        assertEquals("Saved", item.message)
        assertNull(item.supportingText)
        assertNull(item.target)
        assertFalse(item.hasUndo)
        assertEquals(4_000L, item.baseMillis)
        assertNull(item.entryId)
        state.dismissAll()
        shown.await()
    }

    // ---- (g) the state never runs the event's undo lambda ----

    @Test
    fun plainShowWithUndo_reportsUndone_andNeverRunsTheUndoLambda() = runTest {
        val state = FeedbackStackState()
        val undoRuns = AtomicInteger(0)
        val shown = showAsync(
            state,
            FeedbackEvent.WithUndo(
                message = "Deleted",
                undoAction = { undoRuns.incrementAndGet() },
                duration = SnackbarDuration.Indefinite
            )
        )
        runCurrent()
        assertTrue(state.items.isNotEmpty())

        assertTrue(state.resolve(state.items.single(), FeedbackResult.Undone))

        assertEquals(FeedbackResult.Undone, shown.await())
        assertEquals(0, undoRuns.get())
    }

    // ---- (h) resolution is safe for inline-resuming callers and throwing scalers ----

    @Test
    fun inlineResumingCaller_reshowingFromContinuation_doesNotEvictAnOlderCard() = runTest {
        val state = FeedbackStackState(maxVisible = 2)
        val older = launch(Dispatchers.Unconfined) { state.show(indefinite("older")) }
        var second: FeedbackResult? = null
        val newer = launch(Dispatchers.Unconfined) {
            second = state.show(indefinite("newer"))
            // Resumed inline from inside resolve(): the newer card must already be off the stack.
            state.show(indefinite("again"))
        }
        assertEquals(listOf("newer", "older"), state.items.map { it.message })
        val olderItem = state.items.last()

        assertTrue(state.resolve(state.items.first(), FeedbackResult.Opened))

        assertEquals(FeedbackResult.Opened, second)
        assertFalse(olderItem.result.isCompleted)
        assertEquals(listOf("again", "older"), state.items.map { it.message })

        state.dismissAll()
        older.join()
        newer.join()
    }

    @Test
    fun throwingTimeoutScaler_stillShowsCard_withUnscaledTimer() = runTest {
        val state = FeedbackStackState()
        state.timeoutScaler = { _, _ -> error("platform call failed") }
        val shown = showAsync(state, FeedbackEvent.Notify(message = "m"))
        runCurrent()

        assertEquals(1, state.items.size)
        advanceTimeBy(4_000)
        runCurrent()

        assertEquals(FeedbackResult.TimedOut, shown.await())
        assertTrue(state.items.isEmpty())
    }
}
