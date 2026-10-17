package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Plain-JVM contract for [FeedbackStackState] (FSTK-01): no composition, virtual time only.
 *
 * Every test ends with its `show()` calls resolved or cancelled, because `runTest` waits for
 * its children and an unresolved Indefinite card would time the test out.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedbackStackStateTest {

    private val target = FeedbackTarget(key = "item-42", openLabel = "Open item")

    // ---- (a) exactly one result per show() ----

    @Test
    fun showThenResolveOpened_returnsOpenedAndRemovesItem() = runTest {
        val state = FeedbackStackState()
        val shown = async {
            state.show(FeedbackEvent.Notify("m", SnackbarDuration.Indefinite, target = target))
        }
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
        val shown = async { state.show(FeedbackEvent.Notify("m", SnackbarDuration.Indefinite)) }
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
            val shown = async { state.show(FeedbackEvent.Notify("m", SnackbarDuration.Indefinite)) }
            runCurrent()
            assertTrue(state.items.isNotEmpty())
            assertTrue(state.resolve(state.items.single(), result))
            assertEquals(result, shown.await())
        }
    }

    // ---- (b) mapping from events onto items ----

    @Test
    fun notifyMapping_copiesFields_andHasNoUndo() = runTest {
        val state = FeedbackStackState()
        val shown = async {
            state.show(
                FeedbackEvent.Notify(
                    message = "Saved",
                    duration = SnackbarDuration.Indefinite,
                    target = target,
                    supportingText = "Groceries"
                )
            )
        }
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
    fun withUndoMapping_setsHasUndoAndLongMillis() = runTest {
        val state = FeedbackStackState()
        val shown = async { state.show(FeedbackEvent.WithUndo("Deleted", undoAction = { })) }
        runCurrent()

        assertTrue(state.items.isNotEmpty())
        val item = state.items.single()
        assertTrue(item.hasUndo)
        assertTrue(item.hasControls)
        assertEquals(10_000L, item.baseMillis)
        state.dismissAll()
        shown.await()
    }
}
