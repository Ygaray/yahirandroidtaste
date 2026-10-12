package io.github.ygaray.yahirandroidtaste.model

/**
 * The status badge drawn at the start of an
 * [io.github.ygaray.yahirandroidtaste.component.OutcomeSheet] title row in the Receipt style
 * ([io.github.ygaray.yahirandroidtaste.component.OutcomeSheetStyle.Receipt], VRCPT-03). Set it on
 * [VoiceOutcomeUiState.Success.badge] or [VoiceOutcomeUiState.Failure.badge].
 *
 * Only the Receipt style draws a badge; the Classic style ignores it, so a defaulted `null` renders
 * exactly like v2.5.1.
 */
enum class OutcomeBadge {
    /** A check icon on a `primary` filled circle. */
    Success,

    /** The auto-mirrored undo icon on an `onSurfaceVariant` filled circle (a reverted outcome). */
    Reverted,

    /** A close icon on the error tone. */
    Error,

    /**
     * A priority-high icon on the warn tone, with the tone's container and content swapped for the
     * circle (the circle is the content color, the glyph is the container color).
     */
    Warn
}
