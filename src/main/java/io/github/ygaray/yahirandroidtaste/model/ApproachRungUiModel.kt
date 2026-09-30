package io.github.ygaray.yahirandroidtaste.model

/**
 * One rung of a command-approach tier ladder
 * ([io.github.ygaray.yahirandroidtaste.component.ApproachLadderCard], VAPPR-01/02/03) — mirrors
 * [ListItemUiModel]'s all-`val` immutable shape (D-04) so this class stays Compose-inferred
 * STABLE and introduces no `copy()`/`var` ABI trap (see [TagChipUiModel] for that lesson).
 *
 * [offlineCapable] is an APP-derived prop (D-06) — the library never depends on any
 * voice-action-engine `TierPolicy` type; the consumer maps its engine's own tier metadata into
 * this UI-only shape at the call site (no hub-to-hub edge, L7).
 *
 * @param id Stable identifier emitted via `onMaxTierChange` callbacks — never rendered as text.
 * @param label Caller-formatted display text for the rung row.
 * @param rank The rung's position in the ladder's severity/capability ordering — used ONLY to
 *   compute the max-tier cap comparison (`rank <= capRank`); rendering order always follows
 *   [io.github.ygaray.yahirandroidtaste.component.ApproachLadderCard]'s `ladder` LIST order, never
 *   a sort by [rank] (VAPPR-01).
 * @param enabled Whether this rung is usable at all, independent of offline-only/cap state.
 * @param offlineCapable Whether this rung can run with no network access — APP-derived (D-06).
 * @param description Optional caller-formatted supporting text for the rung.
 */
data class ApproachRungUiModel(
    val id: String,
    val label: String,
    val rank: Int,
    val enabled: Boolean = true,
    val offlineCapable: Boolean = false,
    val description: String? = null
)
