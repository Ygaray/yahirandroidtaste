package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.runtime.Composable

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
     */
    data class Success(
        val summary: String,
        val handledBy: HandledByUiModel? = null,
        val editableContent: (@Composable () -> Unit)? = null,
        val inFlight: Boolean = false,
        val batchResults: List<BatchRowResultUiModel> = emptyList()
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
}
