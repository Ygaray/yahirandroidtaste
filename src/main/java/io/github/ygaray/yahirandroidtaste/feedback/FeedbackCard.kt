package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import io.github.ygaray.yahirandroidtaste.component.ActionButtonEmphasis
import io.github.ygaray.yahirandroidtaste.component.DynamicActionButton
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import kotlinx.coroutines.flow.first

/** Test tag on a card's 8dp tone dot. */
internal const val FEEDBACK_STACK_DOT_TEST_TAG = "feedback_stack_dot"

/**
 * One Receipt-style card of the feedback deck (D-01): a `surfaceContainerHighest` surface with
 * 16dp corners and a level-2 shadow, an 8dp tone dot, the message, an optional second line, Undo
 * for a `WithUndo` event and a decorative chevron on a card that can still open its target.
 *
 * Gestures follow the MicButton split. One `detectTapGestures` owner handles the pointer path and
 * there is no `clickable` modifier: a tap on a card at the back of a collapsed deck ([collapsedBack])
 * fans the deck through [onPeekTap] and never opens, a tap on any other card opens its target through
 * [onBodyTap] when it is openable (has a target and is not spent), and a hold past the long-press
 * timeout does neither (D-03). The semantics path is separate: `onClick` is present only when the
 * card is openable and always opens, even on a collapsed back card, so TalkBack reaches the item
 * directly. The root merges its descendants, so the Undo and Dismiss accessibility actions stay on
 * the root, Undo first.
 *
 * A11Y-01: the root is one merged node with a Polite live region, the `dismiss` semantics action and
 * a `traversalIndex` equal to [index] (newest first, whatever the draw order). Every action runs the
 * same resolve-first callbacks as touch, and nothing the node announces changes after the card
 * arrives, so only the arriving card is read out. The Undo button stays a separate focusable child
 * in the unmerged tree and is also offered as a custom action.
 */
@Composable
internal fun FeedbackCard(
    item: FeedbackStackItem,
    undoLabel: String,
    dismissLabel: String,
    onBodyTap: (FeedbackTarget) -> Unit,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    collapsedBack: Boolean,
    onPeekTap: () -> Unit,
    index: Int,
    modifier: Modifier = Modifier
) {
    val target = item.target
    val spent = item.spent
    val openTarget = if (spent) null else target
    val openable = openTarget != null
    // Read through rememberUpdatedState so the gesture coroutine, keyed on the item id only, never
    // captures a stale value and a state flip mid-press cannot cancel or double-fire a gesture.
    val latestBodyTap by rememberUpdatedState(onBodyTap)
    val latestPeekTap by rememberUpdatedState(onPeekTap)
    val latestCollapsedBack by rememberUpdatedState(collapsedBack)
    val latestOpenTarget by rememberUpdatedState(openTarget)
    val travel = remember { PressTravel() }
    val density = LocalDensity.current
    // Seeded with the Settled anchor so the offset is a number from the very first frame; the Start
    // and End anchors arrive once the width is measured. Remembered per item id, so a list change
    // under a live gesture can never move the swipe onto another card.
    val drag = remember(item.id) {
        AnchoredDraggableState(
            initialValue = FeedbackCardAnchor.Settled,
            anchors = DraggableAnchors { FeedbackCardAnchor.Settled at 0f }
        )
    }
    val fling = AnchoredDraggableDefaults.flingBehavior(
        state = drag,
        positionalThreshold = { with(density) { FEEDBACK_SWIPE_THRESHOLD.toPx() } }
    )
    // A card that stops being swipeable mid-drag (a newer arrival pushed it to the back of a collapsed
    // deck) stays draggable until it settles. The flag flips twice per swipe, so it costs no
    // per-frame recomposition.
    val displaced by remember(drag) {
        derivedStateOf { (drag.offset.takeUnless { it.isNaN() } ?: 0f) != 0f }
    }
    val latestOnDismiss by rememberUpdatedState(onDismiss)
    // A settle away from the middle anchor means the card was swiped off. The only thing a swipe may
    // do is resolve the card Dismissed; it never runs Undo and never opens the target (FSTK-05).
    LaunchedEffect(drag) {
        snapshotFlow { drag.settledValue }.first { it != FeedbackCardAnchor.Settled }
        latestOnDismiss()
    }
    // The draggable sits on this stationary root. Only the Surface inside carries the translation,
    // because a draggable inside its own translated layer would see shrinking local deltas.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(FEEDBACK_STACK_ITEM_TEST_TAG)
            .semantics(mergeDescendants = true) {
                // One announcement per arriving card: every body is Polite and nothing it announces
                // changes after arrival (D-07 [live-region]); the order is newest first (A11Y-01).
                liveRegion = LiveRegionMode.Polite
                traversalIndex = index.toFloat()
                dismiss(label = dismissLabel) {
                    onDismiss()
                    true
                }
                if (openTarget != null) {
                    role = Role.Button
                    onClick(label = openTarget.openLabel.ifBlank { item.message }) {
                        latestBodyTap(openTarget)
                        true
                    }
                }
                customActions = buildList {
                    if (item.hasUndo && !spent) {
                        add(
                            CustomAccessibilityAction(undoLabel) {
                                onUndo()
                                true
                            }
                        )
                    }
                    add(
                        CustomAccessibilityAction(dismissLabel) {
                            onDismiss()
                            true
                        }
                    )
                }
            }
            .pointerInput(item.id) {
                // Not a second tap owner: it only records, without consuming, whether the finger
                // travelled past the touch slop. A drag release must never open or fan (T-26-10).
                val slop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    travel.pastSlop = false
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val own = event.changes.firstOrNull { it.id == down.id }
                        if (own != null && (own.position - down.position).getDistance() > slop) {
                            travel.pastSlop = true
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(item.id) {
                detectTapGestures(
                    // A long hold lets the user read; releasing it neither opens nor fans (D-03).
                    onLongPress = { },
                    onTap = {
                        if (!travel.pastSlop) {
                            if (latestCollapsedBack) {
                                latestPeekTap()
                            } else {
                                latestOpenTarget?.let { latestBodyTap(it) }
                            }
                        }
                    }
                )
            }
            .onSizeChanged { size ->
                if (size.width > 0) {
                    val width = size.width.toFloat()
                    drag.updateAnchors(
                        DraggableAnchors {
                            FeedbackCardAnchor.Start at -width
                            FeedbackCardAnchor.Settled at 0f
                            FeedbackCardAnchor.End at width
                        }
                    )
                }
            }
            .anchoredDraggable(
                state = drag,
                orientation = Orientation.Horizontal,
                enabled = !collapsedBack || displaced,
                flingBehavior = fling
            ),
        propagateMinConstraints = true
    ) {
        Surface(
            modifier = Modifier.graphicsLayer {
                // The one offset read: guarded, and inside the draw-phase lambda, so it never throws
                // and never recomposes the card per frame.
                val offset = drag.offset.takeUnless { it.isNaN() } ?: 0f
                translationX = offset
                alpha = feedbackSwipeAlpha(offset, size.width)
            },
            shape = RoundedCornerShape(Dimens.CornerRadius.Card),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shadowElevation = Dimens.Elevation.Level2
        ) {
            FeedbackCardRow(
                item = item,
                undoLabel = undoLabel,
                showUndo = item.hasUndo && !spent,
                showChevron = openable,
                onUndo = onUndo
            )
        }
    }
}

/** The three resting places of a swipe: off to the start side, in place, off to the end side. */
private enum class FeedbackCardAnchor { Start, Settled, End }

/** Whether the current press travelled past the touch slop; a plain holder, not snapshot state. */
private class PressTravel {
    var pastSlop = false
}

private val DotSize = 8.dp
private val SlotGap = 12.dp
private val CardMinHeight = 56.dp

/**
 * The card's content row: tone dot, body, optional Undo, optional chevron, in that logical order.
 * A custom layout instead of a Row because Undo must be capped at 40 percent of the row (widened to
 * its longest word, never beyond 60 percent) while the body takes the rest and wraps without a line
 * cap; the dot and the 24dp chevron stay fixed so nothing leaves the card (D-01 [card-layout]).
 */
@Composable
private fun FeedbackCardRow(
    item: FeedbackStackItem,
    undoLabel: String,
    showUndo: Boolean,
    showChevron: Boolean,
    onUndo: () -> Unit
) {
    val dotColor = if (item.target != null) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Layout(
        content = {
            Box(
                Modifier
                    .size(DotSize)
                    .background(dotColor, CircleShape)
                    .testTag(FEEDBACK_STACK_DOT_TEST_TAG)
            )
            Column {
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                item.supportingText?.let { supporting ->
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (showUndo) {
                Box {
                    DynamicActionButton(
                        label = undoLabel,
                        role = ActionButtonDefaults.ActionButtonRole.Neutral,
                        onClick = onUndo,
                        modifier = Modifier.testTag(FEEDBACK_STACK_UNDO_TEST_TAG),
                        emphasis = ActionButtonEmphasis.Text
                    )
                }
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(Dimens.ReceiptRow.ChevronSize)
                        .testTag(FEEDBACK_STACK_CHEVRON_TEST_TAG)
                )
            }
        },
        modifier = Modifier
            .defaultMinSize(minHeight = CardMinHeight)
            .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
    ) { measurables, constraints ->
        val dotMeasurable = measurables[0]
        val bodyMeasurable = measurables[1]
        var next = 2
        val undoMeasurable = if (showUndo) measurables[next++] else null
        val chevronMeasurable = if (showChevron) measurables[next] else null

        val gap = SlotGap.roundToPx()
        val dotPlaceable = dotMeasurable.measure(Constraints())
        val chevronPlaceable = chevronMeasurable?.measure(Constraints())
        val bounded = constraints.hasBoundedWidth
        val unbounded = Constraints.Infinity
        val rowWidth = if (bounded) {
            constraints.maxWidth
        } else {
            dotPlaceable.width + gap + bodyMeasurable.maxIntrinsicWidth(unbounded) +
                (undoMeasurable?.let { gap + it.maxIntrinsicWidth(unbounded) } ?: 0) +
                (chevronPlaceable?.let { gap + it.width } ?: 0)
        }
        val undoPlaceable = undoMeasurable?.measure(
            Constraints(
                maxWidth = feedbackUndoMaxWidth(
                    rowWidth = rowWidth,
                    naturalWidth = undoMeasurable.maxIntrinsicWidth(unbounded),
                    minIntrinsicWidth = undoMeasurable.minIntrinsicWidth(unbounded)
                )
            )
        )
        val gaps = (measurables.size - 1) * gap
        val bodyMax = (
            rowWidth - dotPlaceable.width - (undoPlaceable?.width ?: 0) -
                (chevronPlaceable?.width ?: 0) - gaps
            ).coerceAtLeast(0)
        val bodyPlaceable = bodyMeasurable.measure(Constraints(maxWidth = bodyMax))

        val ordered = listOfNotNull(dotPlaceable, bodyPlaceable, undoPlaceable, chevronPlaceable)
        val width = if (bounded) rowWidth else ordered.sumOf { it.width } + gaps
        val height = ordered.maxOf { it.height }.coerceIn(constraints.minHeight, constraints.maxHeight)
        val rtl = layoutDirection == LayoutDirection.Rtl
        layout(width, height) {
            var logicalX = 0
            ordered.forEach { placeable ->
                val x = if (rtl) width - logicalX - placeable.width else logicalX
                placeable.place(x, (height - placeable.height) / 2)
                logicalX += placeable.width + gap
            }
        }
    }
}
