package io.github.ygaray.yahirandroidtaste.model

import androidx.compose.ui.graphics.Color

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
 *
 * [color] (Phase 7 Plan 01, TAGCOLOR-01; re-shaped 2026-09-27 per operator ABI ruling — see
 * 07-01-SUMMARY.md deviations) — an opt-in per-tag chip container-color override, auto-threaded
 * into `containerColorOverride` at `CardTagRow`. Every producer except a consumer's own
 * color-aware mapper (e.g. SecondBrain's muted/theme-aware tag-color policy) leaves it `null`.
 * Nullable, NOT a sentinel default like [createdAt]'s `0L`, because `null` means "no override —
 * render the theme default," never "black" or any other real color value.
 *
 * **Deliberately declared OUTSIDE the primary constructor** (a mutable body property, not a
 * data-class component): Kotlin's compiler-synthesized `copy()`/`equals()`/`hashCode()`/
 * `toString()`/`componentN()` always cover the *full* primary-constructor parameter list with a
 * single, non-overloadable signature — `@JvmOverloads` can suppress a constructor's own
 * binary-compat gap (as it does for [id]/[name]/[occurrenceCount]/[createdAt]/[jaccard] below)
 * but Kotlin does not allow annotating those synthetic members, so adding a 6th primary-ctor
 * parameter would have made `copy()`'s old 5-arg overload a genuine, unfixable API removal
 * (`api.txt` `RemovedMethod`) — exactly the non-additive ABI break Phase 07 code review flagged
 * (WR-01) and the repo owner ruled must be a code fix, not an accepted break. Keeping [color] out
 * of the primary constructor means the constructor, `copy()`, `equals()`, `hashCode()`,
 * `toString()`, and every `componentN()` stay byte-identical to the pre-Phase-7 shape — zero
 * change, not merely "additive" — while [color] itself, and the [Companion.of] factory below,
 * are pure new-symbol additions. The one accepted semantic consequence: two [TagChipUiModel]
 * instances that differ only in [color] are still `equals()` (color is styling metadata, not
 * identity), and `copy()` never carries [color] over — callers that `copy()` an instance must
 * re-set [color] explicitly afterward.
 */
data class TagChipUiModel @JvmOverloads constructor(
    val id: String,
    val name: String,
    val occurrenceCount: Int,
    val createdAt: Long = 0L,
    val jaccard: Double? = null
) {
    var color: Color? = null

    companion object {
        /**
         * Java-ergonomic, ABI-sensitive construction path that sets [color] in one call
         * (added 2026-09-27 per Phase 07 code review WR-01 / operator ABI ruling, replacing the
         * rejected approach of adding [color] as a 6th primary-constructor parameter). Kotlin
         * callers may prefer `TagChipUiModel(...).apply { color = ... }`; this factory exists so
         * Java/ABI-sensitive callers have a single-call equivalent without touching `copy()`.
         */
        @JvmStatic
        @JvmOverloads
        fun of(
            id: String,
            name: String,
            occurrenceCount: Int,
            createdAt: Long = 0L,
            jaccard: Double? = null,
            color: Color? = null
        ): TagChipUiModel = TagChipUiModel(id, name, occurrenceCount, createdAt, jaccard).also {
            it.color = color
        }
    }
}
