package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoAffordanceUiModel
import io.github.ygaray.yahirandroidtaste.model.UndoRowState
import io.github.ygaray.yahirandroidtaste.model.UndoRowUiModel
import io.github.ygaray.yahirandroidtaste.model.VoiceOutcomeUiState
import io.github.ygaray.yahirandroidtaste.theme.Dimens
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
 * A [VoiceOutcomeUiState.Failure] renders loudly on the theme's error/errorContainer color
 * roles — never via [AttentionCue], whose own KDoc forbids use as a failure signal — with an
 * optional action slot that renders iff the caller supplies one (D-08); a `null` action renders
 * NO action at all, never an implicit default (VOUT-03).
 *
 * @param outcome The command outcome to render.
 * @param onDismissRequest Invoked on scrim tap, back-press, or drag-to-dismiss.
 * @param modifier Modifier for the sheet's root composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutcomeSheet(
    outcome: VoiceOutcomeUiState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    SheetScaffold(onDismissRequest = onDismissRequest, modifier = modifier) {
        when (outcome) {
            is VoiceOutcomeUiState.Success -> SuccessBody(outcome)
            is VoiceOutcomeUiState.Failure -> FailureBody(outcome)
        }
    }
}

/**
 * Renders a [VoiceOutcomeUiState.Success]: the [VoiceOutcomeUiState.Success.summary] headline,
 * an optional [HandledByRow], an optional batch-results list (D-06; hidden when
 * [VoiceOutcomeUiState.Success.batchResults] is empty), an optional caller-supplied editor
 * slot (D-05; hidden when `null` or while [VoiceOutcomeUiState.Success.inFlight] is `true`), and
 * an optional grouped undo affordance (VUNDO-01; hidden when `null`) as the last child.
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
        success.undo?.let { UndoAffordanceBody(it) }
    }
}

/**
 * Renders a grouped undo affordance (VUNDO-01, D-01/D-02): an optional "Undo all (N)" action
 * button ([UndoAffordanceUiModel.onUndoAll] `null` hides it), every [UndoAffordanceUiModel.rows]
 * row in LIST order (never resorted), then an optional loud undo-refused/partial surface.
 */
@Composable
private fun UndoAffordanceBody(undo: UndoAffordanceUiModel) {
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
                modifier = Modifier.testTag("outcome_sheet_undo_all")
            )
        }
        undo.rows.forEach { row -> UndoRowItem(row) }
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
            ) {
                val suffix = refused.changedItem?.let { ", $it changed since" } ?: ""
                Text(
                    text = "Couldn't undo: ${refused.reason}$suffix",
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
 * muted "Undone" text with no click; [UndoRowState.Unavailable] renders its reason as trailing
 * `labelSmall` text, never clickable and never counted toward [UndoAffordanceUiModel.allLabel]'s
 * number.
 */
@Composable
private fun UndoRowItem(row: UndoRowUiModel) {
    val state = row.state
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.HairlineSpacing)
            // Merge this row's label + trailing Text child into ONE semantics node, mirroring
            // ApproachLadderCard's CapControl -- required so onAllNodesWithTag(...)'s indexed
            // access can resolve BOTH the row's label and its trailing text/click action
            // regardless of state (a clickable Modifier merges automatically; Undone/Unavailable
            // carry no clickable modifier and would otherwise stay unmerged).
            .semantics(mergeDescendants = true) {}
            .then(
                if (state is UndoRowState.Available) {
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
                text = "Undone",
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
private fun BatchResultsList(rows: List<BatchRowResultUiModel>) {
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
 * (VOUT-03) — the [VoiceOutcomeUiState.Failure.reason] headline, an optional [HandledByRow], and
 * an optional action button. This is the ONLY place an action ever renders — a `null`
 * [VoiceOutcomeUiState.Failure.action] renders nothing (never
 * [AttentionCue][io.github.ygaray.yahirandroidtaste.component.AttentionCue] and never an implicit
 * default action).
 */
@Composable
private fun FailureBody(failure: VoiceOutcomeUiState.Failure) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.expressive.cardShapeLarge,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outcome_sheet_failure_surface")
    ) {
        Column(modifier = Modifier.padding(Dimens.HorizontalPadding)) {
            Text(text = failure.reason, style = MaterialTheme.typography.headlineSmall)
            failure.handledBy?.let { HandledByRow(it) }
            failure.action?.let { action ->
                DynamicActionButton(
                    label = action.label,
                    role = ActionButtonDefaults.ActionButtonRole.Neutral,
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
            handledBy.escalationCount?.let { add("Escalations: $it") }
        }
        if (secondary.isNotEmpty()) {
            Text(text = secondary.joinToString(" · "), style = MaterialTheme.typography.labelSmall)
        }
    }
}
