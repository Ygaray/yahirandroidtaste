package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.height
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Layout tests for the feedback deck (D-01, D-08). Tracer: three Indefinite targeted events shown
 * through [FeedbackStackState] render as a collapsed deck, the newest card lowest and each older
 * card's top edge one 14dp strip above the next newer card.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackDeckLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Open item")

    private lateinit var scope: CoroutineScope

    private fun setHost(state: FeedbackStackState) {
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            FeedbackStackHost(state = state, onOpenTarget = {})
        }
    }

    private fun notify(message: String, withTarget: Boolean = true) = FeedbackEvent.Notify(
        message = message,
        duration = SnackbarDuration.Indefinite,
        target = if (withTarget) target else null
    )

    private fun card(message: String): SemanticsNodeInteraction = composeTestRule.onNode(
        hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG) and hasAnyDescendant(hasText(message)),
        useUnmergedTree = true
    )

    private fun deck(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_DECK_TEST_TAG, useUnmergedTree = true)

    private fun showThree(state: FeedbackStackState) {
        setHost(state)
        listOf("Message A", "Message B", "Message C").forEach { message ->
            scope.launch { state.show(notify(message)) }
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun collapsedDeck_threeCards_frontIsLowest_andPeeksStep14dp() {
        val state = FeedbackStackState()
        showThree(state)

        val oldest = card("Message A").getUnclippedBoundsInRoot()
        val middle = card("Message B").getUnclippedBoundsInRoot()
        val newest = card("Message C").getUnclippedBoundsInRoot()
        val deckBounds = deck().getUnclippedBoundsInRoot()

        assertEquals(14f, (middle.top - oldest.top).value, 1f)
        assertEquals(14f, (newest.top - middle.top).value, 1f)
        assertTrue(oldest.top < middle.top && middle.top < newest.top)
        assertEquals(deckBounds.bottom.value, newest.bottom.value, 1f)
        assertEquals(28f, (deckBounds.height - newest.height).value, 1f)
        state.dismissAll()
    }

    @Test
    fun deckRendersNoTargetKeyInTextDescriptionOrTag() {
        val state = FeedbackStackState()
        showThree(state)

        val all = composeTestRule.onRoot(useUnmergedTree = true).fetchSemanticsNode().let(::flatten)
        all.forEach { node ->
            val text = node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
            val description = node.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty()
            val tag = node.config.getOrNull(SemanticsProperties.TestTag)
            (text + description + listOfNotNull(tag)).forEach { value ->
                assertFalse("node leaks the target key: $value", value.contains("item-42"))
            }
        }
        state.dismissAll()
    }

    private fun flatten(node: SemanticsNode): List<SemanticsNode> =
        listOf(node) + node.children.flatMap { flatten(it) }
}
