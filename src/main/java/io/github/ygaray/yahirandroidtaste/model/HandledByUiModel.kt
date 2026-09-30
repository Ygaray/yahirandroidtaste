package io.github.ygaray.yahirandroidtaste.model

/**
 * Provenance indicator for a voice command outcome
 * ([io.github.ygaray.yahirandroidtaste.component.OutcomeSheet], VOUT-02) — mirrors
 * [ApproachRungUiModel]'s all-`val` immutable shape (D-04) so this class stays Compose-inferred
 * STABLE and introduces no `copy()`/`var` ABI trap (see [TagChipUiModel] for that lesson).
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
 */
data class HandledByUiModel(
    val tier: String,
    val approach: String? = null,
    val provider: String? = null,
    val model: String? = null,
    val escalationCount: Int? = null
)
