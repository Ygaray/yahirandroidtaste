package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.FailureTone
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.ProposedItemUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoAffordanceUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.Dimens
import io.github.ygaray.yahirandroidtaste.theme.ToneColors
import io.github.ygaray.yahirandroidtaste.theme.expressive

/**
 * Prop-driven, presentational outcome/failure sheet for the Voice Command family (VOUT-01/02/03,
 * VUNDO-01). Renders a [VoiceOutcomeUiState] — [VoiceOutcomeUiState.Success] or
 * [VoiceOutcomeUiState.Failure] — from props alone: the library authors no app-specific noun of
 * its own (INV-01), never executes a voice command, and never decides success/failure itself.
 *
 * Rides [SheetScaffold]'s chrome (drag handle, window insets, single `.imePadding()` layer) —
 * mirrors [ListCardBottomSheet]'s "rides SheetScaffold" canon. [outcome]'s sealed `when` match is
 * exhaustive (compiler-enforced); Phase 12 adds a `NeedsConfirmation` third arm explicitly, never
 * silently (D-02).
 *
 * As of VOUT-04, [outcome] additionally renders [VoiceOutcomeUiState.NeedsConfirmation] via the
 * new `NeedsConfirmationBody` arm of the `when` below.
 *
 * A [VoiceOutcomeUiState.Failure] renders loudly on the theme's error/errorContainer color
 * roles — never via [AttentionCue], whose own KDoc forbids use as a failure signal — with an
 * optional action slot that renders iff the caller supplies one (D-08); a `null` action renders
 * NO action at all, never an implicit default (VOUT-03).
 *
 * @param outcome The command outcome to render.
 * @param onDismissRequest Invoked on scrim tap, back-press, or drag-to-dismiss.
 * @param modifier Modifier for the sheet's root composable.
 * @param style [OutcomeSheetStyle.Classic] (the default) is the v2.5.1 rendering unchanged;
 *   [OutcomeSheetStyle.Receipt] is the opt-in receipt layout.
 * @param colors The warn and error tone pairs; feeds the Warn failure card and the Receipt badges,
 *   cards and pill dot. Defaults to [OutcomeSheetDefaults.colors].
 * @param sheetState The sheet's [SheetState], passed to [SheetScaffold] in both styles. Hoist it
 *   (`rememberModalBottomSheetState(skipPartiallyExpanded = true)` next to the call) to slide a
 *   finished sheet down: call `hide()` on it for Close/Done/Cancel and clear the outcome only after
 *   it returns. `hide()` does not invoke [onDismissRequest]. Keep ONE call site per sheet so swapping
 *   [outcome] keeps the same state. Pass `skipPartiallyExpanded = true` as the overload without this
 *   parameter does (v2.7.1): without it a tall sheet opens at half height with its pinned action bar
 *   below the fold. Required and passed by name here; a caller that does not hoist the state uses
 *   the overload without it.
 * @param onOpenTarget Opens the item a success is about. The go-to-item row (title, optional
 *   detail, chevron, no action label) shows only when [outcome] is a [VoiceOutcomeUiState.Success]
 *   whose `target` is set AND this callback is non-null; a tap calls it once with that target.
 *   The library never hides the sheet, calls [onDismissRequest] or navigates (D-05): a caller that
 *   wants the slide-down hoists a [sheetState], calls `hide()` on it and then navigates. The handler
 *   must be idempotent, because the library does not debounce a double tap. A Classic or Receipt
 *   sheet without a target, or without this callback, renders exactly as in v2.7.1.
 *
 * Why an overload and not a defaulted parameter: [SheetState] is `@ExperimentalMaterial3Api`, and
 * an experimental type anywhere in a signature makes EVERY call of that function require the
 * opt-in, even calls that leave the parameter at its default. A defaulted `sheetState` on the
 * single entry point would therefore break each consumer call site that does not opt in. Only a
 * caller that passes `sheetState` reaches this overload, so only that caller needs the opt-in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutcomeSheet(
    outcome: VoiceOutcomeUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    style: OutcomeSheetStyle = OutcomeSheetStyle.Classic,
    colors: OutcomeSheetColors = OutcomeSheetDefaults.colors(),
    sheetState: SheetState,
    onOpenTarget: ((FeedbackTarget) -> Unit)? = null
) {
    if (style == OutcomeSheetStyle.Receipt) {
        OutcomeSheetReceipt(outcome, onDismissRequest, modifier, colors, sheetState, onOpenTarget)
        return
    }
    SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier, sheetState = sheetState) {
        // One scaffold call site on purpose: a caller that clears `target` after Undo then swaps only
        // this body and never re-creates the sheet window (PD-2).
        if (onOpenTarget != null && outcome is VoiceOutcomeUiState.Success && outcome.hasOpenTarget()) {
            OutcomeSheetContentOpenTarget(outcome, onOpenTarget)
        } else {
            OutcomeSheetContent(outcome, colors = colors)
        }
    }
}

/**
 * [OutcomeSheet] with the sheet's own internal [SheetState] (the v2.6.0 entry point, signature
 * unchanged, no opt-in needed). Identical to the hoisted-state overload above with
 * `sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)`: since v2.7.1 a tall
 * sheet opens fully expanded, so its pinned action bar is on screen at once (a short sheet opens at
 * its content height either way).
 *
 * @param outcome The command outcome to render.
 * @param onDismissRequest Invoked on scrim tap, back-press, or drag-to-dismiss.
 * @param modifier Modifier for the sheet's root composable.
 * @param style [OutcomeSheetStyle.Classic] (the default) or [OutcomeSheetStyle.Receipt].
 * @param colors The warn and error tone pairs. Defaults to [OutcomeSheetDefaults.colors].
 * @param onOpenTarget Opens the item a success is about; shows the go-to-item row only for a
 *   [VoiceOutcomeUiState.Success] whose `target` is set. The library never hides the sheet, calls
 *   [onDismissRequest] or navigates (D-05); hoist a sheet state (the overload with `sheetState`) to
 *   call `hide()` before navigating. The handler must be idempotent: a double tap is not debounced.
 *   Without a target or without this callback the sheet renders as in v2.7.1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutcomeSheet(
    outcome: VoiceOutcomeUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    style: OutcomeSheetStyle = OutcomeSheetStyle.Classic,
    colors: OutcomeSheetColors = OutcomeSheetDefaults.colors(),
    onOpenTarget: ((FeedbackTarget) -> Unit)? = null
) {
    OutcomeSheet(
        outcome = outcome,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        style = style,
        colors = colors,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        onOpenTarget = onOpenTarget
    )
}

/**
 * Scaffold-free sheet content (v2.4.1): a scrollable body region plus a pinned action footer.
 *
 * **Why this shape (CT Phase 74 defect).** OutcomeSheet's content sits inside a bounded-height
 * [ModalBottomSheet][androidx.compose.material3.ModalBottomSheet]. A plain [Column] measures each
 * non-weighted child with only the main-axis space LEFT by its earlier siblings, so when a branch's
 * body is taller than the sheet the trailing action [Row] (the last child) is measured with
 * near-zero remaining height — collapsing Confirm/Cancel to a ~10px sliver while
 * `minimumInteractiveComponentSize` still reports a 48dp touch target (the exact
 * 135px-touch / 10px-surface / label-absent signature measured on the TESTER). A `defaultMinSize`
 * or a fixed height can NOT fix this: a hard max-height constraint always wins over a child minimum.
 *
 * **The fix.** The body scrolls inside a `weight(1f, fill = false)` region — so short content still
 * wraps (today's look is unchanged) and tall content scrolls — while the action footer renders
 * OUTSIDE that region and is measured with its full intrinsic height first, so it can never be
 * starved. Kept `internal` + [ColumnScope]-free (owns its own outer [Column]) so a height-bounded
 * test can host it directly without driving a `ModalBottomSheet`; being `internal` it stays out of
 * the public Metalava API surface (api dump unchanged).
 */
@Composable
internal fun OutcomeSheetContent(
    outcome: VoiceOutcomeUiState,
    modifier: Modifier = Modifier,
    colors: OutcomeSheetColors = OutcomeSheetDefaults.colors()
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            when (outcome) {
                is VoiceOutcomeUiState.Success -> SuccessBody(outcome)
                is VoiceOutcomeUiState.Failure -> {
                    FailureBody(outcome, colors)
                    // D-11: the surface is edge-to-edge, so the undo block brings its own inset.
                    outcome.undo?.let { undo ->
                        Box(Modifier.padding(horizontal = Dimens.HorizontalPadding)) {
                            UndoAffordanceBody(undo, locked = false)
                        }
                    }
                }
                is VoiceOutcomeUiState.NeedsConfirmation -> NeedsConfirmationBody(outcome)
            }
        }
        // Pinned action footer — stays visible without scrolling. ONLY NeedsConfirmation pins its
        // Cancel/Confirm (a decision that must stay reachable). Failure's optional action and a
        // Success's variable-length undo affordance ride the scrollable body above (un-starved by
        // the scroll, with their Gate-2-passed short-content look unchanged — no footer, no reflow).
        when (outcome) {
            is VoiceOutcomeUiState.Success -> Unit
            is VoiceOutcomeUiState.Failure -> Unit
            is VoiceOutcomeUiState.NeedsConfirmation -> NeedsConfirmationActions(outcome)
        }
    }
}

/**
 * Renders a [VoiceOutcomeUiState.Success]: the [VoiceOutcomeUiState.Success.summary] headline,
 * an optional [HandledByRow], an optional batch-results list (D-06; hidden when
 * [VoiceOutcomeUiState.Success.batchResults] is empty), an optional caller-supplied editor
 * slot (D-05; hidden when `null` or while [VoiceOutcomeUiState.Success.inFlight] is `true`), and
 * an optional grouped undo affordance (VUNDO-01; hidden when `null`) as the last child, locked
 * (disabled, never hidden -- CR-02) while [VoiceOutcomeUiState.Success.inFlight] is `true`.
 */
@Composable
private fun SuccessBody(success: VoiceOutcomeUiState.Success) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.HorizontalPadding)
    ) {
        Text(text = success.summary, style = MaterialTheme.typography.headlineSmall)
        success.handledBy?.let { HandledByRow(it) }
        success.batchResults.takeIf { it.isNotEmpty() }?.let { BatchResultsList(it) }
        success.editableContent?.takeIf { !success.inFlight }?.invoke()
        success.undo?.let { UndoAffordanceBody(it, locked = success.inFlight) }
    }
}

/**
 * Renders a grouped undo affordance (VUNDO-01, D-01/D-02): an optional "Undo all (N)" action
 * button ([UndoAffordanceUiModel.onUndoAll] `null` hides it), every [UndoAffordanceUiModel.rows]
 * row in LIST order (never resorted), then an optional loud undo-refused/partial surface.
 *
 * @param locked When `true` (mirrors [VoiceOutcomeUiState.Success.inFlight], CR-02), every undo
 *   control renders disabled (dimmed, no click action) rather than hidden -- the affordance stays
 *   visible so the user can see what WILL be undoable once the in-flight batch write settles.
 */
@Composable
internal fun UndoAffordanceBody(undo: UndoAffordanceUiModel, locked: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.ContentSpacing)
    ) {
        undo.onUndoAll?.let { onUndoAll ->
            DynamicActionButton(
                label = undo.allLabel,
                role = ActionButtonDefaults.ActionButtonRole.Neutral,
                onClick = onUndoAll,
                enabled = !locked,
                modifier = Modifier.testTag("outcome_sheet_undo_all")
            )
        }
        undo.rows.forEach { row -> UndoRowItem(row, locked = locked) }
        undo.refused?.let { refused ->
            // Loud per VOUT-03's discipline: error-container surface, never AttentionCue.
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = MaterialTheme.expressive.cardShapeLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.ContentSpacing)
                    .testTag("outcome_sheet_undo_refused")
                    // TalkBack announces the surface when it appears; semantics only (VXR-01, D-12).
                    // Merging descendants gives the live-region node the message text: the change
                    // event of an unmerged live-region node has empty text and TalkBack stays
                    // silent (Gate-1 C-1, 28-07-SELF-UAT.md evidence 20/70).
                    .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Assertive }
            ) {
                val suffix = refused.changedItem
                    ?.let { ", $it ${refused.changedSinceSuffix}" }
                    ?: ""
                Text(
                    text = "${refused.refusedPrefix} ${refused.reason}$suffix",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(Dimens.HorizontalPadding)
                )
            }
        }
    }
}

/**
 * One row of a grouped undo affordance (VUNDO-01) — shares a single `testTag` across every row
 * (mirrors [ApproachLadderCard][io.github.ygaray.yahirandroidtaste.component.ApproachLadderCard]'s
 * shared-tag-per-row convention for ordered indexed test access). [UndoRowState.Available] wraps
 * the row in a clickable modifier invoking its own `onUndo`; [UndoRowState.Undone] renders trailing
 * muted [UndoRowUiModel.undoneLabel] text (default "Undone") with no click;
 * [UndoRowState.Unavailable] renders its reason as trailing
 * `labelSmall` text, never clickable and never counted toward [UndoAffordanceUiModel.allLabel]'s
 * number.
 *
 * @param locked When `true` (CR-02), an [UndoRowState.Available] row loses its clickable modifier
 *   and dims (standard Material3 disabled alpha, mirrors
 *   [DateTimePicker][io.github.ygaray.yahirandroidtaste.component.DateTimePicker]'s own
 *   enabled/disabled convention) rather than invoking `onUndo`. No-op for `Undone`/`Unavailable`,
 *   which are never clickable regardless.
 */
@Composable
private fun UndoRowItem(row: UndoRowUiModel, locked: Boolean) {
    val state = row.state
    val isAvailable = state is UndoRowState.Available
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.HairlineSpacing)
            .alpha(if (isAvailable && locked) 0.38f else 1f)
            // Merge this row's label + trailing Text child into ONE semantics node, mirroring
            // ApproachLadderCard's CapControl -- required so onAllNodesWithTag(...)'s indexed
            // access can resolve BOTH the row's label and its trailing text/click action
            // regardless of state (a clickable Modifier merges automatically; Undone/Unavailable
            // carry no clickable modifier and would otherwise stay unmerged).
            .semantics(mergeDescendants = true) {}
            .then(
                if (state is UndoRowState.Available && !locked) {
                    Modifier.clickable(onClick = state.onUndo)
                } else {
                    Modifier
                }
            )
            .testTag("outcome_sheet_undo_row")
    ) {
        Text(text = row.label, modifier = Modifier.weight(1f))
        when (state) {
            is UndoRowState.Available -> Unit
            is UndoRowState.Undone -> Text(
                text = row.undoneLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            is UndoRowState.Unavailable -> Text(
                text = state.reason,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Per-row batch result reporting (D-06) — each row's label plus success/fail status. */
@Composable
internal fun BatchResultsList(rows: List<BatchRowResultUiModel>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.ContentSpacing),
        verticalArrangement = Arrangement.spacedBy(Dimens.HairlineSpacing)
    ) {
        rows.forEach { row ->
            Row(modifier = Modifier.testTag("outcome_sheet_batch_row")) {
                Text(
                    text = row.label,
                    color = if (row.succeeded) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                if (!row.succeeded && row.detail != null) {
                    Text(
                        text = row.detail,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = Dimens.ContentSpacing)
                    )
                }
            }
        }
    }
}

/**
 * Renders a [VoiceOutcomeUiState.Failure] loudly on the theme's error-container color roles
 * (VOUT-03) — the [VoiceOutcomeUiState.Failure.reason] headline, an optional [HandledByRow], an
 * optional caller-supplied [VoiceOutcomeUiState.Failure.body] slot (VFAIL-02, placed after the
 * handled-by row and before the action), and an optional action button that renders with the
 * caller's `FailureActionUiModel.role` (VFAIL-01; default Neutral). This is the ONLY place an
 * action ever renders — a `null`
 * [VoiceOutcomeUiState.Failure.action] renders nothing (never
 * [AttentionCue][io.github.ygaray.yahirandroidtaste.component.AttentionCue] and never an implicit
 * default action). A non-blank [VoiceOutcomeUiState.Failure.semanticsPrefix] is prepended to the
 * reason in the surface's merged accessibility description (VFAIL-03, D-02).
 * Tone [FailureTone.Warn] paints the caller's warn tone, [FailureTone.Error] keeps the error-container look.
 */
@Composable
private fun FailureBody(failure: VoiceOutcomeUiState.Failure, colors: OutcomeSheetColors) {
    val surface = classicFailureSurfaceColors(failure.tone, colors, MaterialTheme.colorScheme)
    Surface(
        color = surface.container,
        contentColor = surface.content,
        shape = MaterialTheme.expressive.cardShapeLarge,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outcome_sheet_failure_surface")
            // VFAIL-03 / D-02: a non-blank caller prefix is announced by a combined description on
            // the surface's merged node — the (trimmed) prefix, one ASCII space, then the reason (plain
            // concatenation, no format parsing; punctuation belongs to the prefix). Merge-only, so
            // the action button stays its own focusable, clickable node. Null/blank adds nothing.
            .then(
                if (!failure.semanticsPrefix.isNullOrBlank()) {
                    Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "${failure.semanticsPrefix.trim()} ${failure.reason}"
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
            Text(text = failure.reason, style = MaterialTheme.typography.headlineSmall)
            failure.handledBy?.let { HandledByRow(it) }
            // VFAIL-02: optional caller slot, after the handled-by row and before the action. No
            // wrapper container, so a null body adds no node and no space; it rides the existing
            // scroll region in OutcomeSheetContent.
            failure.body?.invoke()
            // v2.4.1: the optional action (D-08) stays INSIDE the error surface (unchanged
            // short-content look, Gate-2-passed). The scroll body in OutcomeSheetContent is what
            // keeps it from starving on a tall sheet — no separate pinned footer for Failure.
            failure.action?.let { action ->
                DynamicActionButton(
                    label = action.label,
                    role = action.role,
                    onClick = action.onClick,
                    modifier = Modifier
                        .padding(top = Dimens.ContentSpacing)
                        .testTag("outcome_sheet_action_button")
                )
            }
        }
    }
}

/**
 * The Classic failure surface colors (D-06). [FailureTone.Warn] paints the caller's
 * [OutcomeSheetColors.warnTone]; every other tone keeps the v2.5.1 error-container look. The Error
 * branch reads the [scheme] roles directly because the [OutcomeSheetColors] error tone uses a
 * different content color and would change v2.5.1 pixels.
 */
internal fun classicFailureSurfaceColors(
    tone: FailureTone,
    colors: OutcomeSheetColors,
    scheme: ColorScheme
): ToneColors {
    if (tone == FailureTone.Warn) {
        return colors.warnTone
    }
    return ToneColors(scheme.errorContainer, scheme.onErrorContainer)
}

/**
 * Renders [handledBy]'s [HandledByUiModel.tier] as the primary label, appending
 * approach/provider/model/escalationCount as optional secondary caption text when present
 * (VOUT-02).
 */
@Composable
private fun HandledByRow(handledBy: HandledByUiModel) {
    Column(
        modifier = Modifier
            .padding(top = Dimens.ContentSpacing)
            .testTag("outcome_sheet_handled_by")
    ) {
        Text(text = handledBy.tier, style = MaterialTheme.typography.labelLarge)
        val secondary = buildList {
            handledBy.approach?.let { add(it) }
            handledBy.provider?.let { add(it) }
            handledBy.model?.let { add(it) }
            handledBy.escalationCount?.let { add("${handledBy.escalationsLabel} $it") }
        }
        if (secondary.isNotEmpty()) {
            Text(text = secondary.joinToString(" · "), style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Renders a [VoiceOutcomeUiState.NeedsConfirmation] (VOUT-04, D-01/D-02/D-03/D-05): an optional
 * [VoiceOutcomeUiState.NeedsConfirmation.title] headline, the
 * [VoiceOutcomeUiState.NeedsConfirmation.reason] body, an optional
 * [VoiceOutcomeUiState.NeedsConfirmation.reversibilityHint], every
 * [VoiceOutcomeUiState.NeedsConfirmation.items] row in LIST order (never resorted, never
 * merged/deduplicated), then a trailing Cancel/Confirm action row. The SAME render path handles
 * both a single item and a batch -- no size-based special casing.
 */
@Composable
private fun NeedsConfirmationBody(confirmation: VoiceOutcomeUiState.NeedsConfirmation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.HorizontalPadding)
    ) {
        confirmation.title?.let { Text(it, style = MaterialTheme.typography.headlineSmall) }
        Text(confirmation.reason, style = MaterialTheme.typography.bodyLarge)
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
}

/**
 * Pinned action footer for a [VoiceOutcomeUiState.NeedsConfirmation] (v2.4.1) — the Cancel/Confirm
 * row, rendered OUTSIDE the scrollable body (see [OutcomeSheetContent]) so a confirmation decision
 * is ALWAYS visible and tappable without scrolling, the exact affordance the CT Phase 74 defect
 * destroyed. [Modifier.fillMaxWidth] makes the `Alignment.End` arrangement actually right-align the
 * pair — the pre-patch wrap-content `Row` left-packed them against the sheet's start edge.
 */
@Composable
private fun NeedsConfirmationActions(confirmation: VoiceOutcomeUiState.NeedsConfirmation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.HorizontalPadding)
            .padding(top = Dimens.ContentSpacing),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing, Alignment.End)
    ) {
        DynamicActionButton(
            label = confirmation.cancelLabel,
            role = ActionButtonDefaults.ActionButtonRole.Neutral,
            onClick = confirmation.onCancel,
            modifier = Modifier.testTag("outcome_sheet_confirmation_cancel")
        )
        DynamicActionButton(
            label = confirmation.confirmLabel,
            role = confirmation.severity,
            onClick = confirmation.onConfirm,
            enabled = confirmation.confirmEnabled,
            modifier = Modifier.testTag("outcome_sheet_confirmation_confirm")
        )
    }
}

/**
 * One row of a [VoiceOutcomeUiState.NeedsConfirmation.items] list (VOUT-04) -- shares a single
 * `testTag` across every row (mirrors [UndoRowItem]'s shared-tag-per-row convention for ordered
 * indexed test access). Renders [ProposedItemUiModel.title], an optional
 * [ProposedItemUiModel.subtitle], and an optional [ProposedItemUiModel.confidenceCue].
 */
@Composable
internal fun ProposedItemRow(item: ProposedItemUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Merge this row's title/subtitle/confidenceCue text into ONE semantics node
            // (mirrors UndoRowItem's convention) so an indexed onAllNodesWithTag(...)[n] query can
            // resolve the row's own text via hasText(...). The remove IconButton keeps its own
            // distinct testTag and is queried separately via useUnmergedTree = true.
            .semantics(mergeDescendants = true) {}
            .testTag("outcome_sheet_confirmation_item"),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ContentSpacing)
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.title)
            item.subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
            item.confidenceCue?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
        }
        item.trailingContent?.invoke()
        item.onRemove?.let { onRemove ->
            IconButton(
                onClick = onRemove,
                modifier = Modifier.testTag("outcome_sheet_confirmation_item_remove")
            ) {
                Icon(Icons.Default.Close, contentDescription = item.removeContentDescription)
            }
        }
    }
}

/**
 * The Classic content of a [VoiceOutcomeUiState.Success] that carries an item to open (FOPEN-01):
 * [OutcomeSheetContent]'s scroll shape with [SuccessBodyOpenTarget] as the body. A new function, not
 * a branch inside [OutcomeSheetContent], so the pinned v2.5.1 Classic functions stay byte-equal.
 * Internal so a height-bounded test can host it without driving a `ModalBottomSheet`.
 */
@Composable
internal fun OutcomeSheetContentOpenTarget(
    success: VoiceOutcomeUiState.Success,
    onOpenTarget: (FeedbackTarget) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            SuccessBodyOpenTarget(success, onOpenTarget)
        }
    }
}

/**
 * The Classic Success body with the go-to-item row after the handled-by indicator (D-02): the
 * summary headline, [HandledByRow], the row, the batch list, the editor slot (hidden while in
 * flight) and the undo block (locked while in flight). It calls the pinned helpers; it never copies
 * their text.
 */
@Composable
private fun SuccessBodyOpenTarget(
    success: VoiceOutcomeUiState.Success,
    onOpenTarget: (FeedbackTarget) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.HorizontalPadding)
    ) {
        Text(text = success.summary, style = MaterialTheme.typography.headlineSmall)
        success.handledBy?.let { HandledByRow(it) }
        SuccessOpenTargetRow(success, onOpenTarget)
        if (success.batchResults.hasRowTarget()) {
            OpenTargetBatchList(success.batchResults, onOpenTarget, locked = success.inFlight)
        } else {
            success.batchResults.takeIf { it.isNotEmpty() }?.let { BatchResultsList(it) }
        }
        success.editableContent?.takeIf { !success.inFlight }?.invoke()
        success.undo?.let { UndoAffordanceBody(it, locked = success.inFlight) }
    }
}
