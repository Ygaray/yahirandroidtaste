package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Test tag on every card of [FeedbackStackHost]. */
internal const val FEEDBACK_STACK_ITEM_TEST_TAG = "feedback_stack_item"

/** Test tag on a card's Undo button. */
internal const val FEEDBACK_STACK_UNDO_TEST_TAG = "feedback_stack_undo"

/** Test tag on a card's trailing open chevron. */
internal const val FEEDBACK_STACK_CHEVRON_TEST_TAG = "feedback_stack_chevron"

/**
 * Renders the visible cards of a [FeedbackStackState], newest first (FSTK-01, FSTK-07).
 *
 * - A body tap on a card that carries a [FeedbackTarget] resolves the card as
 *   [FeedbackResult.Opened] and then calls [onOpenTarget] with the target. The callback runs only
 *   for the tap that actually resolved the card, so a double tap, or a tap racing the timer,
 *   navigates at most once.
 * - Undo (shown only for a `WithUndo` event) resolves the card as [FeedbackResult.Undone]. The
 *   host never runs an undo lambda; the tracked path (`emitTrackedWithUndo`) does that.
 * - A card without a target has no click action. A card whose backing undo entry was spent
 *   elsewhere stays on screen, hides Undo and the chevron, and its body is inert.
 * - Each card has one Dismiss accessibility action labelled [dismissLabel] that resolves it as
 *   [FeedbackResult.Dismissed].
 *
 * Push events with `state.show(event)` or `state.emitTrackedWithUndo(store, event)`. The swipe
 * gesture, deck motion, pause and merged-button semantics are not part of this surface yet.
 *
 * @param state the stack state, usually from [rememberFeedbackStackState].
 * @param onOpenTarget called with the card's [FeedbackTarget] after a successful body tap.
 * @param modifier applied to the column holding the cards.
 * @param undoLabel the Undo button text; pass a localized string.
 * @param dismissLabel the label of each card's Dismiss accessibility action; pass a localized
 *   string.
 */
@Composable
fun FeedbackStackHost(
    state: FeedbackStackState,
    onOpenTarget: (FeedbackTarget) -> Unit,
    modifier: Modifier = Modifier,
    undoLabel: String = "Undo",
    dismissLabel: String = "Dismiss"
) {
    state.BindAccessibilityTimeout()
    val currentOnOpenTarget by rememberUpdatedState(onOpenTarget)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.items.forEach { item ->
            key(item.id) {
                FeedbackStackCard(
                    item = item,
                    undoLabel = undoLabel,
                    dismissLabel = dismissLabel,
                    onBodyTap = { target ->
                        if (state.resolve(item, FeedbackResult.Opened)) currentOnOpenTarget(target)
                    },
                    onUndo = { state.resolve(item, FeedbackResult.Undone) },
                    onDismiss = { state.resolve(item, FeedbackResult.Dismissed) }
                )
            }
        }
    }
}

@Composable
private fun FeedbackStackCard(
    item: FeedbackStackItem,
    undoLabel: String,
    dismissLabel: String,
    onBodyTap: (FeedbackTarget) -> Unit,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    val target = item.target
    val spent = item.spent
    val openable = target != null && !spent
    val clickModifier = if (openable && target != null) {
        Modifier.clickable(onClickLabel = target.openLabel.ifBlank { item.message }) {
            onBodyTap(target)
        }
    } else {
        Modifier
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(FEEDBACK_STACK_ITEM_TEST_TAG)
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(dismissLabel) {
                        onDismiss()
                        true
                    }
                )
            }
            .then(clickModifier),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val dotColor = if (target != null) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                item.supportingText?.let { supporting ->
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (item.hasUndo && !spent) {
                TextButton(
                    onClick = onUndo,
                    modifier = Modifier.testTag(FEEDBACK_STACK_UNDO_TEST_TAG)
                ) {
                    Text(undoLabel)
                }
            }
            if (openable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(FEEDBACK_STACK_CHEVRON_TEST_TAG)
                )
            }
        }
    }
}
