package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Composable
import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults

/**
 * Render-only outcome state for a voice command result
 * ([io.github.ygaray.yahirandroidtaste.component.OutcomeSheet], VOUT-01/02/03). This describes
 * WHAT happened after a voice command — success or failure — and HOW the sheet should render it;
 * it is display state the consumer computes and hoists in. The library never executes a voice
 * command, interprets a transcript, or decides success/failure itself (INV-01).
 *
 * Exactly two arms this phase (D-02): [Success] and [Failure]. The top level MUST stay stable —
 * Phase 12 adds a `NeedsConfirmation` third arm purely additively (a new sealed subtype), never a
 * reshape of either arm below. Do not guess at or pre-model that third arm here.
 */
sealed interface VoiceOutcomeUiState {

    /**
     * A voice command that succeeded — possibly as part of a batch, possibly still
     * committed-but-editable (D-05), possibly carrying undo affordances (VUNDO-01, D-01/D-02).
     *
     * @param summary Caller-formatted headline text (e.g. "Logged 2 of 3"). Required.
     * @param handledBy Provenance indicator (tier/approach/provider/model/escalation), or `null`
     *   to hide the indicator entirely (VOUT-02; null-prop-hides convention, see
     *   [ApproachLadderCard][io.github.ygaray.yahirandroidtaste.component.ApproachLadderCard]'s
     *   own KDoc).
     * @param editableContent A caller-supplied composable rendered verbatim for a
     *   committed-but-editable success (D-05) — the library defines no editable fields of its
     *   own, it only reserves and renders this slot. Hidden when `null` OR while [inFlight] is
     *   `true`.
     * @param inFlight Display-only batch-write-in-flight lock signal (D-06) — the sheet uses this
     *   to disable its own rendered undo controls and suppress [editableContent]; the library
     *   never infers this from any other field.
     * @param batchResults Per-row batch result reporting (D-06) — empty hides the section
     *   entirely (a single-item outcome has nothing to list here).
     * @param undo A grouped undo affordance (VUNDO-01, D-01/D-02) — "Undo all (N)" plus per-item
     *   rows, built by the CONSUMER from [io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryStore]
     *   reads. `null` renders no undo section at all; undo lives ON [Success] rather than a new
     *   top-level sealed arm so the top-level type stays additive-ready for Phase 12.
     */
    data class Success(
        val summary: String,
        val handledBy: HandledByUiModel? = null,
        val editableContent: (@Composable () -> Unit)? = null,
        val inFlight: Boolean = false,
        val batchResults: List<BatchRowResultUiModel> = emptyList(),
        val undo: UndoAffordanceUiModel? = null
    ) : VoiceOutcomeUiState

    /**
     * A voice command that failed. Renders loudly, on the theme's error/errorContainer color
     * roles — never via
     * [AttentionCue][io.github.ygaray.yahirandroidtaste.component.AttentionCue], whose own KDoc
     * forbids use as a failure signal.
     *
     * @param reason Caller-formatted, human-readable failure reason. Required.
     * @param handledBy Provenance indicator, or `null` to hide it entirely (VOUT-02).
     * @param action An optional label+callback action slot (D-08) — e.g. "Open Settings" for a
     *   missing/invalid key, or "Retry" only when the caller knows the failure is retry-safe.
     *   `null` renders NO action at all; the library never adds an implicit default action
     *   (VOUT-03 — retry-safety is never assumed by the library).
     */
    data class Failure(
        val reason: String,
        val handledBy: HandledByUiModel? = null,
        val action: FailureActionUiModel? = null
    ) : VoiceOutcomeUiState

    /**
     * A generic needs-confirmation state (VOUT-04, D-01/D-02/D-03/D-05/D-06) -- a reason string,
     * one-or-more proposed items, and confirm/cancel actions. [items].size == 1 is a single
     * confirm (SecondBrain's `MutationGate`/`VoiceConfirmGate` destructive risk confirm, or
     * CalTracker's weak single match); [items].size > 1 is a batch (CalTracker's `ProposedBatch`).
     * The SAME render path handles both -- there is no size-based special casing anywhere in this
     * type or in [io.github.ygaray.yahirandroidtaste.component.OutcomeSheet]'s rendering of it.
     *
     * A consumer constructing a [NeedsConfirmation] MUST route
     * [io.github.ygaray.yahirandroidtaste.component.OutcomeSheet]'s own `onDismissRequest` param to
     * the SAME decline logic as [onCancel] -- mirroring
     * [AlbumTitleConfirmSheet][io.github.ygaray.yahirandroidtaste.component.AlbumTitleConfirmSheet]'s
     * own documented `onDismiss`/`onSave` distinction. Dismiss/outside-tap/back = decline (D-05);
     * this is a documented integration contract, not library-enforceable.
     *
     * @param reason Caller-formatted, human-readable reason this confirmation is needed. Required.
     * @param items The proposed item(s) to confirm or cancel. Size 1 = single; size >1 = batch.
     * @param selectionMode How a batch resolves to a confirm action (D-03/D-06). Defaults to
     *   [SelectionMode.AllOrNothing] -- the only mode either live consumer currently exercises.
     * @param title An optional headline separate from [reason] (D-05), e.g. "Delete card?".
     * @param severity Confirm-button color/role treatment -- reuses the existing public
     *   [io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults.ActionButtonRole] rather
     *   than a new library-defined enum (D-05, Don't Hand-Roll). Defaults to
     *   [ActionButtonDefaults.ActionButtonRole.Neutral].
     * @param reversibilityHint An optional "Undoable"/"Irreversible"/free-text hint that stays
     *   visible (D-05). `null` renders nothing.
     * @param confirmLabel The confirm button's verb (D-05), e.g. "Delete"/"Merge"/"Allow"/
     *   "Confirm all (N)". Defaults to "Confirm".
     * @param cancelLabel The cancel button's label. Defaults to "Cancel".
     * @param topLevelContent An optional sheet-level content slot rendered ONCE regardless of
     *   [items]'s size (D-06) -- e.g. CalTracker's single shared date-picker row for a batch.
     *   `null` renders nothing (null-prop-hides).
     * @param onConfirm Invoked exactly once on an explicit Confirm-button tap. Never invoked on
     *   composition/recomposition or on a timeout (all of SecondBrain's `ConfirmSubject` arms are
     *   destructive).
     * @param onCancel Invoked exactly once on an explicit Cancel-button tap (or routed from a
     *   genuine scrim/back/drag dismiss gesture the consumer wires to the same decline logic).
     */
    data class NeedsConfirmation(
        val reason: String,
        val items: List<ProposedItemUiModel>,
        val selectionMode: SelectionMode = SelectionMode.AllOrNothing,
        val title: String? = null,
        val severity: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral,
        val reversibilityHint: String? = null,
        val confirmLabel: String = "Confirm",
        val cancelLabel: String = "Cancel",
        val topLevelContent: (@Composable () -> Unit)? = null,
        val onConfirm: () -> Unit,
        val onCancel: () -> Unit
    ) : VoiceOutcomeUiState
}
