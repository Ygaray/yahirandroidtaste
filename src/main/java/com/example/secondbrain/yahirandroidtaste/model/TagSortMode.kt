package com.example.secondbrain.yahirandroidtaste.model

/**
 * Canonical global tag sort mode (TSORT-01), consumed by [com.example.secondbrain.yahirandroidtaste.component.SortControl]
 * (UI) and `TagSortPreferenceManager` (persistence, `:app`).
 *
 * Three modes, default-first menu order (D-01/D-04/D-05): [FREQUENCY] (default) → [ALPHABETICAL]
 * → [DATE_CREATED]. Pure enum — zero Room/Hilt/DataStore imports, keeping `:designsystem`
 * boundary-clean; the actual comparator logic for each mode lives in `:app` (module boundary rule).
 *
 * @property label Display copy shown in [com.example.secondbrain.yahirandroidtaste.component.SortControl]'s dropdown menu.
 */
enum class TagSortMode(val label: String) {
    FREQUENCY("Frequency"),
    ALPHABETICAL("A – Z"),
    DATE_CREATED("Date created");

    companion object {
        /** Default sort mode when no preference has ever been written (D-04/D-05). */
        val DEFAULT = FREQUENCY
    }
}
