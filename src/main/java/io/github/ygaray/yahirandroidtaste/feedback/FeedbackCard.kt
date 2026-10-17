package io.github.ygaray.yahirandroidtaste.feedback

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults
import io.github.ygaray.yahirandroidtaste.component.ActionButtonEmphasis
import io.github.ygaray.yahirandroidtaste.component.DynamicActionButton
import io.github.ygaray.yahirandroidtaste.theme.Dimens

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
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(FEEDBACK_STACK_ITEM_TEST_TAG)
            .semantics(mergeDescendants = true) {
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
                // travelled past the touch slop. detectTapGestures alone still reports a tap after a
                // drag that nothing consumes, and a drag release must never open or fan (T-26-10).
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
