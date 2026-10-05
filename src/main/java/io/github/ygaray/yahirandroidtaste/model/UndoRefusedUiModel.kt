package io.github.ygaray.yahirandroidtaste.model

import kotlin.jvm.internal.DefaultConstructorMarker

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

    // v2.4.x binary-compatibility shims (INC-2026-10-05-02 F1c): re-emit the v2.4.1 compiler-generated
    // default-argument constructor and static `copy$default`. Mask bit i means v2.4.1 parameter i
    // took its default. See HandledByUiModel for the full rationale. Hidden from Kotlin and Java
    // source: do not call them or document them as API.
    @Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
    constructor(
        reason: String,
        changedItem: String?,
        mask: Int,
        marker: DefaultConstructorMarker?
    ) : this(
        reason = reason,
        changedItem = if (mask and (1 shl 1) != 0) null else changedItem
    )

    private companion object {
        @Deprecated("Binary compatibility with v2.4.x", level = DeprecationLevel.HIDDEN)
        @JvmStatic
        @JvmName("copy\$default")
        fun legacyCopyDefault(
            self: UndoRefusedUiModel,
            reason: String?,
            changedItem: String?,
            mask: Int,
            marker: Any?
        ): UndoRefusedUiModel = self.copy(
            reason = if (mask and (1 shl 0) != 0) self.reason else requireNotNull(reason),
            changedItem = if (mask and (1 shl 1) != 0) self.changedItem else changedItem,
            refusedPrefix = self.refusedPrefix,
            changedSinceSuffix = self.changedSinceSuffix
        )
    }
}
