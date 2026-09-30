package io.github.ygaray.yahirandroidtaste.model

/**
 * One selectable row in a model dropdown
 * ([io.github.ygaray.yahirandroidtaste.component.ModelSelectCard], VSET-02) — mirrors
 * [ListItemUiModel]'s all-`val` immutable shape (D-04) so this class stays Compose-inferred
 * STABLE and introduces no `copy()`/`var` ABI trap (see [TagChipUiModel] for that lesson). The
 * library knows no models of its own; the consumer maps its engine's known models into this
 * UI-only shape at the call site.
 *
 * @param id Stable identifier emitted via `onModelSelected` callbacks — never rendered as text.
 * @param label Caller-formatted display text for the dropdown row.
 * @param subtitle Optional caller-formatted secondary text (e.g. a short capability summary).
 * @param badge Optional caller-formatted short badge text (e.g. "New" or a context-window size).
 */
data class ModelOptionUiModel(
    val id: String,
    val label: String,
    val subtitle: String? = null,
    val badge: String? = null
)
