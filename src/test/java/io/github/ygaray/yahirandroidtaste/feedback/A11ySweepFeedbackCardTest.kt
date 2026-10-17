package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Layer 1 of the scoped TalkBack sweep (A11Y-02, D-03 [talkback-method]) for the snackbar card: the
 * open label reaches the semantics tree byte for byte, and the locked forms (brief D-11: Undo is a
 * separate focusable child AND a custom action; brief D-07: both Dismiss forms are kept) are pinned.
 * C-3 and C-4 are designer decisions: this class records the baseline they would change, it never
 * changes them. The existing [FeedbackStackHostSemanticsTest] covers the rest of the card semantics.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class A11ySweepFeedbackCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var scope: CoroutineScope

    private val composed = "Eintrag öffnen"
    private val decomposed = "Eintrag öffnen"

    private fun setHost(state: FeedbackStackState) {
        composeTestRule.setContent {
            scope = rememberCoroutineScope()
            FeedbackStackHost(
                state = state,
                onOpenTarget = {},
                undoLabel = "Rückgängig",
                dismissLabel = "Schließen"
            )
        }
    }

    private fun notify(message: String, target: FeedbackTarget? = null) = FeedbackEvent.Notify(
        message = message,
        duration = SnackbarDuration.Indefinite,
        target = target
    )

    private fun withUndo(message: String, target: FeedbackTarget? = null) = FeedbackEvent.WithUndo(
        message = message,
        undoAction = {},
        duration = SnackbarDuration.Indefinite,
        target = target
    )

    private fun show(state: FeedbackStackState, event: FeedbackEvent) {
        scope.launch { state.show(event) }
        composeTestRule.waitForIdle()
    }

    private fun card(message: String): SemanticsNodeInteraction = composeTestRule.onNode(
        hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG) and hasText(message, substring = true)
    )

    private fun isDescendantOf(node: SemanticsNode, ancestorTag: String): Boolean {
        var current: SemanticsNode? = node.parent
        while (current != null) {
            if (current.config.getOrNull(SemanticsProperties.TestTag) == ancestorTag) return true
            current = current.parent
        }
        return false
    }

    @Test
    fun cardOpenLabel_passesThroughVerbatim_composedAndDecomposed() {
        assertNotEquals("the two Unicode forms are different strings", composed, decomposed)
        val state = FeedbackStackState()
        setHost(state)
        show(state, notify("Eins", FeedbackTarget("k-1", composed)))
        show(state, notify("Zwei", FeedbackTarget("k-2", decomposed)))
        show(state, notify("Drei", FeedbackTarget("k-3", "   ")))

        val labelOne = card("Eins").fetchSemanticsNode().config[SemanticsActions.OnClick].label
        val labelTwo = card("Zwei").fetchSemanticsNode().config[SemanticsActions.OnClick].label
        val labelThree = card("Drei").fetchSemanticsNode().config[SemanticsActions.OnClick].label

        assertEquals(composed, labelOne)
        assertEquals(decomposed, labelTwo)
        // Control: no normalization collapsed the two forms into one.
        assertNotEquals(labelOne, labelTwo)
        // A blank label falls back to the message.
        assertEquals("Drei", labelThree)
        state.dismissAll()
    }

    @Test
    fun lockedForms_undoIsASeparateFocusableChild_andACustomAction_andBothDismissFormsExist() {
        val state = FeedbackStackState()
        setHost(state)
        show(state, withUndo("Eintrag gelöscht", FeedbackTarget("k-1", composed)))

        // D-11: Undo is its own focusable child in the unmerged tree, and the card's merged text does not hold it.
        val undo = composeTestRule.onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
        assertNotNull("Undo is clickable on its own", undo.config.getOrNull(SemanticsActions.OnClick))
        assertTrue("Undo sits inside the card", isDescendantOf(undo, FEEDBACK_STACK_ITEM_TEST_TAG))
        val config = card("Eintrag gelöscht").fetchSemanticsNode().config
        val mergedTexts = config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
        assertTrue("merged card text does not carry the Undo label: $mergedTexts", "Rückgängig" !in mergedTexts)

        // D-11: it is also a custom action, and Undo precedes Dismiss in the action list.
        val labels = config[SemanticsActions.CustomActions].map { it.label }
        assertEquals(listOf("Rückgängig", "Schließen"), labels)

        // D-07: both Dismiss forms exist, the platform Dismiss action and the custom action.
        assertNotNull(config.getOrNull(SemanticsActions.Dismiss))
        assertEquals("Schließen", config[SemanticsActions.Dismiss].label)
        assertTrue("custom Dismiss action present", "Schließen" in labels)

        state.dismissAll()
        composeTestRule.waitForIdle()

        // Control: a Notify card has no Undo child and no Undo custom action, but keeps both Dismiss forms.
        show(state, notify("Nur Hinweis"))
        val plain = card("Nur Hinweis").fetchSemanticsNode().config
        assertEquals(listOf("Schließen"), plain[SemanticsActions.CustomActions].map { it.label })
        assertNotNull(plain.getOrNull(SemanticsActions.Dismiss))
        assertEquals(
            "no Undo child on a Notify card",
            0,
            composeTestRule.onAllNodes(hasTestTag(FEEDBACK_STACK_UNDO_TEST_TAG), useUnmergedTree = true)
                .fetchSemanticsNodes().size
        )
        state.dismissAll()
    }

    @Test
    fun deckOrder_isNewestFirst_andUndoPrecedesDismiss() {
        val state = FeedbackStackState()
        setHost(state)
        listOf("Ältester", "Mittlerer", "Neuester").forEach { show(state, withUndo(it, FeedbackTarget("k-$it", composed))) }

        val indices = listOf("Neuester", "Mittlerer", "Ältester")
            .map { card(it).fetchSemanticsNode().config[SemanticsProperties.TraversalIndex] }
        assertEquals("newest reads first", listOf(0f, 1f, 2f), indices)
        // Control: the order is not the arrival order.
        assertNotEquals(indices, indices.sorted().reversed())

        listOf("Neuester", "Mittlerer", "Ältester").forEach { message ->
            val labels = card(message).fetchSemanticsNode().config[SemanticsActions.CustomActions].map { it.label }
            assertEquals("$message: Undo precedes Dismiss", listOf("Rückgängig", "Schließen"), labels)
        }
        state.dismissAll()
    }
}
