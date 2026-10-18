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
import kotlinx.coroutines.delay

/** Test tag on every card of [FeedbackStackHost]. */
internal const val FEEDBACK_STACK_ITEM_TEST_TAG = "feedback_stack_item"

/** Test tag on a card's Undo button. */
internal const val FEEDBACK_STACK_UNDO_TEST_TAG = "feedback_stack_undo"

/** Test tag on a card's trailing open chevron. */
internal const val FEEDBACK_STACK_CHEVRON_TEST_TAG = "feedback_stack_chevron"

/**
 * Renders the visible cards of a [FeedbackStackState] as a Receipt-card deck, newest first
 * (FSTK-01, FSTK-02, FSTK-07, A11Y-01).
 *
 * **Deck.** Up to the state's `maxVisible` cards (three by default) are shown. The newest card sits
 * in front and each older card tucks 14dp behind the next newer one, smaller and slightly faded.
 * Tapping a peek strip fans the deck into a column of full cards, oldest on top. The column
 * re-collapses after 4 s without a pointer event, and when one card or none is left. It is capped at
 * the smaller of 60 percent of the available height and what the on-screen keyboard leaves of it
 * (only the part of the keyboard that covers the host counts), and the cards scroll inside the cap,
 * newest in view. Handing the host a different state starts it collapsed.
 *
 * **Card.**
 * - A body tap on a card that carries a [FeedbackTarget] resolves the card as
 *   [FeedbackResult.Opened] and then calls [onOpenTarget] with the target. The callback runs only
 *   for the tap that actually resolved the card, so a double tap, or a tap racing the timer,
 *   navigates at most once. A hold past the long-press timeout neither opens nor fans.
 * - Undo (shown only for a `WithUndo` event) resolves the card as [FeedbackResult.Undone]. The
 *   host never runs an undo lambda; the tracked path (`emitTrackedWithUndo`) does that.
 * - A card without a target has no open action. A card whose backing undo entry was spent
 *   elsewhere stays on screen, hides Undo and the chevron, and its body is inert.
 * - A horizontal swipe, to either side, resolves a card as [FeedbackResult.Dismissed] and never
 *   undoes it. While the deck is collapsed only the front card can be swiped; while it is fanned
 *   every card can.
 *
 * **Timers.** Every card's timeout pauses while any pointer is down on the deck and while the deck
 * is fanned, and resumes when both clear, with at least 800 ms left.
 *
 * **Accessibility.** Each card is one merged node: `Role.Button` with [FeedbackTarget.openLabel] as
 * the click label when the card can open, and a Polite live region. Undo is a separate focusable
 * child and also a custom action labelled [undoLabel], listed before the Dismiss custom action
 * labelled [dismissLabel]; Dismiss is also exposed as the `dismiss` semantics action. Every action
 * resolves the card exactly like touch does. The cards stay in the semantics tree newest first,
 * collapsed or fanned.
 *
 * **Placement.** The host adds no window-inset padding of its own, so an M3 Scaffold snackbar slot or
 * an overlay `Box` places it. [modifier] applies to the deck container holding the cards.
 *
 * Push events with `state.show(event)` or `state.emitTrackedWithUndo(store, event)`.
 *
 * @param state the stack state, usually from [rememberFeedbackStackState].
 * @param onOpenTarget called with the card's [FeedbackTarget] after a successful body tap.
 * @param modifier applied to the deck container holding the cards.
 * @param undoLabel the Undo button text and Undo action label; pass a localized string.
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
    // The local flags belong to one state: handing the host a different state starts it collapsed and
    // with no pointer down, so the deck the user sees always matches the pause the state holds.
    var fanned by remember(state) { mutableStateOf(false) }
    // The fan is a pause reason for every timer; the state lives beyond composition, so a host that
    // leaves while fanned or mid-press must not leave a reason set behind (T-26-11).
    var pointerDown by remember(state) { mutableStateOf(false) }
    var interactionTick by remember(state) { mutableIntStateOf(0) }
    LaunchedEffect(state, fanned) { state.setFanned(fanned) }
    // Re-collapse after the idle delay. Every pointer event bumps interactionTick, which restarts the
    // wait, and a held finger (pointerDown) keeps the effect from counting at all.
    LaunchedEffect(state, fanned, interactionTick, pointerDown) {
        if (fanned && !pointerDown) {
            delay(state.idleCollapseMillis)
            fanned = false
        }
    }
    // One card or none has nothing to fan, so the deck folds back at once.
    LaunchedEffect(state, state.items.size) {
        if (state.items.size <= 1) fanned = false
    }
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
            onPeekTap = { if (state.items.size > 1) fanned = true },
            index = index,
            // The deck holds at most three cards, so a collapsed deck has exactly one middle strip.
            hiddenFromAccessibility = !fanned && index > 0 && index < state.items.size - 1
        )
    }
}
