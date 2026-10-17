package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * First characterization test of the Material3 tracked path, [emitTrackedWithUndo] (Phase 25,
 * FTGT-02). None existed before: the helper was only exercised through consumers.
 *
 * Pins the unchanged v2.7.1 behavior (action label, duration passthrough, undo on action tap only,
 * never on dismiss) AND the new behavior (a [FeedbackTarget] on the event lands on the store entry
 * for the whole time the snackbar is up, and stops being openable once the entry is spent).
 *
 * [SnackbarHostState] needs no composition, so the snackbar is driven directly through
 * `currentSnackbarData`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UndoTrackingMaterialPathTest {

    private val target = FeedbackTarget(key = "item-42", openLabel = "Open")

    @Test
    fun targetedEvent_isInTheStoreWithItsTarget_whileTheSnackbarIsUp() = runTest {
        val store = UndoHistoryStore()
        val host = SnackbarHostState()
        val event = FeedbackEvent.WithUndo(message = "Deleted", undoAction = { }, target = target)

        val job = launch { host.emitTrackedWithUndo(store, event) }
        runCurrent()

        assertNotNull("snackbar must be showing", host.currentSnackbarData)
        val entry = store.entries.value.single()
        assertEquals(target, entry.target)
        assertEquals(UndoStatus.Available, entry.status)
        assertEquals(target, entry.openableTarget)

        host.currentSnackbarData!!.dismiss()
        runCurrent()
        job.join()
    }

    @Test
    fun actionPerformed_undoesOnce_andTheEntryStopsBeingOpenable() = runTest {
        val store = UndoHistoryStore()
        val host = SnackbarHostState()
        val runs = AtomicInteger(0)
        val event = FeedbackEvent.WithUndo(
            message = "Deleted",
            undoAction = { runs.incrementAndGet() },
            target = target,
        )

        val job = launch { host.emitTrackedWithUndo(store, event) }
        runCurrent()
        host.currentSnackbarData!!.performAction()
        runCurrent()
        job.join()

        assertEquals(1, runs.get())
        val entry = store.entries.value.single()
        assertEquals(UndoStatus.Undone, entry.status)
        assertNull("a spent entry must not be openable", entry.openableTarget)
        assertEquals("the original target is still carried", target, entry.target)
    }

    @Test
    fun dismissed_neverUndoes_andTheEntryStaysOpenable() = runTest {
        val store = UndoHistoryStore()
        val host = SnackbarHostState()
        val runs = AtomicInteger(0)
        val event = FeedbackEvent.WithUndo(
            message = "Deleted",
            undoAction = { runs.incrementAndGet() },
            target = target,
        )

        val job = launch { host.emitTrackedWithUndo(store, event) }
        runCurrent()
        host.currentSnackbarData!!.dismiss()
        runCurrent()
        job.join()

        assertEquals(0, runs.get())
        val entry = store.entries.value.single()
        assertEquals(UndoStatus.Available, entry.status)
        assertEquals(target, entry.openableTarget)
    }

    @Test
    fun untargetedEvent_behavesExactlyAsV271() = runTest {
        val store = UndoHistoryStore()
        val host = SnackbarHostState()
        val runs = AtomicInteger(0)
        val event = FeedbackEvent.WithUndo(message = "Deleted", undoAction = { runs.incrementAndGet() })

        val job = launch { host.emitTrackedWithUndo(store, event) }
        runCurrent()

        val before = store.entries.value.single()
        assertNull(before.target)
        assertNull(before.openableTarget)

        host.currentSnackbarData!!.performAction()
        runCurrent()
        job.join()

        assertEquals(1, runs.get())
        val after = store.entries.value.single()
        assertEquals(UndoStatus.Undone, after.status)
        assertNull(after.target)
        assertNull(after.openableTarget)
    }

    @Test
    fun undoCenterTapThenSnackbarAction_runsUndoExactlyOnce() = runTest {
        val store = UndoHistoryStore()
        val host = SnackbarHostState()
        val runs = AtomicInteger(0)
        val event = FeedbackEvent.WithUndo(
            message = "Deleted",
            undoAction = { runs.incrementAndGet() },
            target = target,
        )

        val job = launch { host.emitTrackedWithUndo(store, event) }
        runCurrent()
        val entryId = store.entries.value.single().id

        // The Undo Center tap wins the race; the snackbar action then arrives second.
        store.attemptUndo(entryId)
        host.currentSnackbarData!!.performAction()
        runCurrent()
        job.join()

        assertEquals("first-consumer-wins across the two tap sources", 1, runs.get())
        assertEquals(UndoStatus.Undone, store.entries.value.single().status)
    }

    @Test
    fun visibleSnackbar_keepsUndoLabel_duration_andMessage() = runTest {
        val store = UndoHistoryStore()
        val host = SnackbarHostState()
        val event = FeedbackEvent.WithUndo(
            message = "Deleted",
            undoAction = { },
            duration = SnackbarDuration.Indefinite,
            target = target,
        )

        val job = launch { host.emitTrackedWithUndo(store, event) }
        runCurrent()

        val visuals = host.currentSnackbarData!!.visuals
        assertEquals("Undo", visuals.actionLabel)
        assertEquals(SnackbarDuration.Indefinite, visuals.duration)
        assertEquals("Deleted", visuals.message)

        host.currentSnackbarData!!.dismiss()
        runCurrent()
        job.join()
    }
}
