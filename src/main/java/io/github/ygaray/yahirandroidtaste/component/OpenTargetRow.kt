package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.Dimens

/**
 * Test tag of the go-to-item row on a success sheet (FOPEN-01). Deliberately NOT prefixed
 * `outcome_sheet_`: the docs drift guard demands an API.md entry for every such literal, and this
 * row's docs are owned by a later phase (PD-3).
 */
internal const val OPEN_TARGET_ROW_TEST_TAG = "open_target_row"

/** Test tag of the row's trailing chevron; same `outcome_sheet_` exemption as [OPEN_TARGET_ROW_TEST_TAG]. */
internal const val OPEN_TARGET_CHEVRON_TEST_TAG = "open_target_chevron"

/** Space between the heading block (or the handled-by indicator) and the row's hairline. */
private val OpenTargetRowTopGap = 8.dp

/** Horizontal inset of the row's content, so the ripple sits just inside the sheet padding. */
private val OpenTargetRowHorizontalPadding = 4.dp

/** Whether this outcome is a [VoiceOutcomeUiState.Success] that carries an item to open. */
internal fun VoiceOutcomeUiState.hasOpenTarget(): Boolean =
    this is VoiceOutcomeUiState.Success && target != null

/** The row title: a non-blank `targetLabel`, else the `summary` (PD-5). */
internal fun VoiceOutcomeUiState.Success.openTargetTitle(): String =
    targetLabel?.takeIf { it.isNotBlank() } ?: summary

/** The optional second line: a non-blank `targetDetail`, else `null` (no node is drawn). */
internal fun VoiceOutcomeUiState.Success.openTargetDetail(): String? =
    targetDetail?.takeIf { it.isNotBlank() }

/**
 * The go-to-item row of a success sheet (FOPEN-01, brief D-10 and D-11): a title, an optional
 * detail line and a trailing decorative chevron, with no visible action label.
 *
 * It is ONE merged node. While [enabled] it is a [Role.Button] whose click label is the target's
 * `openLabel` (the [title] when that is blank) and it has no state description, unlike
 * [ReceiptRow], which always announces Expanded or Collapsed. While not [enabled] (the success is
 * in flight, PD-1) it is dimmed to 38% and has no click action and no role, but keeps its text and
 * its chevron.
 *
 * The click calls [onOpenTarget] and nothing else: the library never hides the sheet, dismisses
 * it or navigates (D-05). The row reads only the target's label, never its opaque key.
 *
 * The title and detail wrap without a line cap (the sheet body scrolls). The chevron is a fixed
 * sibling measured before the weighted text column, so a long localized title can never push it
 * out of the window (PD-4).
 */
@Composable
internal fun OpenTargetRow(
    title: String,
    detail: String?,
    target: FeedbackTarget,
    onOpenTarget: (FeedbackTarget) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    val hairline = scheme.outlineVariant
    val hairlineWidth = Dimens.HairlineBorder
    Row(
        modifier = modifier
            .padding(top = OpenTargetRowTopGap)
            .fillMaxWidth()
            .testTag(OPEN_TARGET_ROW_TEST_TAG)
            .semantics(mergeDescendants = true) {}
            .then(
                if (enabled) {
                    Modifier.clickable(
                        onClickLabel = target.openLabel.ifBlank { title },
                        role = Role.Button,
                        onClick = { onOpenTarget(target) }
                    )
                } else {
                    Modifier.alpha(DISABLED_CONTENT_ALPHA)
                }
            )
            // A hairline drawn behind the row adds no semantics node.
            .drawBehind {
                val stroke = hairlineWidth.toPx()
                drawLine(hairline, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
            }
            .defaultMinSize(minHeight = Dimens.TouchTarget)
            .padding(
                horizontal = OpenTargetRowHorizontalPadding,
                vertical = Dimens.ReceiptRow.VerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.ReceiptRow.ColumnGap)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.ReceiptRow.LineGap)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = scheme.onSurface
            )
            if (detail != null) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier
                .size(Dimens.ReceiptRow.ChevronSize)
                .testTag(OPEN_TARGET_CHEVRON_TEST_TAG)
        )
    }
}

/**
 * The success sheet's go-to-item row: a no-op unless [success] carries a target AND the caller
 * passed [onOpenTarget]; otherwise an [OpenTargetRow] locked while [VoiceOutcomeUiState.Success.inFlight]
 * (PD-1, because a write that is still running may not have created the item yet).
 */
@Composable
internal fun SuccessOpenTargetRow(
    success: VoiceOutcomeUiState.Success,
    onOpenTarget: ((FeedbackTarget) -> Unit)?
) {
    val target = success.target ?: return
    if (onOpenTarget == null) return
    OpenTargetRow(
        title = success.openTargetTitle(),
        detail = success.openTargetDetail(),
        target = target,
        onOpenTarget = onOpenTarget,
        enabled = !success.inFlight
    )
}
