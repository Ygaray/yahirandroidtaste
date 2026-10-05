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
 * @param refusedPrefix Caller-localizable lead-in rendered before [reason]; English default
 *   `"Couldn't undo:"`, plain text, no templating (VI18N-04).
 * @param changedSinceSuffix Caller-localizable fragment rendered after [changedItem] when it is
 *   non-null; English default `"changed since"`, plain text, no templating (VI18N-04).
 *
 * Word-order limitation: the sheet renders `<refusedPrefix> <reason>` and, only when [changedItem]
 * is non-null, `, <changedItem> <changedSinceSuffix>`. A language that needs a different order
 * should fold the item into [reason] and pass `changedItem = null`.
 */
data class UndoRefusedUiModel @JvmOverloads constructor(
    val reason: String,
    val changedItem: String? = null,
    val refusedPrefix: String = "Couldn't undo:",
    val changedSinceSuffix: String = "changed since"
) {
    // Hand-written pre-v2.5 `copy` arity. Without it Metalava reports the shipped JVM
    // `copy(String, String?)` as a removed method once the label fields are appended (the
    // compiler only generates the full-arity copy). Delegates with the CURRENT label fields so a
    // legacy two-argument copy never resets a caller's custom labels. A body `var` is rejected
    // because it would cost Compose all-val stability.
    fun copy(reason: String, changedItem: String?): UndoRefusedUiModel = copy(
        reason = reason,
        changedItem = changedItem,
        refusedPrefix = refusedPrefix,
        changedSinceSuffix = changedSinceSuffix
    )
}
