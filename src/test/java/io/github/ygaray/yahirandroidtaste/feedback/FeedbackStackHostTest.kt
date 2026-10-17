package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for [FeedbackStackHost]. Every event uses [SnackbarDuration.Indefinite]
 * because the card timer runs on the `show()` caller's dispatcher, not on the Compose clock.
 *
 * Tracer (Task 1): a manually constructed state shows a targeted card, the host renders it and a
 * body tap calls `onOpenTarget(target)` once and resolves Opened.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackStackHostTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bodyTap_onTargetedCard_opensTargetOnceAndResolvesOpened() {
        val state = FeedbackStackState()
        val target = FeedbackTarget("item-42", "Open item")
        val opened = mutableListOf<FeedbackTarget>()
        var result: FeedbackResult? = null
        lateinit var scope: CoroutineScope

        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            FeedbackStackHost(state = state, onOpenTarget = { opened += it })
        }
        scope.launch {
            result = state.show(
                FeedbackEvent.Notify(
                    message = "Item saved",
                    duration = SnackbarDuration.Indefinite,
                    target = target
                )
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Item saved").assertExists()
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).performTouchInput {
            click(Offset(width * 0.1f, height * 0.5f))
        }
        composeTestRule.waitForIdle()

        assertEquals(listOf(target), opened)
        assertEquals(FeedbackResult.Opened, result)
    }
}
