package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
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
 * Robolectric gesture tests for the deck wiring of [FeedbackStackHost] (phase 26, plan 03): the
 * single-owner tap gesture, the peek-strip fan, long-hold suppression, the deck-wide pause hook and
 * the idle collapse. Every event is [SnackbarDuration.Indefinite] because the card timer runs on
 * the `show()` caller's dispatcher, not on the Compose clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackStackHostDeckTest {

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

    /** Shows [messages] oldest first, recording each card's result by message. */
    private fun showAll(
        state: FeedbackStackState,
        results: MutableMap<String, FeedbackResult> = mutableMapOf(),
        vararg messages: String
    ) {
        messages.forEach { message ->
            scope.launch { results[message] = state.show(notify(message)) }
            composeTestRule.waitForIdle()
        }
    }

    private fun card(message: String): SemanticsNodeInteraction = composeTestRule.onNode(
        hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG) and hasAnyDescendant(hasText(message)),
        useUnmergedTree = true
    )

    private fun deck(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_DECK_TEST_TAG, useUnmergedTree = true)

    @Test
    fun peekTap_fansDeck_neverOpens_andPausesTimers() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        val results = mutableMapOf<String, FeedbackResult>()
        setHost(state, opened)
        showAll(state, results, "First", "Second", "Third")

        assertTrue(!state.pauseState.value.fanned)

        // Pitfall 3: a centre click lands on the front card, so click inside the oldest strip.
        card("First").performTouchInput { click(Offset(width * 0.5f, 4.dp.toPx())) }
        composeTestRule.waitForIdle()

        assertTrue(opened.isEmpty())
        assertTrue(results.isEmpty())
        assertTrue(state.pauseState.value.fanned)

        val first = card("First").getUnclippedBoundsInRoot()
        val second = card("Second").getUnclippedBoundsInRoot()
        val third = card("Third").getUnclippedBoundsInRoot()
        assertTrue((second.top - first.top) >= first.height)
        assertTrue((third.top - second.top) >= second.height)
        // The deck is bottom-anchored: the newest card stays on the deck's bottom edge when it grows.
        assertEquals(deck().getUnclippedBoundsInRoot().bottom.value, third.bottom.value, 1f)
        assertNull(results["First"])
        state.dismissAll()
    }
}
