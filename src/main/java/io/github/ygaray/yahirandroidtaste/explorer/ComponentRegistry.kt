package io.github.ygaray.yahirandroidtaste.explorer

import androidx.compose.runtime.Composable

/**
 * D-05 single source of truth that drives family component-lists, name search, AND
 * per-component detail-page routing (Phase 62) — in addition to the drift-guard purpose it
 * already served (CATALOG-03, Phase 61 Plan 04). Each [Entry] additionally resolves to its own
 * detail States matrix ([Entry.states]) and Variants content ([Entry.content]); there is no
 * second name->composable dispatch anywhere in the explorer — [entries] alone is authoritative.
 *
 * "Component" = a public top-level `@Composable` function in one of the library's visual
 * packages (`component/`, `feedback/`, `modifier/`, `theme/`). Every such function must appear
 * either in [entries] (registered, and — as of Phase 62 — rendered via its own `.states`/
 * `.content`) or in [INTENTIONALLY_UNREGISTERED] (a documented sub-part exclusion) — never
 * neither.
 *
 * Regenerated from a live source scan at Phase 87 Plan 01 execution time (2026-08-08):
 * `component/` (41), `feedback/` (1 — `UndoCenterScreen`; `UndoHistoryRow`/`UndoPreviewPeek`/
 * `RelativeTimestampText` are `private`), `modifier/` (1 — `SwipeableActionRow`), `theme/`
 * (1 — `YahirAndroidTasteTheme`) = 44 total public composables. 40 registered below; 4
 * intentionally unregistered. Phases 85-86 (`FilterBar`, `SortControl`, `CountBadge`,
 * `CropOverlay`, `TagListItem`) are the delta since the Phase-61 count (then: 34 registered,
 * total public composables numbered 38) — the CATALOG-04 completeness audit (87-01) confirmed
 * zero drift: every scanned composable is registered or allowlisted, and every
 * registered/allowlisted name matches a real scanned composable.
 */
object ComponentRegistry {

    /**
     * One cell of a per-component detail page's States matrix (D-03 axis). A non-null [render]
     * is an applicable interaction state that is actually rendered; a null [render] is a
     * documented "Not applicable" state — visible content, not an omission
     * (`conditional-render-no-dead-space` reconciliation, see 62-CONTEXT.md D-03).
     */
    data class StateCell(val label: String, val render: (@Composable () -> Unit)? = null)

    /**
     * @param states The detail page's States matrix (D-03 axis), authored in fixed cell order
     *   (Default -> Pressed / Selected -> Disabled -> Focused). Defaults to `emptyList()` so the
     *   34 pre-Phase-62 two-argument `Entry(name, family)` call sites below keep compiling
     *   unchanged; each is enriched in Plans 03-05.
     * @param content The component's detail-page Variants section body (the preserved Phase-61
     *   curated `XxxSection` fixtures). Defaults to `null` for the same pre-Phase-62 compilation
     *   reason as [states].
     * @param controls The detail page's Playground live prop/state knobs (EXPLORE-04, D-03/D-04).
     *   Defaults to `emptyList()` so all pre-Phase-63 `Entry(...)` call sites keep compiling
     *   unchanged; curated entries populate this in Plan 04's authoring pass. `entries` remains
     *   the single source of truth — this is an additive field, never a parallel map.
     * @param preview The Playground section's live preview body, invoked with the entry's
     *   [PlaygroundState] so it can read the live knob values (EXPLORE-04, D-03). Defaults to
     *   `null` for the same pre-Phase-63 compilation reason as [controls]; must be non-null
     *   whenever [controls] is non-empty (enforced by `ComponentPlaygroundIntegrityTest`).
     */
    data class Entry(
        val name: String,
        val family: String,
        val states: List<StateCell> = emptyList(),
        val content: (@Composable () -> Unit)? = null,
        val controls: List<Control> = emptyList(),
        val preview: (@Composable (PlaygroundState) -> Unit)? = null
    )

    /**
     * One entry per showcaseable public `@Composable`, authored in a fixed, deterministic order
     * (family order matches [ExplorerFamilies.ORDERED_KEYS]; within a family, declaration order
     * matches the family screen's rendered section order) so registry iteration is reproducible
     * across runs (EDGE ordering).
     *
     * D-05 (Phase 62 Plan 02): the seven per-family lists below are each declared in their own
     * family screen file (`cardsFamilyEntries` in `CardsFamilyScreen.kt`, etc.) rather than
     * inline here, so family-content plans (03-05) can author `states`/`content` on disjoint
     * files without touching this shared registry file. This concatenation — reproducing the
     * exact prior declaration order — is the only place the seven lists are combined.
     */
    val entries: List<Entry> = cardsFamilyEntries +
        chipsFamilyEntries +
        sheetsFamilyEntries +
        buttonsFabFamilyEntries +
        pickersFamilyEntries +
        feedbackFamilyEntries +
        emptyStateFamilyEntries

    /**
     * Sub-part composables intentionally excluded from a standalone tile (D-04), each with a
     * one-line reason. Curated against the live scan, not assumed from CONTEXT.md's illustrative
     * list.
     */
    val INTENTIONALLY_UNREGISTERED: Map<String, String> = mapOf(
        "CardBase" to
            "Structural shell every card type wraps — already exercised indirectly via " +
            "TextCard/ListCard/AlbumCard/VoiceCard in the Cards family.",
        "WaveformCanvas" to
            "Sub-part rendered inside RecordingBottomSheetContent/VoiceCard's private wrapper " +
            "(VoiceWaveformCanvas) — already exercised indirectly.",
        "SwipeableActionRow" to
            "Swipe-reveal mechanics powering CardBase and EditorItemRow — infrastructure, not " +
            "an independent visual archetype; already exercised indirectly via every card " +
            "entry's reveal-confirm swipe and via EditorItemRow's own demo.",
        "YahirAndroidTasteTheme" to
            "Theme-level wrapper every explorer screen (including this registry's own family " +
            "screens) already composes around itself — not an independently showcaseable " +
            "component, it IS the chrome every other entry renders inside."
    )

    init {
        val entryNames = entries.map { it.name }
        val duplicateEntryNames = entryNames
            .groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .keys
        require(duplicateEntryNames.isEmpty()) {
            "ComponentRegistry.entries contains duplicate name(s): $duplicateEntryNames — " +
                "each showcaseable component must appear exactly once."
        }

        val overlap = entryNames.toSet() intersect INTENTIONALLY_UNREGISTERED.keys
        require(overlap.isEmpty()) {
            "Name(s) present in both entries and INTENTIONALLY_UNREGISTERED: $overlap — a " +
                "component must be registered XOR allowlisted, never both."
        }

        val blankReasons = INTENTIONALLY_UNREGISTERED.filterValues { it.isBlank() }.keys
        require(blankReasons.isEmpty()) {
            "INTENTIONALLY_UNREGISTERED reason(s) must be non-blank for: $blankReasons."
        }
    }
}
