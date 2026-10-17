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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * One Receipt-style card of the feedback deck (D-01): a `surfaceContainerHighest` surface with
 * 16dp corners and a level-2 shadow, an 8dp tone dot, the message, an optional second line, Undo
 * for a `WithUndo` event and a decorative chevron on a card that can still open its target.
 *
 * The Phase 25 behavior is carried verbatim: the click modifier exists only for a targeted, unspent
 * card, and the semantics expose Undo (when available) and Dismiss as accessibility actions in that
 * order, because a clickable card merges its descendants and would otherwise hide the Undo button
 * from TalkBack.
 */
@Composable
internal fun FeedbackCard(
    item: FeedbackStackItem,
    undoLabel: String,
    dismissLabel: String,
    onBodyTap: (FeedbackTarget) -> Unit,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .fillMaxWidth()
            .testTag(FEEDBACK_STACK_ITEM_TEST_TAG)
            .semantics {
                // A targeted card is clickable, which merges its descendants, so the Undo button
                // is invisible to TalkBack there; expose Undo as an action on the card root too.
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
            .then(clickModifier),
        shape = RoundedCornerShape(Dimens.CornerRadius.Card),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = Dimens.Elevation.Level2
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
