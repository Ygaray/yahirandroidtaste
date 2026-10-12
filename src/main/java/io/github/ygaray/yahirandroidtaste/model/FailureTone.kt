package io.github.ygaray.yahirandroidtaste.model

/**
 * The hue of a failed [VoiceOutcomeUiState.Failure] (VRCPT-04), set on
 * [VoiceOutcomeUiState.Failure.tone].
 *
 * In the Receipt style it selects the hue of the `ToneCard` holding the reason. The Classic style
 * honors [Warn] too, by painting the warn tone from
 * [io.github.ygaray.yahirandroidtaste.component.OutcomeSheetColors]; it keeps its existing
 * error-container look for [Error], so the defaulted [Error] renders exactly like v2.5.1.
 */
enum class FailureTone {
    /** The error hue; the default, and today's rendering. */
    Error,

    /** The warn hue, for a failure that is a heads-up rather than an error. */
    Warn
}
