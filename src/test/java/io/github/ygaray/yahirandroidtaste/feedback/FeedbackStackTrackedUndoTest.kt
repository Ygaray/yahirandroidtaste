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
}
