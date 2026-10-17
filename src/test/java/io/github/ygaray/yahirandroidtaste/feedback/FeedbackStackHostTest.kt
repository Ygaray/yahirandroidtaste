package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
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
 * Robolectric tests for [FeedbackStackHost]. Every event uses [SnackbarDuration.Indefinite]
 * because the card timer runs on the `show()` caller's dispatcher, not on the Compose clock.
 *
 * Tracer: a manually constructed state shows a targeted card, the host renders it and a body tap
 * calls `onOpenTarget(target)` once and resolves Opened. The rest pin Undo, the caller-localized
 * labels, inert and spent cards, the click-label fallback and ordering.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackStackHostTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Open item")

    private lateinit var scope: CoroutineScope

    /** Composes [FeedbackStackHost] over [state], capturing a composition-bound scope. */
    private fun setHost(
        state: FeedbackStackState,
        opened: MutableList<FeedbackTarget> = mutableListOf(),
        undoLabel: String = "Undo",
        dismissLabel: String = "Dismiss"
    ) {
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            FeedbackStackHost(
                state = state,
                onOpenTarget = { opened += it },
                undoLabel = undoLabel,
                dismissLabel = dismissLabel
            )
        }
    }

    private fun notify(
        message: String,
        target: FeedbackTarget? = null,
        supportingText: String? = null
    ) = FeedbackEvent.Notify(
        message = message,
        duration = SnackbarDuration.Indefinite,
        target = target,
        supportingText = supportingText
    )

    private fun withUndo(
        message: String,
        target: FeedbackTarget? = null,
        undoAction: suspend () -> Unit = {}
    ) = FeedbackEvent.WithUndo(
        message = message,
        undoAction = undoAction,
        duration = SnackbarDuration.Indefinite,
        target = target
    )

    private fun tapBody() {
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).performTouchInput {
            click(Offset(width * 0.1f, height * 0.5f))
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun bodyTap_onTargetedCard_opensTargetOnceAndResolvesOpened() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        var result: FeedbackResult? = null
        setHost(state, opened)
        scope.launch { result = state.show(notify("Item saved", target)) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Item saved").assertExists()
        tapBody()

        assertEquals(listOf(target), opened)
        assertEquals(FeedbackResult.Opened, result)
    }

    @Test
    fun doubleTap_navigatesOnce_andResolvesOpened() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        var result: FeedbackResult? = null
        setHost(state, opened)
        scope.launch { result = state.show(notify("Item saved", target)) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).performTouchInput {
            click(Offset(width * 0.1f, height * 0.5f))
            click(Offset(width * 0.1f, height * 0.5f))
        }
        composeTestRule.waitForIdle()

        assertEquals(1, opened.size)
        assertEquals(FeedbackResult.Opened, result)
    }

    @Test
    fun undoTap_resolvesUndone_doesNotOpen_andNeverRunsTheUndoLambda() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        var result: FeedbackResult? = null
        var undoRuns = 0
        setHost(state, opened)
        scope.launch {
            result = state.show(withUndo("Item deleted", target) { undoRuns++ })
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Undone, result)
        assertTrue(opened.isEmpty())
        assertEquals(0, undoRuns)
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun germanLabels_renderAsPassed_andDismissActionResolvesDismissed() {
        val state = FeedbackStackState()
        var result: FeedbackResult? = null
        setHost(state, undoLabel = "Rückgängig", dismissLabel = "Schließen")
        scope.launch { result = state.show(withUndo("Eintrag gelöscht", target)) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Rückgängig", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Undo", useUnmergedTree = true).assertDoesNotExist()

        val actions = composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG)
            .fetchSemanticsNode().config[SemanticsActions.CustomActions]
        val dismiss = actions.single { it.label == "Schließen" }
        composeTestRule.runOnIdle { dismiss.action() }
        composeTestRule.waitForIdle()

        assertEquals(FeedbackResult.Dismissed, result)
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun cardWithoutTarget_hasNoClickAction_tapDoesNothing_andShowsNoChevron() {
        val state = FeedbackStackState()
        val opened = mutableListOf<FeedbackTarget>()
        var result: FeedbackResult? = null
        setHost(state, opened)
        scope.launch { result = state.show(notify("Plain note")) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).assertHasNoClickAction()
        tapBody()

        assertTrue(opened.isEmpty())
        assertNull(result)
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_CHEVRON_TEST_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun cardWithTarget_showsChevron_andSupportingTextWhenSet() {
        val state = FeedbackStackState()
        setHost(state)
        scope.launch { state.show(notify("Item saved", target, supportingText = "Draft")) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(FEEDBACK_STACK_CHEVRON_TEST_TAG, useUnmergedTree = true)
            .assertExists()
        composeTestRule.onNodeWithText("Draft", useUnmergedTree = true).assertExists()
    }

    @Test
    fun blankOpenLabel_fallsBackToTheCardMessage_nonBlankIsUsed() {
        val state = FeedbackStackState()
        setHost(state)
        scope.launch { state.show(notify("Item saved", FeedbackTarget("item-42", "   "))) }
        composeTestRule.waitForIdle()

        val blankLabel = composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG)
            .fetchSemanticsNode().config[SemanticsActions.OnClick].label
        assertEquals("Item saved", blankLabel)

        state.dismissAll()
        composeTestRule.waitForIdle()
        scope.launch { state.show(notify("Second note", target)) }
        composeTestRule.waitForIdle()

        val givenLabel = composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG)
            .fetchSemanticsNode().config[SemanticsActions.OnClick].label
        assertEquals("Open item", givenLabel)
    }

    @Test
    fun newestCardComesFirst() {
        val state = FeedbackStackState()
        setHost(state)
        scope.launch { state.show(notify("Message A", target)) }
        composeTestRule.waitForIdle()
        scope.launch { state.show(notify("Message B", target)) }
        composeTestRule.waitForIdle()

        val cards = composeTestRule.onAllNodesWithTag(
            FEEDBACK_STACK_ITEM_TEST_TAG,
            useUnmergedTree = true
        )
        cards[0].assert(hasAnyDescendant(hasText("Message B")))
        cards[1].assert(hasAnyDescendant(hasText("Message A")))
    }

    @Test
    fun spentCard_staysVisible_hidesUndoAndChevron_andIsInert() {
        val state = FeedbackStackState()
        val store = UndoHistoryStore()
        val opened = mutableListOf<FeedbackTarget>()
        var undoRuns = 0
        setHost(state, opened)
        scope.launch {
            state.emitTrackedWithUndo(store, withUndo("Item deleted", target) { undoRuns++ })
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .assertExists()
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_CHEVRON_TEST_TAG, useUnmergedTree = true)
            .assertExists()

        val entryId = store.entries.value.single().id
        scope.launch { store.attemptUndo(entryId) }
        composeTestRule.waitForIdle()

        assertEquals(1, undoRuns)
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_CHEVRON_TEST_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).assertHasNoClickAction()
        tapBody()
        assertTrue(opened.isEmpty())
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG).assertExists()
    }

    @Test
    fun hostWorksOverAManuallyConstructedState() {
        val state = FeedbackStackState(maxVisible = 2)
        setHost(state)
        scope.launch { state.show(notify("One")) }
        scope.launch { state.show(notify("Two")) }
        composeTestRule.waitForIdle()

        assertEquals(
            2,
            composeTestRule.onAllNodesWithTag(FEEDBACK_STACK_ITEM_TEST_TAG)
                .fetchSemanticsNodes().size
        )
    }
}
