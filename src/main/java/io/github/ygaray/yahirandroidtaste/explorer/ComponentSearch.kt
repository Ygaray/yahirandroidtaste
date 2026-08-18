package io.github.ygaray.yahirandroidtaste.explorer

/**
 * D-04 pure name-filter helper backing the index screen's search field (wired in Plan 02) — the
 * single place the case-insensitive substring matching rule is defined, over
 * [ComponentRegistry.entries] (D-05's single source).
 */
object ComponentSearch {

    /**
     * Returns `null` when [query] is blank (family-index sentinel — caller renders the normal
     * family-grouped list), otherwise a flat list of [entries] whose [ComponentRegistry.Entry.name]
     * contains [query] as a case-insensitive substring, preserving [entries]' declaration order
     * (stable — EDGE ordering).
     */
    fun filter(
        query: String,
        entries: List<ComponentRegistry.Entry> = ComponentRegistry.entries
    ): List<ComponentRegistry.Entry>? {
        if (query.isBlank()) return null
        return entries.filter { it.name.contains(query, ignoreCase = true) }
    }
}
