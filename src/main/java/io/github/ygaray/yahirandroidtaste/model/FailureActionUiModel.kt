package io.github.ygaray.yahirandroidtaste.model

/**
 * An optional label+callback action slot for a [VoiceOutcomeUiState.Failure] (D-08) — e.g. "Open
 * Settings" for a missing/invalid key, or "Retry" only when the caller knows the failure is
 * retry-safe.
 *
 * Prop-driven and strictly opt-in: the app decides whether and what to show. A `null`
 * [VoiceOutcomeUiState.Failure.action] renders NO action at all — the library never adds an
 * implicit default action (VOUT-03); retry-safety is never assumed by the library.
 *
 * @param label The action button's visible text.
 * @param onClick Invoked when the action is tapped. The library never inspects or interprets
 *   this callback — it crosses back OUT to consumer-owned code.
 */
data class FailureActionUiModel(
    val label: String,
    val onClick: () -> Unit
)
