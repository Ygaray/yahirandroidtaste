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
 * Recomputed from the live `entries`/`INTENTIONALLY_UNREGISTERED` state at Phase 129 Plan 01
 * Task 3 execution time (2026-08-28) — **53 registered, 4 intentionally unregistered = 57 total
 * public composables** (`ComponentRegistry.entries.size` + `INTENTIONALLY_UNREGISTERED.size`,
 * cross-checked against
 * [io.github.ygaray.yahirandroidtaste.explorer.ComponentRegistryDriftGuardTest]'s own live source
 * scan, which stays green). This is a full recount, not an adjustment of the prior comment's
 * numbers (40 registered / 4 unregistered = 44 total, dated Phase 87 Plan 01 2026-08-08) — this
 * phase's own audit found that prior count already stale against the live maps before any Phase
 * 129 edit landed, so the delta below is Phase 129's contribution only, not the full gap: Phase
 * 129 adds `CardTypeChip` as a genuinely new public composable (+1 to the total), and moves
 * `CardBase` from [INTENTIONALLY_UNREGISTERED] into `entries` (net zero on the total; +1
 * registered, -1 unregistered) now that it carries its own opt-in Tactile depth-card states
 * matrix (DS-02) instead of being exercised only indirectly through
 * `TextCard`/`ListCard`/`AlbumCard`/`VoiceCard`.
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
     * D-01/D-03 (Phase 1, tier-legibility): the two-value altitude classification every
     * showcaseable component must carry — PRIMITIVE (fully generic, zero domain nouns in name or
     * params, no baked-in interaction/composition convention) or PATTERN (a domain noun in its
     * name/params OR a baked-in interaction/composition convention). Assigned by applying the
     * D-03 litmus per-component; never a default/convenience assignment (see 01-CONTEXT.md D-03).
     */
    enum class Tier { PRIMITIVE, PATTERN }

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
     * @param tier The component's PRIMITIVE/PATTERN altitude classification (D-01, Phase 1).
     *   Required, no default — the whole module will not compile again until every `Entry(...)`
     *   call site across all 9 family files supplies an explicit value (see 01-01-PLAN.md).
     */
    data class Entry(
        val name: String,
        val family: String,
        val states: List<StateCell> = emptyList(),
        val content: (@Composable () -> Unit)? = null,
        val controls: List<Control> = emptyList(),
        val preview: (@Composable (PlaygroundState) -> Unit)? = null,
        val tier: Tier
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
        emptyStateFamilyEntries +
        progressFamilyEntries +
        tactileFoundationFamilyEntries

    /**
     * Sub-part composables intentionally excluded from a standalone tile (D-04), each with a
     * one-line reason. Curated against the live scan, not assumed from CONTEXT.md's illustrative
     * list.
     */
    val INTENTIONALLY_UNREGISTERED: Map<String, String> = mapOf(
        "WaveformCanvas" to
            "Sub-part rendered inside RecordingBottomSheetContent, and called directly by " +
            "VoiceCard's clip mini-rows (Phase 129 DS-03 D-02, VoiceClipRow) — VoiceCard's " +
            "own overview strip still uses its separate private wrapper (VoiceWaveformCanvas). " +
            "No standalone showcase tile; already exercised indirectly through those callers.",
        "SwipeableActionRow" to
            "Swipe-reveal mechanics powering CardBase and EditorItemRow — infrastructure, not " +
            "an independent visual archetype; already exercised indirectly via every card " +
            "entry's reveal-confirm swipe and via EditorItemRow's own demo.",
        "RevealActionRow" to
            "Swipe-reveal mechanics for arbitrary 0-2 action slots (vs SwipeableActionRow's fixed " +
            "Delete/Edit pair) — infrastructure, not an independent visual archetype; exercised " +
            "indirectly via callers' own row demos, mirroring SwipeableActionRow's own allowlist " +
            "precedent.",
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
