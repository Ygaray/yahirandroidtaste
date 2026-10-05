package io.github.ygaray.yahirandroidtaste.model

/**
 * Provenance indicator for a voice command outcome
 * ([io.github.ygaray.yahirandroidtaste.component.OutcomeSheet], VOUT-02) — mirrors
 * [ApproachRungUiModel]'s all-`val` immutable shape (D-04), so this class stays Compose-inferred
 * STABLE. It keeps evolving additively through a `@JvmOverloads` constructor plus a hand-written
 * old-arity `copy` overload (see [TagChipUiModel] for the `copy()`/`var` ABI lesson this avoids).
 *
 * Only [tier] is required; every other field is optional so the shape grows additively (D-04).
 * Apps fill these from voice-action-engine's committed TEL-01 `CommandTrace` (per-tier attempts,
 * escalation reasons, provider/model) — the library never depends on any voice-action-engine
 * type; the consumer maps its engine's own trace metadata into this UI-only shape at the call
 * site (no hub-to-hub edge, L7).
 *
 * @param tier Required primary label (e.g. "Local", "Cloud") — always rendered.
 * @param approach Optional secondary label naming the specific approach used within [tier].
 * @param provider Optional secondary label naming the provider that handled the command.
 * @param model Optional secondary label naming the specific model that handled the command.
 * @param escalationCount Optional count of escalations that occurred before this outcome.
 * @param escalationsLabel Caller-localizable prefix of the escalation caption, rendered as
 *   `"<escalationsLabel> <escalationCount>"`; English default `"Escalations:"`, plain text
 *   (VI18N-04).
 */
data class HandledByUiModel @JvmOverloads constructor(
    val tier: String,
    val approach: String? = null,
    val provider: String? = null,
    val model: String? = null,
    val escalationCount: Int? = null,
    val escalationsLabel: String = "Escalations:"
) {
    // Hand-written pre-v2.5 `copy` arity. Without it Metalava reports the shipped JVM five-parameter
    // `copy` as a removed method once [escalationsLabel] is appended (the compiler only generates
    // the full-arity copy). Delegates with the CURRENT escalationsLabel so a legacy five-argument
    // copy never resets a caller's custom label. A body `var` is rejected because it would cost
    // Compose all-val stability.
    fun copy(
        tier: String,
        approach: String?,
        provider: String?,
        model: String?,
        escalationCount: Int?
    ): HandledByUiModel = copy(
        tier = tier,
        approach = approach,
        provider = provider,
        model = model,
        escalationCount = escalationCount,
        escalationsLabel = escalationsLabel
    )
}
