package io.github.ygaray.yahirandroidtaste.model

import io.github.ygaray.yahirandroidtaste.component.ActionButtonDefaults

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
 * @param role The caller-chosen button role/severity of the failure action (VFAIL-01) — e.g.
 *   [ActionButtonDefaults.ActionButtonRole.Destructive] for a destructive remedy. Reuses the
 *   existing public enum (English-free; no new library type). Defaults to
 *   [ActionButtonDefaults.ActionButtonRole.Neutral], today's rendering.
 */
data class FailureActionUiModel(
    val label: String,
    val onClick: () -> Unit,
    val role: ActionButtonDefaults.ActionButtonRole = ActionButtonDefaults.ActionButtonRole.Neutral
) {
    // Why an explicit two-argument secondary constructor instead of `@JvmOverloads` (INC-2026-10-05-02
    // F1b): `@JvmOverloads` would generate the same JVM `(String, Function0)` constructor (so both
    // together are a platform-declaration clash), and its overloads are invisible to Kotlin overload
    // resolution -- which is why the v2.4.0 trailing-lambda shape `FailureActionUiModel("l") { }` bound
    // the lambda to `role` and stopped compiling. With the explicit constructor a trailing lambda can
    // only match it, and two positional or named arguments prefer it because it uses no defaults; both
    // keep the role Neutral. It also keeps the shipped v2.4.1 JVM `(String, Function0)` constructor.
    constructor(label: String, onClick: () -> Unit) :
        this(label, onClick, ActionButtonDefaults.ActionButtonRole.Neutral)

    // Hand-written pre-v2.5 `copy` arity. Without it Metalava reports the shipped JVM
    // `copy(String, Function0)` as a removed method once [role] is appended (the compiler only
    // generates the full-arity copy). Delegates with the CURRENT role so a legacy two-argument
    // copy never resets a caller's custom role. A body `var` (the TagChipUiModel alternative) is
    // rejected because it would cost Compose all-val stability.
    fun copy(label: String, onClick: () -> Unit): FailureActionUiModel =
        copy(label = label, onClick = onClick, role = role)
}
