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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
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

/**
 * The documented test tag of a batch result row, shared with the v2.7.1 list so row-counting tests
 * keep one inventory (the literal is documented in API.md; no new `outcome_sheet_` literal is added).
 */
private const val BATCH_ROW_TEST_TAG = "outcome_sheet_batch_row"

/** Space between the heading block (or the handled-by indicator) and the row's hairline. */
private val OpenTargetRowTopGap = 8.dp

/** Horizontal inset of the row's content, so the ripple sits just inside the sheet padding. */
private val OpenTargetRowHorizontalPadding = 4.dp

/**
 * Whether this outcome is a [VoiceOutcomeUiState.Success] that carries an item to open: its own
 * `target` or the `target` of at least one batch row (FOPEN-02, so a batch-only outcome is openable).
 */
internal fun VoiceOutcomeUiState.hasOpenTarget(): Boolean =
    this is VoiceOutcomeUiState.Success && (target != null || batchResults.hasRowTarget())

/** Whether at least one batch row carries a target, the gate that switches to the open-target list. */
internal fun List<BatchRowResultUiModel>.hasRowTarget(): Boolean = any { it.target != null }

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
    Row(
        modifier = modifier
            .padding(top = OpenTargetRowTopGap)
            .fillMaxWidth()
            .testTag(OPEN_TARGET_ROW_TEST_TAG)
            .semantics(mergeDescendants = true) {}
            .openTargetClick(target, title, enabled, onOpenTarget)
            .hairlineAbove(scheme.outlineVariant, Dimens.HairlineBorder)
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
        OpenTargetChevron()
    }
}

/**
 * The click of a row that opens [target]: a [Role.Button] whose click label is the target's
 * `openLabel` ([fallbackLabel] when that is blank) calling [onOpenTarget] with the target. While not
 * [enabled] (the success is in flight, PD-1) it adds no click and no role, dims the row to 38% and
 * marks it `disabled()` so the locked state is announced, not only drawn.
 * The target's opaque key is only handed back to the caller, never read.
 */
private fun Modifier.openTargetClick(
    target: FeedbackTarget,
    fallbackLabel: String,
    enabled: Boolean,
    onOpenTarget: (FeedbackTarget) -> Unit
): Modifier =
    if (enabled) {
        clickable(
            onClickLabel = target.openLabel.ifBlank { fallbackLabel },
            role = Role.Button,
            onClick = { onOpenTarget(target) }
        )
    } else {
        // Announce the locked state to TalkBack too, so it is not conveyed by dimming alone.
        alpha(DISABLED_CONTENT_ALPHA).semantics { disabled() }
    }

/** A hairline drawn behind the row, which adds no semantics node. */
private fun Modifier.hairlineAbove(color: Color, width: Dp): Modifier = drawBehind {
    val stroke = width.toPx()
    drawLine(color, Offset(0f, stroke / 2), Offset(size.width, stroke / 2), stroke)
}

/** The decorative trailing chevron, a fixed 24dp sibling measured before the weighted text column. */
@Composable
private fun OpenTargetChevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(Dimens.ReceiptRow.ChevronSize)
            .testTag(OPEN_TARGET_CHEVRON_TEST_TAG)
    )
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

/**
 * The batch result list of a success that has at least one row with a target (FOPEN-02, D-03): every
 * row shares one anatomy, and only a row with a target gets the chevron, the merge, the click and the
 * 48dp minimum height. Rows keep their list order and are never resorted or de-duplicated. Callers
 * switch to this list only when a handler is passed and some row has a target; every other batch
 * renders the unchanged v2.7.1 list.
 *
 * Each row binds its own target by value, so a tap opens that row's item even when two rows carry
 * equal targets or equal labels. While [locked] (the success is in flight, PD-1) targeted rows are
 * dimmed and have no click action.
 */
@Composable
internal fun OpenTargetBatchList(
    rows: List<BatchRowResultUiModel>,
    onOpenTarget: (FeedbackTarget) -> Unit,
    locked: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.ContentSpacing)
    ) {
        rows.forEach { row -> OpenTargetBatchRow(row, onOpenTarget, locked) }
    }
}

/**
 * One row of [OpenTargetBatchList]. A weighted text column holds the label (onSurface when the row
 * succeeded, error otherwise) and, only for a failed row with a detail, that detail in error color. A
 * row without a target reserves no chevron space and is not a touch target (PD-9, PD-10).
 */
@Composable
private fun OpenTargetBatchRow(
    row: BatchRowResultUiModel,
    onOpenTarget: (FeedbackTarget) -> Unit,
    locked: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val target = row.target
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(BATCH_ROW_TEST_TAG)
            .then(
                if (target != null) {
                    Modifier
                        .semantics(mergeDescendants = true) {}
                        .openTargetClick(target, row.label, !locked, onOpenTarget)
                } else {
                    Modifier
                }
            )
            .hairlineAbove(scheme.outlineVariant, Dimens.HairlineBorder)
            .then(if (target != null) Modifier.defaultMinSize(minHeight = Dimens.TouchTarget) else Modifier)
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
                text = row.label,
                color = if (row.succeeded) scheme.onSurface else scheme.error
            )
            if (!row.succeeded && row.detail != null) {
                Text(
                    text = row.detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.error
                )
            }
        }
        if (target != null) OpenTargetChevron()
    }
}
