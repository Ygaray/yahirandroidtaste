package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Plain-JVM contract for the tracked stack path, `FeedbackStackState.emitTrackedWithUndo`
 * (FSTK-07, FTGT-02): no composition, virtual time only.
 *
 * Every test ends with its tracked call resolved or cancelled, because `runTest` waits for its
 * children and an unresolved Indefinite card would time the test out.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackStackTrackedUndoTest {

    private val target = FeedbackTarget(key = "item-42", openLabel = "Open item")

    private fun withUndo(
        undone: AtomicInteger,
        duration: SnackbarDuration = SnackbarDuration.Indefinite,
        withTarget: Boolean = true
    ): FeedbackEvent.WithUndo = FeedbackEvent.WithUndo(
        message = "m",
        undoAction = { undone.incrementAndGet() },
        duration = duration,
        target = if (withTarget) target else null
    )

    private fun CoroutineScope.trackedAsync(
        state: FeedbackStackState,
        store: UndoHistoryStore,
        event: FeedbackEvent.WithUndo
    ): Deferred<FeedbackResult> = async { state.emitTrackedWithUndo(store, event) }

    // ---- (a) append before show, Undone goes through the store guard once ----

    @Test
    fun entryExistsAsSoonAsCardIsVisible_andMatchesItemEntryId() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone))
        runCurrent()

        assertEquals(1, store.entries.value.size)
        assertEquals(1, state.items.size)
        val entry = store.entries.value.single()
        assertEquals(target, entry.target)
        assertEquals(entry.id, state.items.single().entryId)

        state.dismissAll()
        shown.await()
    }

    @Test
    fun undoneResult_runsStoreUndoOnce_andEntryLosesOpenableTarget() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone))
        runCurrent()

        assertEquals(1, state.items.size)
        assertTrue(state.resolve(state.items.single(), FeedbackResult.Undone))
        assertEquals(FeedbackResult.Undone, shown.await())

        assertEquals(1, undone.get())
        val entry = store.entries.value.single()
        assertEquals(UndoStatus.Undone, entry.status)
        assertNull(entry.openableTarget)
        assertEquals(target, entry.target)
    }

    @Test
    fun openedResult_neverUndoes_andEntryStaysOpenable() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone))
        runCurrent()

        assertEquals(1, state.items.size)
        assertTrue(state.resolve(state.items.single(), FeedbackResult.Opened))
        assertEquals(FeedbackResult.Opened, shown.await())

        assertEquals(0, undone.get())
        val entry = store.entries.value.single()
        assertEquals(UndoStatus.Available, entry.status)
        assertEquals(target, entry.openableTarget)
    }

    @Test
    fun dismissedResult_neverUndoes_andEntryStaysOpenable() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone))
        runCurrent()

        assertEquals(1, state.items.size)
        state.dismissAll()
        assertEquals(FeedbackResult.Dismissed, shown.await())

        assertEquals(0, undone.get())
        val entry = store.entries.value.single()
        assertEquals(UndoStatus.Available, entry.status)
        assertEquals(target, entry.openableTarget)
    }

    @Test
    fun timedOutResult_neverUndoes_andEntryStaysOpenable() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone, SnackbarDuration.Long))
        runCurrent()

        assertEquals(1, state.items.size)
        advanceTimeBy(10_000)
        runCurrent()
        assertEquals(FeedbackResult.TimedOut, shown.await())

        assertEquals(0, undone.get())
        val entry = store.entries.value.single()
        assertEquals(UndoStatus.Available, entry.status)
        assertEquals(target, entry.openableTarget)
    }

    // ---- (b) races between the card's Undo and the Undo Center ----

    @Test
    fun raceUndoCenterFirst_thenCardUndo_lambdaRunsExactlyOnce() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone))
        runCurrent()

        assertEquals(1, state.items.size)
        val entryId = store.entries.value.single().id
        store.attemptUndo(entryId)
        assertTrue(state.resolve(state.items.single(), FeedbackResult.Undone))

        assertEquals(FeedbackResult.Undone, shown.await())
        assertEquals(1, undone.get())
        assertEquals(UndoStatus.Undone, store.entries.value.single().status)
    }

    @Test
    fun raceCardUndoFirst_thenUndoCenter_lambdaRunsExactlyOnce() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone))
        runCurrent()

        assertEquals(1, state.items.size)
        val entryId = store.entries.value.single().id
        assertTrue(state.resolve(state.items.single(), FeedbackResult.Undone))
        assertEquals(FeedbackResult.Undone, shown.await())
        store.attemptUndo(entryId)

        assertEquals(1, undone.get())
        assertEquals(UndoStatus.Undone, store.entries.value.single().status)
    }

    // ---- (c) spent card is kept, not auto-dismissed ----

    @Test
    fun entryUndoneElsewhere_flagsCardSpent_andKeepsIt() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone))
        runCurrent()

        assertEquals(1, state.items.size)
        val item = state.items.single()
        assertFalse(item.spent)
        store.attemptUndo(store.entries.value.single().id)
        runCurrent()

        assertTrue(item.spent)
        assertEquals(1, state.items.size)
        assertFalse(shown.isCompleted)

        state.dismissAll()
        assertEquals(FeedbackResult.Dismissed, shown.await())
    }

    @Test
    fun entryFailedElsewhere_flagsCardSpent() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val event = FeedbackEvent.WithUndo(
            message = "m",
            undoAction = { throw IllegalStateException("boom") },
            duration = SnackbarDuration.Indefinite,
            target = target
        )
        val shown = trackedAsync(state, store, event)
        runCurrent()

        assertEquals(1, state.items.size)
        val item = state.items.single()
        store.attemptUndo(store.entries.value.single().id)
        runCurrent()

        assertEquals(UndoStatus.Failed, store.entries.value.single().status)
        assertTrue(item.spent)
        assertEquals(1, state.items.size)

        state.dismissAll()
        shown.await()
    }

    // ---- (d) eviction ----

    @Test
    fun fourthArrivalEvictsTrackedCard_returnsDismissed_entryStaysAvailable() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val tracked = trackedAsync(state, store, withUndo(undone))
        runCurrent()
        val others = (1..3).map {
            async {
                state.show(
                    FeedbackEvent.Notify(message = "n$it", duration = SnackbarDuration.Indefinite)
                )
            }
        }
        runCurrent()

        assertEquals(FeedbackResult.Dismissed, tracked.await())
        assertEquals(0, undone.get())
        assertEquals(1, store.entries.value.size)
        assertEquals(UndoStatus.Available, store.entries.value.single().status)
        assertEquals(3, state.items.size)

        state.dismissAll()
        others.forEach { it.await() }
    }

    // ---- (e) cancellation ----

    @Test
    fun cancelBeforeResult_runsNoUndo_removesCard_entryStillUndoable() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        var rethrown = false
        val job = launch {
            try {
                state.emitTrackedWithUndo(store, withUndo(undone))
            } catch (e: CancellationException) {
                rethrown = true
                throw e
            }
        }
        runCurrent()
        assertEquals(1, state.items.size)

        job.cancel()
        runCurrent()

        assertTrue(rethrown)
        assertTrue(job.isCancelled)
        assertTrue(state.items.isEmpty())
        assertEquals(0, undone.get())
        assertEquals(UndoStatus.Available, store.entries.value.single().status)

        store.attemptUndo(store.entries.value.single().id)
        assertEquals(1, undone.get())
        assertEquals(UndoStatus.Undone, store.entries.value.single().status)
    }

    @Test
    fun cancelDuringStoreUndo_undoStillFinishes_entryEndsUndone() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val gate = CompletableDeferred<Unit>()
        val event = FeedbackEvent.WithUndo(
            message = "m",
            undoAction = {
                gate.await()
                undone.incrementAndGet()
            },
            duration = SnackbarDuration.Indefinite,
            target = target
        )
        val job = launch { state.emitTrackedWithUndo(store, event) }
        runCurrent()
        assertEquals(1, state.items.size)

        assertTrue(state.resolve(state.items.single(), FeedbackResult.Undone))
        runCurrent()
        // The store undo has started and is parked on the gate.
        assertEquals(0, undone.get())
        job.cancel()
        runCurrent()
        assertFalse(job.isCompleted)

        gate.complete(Unit)
        job.join()

        assertTrue(job.isCancelled)
        assertEquals(1, undone.get())
        assertEquals(UndoStatus.Undone, store.entries.value.single().status)
    }

    // ---- (f) failure, mapping, timeout, append count ----

    @Test
    fun throwingUndoLambda_callStillReturnsUndone_entryFailed() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val event = FeedbackEvent.WithUndo(
            message = "m",
            undoAction = { throw IllegalStateException("boom") },
            duration = SnackbarDuration.Indefinite,
            target = target
        )
        val shown = trackedAsync(state, store, event)
        runCurrent()

        assertEquals(1, state.items.size)
        assertTrue(state.resolve(state.items.single(), FeedbackResult.Undone))

        assertEquals(FeedbackResult.Undone, shown.await())
        assertEquals(UndoStatus.Failed, store.entries.value.single().status)
    }

    @Test
    fun eventWithoutTarget_entryAndCardCarryNullTarget() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone, withTarget = false))
        runCurrent()

        assertEquals(1, state.items.size)
        assertNull(store.entries.value.single().target)
        assertNull(state.items.single().target)

        state.dismissAll()
        shown.await()
    }

    @Test
    fun visibleCard_mapsEventFieldsAndEntryId() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val event = FeedbackEvent.WithUndo(
            message = "Deleted item-42",
            undoAction = { },
            supportingText = "from list",
            target = target
        )
        val shown = trackedAsync(state, store, event)
        runCurrent()

        assertEquals(1, state.items.size)
        val item = state.items.single()
        assertEquals("Deleted item-42", item.message)
        assertEquals("from list", item.supportingText)
        assertTrue(item.hasUndo)
        assertEquals(10_000L, item.baseMillis)
        assertEquals(store.entries.value.single().id, item.entryId)

        state.dismissAll()
        shown.await()
    }

    @Test
    fun trackedLongCard_timesOutAtExactly10000ms_withoutUndo() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val shown = trackedAsync(state, store, withUndo(undone, SnackbarDuration.Long))
        runCurrent()

        assertEquals(1, state.items.size)
        advanceTimeBy(9999)
        runCurrent()
        assertFalse(shown.isCompleted)
        assertEquals(1, state.items.size)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(FeedbackResult.TimedOut, shown.await())
        assertEquals(0, undone.get())
        assertEquals(UndoStatus.Available, store.entries.value.single().status)
    }

    @Test
    fun eachTrackedCall_appendsExactlyOneEntry() = runTest {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val undone = AtomicInteger()
        val calls = (1..3).map { trackedAsync(state, store, withUndo(undone)) }
        runCurrent()

        assertEquals(3, state.items.size)
        assertEquals(3, store.entries.value.size)
        assertEquals(3, store.entries.value.map { it.id }.toSet().size)

        state.dismissAll()
        calls.forEach { it.await() }
    }
}
