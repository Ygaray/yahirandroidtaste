package io.github.ygaray.yahirandroidtaste.component

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
import io.github.ygaray.yahirandroidtaste.model.BatchRowResultUiModel
import io.github.ygaray.yahirandroidtaste.model.HandledByUiModel
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
 * [VoiceOutcomeUiState.Success.batchResults] is empty), and an optional caller-supplied editor
 * slot (D-05; hidden when `null` or while [VoiceOutcomeUiState.Success.inFlight] is `true`).
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
