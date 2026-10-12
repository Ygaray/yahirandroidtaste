package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.ToneColors

/**
 * The Receipt rendering of [OutcomeSheet] (VRCPT-01, D-04): a [SheetScaffold] with 24dp top corners
 * around [OutcomeSheetReceiptContent]. Internal: the public entry point is [OutcomeSheet] with
 * `style = OutcomeSheetStyle.Receipt`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OutcomeSheetReceipt(
    outcome: VoiceOutcomeUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    colors: OutcomeSheetColors
) {
    SheetScaffold(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = Dimens.OutcomeReceipt.CornerRadius,
            topEnd = Dimens.OutcomeReceipt.CornerRadius
        )
    ) {
        OutcomeSheetReceiptContent(outcome, colors = colors)
    }
}

/**
 * Scaffold-free Receipt content: a scrollable body region plus a pinned action bar. Kept internal
 * and [ColumnScope][androidx.compose.foundation.layout.ColumnScope]-free (it owns its outer
 * [Column]) so a height-bounded test can host it directly without driving a `ModalBottomSheet`.
 *
 * The bar is a SIBLING placed after the `weight(1f, fill = false)` scroll body, so it is measured
 * with its full intrinsic height first and a tall body can never starve it (the v2.4.1 rule).
 */
@Composable
internal fun OutcomeSheetReceiptContent(
    outcome: VoiceOutcomeUiState,
    modifier: Modifier = Modifier,
    colors: OutcomeSheetColors = OutcomeSheetDefaults.colors()
) {
    val barFollows = outcome is VoiceOutcomeUiState.NeedsConfirmation
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Dimens.HorizontalPadding,
                    end = Dimens.HorizontalPadding,
                    top = Dimens.OutcomeReceipt.BodyTopPadding,
                    bottom = if (barFollows) 0.dp else Dimens.HorizontalPadding
                )
        ) {
            when (outcome) {
                is VoiceOutcomeUiState.Success -> ReceiptSuccessBody(outcome, colors)
                is VoiceOutcomeUiState.Failure -> ReceiptFailureBody(outcome, colors)
                is VoiceOutcomeUiState.NeedsConfirmation -> ReceiptNeedsConfirmationBody(outcome, colors)
            }
        }
        when (outcome) {
            is VoiceOutcomeUiState.Success -> Unit
            is VoiceOutcomeUiState.Failure -> Unit
            is VoiceOutcomeUiState.NeedsConfirmation -> ReceiptNeedsConfirmationBar(outcome)
        }
    }
}

/** The glyph drawn inside the status badge circle for [badge]. */
internal fun outcomeBadgeIcon(badge: OutcomeBadge): ImageVector = when (badge) {
    OutcomeBadge.Success -> Icons.Filled.Check
    OutcomeBadge.Reverted -> Icons.AutoMirrored.Filled.Undo
    OutcomeBadge.Error -> Icons.Filled.Close
    OutcomeBadge.Warn -> Icons.Filled.PriorityHigh
}

/**
 * The circle (container) and glyph (content) colors of the status badge for [badge] (D-08). The
 * Warn badge swaps the tone: the circle is the tone's content color and the glyph its container.
 */
internal fun outcomeBadgeColors(badge: OutcomeBadge, colors: OutcomeSheetColors, scheme: ColorScheme): ToneColors =
    when (badge) {
        OutcomeBadge.Success -> ToneColors(scheme.primary, scheme.onPrimary)
        OutcomeBadge.Reverted -> ToneColors(scheme.onSurfaceVariant, scheme.surface)
        OutcomeBadge.Error -> colors.errorTone
        OutcomeBadge.Warn -> ToneColors(colors.warnTone.content, colors.warnTone.container)
    }

/**
 * The title row: optional badge, title (heading, at most two lines) and the caller's title
 * actions. Renders nothing when all three are absent.
 */
@Composable
private fun ReceiptTitleRow(
    title: String?,
    badge: OutcomeBadge?,
    titleActions: (@Composable () -> Unit)?,
    colors: OutcomeSheetColors
) {
    if (title == null && badge == null && titleActions == null) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.OutcomeReceipt.TitleRowGap)
    ) {
        badge?.let { ReceiptBadge(it, colors) }
        if (title != null) {
            Text(
                text = title,
                style = OutcomeSheetDefaults.ReceiptTitleStyle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
                    .testTag("outcome_sheet_title")
            )
        } else if (titleActions != null) {
            Spacer(Modifier.weight(1f))
        }
        titleActions?.let { actions ->
            Box(modifier = Modifier.testTag("outcome_sheet_title_actions")) { actions() }
        }
    }
}

/** The 32dp status badge: a filled circle holding the glyph for [badge]. */
@Composable
private fun ReceiptBadge(badge: OutcomeBadge, colors: OutcomeSheetColors) {
    val palette = outcomeBadgeColors(badge, colors, MaterialTheme.colorScheme)
    Box(
        modifier = Modifier
            .size(Dimens.OutcomeReceipt.BadgeSize)
            .background(palette.container, CircleShape)
            .testTag("outcome_sheet_badge"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = outcomeBadgeIcon(badge),
            contentDescription = null,
            tint = palette.content,
            modifier = Modifier.size(Dimens.OutcomeReceipt.BadgeIconSize)
        )
    }
}

/** The italic spoken-transcript line; callers pass it only for non-blank text. */
@Composable
private fun ReceiptTranscript(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(
                top = Dimens.OutcomeReceipt.TranscriptTopPadding,
                bottom = Dimens.OutcomeReceipt.TranscriptBottomPadding
            )
            .testTag("outcome_sheet_transcript")
    )
}

@Composable
private fun ReceiptSuccessBody(success: VoiceOutcomeUiState.Success, colors: OutcomeSheetColors) {
    ReceiptTitleRow(success.summary, success.badge, success.titleActions, colors)
}

@Composable
private fun ReceiptFailureBody(failure: VoiceOutcomeUiState.Failure, colors: OutcomeSheetColors) {
    ReceiptTitleRow(failure.title, failure.badge, null, colors)
}

@Composable
private fun ReceiptNeedsConfirmationBody(
    confirmation: VoiceOutcomeUiState.NeedsConfirmation,
    colors: OutcomeSheetColors
) {
    ReceiptTitleRow(confirmation.title, null, confirmation.titleActions, colors)
    if (confirmation.reason.isNotBlank()) ReceiptTranscript(confirmation.reason)
    confirmation.reversibilityHint?.let {
        Text(
            it,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    confirmation.topLevelContent?.let { content ->
        Box(modifier = Modifier.testTag("outcome_sheet_confirmation_top_level_content")) { content() }
    }
    confirmation.items.forEach { item -> ProposedItemRow(item) }
}

/** Pinned Cancel (Text) / Confirm (Filled) bar; keeps the Classic confirmation tags (D-13). */
@Composable
private fun ReceiptNeedsConfirmationBar(confirmation: VoiceOutcomeUiState.NeedsConfirmation) {
    ReceiptActionBar(
        start = {
            DynamicActionButton(
                label = confirmation.cancelLabel,
                role = ActionButtonDefaults.ActionButtonRole.Neutral,
                onClick = confirmation.onCancel,
                emphasis = ActionButtonEmphasis.Text,
                modifier = Modifier.testTag("outcome_sheet_confirmation_cancel")
            )
        },
        end = {
            DynamicActionButton(
                label = confirmation.confirmLabel,
                role = confirmation.severity,
                onClick = confirmation.onConfirm,
                enabled = confirmation.confirmEnabled,
                emphasis = ActionButtonEmphasis.Filled,
                modifier = Modifier.testTag("outcome_sheet_confirmation_confirm")
            )
        }
    )
}

/** Two actions spread to both edges; a lone end action stays at the end edge. */
private fun barArrangement(hasStart: Boolean, hasEnd: Boolean): Arrangement.Horizontal = when {
    hasStart && hasEnd -> Arrangement.SpaceBetween
    hasEnd -> Arrangement.End
    else -> Arrangement.Start
}

/** A 1dp divider above a start / end action row. */
@Composable
private fun ReceiptActionBar(
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().testTag("outcome_sheet_action_bar")) {
        HorizontalDivider(
            modifier = Modifier.testTag("outcome_sheet_action_bar_divider"),
            thickness = Dimens.HairlineBorder,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.HorizontalPadding,
                    end = Dimens.HorizontalPadding,
                    top = Dimens.OutcomeReceipt.BarTopPadding,
                    bottom = Dimens.OutcomeReceipt.BarBottomPadding
                ),
            horizontalArrangement = barArrangement(hasStart = start != null, hasEnd = end != null),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // P7 escape hatch: each slot shares the row (weight 1f, fill = false) so a long localized
            // label wraps instead of squeezing the other action out of the window.
            start?.let { Box(Modifier.weight(1f, fill = false)) { it() } }
            end?.let { Box(Modifier.weight(1f, fill = false)) { it() } }
        }
    }
}
