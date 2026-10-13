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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.OutcomeBadge
import io.github.ygaray.yahirandroidtaste.model.SheetActionUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.ToneColors

/**
 * The Receipt rendering of [OutcomeSheet] (VRCPT-01, D-04): a [SheetScaffold] with 24dp top corners
 * around [OutcomeSheetReceiptContent]. Internal: the public entry point is [OutcomeSheet] with
 * `style = OutcomeSheetStyle.Receipt`, which forwards its (optionally hoisted) [sheetState].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OutcomeSheetReceipt(
    outcome: VoiceOutcomeUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    colors: OutcomeSheetColors,
    sheetState: SheetState
) {
    SheetScaffold(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
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
    val barFollows = receiptHasBar(outcome)
    // The body's own 16dp bottom padding is dropped whenever something already supplies the space
    // below the last section: the pinned bar, or the Success actions row (its own 16dp, IN-01).
    val bodyEndsFlush = barFollows || receiptSuccessHasActions(outcome)
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Dimens.HorizontalPadding,
                    end = Dimens.HorizontalPadding,
                    top = Dimens.OutcomeReceipt.BodyTopPadding,
                    bottom = if (bodyEndsFlush) 0.dp else Dimens.HorizontalPadding
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
            is VoiceOutcomeUiState.Failure -> ReceiptFailureBar(outcome)
            is VoiceOutcomeUiState.NeedsConfirmation -> ReceiptNeedsConfirmationBar(outcome)
        }
    }
}

/**
 * Whether a pinned bar follows the scroll body: always for a confirmation, for a Failure only when it
 * has an action or a secondary action (OQ-3), never for Success (its actions row scrolls).
 */
private fun receiptHasBar(outcome: VoiceOutcomeUiState): Boolean = when (outcome) {
    is VoiceOutcomeUiState.NeedsConfirmation -> true
    is VoiceOutcomeUiState.Failure -> outcome.action != null || outcome.secondaryAction != null
    is VoiceOutcomeUiState.Success -> false
}

/** Whether [outcome] is a Success that renders its actions row at the end of the scroll content. */
private fun receiptSuccessHasActions(outcome: VoiceOutcomeUiState): Boolean =
    outcome is VoiceOutcomeUiState.Success && (outcome.startAction != null || outcome.endAction != null)

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

/**
 * The Receipt Success body (D-08), in order: title row, handled-by pill, the caller's editable slot
 * (hidden while in flight, as in Classic and the model KDoc, OQ-1), the shared batch-result list, the
 * shared undo block (locked while in flight) and the actions row at the end of the scroll content.
 */
@Composable
private fun ReceiptSuccessBody(success: VoiceOutcomeUiState.Success, colors: OutcomeSheetColors) {
    ReceiptTitleRow(success.summary, success.badge, success.titleActions, colors)
    success.handledBy?.let { ReceiptHandledByPill(it, colors) }
    success.editableContent?.takeIf { !success.inFlight }?.invoke()
    success.batchResults.takeIf { it.isNotEmpty() }?.let { BatchResultsList(it) }
    success.undo?.let { UndoAffordanceBody(it, locked = success.inFlight) }
    ReceiptSuccessActions(success)
}

/**
 * Whether [action] is enabled: the caller's own `enabled` always wins, and while [inFlight] only an
 * action marked `activeWhileInFlight` (for example a Cancel) stays usable (T-22-15).
 */
internal fun actionEnabled(action: SheetActionUiModel, inFlight: Boolean): Boolean =
    action.enabled && (!inFlight || action.activeWhileInFlight)

/** The pill dot color: the warn tone's content color for an on-device result, else the primary role. */
internal fun handledByDotColor(onDevice: Boolean, colors: OutcomeSheetColors, scheme: ColorScheme): Color =
    if (onDevice) colors.warnTone.content else scheme.primary

/**
 * The parts that follow the tier in the pill, in the Classic secondary-line order: approach, provider,
 * model, then the escalation caption when a count is present (OQ-2).
 */
internal fun handledBySecondaryParts(handledBy: HandledByUiModel): List<String> = buildList {
    handledBy.approach?.let { add(it) }
    handledBy.provider?.let { add(it) }
    handledBy.model?.let { add(it) }
    handledBy.escalationCount?.let { add("${handledBy.escalationsLabel} $it") }
}

/**
 * The handled-by pill (D-10): a dot plus ONE string, so the merged node reads
 * "Handled by Local grammar · offline". The text wraps inside the pill rather than pushing the dot out.
 */
@Composable
private fun ReceiptHandledByPill(handledBy: HandledByUiModel, colors: OutcomeSheetColors) {
    val scheme = MaterialTheme.colorScheme
    val secondary = handledBySecondaryParts(handledBy)
    Row(
        modifier = Modifier
            .padding(top = Dimens.ContentSpacing)
            .background(scheme.surfaceContainerHighest, CircleShape)
            .padding(
                vertical = Dimens.OutcomeReceipt.PillVerticalPadding,
                horizontal = Dimens.OutcomeReceipt.PillHorizontalPadding
            )
            .semantics(mergeDescendants = true) {}
            .testTag("outcome_sheet_handled_by"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.OutcomeReceipt.DotSize)
                .background(handledByDotColor(handledBy.onDevice, colors, scheme), CircleShape)
                .testTag("outcome_sheet_handled_by_dot")
        )
        Spacer(Modifier.width(Dimens.OutcomeReceipt.DotGap))
        Text(
            text = buildAnnotatedString {
                append("${handledBy.prefixLabel} ")
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = scheme.onSurface)) {
                    append(handledBy.tier)
                }
                if (secondary.isNotEmpty()) append(" · ${secondary.joinToString(" · ")}")
            },
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

/** One action button from a [SheetActionUiModel]; [enabled] is already resolved by the caller. */
@Composable
private fun ReceiptSheetAction(action: SheetActionUiModel, enabled: Boolean, tag: String) {
    DynamicActionButton(
        label = action.label,
        role = action.role,
        onClick = action.onClick,
        enabled = enabled,
        emphasis = action.emphasis,
        icon = action.icon,
        modifier = Modifier.testTag(tag)
    )
}

/**
 * The Success actions row at the END of the scroll content (not pinned, no divider, not tagged as
 * the bar, OQ-3). Renders nothing when both actions are absent.
 */
@Composable
private fun ReceiptSuccessActions(success: VoiceOutcomeUiState.Success) {
    val startAction = success.startAction
    val endAction = success.endAction
    if (startAction == null && endAction == null) return
    ReceiptActionRow(
        start = startAction?.let {
            { ReceiptSheetAction(it, actionEnabled(it, success.inFlight), "outcome_sheet_action_start") }
        },
        end = endAction?.let {
            { ReceiptSheetAction(it, actionEnabled(it, success.inFlight), "outcome_sheet_action_end") }
        },
        modifier = Modifier.padding(
            top = Dimens.OutcomeReceipt.SuccessActionsTopPadding,
            bottom = Dimens.OutcomeReceipt.SuccessActionsBottomPadding
        )
    )
}

/** The card tone of a Failure (D-09): Warn paints the warn tone, every other tone the error tone. */
internal fun failureToneColors(tone: FailureTone, colors: OutcomeSheetColors): ToneColors =
    if (tone == FailureTone.Warn) colors.warnTone else colors.errorTone

/**
 * The Receipt Failure body (D-09, D-11), in order: title row outside the card, transcript, handled-by
 * pill, the tone card (reason plus the caller's body slot), the tip, and the shared undo block. The
 * pinned bar follows as a sibling of the scroll body ([ReceiptFailureBar]).
 */
@Composable
private fun ReceiptFailureBody(failure: VoiceOutcomeUiState.Failure, colors: OutcomeSheetColors) {
    ReceiptTitleRow(failure.title, failure.badge, null, colors)
    failure.transcript?.takeIf { it.isNotBlank() }?.let { ReceiptTranscript(it) }
    failure.handledBy?.let { ReceiptHandledByPill(it, colors) }
    ToneCard(
        tone = failureToneColors(failure.tone, colors),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.ContentSpacing)
            // Same VFAIL-03 merge as Classic: a non-blank prefix is announced with the reason on the
            // card's merged node; the bar buttons are not descendants, so they stay separate nodes.
            .then(
                if (!failure.semanticsPrefix.isNullOrBlank()) {
                    Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "${failure.semanticsPrefix.trim()} ${failure.reason}"
                    }
                } else {
                    Modifier
                }
            )
            .testTag("outcome_sheet_failure_surface")
    ) {
        Text(
            text = failure.reason,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        failure.body?.let { body -> ProvideTextStyle(MaterialTheme.typography.bodyMedium) { body() } }
    }
    failure.tip?.let { tip ->
        Box(modifier = Modifier.padding(top = Dimens.ContentSpacing).testTag("outcome_sheet_failure_tip")) { tip() }
    }
    failure.undo?.let { UndoAffordanceBody(it, locked = false) }
}

/**
 * The Failure pinned bar: the secondary action at the START and the role-hued action at the END
 * (D-09). Renders nothing, no bar and no divider, when both are absent (OQ-3).
 */
@Composable
private fun ReceiptFailureBar(failure: VoiceOutcomeUiState.Failure) {
    val action = failure.action
    val secondary = failure.secondaryAction
    if (action == null && secondary == null) return
    ReceiptActionBar(
        start = secondary?.let { { ReceiptSheetAction(it, it.enabled, "outcome_sheet_action_start") } },
        end = action?.let {
            {
                DynamicActionButton(
                    label = it.label,
                    role = it.role,
                    onClick = it.onClick,
                    emphasis = ActionButtonEmphasis.Filled,
                    icon = it.icon,
                    modifier = Modifier.testTag("outcome_sheet_action_end")
                )
            }
        }
    )
}

/**
 * The Receipt confirmation body, in order: title row, the optional handled-by pill (same place as on
 * Success), transcript (the reason), reversibility hint, the caller's top-level slot and the items.
 */
@Composable
private fun ReceiptNeedsConfirmationBody(
    confirmation: VoiceOutcomeUiState.NeedsConfirmation,
    colors: OutcomeSheetColors
) {
    ReceiptTitleRow(confirmation.title, null, confirmation.titleActions, colors)
    confirmation.handledBy?.let { ReceiptHandledByPill(it, colors) }
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

/**
 * Splits [available] px between a start and an end action whose natural (single-line, capped at
 * [available]) widths are [startNatural] and [endNatural]. When both fit, each keeps its natural
 * width. Otherwise an action that needs no more than half the row keeps its natural width and the
 * other takes everything that is left; if both need more than half, each gets half. Space one action
 * leaves unused is therefore never stranded (WR-01), and a short sibling never makes a long label
 * wrap early.
 */
internal fun receiptActionSlotWidths(available: Int, startNatural: Int, endNatural: Int): Pair<Int, Int> {
    val half = available / 2
    return when {
        startNatural + endNatural <= available -> startNatural to endNatural
        startNatural <= half -> startNatural to (available - startNatural)
        endNatural <= half -> (available - endNatural) to endNatural
        else -> half to (available - half)
    }
}

/**
 * The start / end action row shared by the pinned bar and the Success actions row. Two actions spread
 * to both edges; a lone end action stays at the end edge. Each slot is measured at its natural width
 * first and the row only constrains an action when the two do not fit together
 * ([receiptActionSlotWidths]), so a long localized label wraps instead of squeezing the other action
 * out of the window (P7 escape hatch) without wasting the space a short sibling leaves free.
 */
@Composable
private fun ReceiptActionRow(
    start: (@Composable () -> Unit)?,
    end: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier
) {
    val hasStart = start != null
    val hasEnd = end != null
    Layout(
        content = {
            start?.let { Box { it() } }
            end?.let { Box { it() } }
        },
        modifier = modifier.fillMaxWidth()
    ) { measurables, constraints ->
        val available = if (constraints.hasBoundedWidth) constraints.maxWidth else Constraints.Infinity
        val startMeasurable = if (hasStart) measurables.first() else null
        val endMeasurable = if (hasEnd) measurables.last() else null
        // Natural widths come from the intrinsics: a Measurable may only be measured once per pass.
        val startNatural = startMeasurable?.maxIntrinsicWidth(Constraints.Infinity)?.coerceAtMost(available)
        val endNatural = endMeasurable?.maxIntrinsicWidth(Constraints.Infinity)?.coerceAtMost(available)
        val (startMax, endMax) = when {
            available == Constraints.Infinity -> Constraints.Infinity to Constraints.Infinity
            startNatural != null && endNatural != null -> receiptActionSlotWidths(available, startNatural, endNatural)
            else -> available to available
        }
        val startPlaceable = startMeasurable?.measure(
            Constraints(maxWidth = startMax, maxHeight = constraints.maxHeight)
        )
        val endPlaceable = endMeasurable?.measure(
            Constraints(maxWidth = endMax, maxHeight = constraints.maxHeight)
        )

        val rowWidth = if (available == Constraints.Infinity) {
            (startPlaceable?.width ?: 0) + (endPlaceable?.width ?: 0)
        } else {
            available
        }
        val rowHeight = maxOf(startPlaceable?.height ?: 0, endPlaceable?.height ?: 0)
            .coerceIn(constraints.minHeight, constraints.maxHeight)
        val rtl = layoutDirection == LayoutDirection.Rtl
        layout(rowWidth, rowHeight) {
            // Logical x is measured from the start edge; mirror it for RTL.
            fun place(p: Placeable, logicalX: Int) {
                val x = if (rtl) rowWidth - logicalX - p.width else logicalX
                p.place(x, (rowHeight - p.height) / 2)
            }
            startPlaceable?.let { place(it, 0) }
            endPlaceable?.let { place(it, rowWidth - it.width) }
        }
    }
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
        ReceiptActionRow(
            start = start,
            end = end,
            modifier = Modifier.padding(
                start = Dimens.HorizontalPadding,
                end = Dimens.HorizontalPadding,
                top = Dimens.OutcomeReceipt.BarTopPadding,
                bottom = Dimens.OutcomeReceipt.BarBottomPadding
            )
        )
    }
}
