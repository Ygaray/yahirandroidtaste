package com.example.secondbrain.yahirandroidtaste.model

/**
 * UI model for a co-occurrence chip in the Browse screen.
 *
 * Maps from the DAO projection `TagWithCount` inside BrowseViewModel so that screen
 * composables (CoOccurrenceChipBar, BrowseScreen) no longer import any DAO type
 * (UIQ-07 / D-09). Relocated into `:designsystem/model` (Phase 46 Plan 1) — a pure
 * UI-shaped data class with zero app-package imports.
 *
 * Fields preserved from TagWithCount:
 *  - [id]              — tag UUID; used to resolve the full TagEntity inside the ViewModel
 *                        when the chip is tapped (appendToDrillPath(tagId))
 *  - [name]            — display label rendered by CoOccurrenceChipBar → AppChip
 *  - [occurrenceCount] — frequency count; retained for potential future ordering affordance
 *                        (currently DAO order is authoritative — D-02a)
 *
 * [createdAt] (Phase 48 Plan 02, D-08/D-10) — defaulted to `0L` so every existing producer
 * (Browse, Search, TagChipEditor, showcase fakes, tests) that constructs this model without
 * supplying it keeps compiling unchanged. Tag-widget producers (Plan 05) populate the real
 * epoch-millis value so the widget's date-created sort mode has data to sort by.
 *
 * [jaccard] (Phase 91 Plan 01, VISUAL-01/02/03) — populated ONLY by Related-mode chip sources
 * (Browse's `coOccurrenceChips` when the drill path's relatedness engine supplies a value);
 * every other producer leaves it `null`. Nullable, NOT a numeric default like [createdAt]'s
 * `0L`, because a real jaccard of `0.0` (zero shared cards, still a valid relatedness result
 * per v1.17's zero-share-sunk-to-tail policy) must not collide with "not applicable."
 */
data class TagChipUiModel(
    val id: String,
    val name: String,
    val occurrenceCount: Int,
    val createdAt: Long = 0L,
    val jaccard: Double? = null
)
