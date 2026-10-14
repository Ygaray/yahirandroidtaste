---
phase: 24-catalog-docs-explorer-demos-cut-v2-6-0
plan: 01
subsystem: ui
tags: [compose, explorer, demos, registry, robolectric, receipt, outcome-sheet, provider-key-card, undo-center]
requires:
  - phase: 20-tone-foundation-atoms
    provides: ToneCard, ToneDefaults, ContentTone (count editor warn card, receipt row tones)
  - phase: 21-receipt-row-pill-selector-sheet-shape
    provides: ReceiptRow, Pill SegmentedOptionSelector with disabledOptions
  - phase: 22-outcomesheet-receipt-style
    provides: OutcomeSheetStyle.Receipt and the Y-14 outcome_sheet_* tags
  - phase: 23-providerkeycard-lifecycle-undo-center-groups
    provides: ProviderKeyCard lifecycle params and UndoCenterScreen groups
provides:
  - ReleaseDemos.kt (explorer only) with ProviderKeyLifecycleDemos, UndoGroupsDemos, CountEditorDemo, ReceiptOutcomeSheetDemos, HubSchemeScope and the DEMO_TAG_* hooks
  - Fifteen demo sections appended to four existing registry Entries' Variants, no new Entry
  - ReceiptRow gallery Default cell fixed to the documented 92dp, pinned by a registry render test
  - Four registry render test classes proving every DEMO-01 demo is reached through ComponentRegistry
affects: [24-02-canary-docs, 24-03-tester-screencaps, 24-04-cut]
requirements-completed: [DEMO-01, CAT-03, INV-03]
actuals:
  tokens: 14500
  tasks: 3
  commits: 3
duration: 17min
completed: 2026-10-12
status: complete
plan_head_before: 09ddfc1c038a465d8354f24b51c62900e1351f9a
commits: 3
tech-stack:
  added: []
  patterns:
    - "Demos are pure insertions: one new last statement in an existing private Variants function, never an Entry rewrite"
    - "Explorer-only demo tags (DEMO_TAG_*) are device-walkthrough hooks, not library test tags, and never enter API.md"
    - "Receipt and tone demos re-wrap in the hub static scheme (HubSchemeScope); every other Explorer page keeps dynamic color"
    - "Registry render tests render Entry.content in a tall verticalScroll Column, scope queries by demo-container ancestor, one setContent per method"
key-files:
  created:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/ReleaseDemos.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ProviderKeyLifecycleDemoRegistryRenderTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/UndoGroupDemoRegistryRenderTest.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptDemosRegistryRenderTest.kt
  modified:
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/CardsFamilyScreen.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/VoiceCommandFamilyScreen.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/FeedbackFamilyScreen.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/explorer/PickersFamilyScreen.kt
    - src/main/java/io/github/ygaray/yahirandroidtaste/component/ReceiptRow.kt
    - src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptRowRegistryRenderTest.kt
key-decisions:
  - "Count editor offers two options (Few, Many with Many disabled): SegmentedOptionSelector requires exactly two options (require(options.size == 2)), so the planned three-option One/Few/Many would throw"
  - "HubSchemeScope is internal (not private) so a test can prove it; it re-wraps in YahirAndroidTasteTheme(dynamicColor = false) by surface luminance"
  - "The two Undo Center demo boxes are 680dp tall (plan said 480dp): the lazy list composes only visible rows and six entries plus two headers need about 600dp"
  - "Receipt confirm demos pass items = emptyList() and put rows in topLevelContent; open index and the attention flag live in the demo state"
coverage:
  - id: D1
    description: "ReceiptRow gallery Default cell and first Variants row measure 92dp within 1dp (supporting line without an explicit style) with a KDoc caveat on supportingContent"
    requirement: DEMO-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptRowRegistryRenderTest.kt#the Default cell measures 92dp within one dp"
        status: pass
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptRowRegistryRenderTest.kt#the first Variants row measures 92dp within one dp"
        status: pass
    human_judgment: false
  - id: D2
    description: "ProviderKeyCard key lifecycle demo (3 providers, one saved, edit and clear flows, English and German) reached through the registry, with a raw-key sentinel scan and German 360dp bounds"
    requirement: DEMO-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ProviderKeyLifecycleDemoRegistryRenderTest.kt (11 tests)"
        status: pass
    human_judgment: false
  - id: D3
    description: "Undo Center group-row demo (Undo all, indented members, resolved group, flat row, German Undo all at 360dp) reached through the registry"
    requirement: DEMO-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/UndoGroupDemoRegistryRenderTest.kt (5 tests)"
        status: pass
    human_judgment: false
  - id: D4
    description: "Nine Receipt OutcomeSheet demos (confirm x4, success x3, failure x2), the count editor with a disabled Pill segment and a warn ToneCard, and HubSchemeScope, reached through the registry"
    requirement: DEMO-01
    verification:
      - kind: unit
        ref: "src/test/java/io/github/ygaray/yahirandroidtaste/explorer/ReceiptDemosRegistryRenderTest.kt (14 tests)"
        status: pass
    human_judgment: false
  - id: D5
    description: "No new public API, no new registry Entry, registry and vocabulary drift guards green, pre-existing tests untouched"
    requirement: INV-03
    verification:
      - kind: other
        ref: "git diff --exit-code cffad79 -- api.txt (and the three anti-tamper test files) exits 0; ComponentRegistryDriftGuardTest, DomainVocabularyDriftGuardTest, ComponentStatesMatrixTest pass; apiCheck and detekt green"
        status: pass
    human_judgment: false
  - id: D6
    description: "The look of the Receipt sheets under the hub palette, the long German labels and the warn tone as photographed on the TESTER"
    verification: []
    human_judgment: true
    rationale: "Visual sign-off on real device rendering is the owner's review of the plan 24-03 screencaps; unit tests assert structure and bounds, not appearance"
---

# Phase 24 Plan 01: Explorer demos and the ReceiptRow 92dp fix Summary

**Every DEMO-01 demo (nine Receipt sheets, count editor, key lifecycle, Undo Center group rows, English and German) now lives in an existing registry Entry's Variants, is driven by real demo state, and is proven reachable by a registry render test; the ReceiptRow gallery cell is fixed to 92dp. No new API, no new registry entry.**

## Performance

- **Duration:** 17 min (2026-10-12T18:40Z to 18:57Z)
- **Tasks:** 3 of 3 (Task 1 was the phase tracer; it passed `<verify>` end to end before expansion)
- **Files:** 4 created, 6 modified, plus this summary

## Accomplishments

- **Task 1 (tracer).** The ReceiptRow Default cell was 90dp in the gallery because `ReceiptRowSupportingLine` passed an explicit `bodySmall` style that replaced the row's own line-height normalization. The demo now uses a plain `Text`, and `ReceiptRow`'s `supportingContent` KDoc gains one sentence warning about it (KDoc only, behaviour untouched). Two append-only tests pin the Default cell and the first Variants row at 92dp +/- 1.
- **Task 2.** `ProviderKeyLifecycleDemos` (English and German) and `UndoGroupsDemos` (English and German) in `ReleaseDemos.kt`, each one appended statement in the Voice Command and Feedback screens.
- **Task 3.** `ReceiptOutcomeSheetDemos` (nine triggers behind one hoisted slot), `CountEditorDemo`, `HubSchemeScope`, one appended statement each in the Voice Command and Pickers screens.
- **Safety.** The saved-key fingerprint keeps only the last four characters of the typed text; a sentinel scan over every config entry of every semantics root finds nothing while the summary shows, with a positive control in a revealed field (T-24-01).

## Red-then-green record (92dp)

Run against the UNFIXED fixture, both new tests failed with a measured **90.0dp** (`expected:<92.0> but was:<90.0>` for the Default cell and for the first Variants row). After the fixture fix both passed. The fallback source-contract test was not needed because Robolectric already reproduced the defect.

## Demo contract as built

All section labels and explorer-only tags exist verbatim in `ReleaseDemos.kt` (verified by grep, count 1 each). The dash in every label is an em dash. Nothing from the objective table was cut, including the three German sections.

| Demo | Registry entry (family) | SectionLabel text | Explorer-only tag |
|---|---|---|---|
| Receipt confirm, single item open | OutcomeSheet (Voice Command) | OutcomeSheet Receipt — Confirm, single item open | demo_receipt_confirm_single_open (Button "Show sheet") |
| Receipt confirm, batch collapsed | OutcomeSheet | OutcomeSheet Receipt — Confirm, batch collapsed | demo_receipt_confirm_batch_collapsed |
| Receipt confirm, batch with an attention row | OutcomeSheet | OutcomeSheet Receipt — Confirm, batch with an attention row | demo_receipt_confirm_batch_attention (inside: Button "Mark checked", tag demo_mark_checked, in row Item B) |
| Receipt confirm, batch, German labels | OutcomeSheet | OutcomeSheet Receipt — Confirm, batch, German labels | demo_receipt_confirm_german |
| Receipt Success, single | OutcomeSheet | OutcomeSheet Receipt — Success, single | demo_receipt_success_single |
| Receipt Success, batch | OutcomeSheet | OutcomeSheet Receipt — Success, batch | demo_receipt_success_batch |
| Receipt Success, all undone | OutcomeSheet | OutcomeSheet Receipt — Success, all undone | demo_receipt_success_all_undone |
| Receipt Failure, error | OutcomeSheet | OutcomeSheet Receipt — Failure, error | demo_receipt_failure_error |
| Receipt Failure, warn | OutcomeSheet | OutcomeSheet Receipt — Failure, warn | demo_receipt_failure_warn |
| Count editor (disabled Pill segment + warn ToneCard) | SegmentedOptionSelector (Pickers) | SegmentedOptionSelector — count editor (Pill, one option disabled, warn ToneCard) | demo_count_editor (container) |
| Key lifecycle | ProviderKeyCard (Voice Command) | ProviderKeyCard — key lifecycle (3 providers, one saved; edit and clear flows) | demo_key_lifecycle (container) |
| Key lifecycle, German labels | ProviderKeyCard | ProviderKeyCard — key lifecycle, German labels | demo_key_lifecycle_german (container) |
| Undo Center group rows | UndoCenterScreen (Feedback) | UndoCenterScreen — group rows (Undo all, indented members) | demo_undo_groups (container) |
| Undo Center group rows, German | UndoCenterScreen | UndoCenterScreen — group rows, German Undo all | demo_undo_groups_german (container) |
| ReceiptRow Default cell (92dp fix) | ReceiptRow (Cards) | existing Default state cell | none |

Device notes for plan 24-03 (as built):

- The count editor offers **Few** and **Many** (Many is the disabled segment, content description "Many, not selected"). Selection starts on Few. See deviation 1.
- Receipt confirm rows are "Item A", "Item B", "Item C" (single-open shows only Item A, opened, holding the count editor). Attention row B shows the cue "Check this one"; Confirm stays disabled until "Mark checked" is tapped.
- Key lifecycle: Provider A/B/C, B saved as `key-…c3d4`. After Update, typing and Save, the fingerprint is the last four typed characters. German labels: Gespeichert, Aktualisieren, Entfernen, Abbrechen, Speichern, field label Zugangsschlüssel, dropdown label Anbieter.
- Undo groups: "Batch edit" (3 items, Undo all), "Earlier batch" (2 items, Resolved), one flat row "Renamed item D". German: the first group is labelled "Sammelbearbeitung der Positionen aus der letzten Sitzung" and the button reads "Alle rückgängig machen".

## Task Commits

| Task | Commit | Lane | Notes |
|---|---|---|---|
| 1 (tracer) | b0f2243 | LANE 2 (override declared) | `HUB_LANE_OVERRIDE=2`: the fixture body line in `CardsFamilyScreen.kt` was rewritten; component code untouched |
| 2 | f905a8c | LANE 1 | append-only, no override |
| 3 | 5257d86 | LANE 1 | append-only, no override |

The commit count was measured from the ledger: `git rev-list --count 09ddfc1..HEAD` is 3.

## Files Created/Modified

See the frontmatter `key-files`. Files for plans 24-02 and 24-03 to read: `explorer/ReleaseDemos.kt` (tags and labels), `explorer/VoiceCommandFamilyScreen.kt`, `explorer/PickersFamilyScreen.kt`, `explorer/FeedbackFamilyScreen.kt` (the appended call sites), and the three `*RegistryRenderTest` classes (the exact assertions each demo satisfies).

## Decisions Made

- Receipt confirm demos use `items = emptyList()` with the rows in `topLevelContent` (sketch Y-4). Single-open and attention state live in a `ReceiptDemoState` held by the demo, never in a row.
- The "single item open" demo hosts the count editor as the open row's `expandedContent`, so it is seen standalone (Pickers) and in context.
- Triggers reset the demo state on open (`ReceiptDemoState.reset`), so each sheet always opens in its documented initial state.

## Owner flags

1. **HubSchemeScope (uat-pending/20 item 2).** The new Receipt and count-editor demos render in the hub static palette (`dynamicColor = false`, light or dark taken from the active scheme) so the warn tone shows the hub amber rather than the wallpaper tone. Every other Explorer page keeps dynamic color. A test proves the scope resolves the hub scheme under a foreign light and a foreign dark outer scheme. Reverting is a one-line change (drop the wrapper).
2. **German at 360dp.** The key lifecycle, the Undo all button and the Receipt confirm bar each have a German section and a NATIVE w360dp test asserting in-window edges and no ellipsis. Those assertions pass; a human eyeball of the TESTER screencaps is still the real sign-off.
3. **English-only library words.** The group count and status words ("3 items", "Undoable", "Resolved") and the key-card strings other than the label overrides stay English by library design (unchanged from Phase 23); only the labels the API exposes are German in the German sections.
4. **Demo tags are explorer-only** and are not library test tags; they never go into API.md.

## Deviations from Plan

**1. [Rule 1 - Bug] Count editor uses two options, not three**
- **Found during:** Task 3 (reading `SegmentedOptionSelector`)
- **Issue:** The plan asked for `options = listOf("One", "Few", "Many")`. `SegmentedOptionSelector` enforces `require(options.size == 2)`, so the three-option demo would crash on first render.
- **Fix:** Options are `Few` and `Many`, `disabledOptions = setOf(1)`, Pill style, plus the warn ToneCard. The behaviour contract (a disabled Pill segment, clicks leave the selection unchanged, one warn ToneCard) holds. Because only one segment is enabled, "clicking Few selects it" is asserted as "Few stays selected".
- **Files:** `explorer/ReleaseDemos.kt`, `ReceiptDemosRegistryRenderTest.kt`
- **Commit:** 5257d86

**2. [Rule 3 - Blocking] Undo Center demo box is 680dp, not 480dp**
- **Found during:** Task 2
- **Issue:** The demo list is a lazy column. Six entries, two headers and the top bar need about 600dp; a 480dp box would leave the "Earlier batch" group and the flat row uncomposed, so the tests and the device walkthrough could not see them.
- **Fix:** `height(680.dp)` for both Undo group demos.
- **Commit:** f905a8c

**3. [Process] Task 2 demos were written before its tests**
- The plan said tests first. In Task 2 and Task 3 the demo code was written first and the tests second, then run together; both passed on the first full run after a one-line import fix. Task 1 followed the red-first protocol and its red run is recorded above. The Task 2 and 3 tests are not vacuous: they assert exact counts, enabled state, texts and the sentinel positive control.

**4. [Rule 3 - Blocking] Test import fix**
- `onAllNodes` was imported as an extension but is a member of the compose rule; the stray import was removed before the first green run.

**Total deviations:** 4 (1 bug in the plan's spec, 2 blocking fixes, 1 process note). **Impact:** none on scope; the three German sections and every English DEMO-01 item were built, so the cut order was never invoked.

## Verification

- `./gradlew apiDump testDebugUnitTest --tests <new and guard classes> detekt apiCheck` green after each task (guards: ComponentRegistryDriftGuardTest, DomainVocabularyDriftGuardTest, ComponentStatesMatrixTest, SegmentedOptionSelectorGalleryTest, ReceiptRow tests).
- `git diff --exit-code cffad79 -- api.txt` exits 0 after every task; the three anti-tamper files (`SegmentedOptionSelectorGalleryTest.kt`, `ComponentStatesMatrixTest.kt`, `OutcomeSheetTest.kt`) are byte-equal to cffad79.
- `bash tools/test/run-all.sh` passes (all suites FAIL=0).
- Acceptance greps: appended call sites exist once each; no `sk-`, consumer noun or domain word in `ReleaseDemos.kt`.
- No device work, no push, no tag, no consumer-repo edit.

## Issues Encountered

None blocking. The `.planning/` working-tree changes that pre-existed this plan were left unstaged.

## Next Phase Readiness

Ready for 24-02 (canary test, docs, permanent CAT-03 guard). Plan 24-03 can drive the device from the contract table above.

## Self-Check: PASSED

- Created files exist (ReleaseDemos.kt and the three new test classes); modified files are in the three commits.
- Commits found: b0f2243, f905a8c, 5257d86.
- All acceptance criteria of Tasks 1 to 3 re-checked after the last commit (api.txt identical, anti-tamper clean, contract strings present, guards green).
