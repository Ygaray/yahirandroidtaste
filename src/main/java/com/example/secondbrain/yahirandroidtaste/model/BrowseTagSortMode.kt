package com.example.secondbrain.yahirandroidtaste.model

/**
 * Browse-local tag sort mode (REORDER-01, D-01), consumed by
 * [com.example.secondbrain.yahirandroidtaste.component.SortControl] (UI) and
 * `BrowseTagSortPreferenceManager` (persistence, `:app`).
 *
 * Distinct from the shared [TagSortMode]: this enum adds the Related family ([RELATED] /
 * [RELATED_ALL]), which is Browse-local by design so the shared `TagSortMode` enum and the 4
 * non-Browse `SortControl` call sites (tag-editor, voice-recording, etc.) never surface a Related
 * option with no drill path to resolve it against. [RELATED] / [RELATED_ALL] must never be added
 * to [TagSortMode].
 *
 * Five modes, default-first menu order: [FREQUENCY] (default) → [ALPHABETICAL] → [DATE_CREATED] →
 * [RELATED] → [RELATED_ALL]. Pure enum — zero Room/Hilt/DataStore imports, keeping
 * `:yahirandroidtaste` boundary-clean; the actual comparator/query logic for each mode lives in
 * `:app` (module boundary rule).
 *
 * @property label Display copy shown in [com.example.secondbrain.yahirandroidtaste.component.SortControl]'s dropdown menu.
 */
enum class BrowseTagSortMode(val label: String) {
    FREQUENCY("Frequency"),
    ALPHABETICAL("A – Z"),
    DATE_CREATED("Date created"),
    RELATED("Related"),
    RELATED_ALL("Related - All");

    companion object {
        /** Default sort mode when no preference has ever been written. */
        val DEFAULT = FREQUENCY
    }
}
