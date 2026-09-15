package io.github.ygaray.yahirandroidtaste.model

/**
 * HUBW-02 D-03: a caller-owned saved/named place -- exactly four primitives, nothing more. A
 * consumer maps its own saved-place rows (however it stores or derives them) into this type at
 * the call site; the hub neither stores nor derives saved places itself, and this model never
 * widens beyond these four fields (164-03 review: a caller-supplied disambiguation key or a
 * coordinate-based differentiator were both considered and rejected -- see 164-03-PLAN.md's
 * Review Dispositions Ledger).
 *
 * [label] need not be unique: a real consumer's saved places can be a derived `DISTINCT`
 * projection over label, coordinates and radius (e.g. SecondBrain's `SavedPlace` DAO
 * projection), so two entries can legitimately share a label with different coordinates or
 * radii. [PlaceMapPicker]'s saved-place chips disambiguate duplicate labels with an ordinal and
 * [radiusMeters] (never a coordinate -- coordinates are never displayed, T-157-11).
 *
 * @param label Display text for this place's chip (e.g. "Home"). Not required to be unique.
 * @param latitude The place's latitude. Never rendered as text or a content description.
 * @param longitude The place's longitude. Never rendered as text or a content description.
 * @param radiusMeters The place's geofence radius in meters. Used both to prefill
 *   [PlaceMapPicker]'s radius and, when a label repeats, as part of the chip's disambiguating
 *   content description.
 */
data class SavedPlaceUiModel(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float
)

/**
 * For each item in [items] (in list order), the number of items strictly before it (by
 * structural equality) that are equal to it -- a 0-based occurrence index within its own
 * equal-value group. `occurrenceIndices(listOf("a", "b", "a", "a"))` is `[0, 0, 1, 2]`. Used to
 * give duplicate saved places (including two fully identical entries) distinct `ChipBar`
 * composition keys (164-03 review round 1 MEDIUM).
 */
internal fun <T> occurrenceIndices(items: List<T>): List<Int> {
    val seenCounts = mutableMapOf<T, Int>()
    return items.map { item ->
        val occurrence = seenCounts.getOrDefault(item, 0)
        seenCounts[item] = occurrence + 1
        occurrence
    }
}

/**
 * A saved-place chip's accessible name and visible supporting text (164-03 review round 1
 * MEDIUM, round 2 MEDIUM): [contentDescription] is the single string TalkBack announces for the
 * chip's one clickable node; [supportingLabel] is the same disambiguator rendered as visible
 * text (`null` when the label is unique, matching [savedPlaceChipLabels]'s null case).
 */
internal data class SavedPlaceChipLabel(val contentDescription: String, val supportingLabel: String?)

/**
 * Builds one [SavedPlaceChipLabel] per entry in [labels], disambiguating a label that repeats in
 * [labels] with its pre-formatted radius text (from the paired entry in [radiusTexts]) and its
 * 1-based ordinal *among same-labelled entries in the caller's list order* -- this function never
 * reorders its input, so the caller is responsible for passing a deterministic order (a stable
 * sort, not just the DAO's own label-only order) or the same place can change ordinal between
 * emissions (164-03 review round 2 MEDIUM).
 *
 * A label that occurs exactly once yields `"Saved places <label>"` and a `null` supporting label
 * (conditional-render-no-dead-space: no ordinal is shown when there is nothing to disambiguate).
 * A label occurring `count` times yields, for its `n`-th (1-based) occurrence in list order,
 * `"Saved places <label>, <radiusText> radius, <n> of <count>"` and a supporting label of
 * `"<radiusText>, <n> of <count>"`. This file keeps no imports -- [radiusTexts] is passed in
 * already formatted (e.g. via `formatMeters`) so this model stays pure string composition.
 *
 * @throws IllegalArgumentException unless [labels] and [radiusTexts] are the same size.
 */
internal fun savedPlaceChipLabels(labels: List<String>, radiusTexts: List<String>): List<SavedPlaceChipLabel> {
    require(labels.size == radiusTexts.size) {
        "savedPlaceChipLabels requires labels and radiusTexts to be the same size, was " +
            "${labels.size} label(s) and ${radiusTexts.size} radius text(s)"
    }
    val countsByLabel = labels.groupingBy { it }.eachCount()
    val occurrences = occurrenceIndices(labels)
    return labels.indices.map { index ->
        val label = labels[index]
        val count = countsByLabel.getValue(label)
        if (count <= 1) {
            SavedPlaceChipLabel(contentDescription = "Saved places $label", supportingLabel = null)
        } else {
            val ordinal = occurrences[index] + 1
            val radiusText = radiusTexts[index]
            SavedPlaceChipLabel(
                contentDescription = "Saved places $label, $radiusText radius, $ordinal of $count",
                supportingLabel = "$radiusText, $ordinal of $count"
            )
        }
    }
}
