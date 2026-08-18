package io.github.ygaray.yahirandroidtaste.model

/**
 * UI model for a single tag row in the Tag Management screen.
 *
 * Maps from the DAO projection `TagWithCardCount` inside TagManagementViewModel so that
 * TagManagementScreen composables no longer import any DAO type (UIQ-07 / D-09).
 *
 * Phase 86 (GADGET-03): co-moved verbatim from `:app`'s
 * `feature/settings/tagmanagement/TagManagementUiModel.kt` into `:yahirandroidtaste/model/`
 * alongside [io.github.ygaray.yahirandroidtaste.component.TagListItem] — the library
 * composable's own relocation requires a library-resident parameter type, since `:app`
 * depends on `:yahirandroidtaste` and never the reverse (86-RESEARCH.md Pitfall A). Same
 * "UI model split off and relocated" shape as [TagChipUiModel]/`ListItemUiModel`.
 *
 * All fields the screen's delete/promote/demote/edit handlers need are included:
 *  - [id]        — tag UUID; passed to all ViewModel mutation calls
 *  - [name]      — display name; shown in list row, rename field initial value, dialog copy
 *  - [cardCount] — active card count; shown in list row and delete-confirm dialog impact text
 *  - [isHome]    — controls whether Appearance section (home icon/color) or Promote CTA renders
 *  - [iconName]  — current icon selection for appearance editing; null for plain tags
 *  - [color]     — current accent color for appearance editing; null for plain tags
 */
data class TagManagementUiModel(
    val id: String,
    val name: String,
    val cardCount: Int,
    val isHome: Boolean,
    val iconName: String?,
    val color: Long?
)
