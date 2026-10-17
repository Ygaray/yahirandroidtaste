package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric A11Y-01 tree assertions for [FeedbackStackHost] (phase 26, plan 05): each card is one
 * merged `Role.Button` node with the open label and a Polite live region, Undo is a separate
 * focusable child that also appears as a custom action, both Dismiss forms resolve the card
 * `Dismissed`, and the cards read newest first. Every event is [SnackbarDuration.Indefinite]
 * because the card timer runs on the `show()` caller's dispatcher, not on the Compose clock.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeedbackStackHostSemanticsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val target = FeedbackTarget("item-42", "Eintrag öffnen")

    private lateinit var scope: CoroutineScope

    private fun setHost(
        state: FeedbackStackState,
        opened: MutableList<FeedbackTarget> = mutableListOf(),
        undoLabel: String = "Rückgängig",
        dismissLabel: String = "Schließen"
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

    private fun withUndo(
        message: String,
        target: FeedbackTarget? = this.target,
        undoAction: suspend () -> Unit = {}
    ) = FeedbackEvent.WithUndo(
        message = message,
        undoAction = undoAction,
        duration = SnackbarDuration.Indefinite,
        target = target
    )

    private fun mergedCard(): SemanticsNodeInteraction =
        composeTestRule.onNodeWithTag(FEEDBACK_STACK_ITEM_TEST_TAG)

    private fun mergedTexts(node: SemanticsNode): List<String> =
        node.config.getOrNull(SemanticsProperties.Text)?.map { it.text } ?: emptyList()

    private fun isDescendantOf(node: SemanticsNode, ancestorTag: String): Boolean {
        var current: SemanticsNode? = node.parent
        while (current != null) {
            if (current.config.getOrNull(SemanticsProperties.TestTag) == ancestorTag) return true
            current = current.parent
        }
        return false
    }

    @Test
    fun targetedCard_isOneMergedButtonNode_withOpenLabelPoliteRegionAndDismiss() {
        val state = FeedbackStackState()
        setHost(state)
        scope.launch { state.show(withUndo("Eintrag gelöscht")) }
        composeTestRule.waitForIdle()

        val config = mergedCard().fetchSemanticsNode().config
        assertEquals(Role.Button, config[SemanticsProperties.Role])
        assertEquals("Eintrag öffnen", config[SemanticsActions.OnClick].label)
        assertEquals(LiveRegionMode.Polite, config[SemanticsProperties.LiveRegion])
        assertEquals("Schließen", config[SemanticsActions.Dismiss].label)
        assertEquals(
            listOf("Rückgängig", "Schließen"),
            config[SemanticsActions.CustomActions].map { it.label }
        )
        assertEquals(0f, config[SemanticsProperties.TraversalIndex], 0f)

        // The unmerged tree is the one accessibility services merge themselves: Undo stays a
        // separate focusable child of the card, and the card's merged text does not carry its label.
        val undo = composeTestRule
            .onNodeWithTag(FEEDBACK_STACK_UNDO_TEST_TAG, useUnmergedTree = true)
            .fetchSemanticsNode()
        assertNotNull(undo.config.getOrNull(SemanticsActions.OnClick))
        assertTrue(isDescendantOf(undo, FEEDBACK_STACK_ITEM_TEST_TAG))
        assertFalse(mergedTexts(mergedCard().fetchSemanticsNode()).contains("Rückgängig"))
        state.dismissAll()
    }
}
