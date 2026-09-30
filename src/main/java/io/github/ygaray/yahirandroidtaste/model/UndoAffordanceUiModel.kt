package io.github.ygaray.yahirandroidtaste.model

/**
 * Grouped undo affordance rendered by [io.github.ygaray.yahirandroidtaste.component.OutcomeSheet]
 * as part of a [VoiceOutcomeUiState.Success] (VUNDO-01, D-01/D-02). Built by the CONSUMER from
 * [io.github.ygaray.yahirandroidtaste.feedback.UndoHistoryStore.group]/`.groupStatus` reads — the
 * library itself never holds a reference to any app's `UndoHistoryStore` instance and never
 * recomputes [allLabel]'s number itself (INV-01).
 *
 * @param allLabel Caller-formatted "Undo all (N)" label — N is however many of the group's own
 *   members are [io.github.ygaray.yahirandroidtaste.feedback.UndoStatus.Available] (D-01's
 *   2026-09-30 amendment: edits ARE undoable, so the library imposes no exclusion of its own);
 *   the library renders this string verbatim, never recomputing the count.
 * @param rows Per-item rows, rendered in LIST order — never resorted.
 * @param onUndoAll Invoked when the "Undo all" action is tapped. `null` renders no "Undo all"
 *   button at all (null-prop-hides convention).
 * @param refused A non-null loud undo-refused/partial substate, fed from
 *   [io.github.ygaray.yahirandroidtaste.feedback.UndoGroupResult.Refused].
 */
data class UndoAffordanceUiModel(
    val allLabel: String,
    val rows: List<UndoRowUiModel> = emptyList(),
    val onUndoAll: (() -> Unit)? = null,
    val refused: UndoRefusedUiModel? = null
)
