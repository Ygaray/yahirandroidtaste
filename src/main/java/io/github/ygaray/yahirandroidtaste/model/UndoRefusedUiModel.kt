package io.github.ygaray.yahirandroidtaste.model

/**
 * Loud undo-refused/partial substate fed from
 * [io.github.ygaray.yahirandroidtaste.feedback.UndoGroupResult.Refused] (VUNDO-01, D-01) —
 * renders on the theme's error-container roles, never
 * [io.github.ygaray.yahirandroidtaste.component.AttentionCue] (its KDoc forbids use as a failure
 * signal).
 *
 * @param reason Caller-formatted, human-readable refusal reason.
 * @param changedItem Optional identifier of the item that changed since the group was opened —
 *   `null` for consumers whose undo has no restore payload (e.g. CT's create-only writes, D-01).
 */
data class UndoRefusedUiModel(val reason: String, val changedItem: String? = null)
