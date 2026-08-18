package com.example.secondbrain.yahirandroidtaste.model

/**
 * Sort-preference model for the Browse screen — option + direction pair.
 *
 * Three sort options (D-02) and two directions; DEFAULT is creation-date descending
 * (newest cards on top, D-02 default behavior).
 *
 * Canonical card-type order for [SortOption.CARD_TYPE] (D-04): Text → List → Voice → Album.
 * This ordering is documented in `ModuleViewModelSortTest` and enforced in Plan 05's
 * `BrowseViewModel.applySort` / `cardTypeOrdinal`.
 *
 * Instantiation note: only [BrowseSortPreferenceManager] reads and writes this model;
 * consumers obtain a [kotlinx.coroutines.flow.Flow] of [BrowseSortPreference] via the manager.
 */
data class BrowseSortPreference(
    val option: SortOption,
    val direction: SortDirection
) {
    enum class SortOption { CREATION_DATE, CARD_TYPE, TITLE }
    enum class SortDirection { ASC, DESC }

    companion object {
        val DEFAULT = BrowseSortPreference(SortOption.CREATION_DATE, SortDirection.DESC)
    }
}
