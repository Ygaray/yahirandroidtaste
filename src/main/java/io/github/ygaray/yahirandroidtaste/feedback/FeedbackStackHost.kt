package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

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
 * - Each card has a Dismiss accessibility action labelled [dismissLabel] that resolves it as
 *   [FeedbackResult.Dismissed]. A `WithUndo` card that is not spent also has an Undo accessibility
 *   action labelled [undoLabel], so TalkBack can reach Undo even when a target makes the card
 *   clickable (which merges the Undo button into the card's semantics).
 *
 * Push events with `state.show(event)` or `state.emitTrackedWithUndo(store, event)`. The swipe
 * gesture, deck motion and pause are not part of this surface yet.
 *
 * @param state the stack state, usually from [rememberFeedbackStackState].
 * @param onOpenTarget called with the card's [FeedbackTarget] after a successful body tap.
 * @param modifier applied to the deck container holding the cards.
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
    var fanned by remember { mutableStateOf(false) }
    // The fan is a pause reason for every timer; the state lives beyond composition, so a host that
    // leaves while fanned or mid-press must not leave a reason set behind (T-26-11).
    var pointerDown by remember { mutableStateOf(false) }
    var interactionTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(fanned) { state.setFanned(fanned) }
    DisposableEffect(state) { onDispose { state.resetPause() } }
    FeedbackDeck(
        items = state.items,
        modifier = modifier,
        fanned = fanned,
        onPointerChange = { down ->
            state.setPointerDown(down)
            pointerDown = down
            interactionTick++
        }
    ) { item, index ->
        FeedbackCard(
            item = item,
            undoLabel = undoLabel,
            dismissLabel = dismissLabel,
            onBodyTap = { target ->
                if (state.resolve(item, FeedbackResult.Opened)) currentOnOpenTarget(target)
            },
            onUndo = { state.resolve(item, FeedbackResult.Undone) },
            onDismiss = { state.resolve(item, FeedbackResult.Dismissed) },
            collapsedBack = !fanned && index > 0,
            onPeekTap = { if (state.items.size > 1) fanned = true }
        )
    }
}
