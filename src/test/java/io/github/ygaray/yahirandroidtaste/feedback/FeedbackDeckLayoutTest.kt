package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
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

    // ---- Direct deck tests: simple tagged boxes of known heights (newest first) ----

    private fun deckItems(count: Int): List<FeedbackStackItem> =
        (0 until count).map { FeedbackStackItem(it.toLong(), "m$it", null, null, false, null, null) }

    /** Index 0 is the newest card, as in the host. */
    private fun setDeck(
        heights: List<Dp>,
        fanned: MutableState<Boolean> = mutableStateOf(false),
        host: @Composable (@Composable () -> Unit) -> Unit = { it() }
    ) {
        val items = deckItems(heights.size)
        composeTestRule.setContent {
            host {
                FeedbackDeck(items = items, fanned = fanned.value) { _, index ->
                    Box(Modifier.fillMaxWidth().height(heights[index]).testTag("c$index"))
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun box(index: Int): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag("c$index", useUnmergedTree = true)

    @Test
    fun fannedDeck_listsOldestAtTop_newestAtBottom_withEightDpGaps() {
        setDeck(listOf(60.dp, 60.dp, 60.dp), fanned = mutableStateOf(true))

        val newest = box(0).getUnclippedBoundsInRoot()
        val middle = box(1).getUnclippedBoundsInRoot()
        val oldest = box(2).getUnclippedBoundsInRoot()
        assertTrue(oldest.top < middle.top && middle.top < newest.top)
        assertEquals(68f, (middle.top - oldest.top).value, 1f)
        assertEquals(68f, (newest.top - middle.top).value, 1f)
        assertEquals(196f, deck().getUnclippedBoundsInRoot().height.value, 1f)
    }

    @Test
    fun fannedDeck_isCappedAtSixtyPercent_andScrollsInside() {
        setDeck(
            listOf(120.dp, 120.dp, 120.dp),
            fanned = mutableStateOf(true),
            host = { content -> Box(Modifier.height(300.dp)) { content() } }
        )

        assertTrue(deck().getUnclippedBoundsInRoot().height.value <= 181f)
        box(0).assertIsDisplayed()
        box(2).assertIsNotDisplayed()
        box(2).performScrollTo()
        box(2).assertIsDisplayed()
    }

    @Test
    fun fanToggle_returnsToCollapsedGeometry() {
        val fanned = mutableStateOf(false)
        setDeck(listOf(60.dp, 120.dp, 120.dp), fanned = fanned)
        fanned.value = true
        composeTestRule.waitForIdle()
        assertTrue(box(1).fetchSemanticsNode().size.height > box(0).fetchSemanticsNode().size.height)
        fanned.value = false
        composeTestRule.waitForIdle()

        val newest = box(0).getUnclippedBoundsInRoot()
        val middle = box(1).getUnclippedBoundsInRoot()
        val oldest = box(2).getUnclippedBoundsInRoot()
        assertEquals(14f, (middle.top - oldest.top).value, 1f)
        assertEquals(14f, (newest.top - middle.top).value, 1f)
        assertEquals(newest.height.value, middle.height.value, 1f)
        assertEquals(newest.height.value, oldest.height.value, 1f)
    }

    @Test
    fun backCardsAreClampedToFrontCardHeight_whenOlderCardIsTaller() {
        setDeck(listOf(60.dp, 120.dp, 120.dp))

        val front = box(0).fetchSemanticsNode().size.height
        assertEquals(front, box(1).fetchSemanticsNode().size.height)
        assertEquals(front, box(2).fetchSemanticsNode().size.height)
    }

    @Test
    @Config(qualifiers = "w800dp-h600dp")
    fun deckWidth_isCappedAt560dp_andCentered() {
        setDeck(listOf(60.dp, 60.dp))

        val bounds = box(0).getUnclippedBoundsInRoot()
        assertEquals(560f, bounds.width.value, 1f)
        assertEquals(120f, bounds.left.value, 1f)
        assertEquals(120f, 800f - bounds.right.value, 1f)
    }

    @Test
    fun unboundedHeightHost_doesNotCrash_andDoesNotCap() {
        setDeck(
            listOf(200.dp, 200.dp, 200.dp),
            fanned = mutableStateOf(true),
            host = { content ->
                Column(Modifier.verticalScroll(rememberScrollState())) { content() }
            }
        )

        (0..2).forEach { box(it).assertExists() }
        assertEquals(616f, deck().getUnclippedBoundsInRoot().height.value, 1f)
    }

    @Test
    fun emptyDeck_rendersNoCard_andTakesNoHeight() {
        setDeck(emptyList())

        box(0).assertDoesNotExist()
        assertEquals(0f, deck().getUnclippedBoundsInRoot().height.value, 0.5f)
    }

    @Test
    fun singleCard_hasNoPeek_andDeckHeightIsCardHeight() {
        setDeck(listOf(60.dp))

        val card = box(0).getUnclippedBoundsInRoot()
        val container = deck().getUnclippedBoundsInRoot()
        assertEquals(60f, container.height.value, 1f)
        assertEquals(container.top.value, card.top.value, 0.5f)
    }

    // ---- Host-level FSTK-02: the 4th arrival evicts the oldest ----

    @Test
    fun fourthArrival_evictsOldest_withDismissed_andTrackedEntryStaysInStore() {
        val state = FeedbackStackState(maxVisible = 3)
        val store = UndoHistoryStore()
        var firstResult: FeedbackResult? = null
        setHost(state)
        scope.launch {
            firstResult = state.emitTrackedWithUndo(
                store,
                FeedbackEvent.WithUndo(
                    message = "Message A",
                    undoAction = {},
                    duration = SnackbarDuration.Indefinite,
                    target = target
                )
            )
        }
        composeTestRule.waitForIdle()
        listOf("Message B", "Message C", "Message D").forEach { message ->
            scope.launch { state.show(notify(message)) }
            composeTestRule.waitForIdle()
        }

        assertEquals(FeedbackResult.Dismissed, firstResult)
        assertEquals(
            3,
            composeTestRule.onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG, useUnmergedTree = true)
                .fetchSemanticsNodes().size
        )
        card("Message A").assertDoesNotExist()
        assertEquals(UndoStatus.Available, store.entries.value.single().status)
        state.dismissAll()
    }

    private fun flatten(node: SemanticsNode): List<SemanticsNode> =
        listOf(node) + node.children.flatMap { flatten(it) }
}

