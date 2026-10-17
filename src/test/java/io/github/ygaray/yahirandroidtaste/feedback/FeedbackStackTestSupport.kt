package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText

/**
 * The Undo button of the feedback card holding [message], from the unmerged tree (the merged card
 * hides it). A back card of a collapsed deck still composes and measures its Undo. Shared by the deck
 * gesture tests and the A11Y semantics tests so a tag rename is one edit.
 */
internal fun SemanticsNodeInteractionsProvider.undoOf(message: String): SemanticsNodeInteraction = onNode(
    hasTestTag(FEEDBACK_STACK_UNDO_TEST_TAG) and hasAnyAncestor(
        hasTestTag(FEEDBACK_STACK_ITEM_TEST_TAG) and hasAnyDescendant(hasText(message))
    ),
    useUnmergedTree = true
)
