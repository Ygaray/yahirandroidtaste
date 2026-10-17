package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.down
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric swipe tests for [FeedbackStackHost] (phase 26, plan 04, FSTK-05): a horizontal swipe
 * removes exactly the swiped card with [FeedbackResult.Dismissed] and never undoes anything.
 *
 * Drags are slow, 16 ms apart, because a release with exactly zero velocity picks the closest anchor
 * and ignores the 72dp threshold (probe A3b). Every event is Indefinite: the card timer runs on the
 * `show()` caller's dispatcher, not on the Compose clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackStackHostSwipeTest {

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

    private fun show(
        state: FeedbackStackState,
        event: FeedbackEvent,
        key: String,
        results: MutableMap<String, FeedbackResult>
    ) {
        scope.launch { results[key] = state.show(event) }
        composeTestRule.waitForIdle()
    }

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

    private fun cardCount(): Int =
        composeTestRule.onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNodes().size

    /** A point left of the middle, clear of the Undo button on the right of the row. */
    private fun TouchInjectionScope.grip() = Offset(width * 0.3f, height * 0.5f)

    /** Presses, moves [dxDp] in 16 ms steps (so the release carries a little velocity) and lifts. */
    private fun TouchInjectionScope.slowSwipe(dxDp: Float, steps: Int = 50) {
        down(grip())
        repeat(steps) { moveBy(Offset(dxDp.dp.toPx() / steps, 0f), delayMillis = 16) }
        up()
    }

    private fun swipe(message: String, dxDp: Float) {
        card(message).performTouchInput { slowSwipe(dxDp) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun swipeLeft_onFrontCard_dismissesOnlyThatCard() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        showAll(state, results, "First", "Second", "Third")
        assertEquals(3, cardCount())

        swipe("Third", -120f)

        assertEquals(FeedbackResult.Dismissed, results["Third"])
        assertNull(results["First"])
        assertNull(results["Second"])
        assertTrue(opened.isEmpty())
        assertEquals(2, cardCount())
        card("First").assertExists()
        card("Second").assertExists()
        assertEquals(listOf("Second", "First"), state.items.map { it.message })
        state.dismissAll()
    }
}
