package io.github.ygaray.yahirandroidtaste.model

import io.github.ygaray.yahirandroidtaste.feedback.FeedbackTarget

/**
 * One row's result within a batch voice-command outcome
 * ([io.github.ygaray.yahirandroidtaste.component.OutcomeSheet], D-06) — e.g. one of several
 * items a batch command attempted to log. [VoiceOutcomeUiState.Success.batchResults] being empty
 * hides the whole batch-results section (a single-item outcome has nothing to list here).
 *
 * @param label Caller-formatted display text for the row (e.g. the logged item's title).
 * @param succeeded Whether this row's operation succeeded.
 * @param detail Optional caller-formatted supporting text shown when [succeeded] is `false`
 *   (e.g. a per-row failure reason).
 * @param target Opens THIS row's own item through the `onOpenTarget` of
 *   [io.github.ygaray.yahirandroidtaste.component.OutcomeSheet] (FOPEN-02). A row without a target
 *   shows no chevron and is not clickable. The CALLER clears it after Undo or Undo all, because the
 *   item it pointed at no longer exists; the library never reads the opaque key. While the success
 *   is in flight the row is locked (dimmed, no click action).
 */
data class BatchRowResultUiModel(
    val label: String,
    val succeeded: Boolean,
    val detail: String? = null,
    val target: FeedbackTarget? = null
)
