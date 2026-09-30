package io.github.ygaray.yahirandroidtaste.model

/**
 * One selectable row in a provider dropdown
 * ([io.github.ygaray.yahirandroidtaste.component.ProviderKeyCard], VSET-01) — mirrors
 * [ListItemUiModel]'s all-`val` immutable shape (D-04) so this class stays Compose-inferred
 * STABLE and introduces no `copy()`/`var` ABI trap (see [TagChipUiModel] for that lesson). The
 * library knows no providers of its own; the consumer maps its engine's known providers into
 * this UI-only shape at the call site.
 *
 * @param id Stable identifier emitted via `onProviderSelected` callbacks — never rendered as text.
 * @param label Caller-formatted display text for the dropdown row.
 */
data class ProviderOptionUiModel(
    val id: String,
    val label: String
)
